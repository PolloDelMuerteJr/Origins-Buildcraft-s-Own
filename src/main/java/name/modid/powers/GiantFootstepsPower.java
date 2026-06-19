package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

//This is a dummy power, it is purely here so the GiantFootstepsMixin can detect it

public class GiantFootstepsPower extends Power {

    public GiantFootstepsPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}