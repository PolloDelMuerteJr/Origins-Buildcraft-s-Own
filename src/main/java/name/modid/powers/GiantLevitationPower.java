package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class GiantLevitationPower extends Power {

    public GiantLevitationPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        setTicking(true);
    }

    @Override
    public void tick() {
        super.tick();
        //Counteract the Levitation effect.
        if (entity.hasStatusEffect(StatusEffects.LEVITATION)) {
            Vec3d velocity = entity.getVelocity();

            entity.setVelocity(
                    velocity.x,
                    velocity.y * 0.25,
                    velocity.z
            );

            entity.velocityModified = true;
        }
    }
}
