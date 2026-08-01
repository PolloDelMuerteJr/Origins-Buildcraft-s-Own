package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantSizePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Entity.class)
public class GiantBubbleColumnMixin {

    // Soul sand bubble column acceleration
    @ModifyConstant(
            method = "onBubbleColumnCollision",
            constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 0.06)
    )
    private double reduceUpwardBubbleAcceleration(double original) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof PlayerEntity player)) {
            return original;
        }

        if (!PowerHolderComponent.hasPower(player, GiantSizePower.class)) {
            return original;
        }

        return 0.002;
    }


    // Bubble column surface upward acceleration
    @ModifyConstant(
            method = "onBubbleColumnSurfaceCollision",
            constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 0.1)
    )
    private double reduceSurfaceBubbleAcceleration(double original) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof PlayerEntity player)) {
            return original;
        }

        if (!PowerHolderComponent.hasPower(player, GiantSizePower.class)) {
            return original;
        }

        return 0.003;
    }


    // Magma bubble column downward acceleration
    @ModifyConstant(
            method = "onBubbleColumnCollision",
            constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 0.03)
    )
    private double reduceDownwardBubbleAcceleration(double original) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof PlayerEntity player)) {
            return original;
        }

        if (!PowerHolderComponent.hasPower(player, GiantSizePower.class)) {
            return original;
        }

        return 0.001;
    }


    // Magma bubble column downward acceleration at surface
    @ModifyConstant(
            method = "onBubbleColumnSurfaceCollision",
            constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 0.03)
    )
    private double reduceSurfaceDownwardBubbleAcceleration(double original) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof PlayerEntity player)) {
            return original;
        }

        if (!PowerHolderComponent.hasPower(player, GiantSizePower.class)) {
            return original;
        }

        return 0.001;
    }
}