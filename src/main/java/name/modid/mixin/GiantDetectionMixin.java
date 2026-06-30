package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantSizePower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TargetPredicate.class)
public class GiantDetectionMixin {

    @ModifyConstant(
            method = "test",
            constant = @Constant(doubleValue = 1.0) // fallback distance factor in predicate math
    )
    private double giant$expandRange(double original, LivingEntity target) {

        if (target instanceof PlayerEntity player &&
                PowerHolderComponent.hasPower(player, GiantSizePower.class)) {

            // effectively increases allowed detection radius
            return original * 1.5;
        }

        return original;
    }
}