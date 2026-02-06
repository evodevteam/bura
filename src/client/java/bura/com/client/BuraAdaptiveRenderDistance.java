package bura.com.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public final class BuraAdaptiveRenderDistance {
	private static final int MIN_RD = 8;
	private static final int MAX_RD = 16;
	private static final int TARGET_FPS = 140;
	private static final int DOWN_THRESHOLD = TARGET_FPS - 8;
	private static final int UP_THRESHOLD = TARGET_FPS + 10;
	private static final int DOWN_TICKS = 60;
	private static final int UP_TICKS = 120;
	private static final int COOLDOWN_TICKS = 100;

	private static double fpsAvg = 0.0;
	private static int belowTicks = 0;
	private static int aboveTicks = 0;
	private static int cooldown = 0;

	private BuraAdaptiveRenderDistance() {
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null) {
			return;
		}
		int fps = mc.getFps();
		if (fpsAvg == 0.0) {
			fpsAvg = fps;
		} else {
			fpsAvg = fpsAvg * 0.9 + fps * 0.1;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		boolean overloaded = BuraCompileQueue.isOverloaded() || BuraCompileQueue.isStalled();
		if (overloaded || fpsAvg < DOWN_THRESHOLD) {
			belowTicks++;
			aboveTicks = 0;
		} else if (fpsAvg > UP_THRESHOLD) {
			aboveTicks++;
			belowTicks = 0;
		} else {
			belowTicks = 0;
			aboveTicks = 0;
		}

		if (belowTicks >= DOWN_TICKS) {
			adjustRenderDistance(mc, -1);
			belowTicks = 0;
			cooldown = COOLDOWN_TICKS;
		} else if (aboveTicks >= UP_TICKS) {
			adjustRenderDistance(mc, 1);
			aboveTicks = 0;
			cooldown = COOLDOWN_TICKS;
		}
	}

	private static void adjustRenderDistance(Minecraft mc, int delta) {
		Options options = mc.options;
		int current = options.renderDistance().get();
		int target = Math.max(MIN_RD, Math.min(MAX_RD, current + delta));
		if (target == current) {
			return;
		}
		options.renderDistance().set(target);
	}
}
