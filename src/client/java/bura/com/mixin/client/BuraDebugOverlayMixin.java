package bura.com.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public abstract class BuraDebugOverlayMixin {
	@Shadow
	public abstract boolean showDebugScreen();

	@Inject(method = "render", at = @At("TAIL"))
	private void bura$renderLabel(GuiGraphics graphics, CallbackInfo ci) {
		if (!showDebugScreen()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		String text = "Bura 1.21.11";
		int y = graphics.guiHeight() - mc.font.lineHeight - 2;
		graphics.drawString(mc.font, text, 2, y, 0xE0E0E0, false);
	}
}
