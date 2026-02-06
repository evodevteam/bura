package bura.com.client;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BuraModMenuScreen extends Screen {
	private final Screen parent;
	private double scroll;
	private List<String> lines;

	public BuraModMenuScreen(Screen parent) {
		super(Component.literal("Bura Mod Menu"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int y = this.height - 28;
		Button overlayToggle = Button.builder(
				Component.literal("Overlay: " + (BuraConfig.overlayEnabled ? "ON" : "OFF")),
				button -> {
					BuraConfig.overlayEnabled = !BuraConfig.overlayEnabled;
					button.setMessage(Component.literal("Overlay: " + (BuraConfig.overlayEnabled ? "ON" : "OFF")));
				})
			.bounds(centerX - 210, y, 140, 20)
			.build();
		this.addRenderableWidget(overlayToggle);

		Button done = Button.builder(Component.literal("Done"), button -> {
				Minecraft.getInstance().setScreen(parent);
			})
			.bounds(centerX + 70, y, 140, 20)
			.build();
		this.addRenderableWidget(done);

		buildLines();
	}

	private void buildLines() {
		List<String> list = new ArrayList<>();
		Minecraft mc = Minecraft.getInstance();
		list.add("Bura α | 1.21.11");
		list.add("FPS: " + mc.getFps());
		list.add(String.format("Frame ms: %.2f (avg %.2f, max %.2f)",
			ClientChunkThrottle.getLastFrameMs(),
			ClientChunkThrottle.getAvgFrameMs(),
			ClientChunkThrottle.getMaxFrameMs()));
		list.add("Lag spikes: " + ClientChunkThrottle.getSpikeCount());
		list.add("Queue: " + BuraCompileQueue.size()
			+ "  Burst: " + (ClientChunkThrottle.isOverloadBurstActive() ? "ON" : "off")
			+ "  SpeedBurst: " + (ClientChunkThrottle.isSpeedBurstActive() ? "ON" : "off"));
		list.add(String.format("Speed: %.1f bps  Backlog: %.2f",
			ClientChunkThrottle.getCameraSpeedBps(),
			ClientChunkThrottle.getBacklogPressure()));
		list.add("");
		list.add("Installed mods:");
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			String name = mod.getMetadata().getName();
			String id = mod.getMetadata().getId();
			String version = mod.getMetadata().getVersion().getFriendlyString();
			list.add(" - " + name + " (" + id + ") v" + version);
		}
		this.lines = list;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
		scroll -= amount * 10.0;
		scroll = Math.max(0.0, scroll);
		return true;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(graphics);
		int left = 12;
		int top = 12;
		int lineHeight = this.font.lineHeight + 2;
		int maxLines = Math.max(0, (this.height - 48) / lineHeight);
		int start = (int) (scroll / lineHeight);
		int end = Math.min(lines.size(), start + maxLines);
		int y = top;
		for (int i = start; i < end; i++) {
			graphics.drawString(this.font, lines.get(i), left, y, 0xE0E0E0, false);
			y += lineHeight;
		}
		super.render(graphics, mouseX, mouseY, delta);
	}
}
