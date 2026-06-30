package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantSizePower;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Because of the Pehkui player scaling, default, the Giant cannot crouch on the edge of ledges less then 2.5 blocks. This mixin fixes that.

@Mixin(PlayerEntity.class)
public class GiantSneakMixin {

    @Redirect(
            method = "adjustMovementForSneaking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getStepHeight()F"
            )
    )
    private float giant$sneakStepHeight(PlayerEntity player) {

        if (PowerHolderComponent.hasPower(player, GiantSizePower.class)) {
            return 0.5F;
        }

        return player.getStepHeight();
    }
}