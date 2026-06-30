package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantConstantFoodDrainPower;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//AppleSkin compatability? (At the moment, AppleSkin displays the normal food value of the item)

@Mixin(PlayerEntity.class)
public class GiantConstantFoodDrainMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void giantHunger(CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity)(Object)this;

        if (player.getWorld().isClient()) return;
        if (!PowerHolderComponent.hasPower(player, GiantConstantFoodDrainPower.class)) return;

        // every 10 seconds
        if (player.age % 200 != 0) return;
        player.getHungerManager().addExhaustion(0.2f);
    }

    //Reduce food values
    private int lastFood = -1;
    private float lastSaturation = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void captureBefore(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantConstantFoodDrainPower.class)) return;

        HungerManager hunger = player.getHungerManager();

        lastFood = hunger.getFoodLevel();
        lastSaturation = hunger.getSaturationLevel();
    }

    //TODO Test food reduction

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyReduction(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantConstantFoodDrainPower.class)) return;

        HungerManager hunger = player.getHungerManager();

        int newFood = hunger.getFoodLevel();
        float newSaturation = hunger.getSaturationLevel();

        int foodGain = newFood - lastFood;
        float satGain = newSaturation - lastSaturation;

        // only reduce positive gains (food consumption events)
        if (foodGain > 0 || satGain > 0) {

            float factor = 0.5f;

            int adjustedFood = lastFood + Math.round(foodGain * factor);
            float adjustedSat = lastSaturation + (satGain * factor);

            hunger.setFoodLevel(Math.max(0, adjustedFood));
            hunger.setSaturationLevel(Math.max(0, adjustedSat));
        }
    }
}