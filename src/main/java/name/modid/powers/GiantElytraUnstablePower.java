package name.modid.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

//TODO make fireworks less effective as boosts (need mixin)

public class GiantElytraUnstablePower extends Power {

    int cooldown = 0;

    public GiantElytraUnstablePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        setTicking(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (entity.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {

            // make elytra gliding less useful by slowing down the giant. Doesn't affect flight during firework use.
            if (entity.isFallFlying()) {

                entity.addVelocity(0, -0.06, 0);

                // damage elytra
                if (cooldown >= 15) {
                    ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
                    chest.damage(1, entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
                    cooldown = 0; //reset elytra damage cooldown
                }

                // slow horizontal movement
                Vec3d v = entity.getVelocity();
                entity.setVelocity(
                        v.x * 0.97,
                        v.y,
                        v.z * 0.97
                );

                entity.velocityModified = true;

                //increase elytra damage cooldown
                cooldown += 1;
            }
        }
    }
}