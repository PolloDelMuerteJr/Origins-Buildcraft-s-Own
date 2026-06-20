package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

//This is a dummy power for the Mixin GiantConstantFoodDrainMixin to detect

public class GiantConstantFoodDrainPower extends Power {
    public GiantConstantFoodDrainPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}