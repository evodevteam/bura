package bura.com.client;

import net.minecraft.world.phys.Vec3;

public final class ClientChunkThrottle {
	public static final int MAX_REBUILDS_PER_TICK_FAST = 96;
	public static final int MAX_REBUILDS_PER_TICK_NORMAL = 64;
	public static final int MAX_REBUILDS_PER_TICK_SLOW = 32;
	public static final int MAX_COMPILE_QUEUE = 256;
	public static final boolean ULTRA_AGGRESSIVE = true;
	public static final double NEAR_RADIUS_SQ = 64.0 * 64.0;
	public static final double MID_RADIUS_SQ = 96.0 * 96.0;
	public static final double FAR_RADIUS_SQ = 128.0 * 128.0;
	private static final double PANIC_FRAME_MS = 18.0;
	private static final int PANIC_FRAMES = 4;
	private static final int FRAME_WINDOW = 8;
	private static final double SPEED_FAST_BPS = 30.0;
	private static final double SPEED_VERY_FAST_BPS = 60.0;
	private static final double SPEED_ULTRA_BPS = 90.0;
	private static final double SPEED_CRUISE_BPS = 6.0;
	private static final double GUARANTEED_NEAR_RADIUS_SQ = 48.0 * 48.0;
private static final int UPLOAD_DEBT_DECAY_PER_TICK = 2;
private static final int UPLOAD_DEBT_SOFT_CAP = 32;
private static final double RECOVERY_RAMP_STEP = 0.03;
private static final long SCHEDULE_BUDGET_NS = 600_000L;
private static final long BURST_SCHEDULE_BUDGET_NS = 900_000L;
private static final int BURST_TICKS = 30;
private static final int SPIKE_DAMPEN_TICKS = 20;
private static final int LEVEL_WARMUP_TICKS = 140;

	private static int rebuildBudget = MAX_REBUILDS_PER_TICK_NORMAL;
	private static int nearBudget = MAX_REBUILDS_PER_TICK_NORMAL / 2;
	private static int farBudget = MAX_REBUILDS_PER_TICK_NORMAL / 2;
	private static double lastFrameMs = 16.6;
	private static final double[] frameWindow = new double[FRAME_WINDOW];
	private static int frameIndex = 0;
	private static int frameCount = 0;
	private static double avgFrameMs = 16.6;
	private static double maxFrameMs = 16.6;
private static double perfScale = 1.0;
private static int spikeCount = 0;
private static int panicCounter = 0;
private static int spikeClampFrames = 0;
private static int spikeDampenTicks = 0;
private static int levelWarmupTicks = -1;
	private static long scheduleWindowStartNs = 0L;
	private static int overloadBurstTicks = 0;
	private static int speedBurstTicks = 0;
	private static Vec3 lastCameraPos = null;
	private static double cameraSpeedBps = 0.0;
	private static MovementState movementState = MovementState.IDLE;
	private static int movementStateTicks = 0;
	private static double recoveryRamp = 1.0;
	private static int uploadDebt = 0;
	private static double backlogPressure = 0.0;
	private static boolean bypassCompileCap = false;

	private ClientChunkThrottle() {
	}

	public static void resetBudgets(boolean singleplayer) {
		if (singleplayer) {
			rebuildBudget = pickRebuildBudget();
			int near = (int) Math.max(1, rebuildBudget * 0.6);
			int far = Math.max(0, rebuildBudget - near);
			nearBudget = near;
			farBudget = far;
		}
	}

	public static boolean tryConsumeRebuild(boolean singleplayer) {
		if (!singleplayer) {
			return true;
		}
		if (rebuildBudget <= 0) {
			return false;
		}
		rebuildBudget--;
		return true;
	}

public static boolean tryConsumeRebuild(double distanceSq, int compileQueueSize, boolean singleplayer) {
	if (!singleplayer) {
		return true;
	}
	updateBacklogPressure(compileQueueSize);
	if (spikeDampenTicks > 0 && distanceSq > GUARANTEED_NEAR_RADIUS_SQ) {
		return false;
	}
	if (distanceSq <= GUARANTEED_NEAR_RADIUS_SQ) {
		if (nearBudget <= 0) {
			return false;
		}
			nearBudget--;
			return true;
		}
		if (panicCounter > 0) {
			return false;
		}
		if (movementState == MovementState.FAST && distanceSq > MID_RADIUS_SQ && backlogPressure > 0.5) {
			return false;
		}
		double nearSq = scaledRadiusSq(NEAR_RADIUS_SQ);
		double midSq = scaledRadiusSq(MID_RADIUS_SQ);
		double farSq = scaledRadiusSq(FAR_RADIUS_SQ);
		if (distanceSq > farSq) {
			return false;
		}
		if (ULTRA_AGGRESSIVE && distanceSq > midSq && compileQueueSize > getDynamicCompileCap() / 2) {
			return false;
		}
		if (distanceSq > nearSq && compileQueueSize > getDynamicCompileCap() / 3) {
			return false;
		}
		if (distanceSq > nearSq) {
			if (farBudget <= 0) {
				return false;
			}
			farBudget--;
			return true;
		}
		if (nearBudget <= 0) {
			return false;
		}
		nearBudget--;
		return true;
	}

	public static void updateFrameTimeMs(double frameMs) {
		lastFrameMs = frameMs;
		frameWindow[frameIndex] = frameMs;
		frameIndex = (frameIndex + 1) % FRAME_WINDOW;
		if (frameCount < FRAME_WINDOW) {
			frameCount++;
		}
		double sum = 0.0;
		for (int i = 0; i < frameCount; i++) {
			sum += frameWindow[i];
		}
		avgFrameMs = sum / frameCount;
		perfScale = (1000.0 / 150.0) / Math.max(1.0, avgFrameMs);
		if (perfScale < 0.6) {
			perfScale = 0.6;
		} else if (perfScale > 1.4) {
			perfScale = 1.4;
		}
		if (frameMs > maxFrameMs) {
			maxFrameMs = frameMs;
		}
	if (frameMs >= PANIC_FRAME_MS) {
		panicCounter = PANIC_FRAMES;
		spikeClampFrames = 2;
		speedBurstTicks = 0;
		spikeDampenTicks = SPIKE_DAMPEN_TICKS;
		spikeCount++;
	} else if (panicCounter > 0) {
		panicCounter--;
	}
		if (spikeClampFrames > 0) {
			spikeClampFrames--;
		}
	}

	public static void onLevelChanged(boolean hasLevel) {
		levelWarmupTicks = hasLevel ? 0 : -1;
	}

	public static void updateCameraPos(Vec3 cameraPos) {
		if (lastCameraPos != null) {
			double dx = cameraPos.x - lastCameraPos.x;
			double dy = cameraPos.y - lastCameraPos.y;
			double dz = cameraPos.z - lastCameraPos.z;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			cameraSpeedBps = dist * 20.0;
			if (cameraSpeedBps >= SPEED_ULTRA_BPS) {
				speedBurstTicks = 40;
			}
		}
		lastCameraPos = cameraPos;
		updateMovementState();
	}

	public static int pickUploadBudget() {
		if (panicCounter > 0) {
			return 0;
		}
		if (avgFrameMs <= 12.0) {
			return 8;
		}
		if (avgFrameMs <= 16.7) {
			return 6;
		}
		if (avgFrameMs <= 20.0) {
			return 4;
		}
		if (avgFrameMs <= 25.0) {
			return 2;
		}
		return 0;
	}

	public static long pickUploadBudgetNs() {
		if (panicCounter > 0 || spikeClampFrames > 0 || spikeDampenTicks > 0) {
			return 0L;
		}
		if (uploadDebt >= UPLOAD_DEBT_SOFT_CAP) {
			return 0L;
		}
		long base = targetFrameBudgetNs();
		double stateFactor = switch (movementState) {
			case FAST -> 0.65;
			case CRUISE -> 0.85;
			case IDLE -> 1.0;
		};
		base = (long) (base * stateFactor * recoveryRamp);
		base = (long) (base * perfScale);
		if (cameraSpeedBps >= SPEED_VERY_FAST_BPS) {
			return Math.min(base, 700_000L);
		}
		if (cameraSpeedBps >= SPEED_FAST_BPS) {
			return Math.min(base, 1_000_000L);
		}
		if (avgFrameMs <= 12.0) {
			return Math.min(base, 2_000_000L);
		}
		if (avgFrameMs <= 16.7) {
			return Math.min(base, 1_500_000L);
		}
		if (avgFrameMs <= 20.0) {
			return Math.min(base, 1_000_000L);
		}
		if (avgFrameMs <= 25.0) {
			return Math.min(base, 700_000L);
		}
		return 0L;
	}

	private static int pickRebuildBudget() {
		if (panicCounter > 0 || spikeClampFrames > 0) {
			return MAX_REBUILDS_PER_TICK_SLOW;
		}
		int base;
		if (avgFrameMs <= 12.0) {
			base = MAX_REBUILDS_PER_TICK_FAST;
		} else if (avgFrameMs <= 16.7) {
			base = MAX_REBUILDS_PER_TICK_NORMAL;
		} else {
			base = MAX_REBUILDS_PER_TICK_SLOW;
		}
		double stateFactor = switch (movementState) {
			case FAST -> 0.7;
			case CRUISE -> 0.9;
			case IDLE -> 1.0;
		};
		return Math.max(8, (int) (base * stateFactor * recoveryRamp * perfScale * warmupFactor()));
	}

	public static int getDynamicCompileCap() {
		if (overloadBurstTicks > 0) {
			return Math.max(64, (int) (MAX_COMPILE_QUEUE * 2 * perfScale * warmupFactor()));
		}
		if (speedBurstTicks > 0) {
			return Math.max(64,
				(int) ((MAX_COMPILE_QUEUE + (MAX_COMPILE_QUEUE / 2)) * perfScale * warmupFactor()));
		}
		if (cameraSpeedBps >= SPEED_VERY_FAST_BPS) {
			return Math.max(80, (int) (160 * perfScale * warmupFactor()));
		}
		if (cameraSpeedBps >= SPEED_FAST_BPS) {
			return Math.max(80, (int) (200 * perfScale * warmupFactor()));
		}
		return Math.max(80, (int) (MAX_COMPILE_QUEUE * perfScale * warmupFactor()));
	}

	private static double scaledRadiusSq(double baseSq) {
		if (cameraSpeedBps <= SPEED_FAST_BPS) {
			return baseSq;
		}
		double t = Math.min((cameraSpeedBps - SPEED_FAST_BPS) / (SPEED_VERY_FAST_BPS - SPEED_FAST_BPS), 1.0);
		double scale = 1.0 - (0.35 * t);
		return baseSq * scale * scale;
	}

	private static long targetFrameBudgetNs() {
		double targetMs;
		if (avgFrameMs <= 6.0) {
			targetMs = 6.94;
		} else if (avgFrameMs <= 7.5) {
			targetMs = 6.94;
		} else if (avgFrameMs <= 12.0) {
			targetMs = 8.33;
		} else {
			targetMs = 16.67;
		}
		return (long) (targetMs * 0.12 * 1_000_000.0);
	}

	public static void onOverloadSample(int queueSize) {
		if (queueSize > MAX_COMPILE_QUEUE * 2) {
			overloadBurstTicks = BURST_TICKS;
		}
	}

	public static boolean isOverloadBurstActive() {
		return overloadBurstTicks > 0;
	}

	public static boolean isSpeedBurstActive() {
		return speedBurstTicks > 0;
	}

	public static double getCameraSpeedBps() {
		return cameraSpeedBps;
	}

	public static double getBacklogPressure() {
		return backlogPressure;
	}

	public static double getLastFrameMs() {
		return lastFrameMs;
	}

	public static double getAvgFrameMs() {
		return avgFrameMs;
	}

	public static double getMaxFrameMs() {
		return maxFrameMs;
	}

public static int getSpikeCount() {
	return spikeCount;
}

public static boolean isSpikeActive() {
	return panicCounter > 0 || spikeClampFrames > 0 || spikeDampenTicks > 0;
}

public static void onUploads(int count) {
	uploadDebt = Math.min(UPLOAD_DEBT_SOFT_CAP * 2, uploadDebt + count);
}

public static void onTickStart() {
	scheduleWindowStartNs = System.nanoTime();
	if (overloadBurstTicks > 0) {
		overloadBurstTicks--;
	}
	if (speedBurstTicks > 0) {
		speedBurstTicks--;
	}
	if (spikeDampenTicks > 0) {
		spikeDampenTicks--;
	}
	if (levelWarmupTicks >= 0) {
		levelWarmupTicks++;
	}
}

public static boolean canScheduleMore() {
	long elapsed = System.nanoTime() - scheduleWindowStartNs;
	long budget = overloadBurstTicks > 0 ? BURST_SCHEDULE_BUDGET_NS : SCHEDULE_BUDGET_NS;
	if (speedBurstTicks > 0) {
		budget = Math.max(budget, BURST_SCHEDULE_BUDGET_NS);
	}
	if (spikeDampenTicks > 0) {
		budget = Math.min(budget, 300_000L);
	}
	budget = (long) (budget * perfScale * warmupFactor());
	if (budget < 200_000L) {
		budget = 200_000L;
	}
	return elapsed < budget;
}


	public static void perTickDecay() {
		if (uploadDebt > 0) {
			uploadDebt = Math.max(0, uploadDebt - UPLOAD_DEBT_DECAY_PER_TICK);
		}
		if (movementState != MovementState.FAST && recoveryRamp < 1.0) {
			recoveryRamp = Math.min(1.0, recoveryRamp + RECOVERY_RAMP_STEP);
		}
	}

	private static void updateMovementState() {
		MovementState next;
		if (cameraSpeedBps >= SPEED_FAST_BPS) {
			next = MovementState.FAST;
		} else if (cameraSpeedBps >= SPEED_CRUISE_BPS) {
			next = MovementState.CRUISE;
		} else {
			next = MovementState.IDLE;
		}
		if (next != movementState) {
			movementState = next;
			movementStateTicks = 0;
			if (next != MovementState.FAST) {
				recoveryRamp = 0.25;
			}
		} else {
			movementStateTicks++;
		}
	}

	private static void updateBacklogPressure(int compileQueueSize) {
		int cap = getDynamicCompileCap();
		int deferred = BuraCompileQueue.size();
		backlogPressure = Math.min(1.0, (compileQueueSize + deferred) / (double) Math.max(1, cap));
	}

	public static boolean isBypassCompileCap() {
		return bypassCompileCap;
	}

	private static double warmupFactor() {
		if (levelWarmupTicks < 0 || levelWarmupTicks >= LEVEL_WARMUP_TICKS) {
			return 1.0;
		}
		double t = levelWarmupTicks / (double) LEVEL_WARMUP_TICKS;
		return 0.6 + (0.4 * t);
	}

	private enum MovementState {
		FAST,
		CRUISE,
		IDLE
	}
}
