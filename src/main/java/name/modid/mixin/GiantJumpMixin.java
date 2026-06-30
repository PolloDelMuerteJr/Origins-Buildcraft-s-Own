package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantSizePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class GiantJumpMixin {

    @Inject(method = "getJumpVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    private void giant$reduceJump(CallbackInfoReturnable<Float> cir) {

        LivingEntity self = (LivingEntity)(Object)this;

        if (!(self instanceof PlayerEntity player)) return;

        if (!PowerHolderComponent.hasPower(player, GiantSizePower.class)) return;

        if (player.isSpectator()) return;
        if (player.isCreative()) return;

        cir.setReturnValue(cir.getReturnValue() * 0.92f); //Reduce Giant jump height from ~2.6 blocks, to ~2.1 blocks.
    }
}