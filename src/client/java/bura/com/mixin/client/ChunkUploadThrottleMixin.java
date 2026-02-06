package bura.com.mixin.client;

import bura.com.client.ClientChunkThrottle;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderDispatcher.class)
public abstract class ChunkUploadThrottleMixin {
	@Shadow
	@Final
	private Queue<Runnable> toUpload;

	@Inject(method = "uploadAllPendingUploads", at = @At("HEAD"), cancellable = true)
	private void bura$limitUploads(CallbackInfo ci) {
		if (!Minecraft.getInstance().isSingleplayer()) {
			return;
		}

		double frameMs = Minecraft.getInstance().getFrameTimeNs() / 1_000_000.0;
		ClientChunkThrottle.updateFrameTimeMs(frameMs);
		long budgetNs = ClientChunkThrottle.pickUploadBudgetNs();
		int minUploads = ClientChunkThrottle.isSpikeActive() ? 1 : 2;
		if (ClientChunkThrottle.getAvgFrameMs() <= 12.0) {
			minUploads = 3;
		}

		int uploads = 0;
		for (int i = 0; i < minUploads; i++) {
			Runnable task = toUpload.poll();
			if (task == null) {
				break;
			}
			task.run();
			uploads++;
		}

		if (budgetNs > 0 && toUpload.peek() != null) {
			long start = System.nanoTime();
			while (System.nanoTime() - start < budgetNs) {
				Runnable task = toUpload.poll();
				if (task == null) {
					break;
				}
				task.run();
				uploads++;
			}
		}
		if (uploads > 0) {
			ClientChunkThrottle.onUploads(uploads);
		}

		ci.cancel();
	}
}
