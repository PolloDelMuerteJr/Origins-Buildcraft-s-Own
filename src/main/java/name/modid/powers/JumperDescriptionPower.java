package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;

//This is a dummy power, it is just here so I can add one more 'power' to the Jumper so I can extend its description

public class JumperDescriptionPower extends Power {
    public JumperDescriptionPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}
