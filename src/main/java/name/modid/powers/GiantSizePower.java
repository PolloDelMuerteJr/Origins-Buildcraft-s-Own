package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class GiantSizePower extends Power {

    public GiantSizePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);

        setTicking(true);
    }

    // Giant selected
    @Override
    public void onAdded() {
        super.onAdded();
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(entity);

        scaleData.setScale(2.0F);
    }

    // Giant removed
    @Override
    public void onRemoved() {
        super.onRemoved();
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(entity);

        scaleData.setScale(1.0F);
    }
}