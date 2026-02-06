package bura.com.mixin.client;

import bura.com.client.BuraCompileQueue;
import bura.com.client.BuraVisibilityCache;
import bura.com.client.ClientChunkThrottle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class ChunkRebuildThrottleMixin {
	@Redirect(
		method = "rebuildSectionAsync",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;schedule(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$CompileTask;)V"
		)
	)
	private void bura$throttledSchedule(SectionRenderDispatcher dispatcher,
		SectionRenderDispatcher.RenderSection.CompileTask task) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isSingleplayer()) {
			dispatcher.schedule(task);
			return;
		}

		SectionRenderDispatcher.RenderSection section = (SectionRenderDispatcher.RenderSection) (Object) this;
		BlockPos origin = section.getRenderOrigin();
		Vec3 camPos = mc.gameRenderer.getMainCamera().position();
		double dx = (origin.getX() + 8.0) - camPos.x;
		double dy = (origin.getY() + 8.0) - camPos.y;
		double dz = (origin.getZ() + 8.0) - camPos.z;
		double distSq = dx * dx + dy * dy + dz * dz;
		long sectionNode = section.getSectionNode();
		BuraVisibilityCache.invalidate(sectionNode);
		if (distSq <= 32.0 * 32.0) {
			BuraCompileQueue.boost(sectionNode);
			BuraVisibilityCache.forceVisible(sectionNode, 2);
		}
		BuraCompileQueue.enqueue(dispatcher, task, distSq, sectionNode);
	}
}
