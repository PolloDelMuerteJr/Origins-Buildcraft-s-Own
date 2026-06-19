package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.BoatEntity;

public class GiantMountedPower extends Power {

    private int mountTicks = 0;
    private int cooldown = 0;
    private int damageScale = 10;

    private static final int GRACE_PERIOD = 20; // 1 second
    private static final int DAMAGE_INTERVAL = 20; // 1 seconds

    public GiantMountedPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        setTicking(true);
    }

    @Override
    public void tick() {
        super.tick();

        // 1. handle cooldown timer
        if (cooldown > 0) {
            cooldown--;
        }

        // 2. check mount state
        if (entity.hasVehicle()) {

            mountTicks++;

            var mount = entity.getVehicle();

            // Slowdown mount
            if (mount instanceof LivingEntity livingMount) {

                livingMount.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        40,
                        3,
                        true,
                        false
                ));
            }

            //If it's a boat, sink it
            if (mount instanceof BoatEntity boat) {

                boat.addVelocity(0.0, -0.05, 0.0);
                boat.velocityModified = true;
            }

            // 3. still in grace period → do nothing
            if (mountTicks < GRACE_PERIOD) {
                return;
            }

            // 4. cooldown prevents spam damage
            if (cooldown > 0) {
                return;
            }

            // 5. apply damage
            mount.damage(
                    entity.getWorld().getDamageSources().generic(),
                    ((float) damageScale /10)
            );

            // 6. reset cooldown
            cooldown = DAMAGE_INTERVAL;
            damageScale += 2;

        } else {
            // not mounted → reset timer
            mountTicks = 0;
            damageScale = 10;
        }
    }
}