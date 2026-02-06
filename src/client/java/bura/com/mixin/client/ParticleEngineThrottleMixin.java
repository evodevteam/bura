package bura.com.mixin.client;

import bura.com.client.ClientChunkThrottle;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineThrottleMixin {
	@Unique
	private static boolean bura$skipToggle;

	@Inject(method = "add", at = @At("HEAD"), cancellable = true)
	private void bura$throttleAdd(Particle particle, CallbackInfo ci) {
		if (!ClientChunkThrottle.isSpikeActive()) {
			return;
		}
		// Drop ~25% of new particles only during spikes.
		if (ThreadLocalRandom.current().nextInt(4) == 0) {
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void bura$throttleTick(CallbackInfo ci) {
		if (!ClientChunkThrottle.isSpikeActive()) {
			return;
		}
		// Skip some particle ticks during spikes to reduce bursts.
		bura$skipToggle = !bura$skipToggle;
		if (bura$skipToggle && ThreadLocalRandom.current().nextInt(3) == 0) {
			ci.cancel();
		}
	}
}
