package name.modid.powers;

//This is a dummy power for the GiantTrapdoorMixin to detect

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class GiantOnLandingPower extends Power {
    public GiantOnLandingPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}