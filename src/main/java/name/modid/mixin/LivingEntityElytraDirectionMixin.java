package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantElytraUnstablePower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityElytraDirectionMixin {

    @Unique
    private Vec3d giant$smoothedDirection = Vec3d.ZERO;

    @Unique
    private boolean giant$initialized = false;

    @Redirect(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d giant$lagElytraDirection(LivingEntity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return entity.getRotationVector();
        }

        if (!player.isFallFlying()) {
            giant$initialized = false;
            return player.getRotationVector();
        }

        if (!PowerHolderComponent.hasPower(player, GiantElytraUnstablePower.class)) {
            giant$initialized = false;
            return player.getRotationVector();
        }

        Vec3d target = player.getRotationVector();

        if (!giant$initialized) {
            giant$smoothedDirection = target;
            giant$initialized = true;
        }

        // smoothing factor (lower = heavier / more lag)
        double strength = 0.08;

        giant$smoothedDirection = giant$smoothedDirection.lerp(target, strength).normalize();

        return giant$smoothedDirection;
    }
}