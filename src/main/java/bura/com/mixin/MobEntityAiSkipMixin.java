package bura.com.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MobEntityAiSkipMixin {
	@Unique
	private static final double AI_SKIP_DISTANCE_SQ = 64.0 * 64.0;

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void bura$skipAiWhenFar(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Mob)) {
			return;
		}

		if (self.level().isClientSide()) {
			return;
		}
		if (!(self.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		for (Player player : serverLevel.players()) {
			if (self.distanceToSqr(player) <= AI_SKIP_DISTANCE_SQ) {
				return;
			}
		}

		ci.cancel();
	}
}
