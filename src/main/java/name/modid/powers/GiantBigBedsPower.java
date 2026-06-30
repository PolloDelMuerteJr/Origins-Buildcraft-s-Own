package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

public class GiantBigBedsPower extends Power {

    public GiantBigBedsPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public LivingEntity getOwner() {
        return entity;
    }
}