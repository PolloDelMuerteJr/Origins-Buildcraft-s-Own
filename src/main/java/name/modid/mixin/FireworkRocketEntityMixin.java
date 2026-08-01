package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantElytraUnstablePower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    private void giant$modifyRocketBoost(
            LivingEntity entity,
            Vec3d newVelocity
    ) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            entity.setVelocity(newVelocity);
            return;
        }

        if (!PowerHolderComponent.hasPower(player, GiantElytraUnstablePower.class)) return;

        Vec3d oldVelocity = entity.getVelocity();
        Vec3d boost = newVelocity.subtract(oldVelocity);

        Vec3d adjusted = new Vec3d(
                boost.x * 0.35,
                boost.y * 0.15,
                boost.z * 0.35
        );

        entity.setVelocity(oldVelocity.add(adjusted));
    }
}