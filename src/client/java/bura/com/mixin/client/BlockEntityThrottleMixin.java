package bura.com.mixin.client;

import bura.com.client.ClientChunkThrottle;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityThrottleMixin {
	private static final double BE_SKIP_DISTANCE_SQ = 48.0 * 48.0;

	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private <S extends BlockEntityRenderState> void bura$throttleBlockEntity(
		S state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		CameraRenderState cameraState,
		CallbackInfo ci
	) {
		if (!ClientChunkThrottle.isSpikeActive()) {
			return;
		}
		if (state == null || state.blockPos == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		Vec3 camPos = mc.gameRenderer.getMainCamera().position();
		BlockPos pos = state.blockPos;
		double dx = (pos.getX() + 0.5) - camPos.x;
		double dy = (pos.getY() + 0.5) - camPos.y;
		double dz = (pos.getZ() + 0.5) - camPos.z;
		if (dx * dx + dy * dy + dz * dz > BE_SKIP_DISTANCE_SQ) {
			// Skip about half of far block entities during spikes.
			if (ThreadLocalRandom.current().nextBoolean()) {
				ci.cancel();
			}
		}
	}
}
