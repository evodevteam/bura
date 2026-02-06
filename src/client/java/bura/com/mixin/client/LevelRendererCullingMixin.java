package bura.com.mixin.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import bura.com.client.BuraCompileQueue;
import bura.com.client.BuraVisibilityCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererCullingMixin {
	private static final double CAMERA_CACHE_MOVE_SQ = 0.25 * 0.25;
	private static final int SAFE_RADIUS_SECTIONS = 3;
	private static final int GUARD_BAND_SECTIONS = 1;
	private static Vec3 lastCameraPos = null;

	@Shadow
	private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

	@Shadow
	public abstract Frustum getCapturedFrustum();

	@Inject(method = "getVisibleSections", at = @At("HEAD"), cancellable = true)
	private void bura$filterVisibleSections(CallbackInfoReturnable<ObjectArrayList<SectionRenderDispatcher.RenderSection>> cir) {
		Frustum frustum = getCapturedFrustum();
		if (frustum == null || visibleSections.isEmpty()) {
			return;
		}

		int frameId = BuraVisibilityCache.nextFrame();
		boolean overloadBypass = BuraCompileQueue.isStalled() || BuraCompileQueue.isOverloaded();
		Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
		if (lastCameraPos == null
			|| camPos.distanceToSqr(lastCameraPos) > CAMERA_CACHE_MOVE_SQ) {
			lastCameraPos = camPos;
		}
		BuraVisibilityCache.clearIfTooLarge();

		BlockPos camBlock = BlockPos.containing(camPos);
		ObjectArrayList<SectionRenderDispatcher.RenderSection> filtered =
			new ObjectArrayList<>(visibleSections.size());
		for (SectionRenderDispatcher.RenderSection section : visibleSections) {
			SectionMesh mesh = section.getSectionMesh();
			if (mesh == CompiledSectionMesh.UNCOMPILED) {
				filtered.add(section);
				continue;
			}
			BlockPos origin = section.getRenderOrigin();
			int dx = (origin.getX() - camBlock.getX()) >> 4;
			int dy = (origin.getY() - camBlock.getY()) >> 4;
			int dz = (origin.getZ() - camBlock.getZ()) >> 4;
			if (Math.abs(dx) <= SAFE_RADIUS_SECTIONS
				&& Math.abs(dy) <= SAFE_RADIUS_SECTIONS
				&& Math.abs(dz) <= SAFE_RADIUS_SECTIONS) {
				filtered.add(section);
				continue;
			}

			long node = section.getSectionNode();
			if (overloadBypass) {
				boolean visible = frustum.isVisible(section.getBoundingBox());
				if (!visible) {
					int guard = SAFE_RADIUS_SECTIONS + GUARD_BAND_SECTIONS;
					if (Math.abs(dx) <= guard && Math.abs(dy) <= guard && Math.abs(dz) <= guard) {
						visible = true;
					}
				}
				if (visible) {
					filtered.add(section);
				}
				continue;
			}
			if (BuraVisibilityCache.isForceVisible(node)) {
				filtered.add(section);
				continue;
			}
			int lastFrame = BuraVisibilityCache.getLastFrame(node);
			if (lastFrame == frameId - 1) {
				if (BuraVisibilityCache.getCachedVisible(node)) {
					filtered.add(section);
				}
				continue;
			}
			boolean visible = frustum.isVisible(section.getBoundingBox());
			if (!visible) {
				int guard = SAFE_RADIUS_SECTIONS + GUARD_BAND_SECTIONS;
				if (Math.abs(dx) <= guard && Math.abs(dy) <= guard && Math.abs(dz) <= guard) {
					visible = true;
				}
			}
			BuraVisibilityCache.put(node, frameId, visible);
			if (visible) {
				filtered.add(section);
			}
		}
		cir.setReturnValue(filtered);
	}
}
