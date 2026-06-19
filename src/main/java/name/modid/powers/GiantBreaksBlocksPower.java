package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

//This is a dummy power for the GiantBreaksBlocksMixin to detect

public class GiantBreaksBlocksPower extends Power {
    public GiantBreaksBlocksPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}