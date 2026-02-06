package bura.com;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.DeltaTracker;
import org.lwjgl.glfw.GLFW;
import bura.com.client.BuraCompileQueue;
import bura.com.client.BuraConfig;
import bura.com.client.BuraModMenuScreen;
import bura.com.client.ClientChunkThrottle;

public class BuraClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMapping openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.bura.config",
			GLFW.GLFW_KEY_O,
			Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfig.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new BuraModMenuScreen(null));
				}
			}
		});

		HudRenderCallback.EVENT.register((GuiGraphics graphics, DeltaTracker delta) -> {
			if (!BuraConfig.overlayEnabled) {
				return;
			}
			Minecraft mc = Minecraft.getInstance();
			int y = 2;
			graphics.drawString(mc.font, "Bura α | 1.21.11", 2, y, 0xE0E0E0, false);
			y += mc.font.lineHeight + 1;
			int queue = BuraCompileQueue.size();
			boolean burst = ClientChunkThrottle.isOverloadBurstActive();
			boolean speedBurst = ClientChunkThrottle.isSpeedBurstActive();
			String stats = "Queue: " + queue
				+ "  Burst: " + (burst ? "ON" : "off")
				+ "  SpeedBurst: " + (speedBurst ? "ON" : "off");
			graphics.drawString(mc.font, stats, 2, y, 0xB0B0B0, false);
			y += mc.font.lineHeight + 1;
			String perf = String.format("Speed: %.1f bps  Backlog: %.2f",
				ClientChunkThrottle.getCameraSpeedBps(),
				ClientChunkThrottle.getBacklogPressure());
			graphics.drawString(mc.font, perf, 2, y, 0xB0B0B0, false);
		});
	}
}
