package bura.com.mixin;

import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("resource")
@Mixin(ServerChunkCache.class)
public abstract class ChunkGenThrottleMixin {
	@Unique
	private static final int MAX_GEN_CHECKS_PER_TICK = 220;

	@Unique
	private static final int MIN_GEN_CHECKS_PER_TICK = 80;

	@Unique
	private static final double TICK_SMOOTHING = 0.1;

	@Unique
	private static final long DEFAULT_GEN_BUDGET_NS = 4_000_000L;

	@Unique
	private int bura$budget;

	@Unique
	private int bura$budgetCap = MAX_GEN_CHECKS_PER_TICK;

	@Unique
	private long bura$lastTickStartNs;

	@Unique
	private long bura$genDeadlineNs;

	@Unique
	private long bura$timeBudgetNs = DEFAULT_GEN_BUDGET_NS;

	@Unique
	private double bura$avgTickMs;

	@Shadow
	@Final
	private ServerLevel level;

	@Inject(method = "tick", at = @At("HEAD"))
	private void bura$resetBudget(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
		bura$lastTickStartNs = System.nanoTime();
		bura$budget = bura$budgetCap;
		bura$genDeadlineNs = bura$lastTickStartNs + bura$timeBudgetNs;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void bura$updateBudget(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
		if (level.getServer().isDedicatedServer()) {
			return;
		}
		long elapsedNs = System.nanoTime() - bura$lastTickStartNs;
		double tickMs = elapsedNs / 1_000_000.0;
		if (bura$avgTickMs == 0.0) {
			bura$avgTickMs = tickMs;
		} else {
			bura$avgTickMs = (bura$avgTickMs * (1.0 - TICK_SMOOTHING)) + (tickMs * TICK_SMOOTHING);
		}
		int target = pickTargetBudget(bura$avgTickMs);
		if (bura$budgetCap < target) {
			bura$budgetCap = Math.min(target, bura$budgetCap + 8);
		} else if (bura$budgetCap > target) {
			bura$budgetCap = Math.max(target, bura$budgetCap - 12);
		}
		bura$timeBudgetNs = pickTimeBudgetNs(bura$avgTickMs);
	}

	@ModifyArg(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ChunkMap;tick(Ljava/util/function/BooleanSupplier;)V"
		),
		index = 0
	)
	private BooleanSupplier bura$wrapBudget(BooleanSupplier original) {
		if (level.getServer().isDedicatedServer()) {
			return original;
		}
		return () -> original.getAsBoolean()
			&& (bura$budget-- > 0)
			&& (System.nanoTime() <= bura$genDeadlineNs);
	}

	@Unique
	private static int pickTargetBudget(double avgTickMs) {
		if (avgTickMs >= 25.0) {
			return MIN_GEN_CHECKS_PER_TICK;
		}
		if (avgTickMs >= 18.0) {
			return 120;
		}
		if (avgTickMs >= 12.0) {
			return 180;
		}
		return MAX_GEN_CHECKS_PER_TICK;
	}

	@Unique
	private static long pickTimeBudgetNs(double avgTickMs) {
		if (avgTickMs >= 25.0) {
			return 2_000_000L;
		}
		if (avgTickMs >= 18.0) {
			return 3_000_000L;
		}
		if (avgTickMs >= 12.0) {
			return 4_000_000L;
		}
		return 6_000_000L;
	}
}
