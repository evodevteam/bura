package bura.com.mixin.client;

import bura.com.client.BuraCompileQueue;
import bura.com.client.ClientChunkThrottle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftTickThrottleMixin {
	@Unique
	private ClientLevel bura$lastLevel;

	@Inject(method = "tick", at = @At("HEAD"))
	private void bura$resetChunkBudgets(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level != bura$lastLevel) {
			bura$lastLevel = level;
			ClientChunkThrottle.onLevelChanged(level != null);
		}
		ClientChunkThrottle.onTickStart();
		ClientChunkThrottle.updateFrameTimeMs(mc.getFrameTimeNs() / 1_000_000.0);
		Vec3 camPos = mc.gameRenderer.getMainCamera().position();
		ClientChunkThrottle.updateCameraPos(camPos);
		ClientChunkThrottle.perTickDecay();
		ClientChunkThrottle.resetBudgets(mc.isSingleplayer());
		BuraCompileQueue.drain(mc.isSingleplayer());
	}
}
