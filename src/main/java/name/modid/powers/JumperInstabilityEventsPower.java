package name.modid.powers;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

import static name.modid.powers.JumperActive.CHARGING;

public class JumperInstabilityEventsPower extends Power {
    public JumperInstabilityEventsPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        setTicking(true);
    }
    double instability;
    int instabilityTimer = 0;
    @Override
    public void tick() {
        super.tick();

        if (!(entity instanceof PlayerEntity player)) return;

        UUID id = player.getUuid();

        World world = player.getWorld();
        if (world.isClient()) return;

        for (Power power : PowerHolderComponent.KEY.get(player).getPowers()) {
            if (power instanceof JumperActive jumper) {
                instability = jumper.getInstability();
            }
        }
        //Random Instability teleports when instability greater then 40
        instabilityTimer++;

        if (instabilityTimer >= 20) {
            instabilityTimer = 0;

            if (instability > 40) {

                double chance = (instability / 360.0);

                if (Math.random() < chance) {
                    long radius = Math.round((instability) / 3);
                    boolean successfulTeleport = safeRandomTeleport(player, (int) radius);
                    if (successfulTeleport) {
                        instability -= 5;
                        setInstability(player, instability);
                    }
                }
            }
        }
        // Anger Enderman
        if (instability > 60) {

            List<EndermanEntity> endermen = world.getEntitiesByClass(
                    EndermanEntity.class,
                    player.getBoundingBox().expand((5*(instability/15))),
                    e -> true
            );

            for (EndermanEntity enderman : endermen) {
                enderman.setAngryAt(player.getUuid());
                enderman.setTarget(player);
                instability -= 1;
                setInstability(player, instability);
            }
        }
    }

    private void setInstability(PlayerEntity player, double value) {
        for (Power power : PowerHolderComponent.KEY.get(player).getPowers()) {
            if (power instanceof JumperActive jumper) {
                jumper.setInstability(value);
            }
        }
    }
    //TODO make the safe teleport only check for lava, not water
    //Random instability teleport method
    public static boolean safeRandomTeleport(PlayerEntity player, int radius) {

        if (CHARGING(player)) return false; //Dont run during charging

        if (player.isSpectator() || player.isCreative()) return false;

        World world = player.getWorld();
        var random = player.getRandom();

        for (int attempt = 0; attempt < 16; attempt++) {

            double offsetX = (random.nextDouble() - 0.5) * 2 * radius;
            double offsetZ = (random.nextDouble() - 0.5) * 2 * radius;

            double targetX = player.getX() + offsetX;
            double targetZ = player.getZ() + offsetZ;

            if (player.isFallFlying()) radius = radius * 2;

            Vec3d velocity = player.getVelocity();

            // 1. Find top solid ground
            int maxVerticalDistance = radius;

            BlockPos.Mutable pos = new BlockPos.Mutable(
                    (int) targetX,
                    (int) player.getY(),
                    (int) targetZ
            );

            BlockPos groundPos = null;

            for (int y = maxVerticalDistance; y >= -maxVerticalDistance; y--) {

                pos.setY((int) (player.getY() + y));

                BlockState block = world.getBlockState(pos);
                BlockState above = world.getBlockState(pos.up());
                BlockState above2 = world.getBlockState(pos.up(2));

                if (player.isFallFlying()) {
                    if (block.isAir()) {
                        groundPos = pos.toImmutable();
                    }
                } else {
                    if (block.isSolidBlock(world, pos) && above.isAir() && above2.isAir()) {
                        groundPos = pos.toImmutable();
                        break;
                    }
                }
            }

            if (groundPos == null) continue;

            // 2. Check chunk is loaded
            if (!world.isChunkLoaded(groundPos)) continue;

            if (!player.isFallFlying()) {
                // groundPos is the block the player stands on
                BlockState feet = world.getBlockState(groundPos.up());
                BlockState head = world.getBlockState(groundPos.up(2));

                if (!feet.isAir() || !head.isAir()) continue;

                // 7. Avoid unsafe fall (optional but recommended)
                BlockPos scanDown = groundPos.down();
                int safeDrop = 0;

                while (scanDown.getY() > world.getBottomY()
                        && world.getBlockState(scanDown).isAir()) {
                    safeDrop++;
                    scanDown = scanDown.down();

                    if (safeDrop > 5) break;
                }

                if (safeDrop > 5) continue;
            }

            double oldX = player.getX();
            double oldY = player.getY() + 1;
            double oldZ = player.getZ();

            float yaw = (float) (player.getYaw() + ((random.nextFloat() - 0.5) * radius * 4));
            float pitch = (float) MathHelper.clamp((player.getPitch() + ((random.nextFloat() - 0.5) * radius * 1.3)), -85.0F, 85.0F);

            // 8. Teleport player
            player.requestTeleport(
                    groundPos.getX() + 0.5,
                    groundPos.getY() + 1,
                    groundPos.getZ() + 0.5
            );

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.requestTeleport(
                        groundPos.getX() + 0.5,
                        groundPos.getY() + 1,
                        groundPos.getZ() + 0.5,
                        yaw,
                        pitch
                );
            }

            player.setYaw(yaw);
            player.setPitch(pitch);

            player.setVelocity(velocity);
            player.velocityModified = true;

            //Instability damage
            player.damage(player.getDamageSources().outOfWorld(), 2);

            // 9. Effects
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        oldX,
                        oldY + 1,
                        oldZ,
                        16,
                        0.5,
                        0.5,
                        0.5,
                        0.2
                );

                serverWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        player.getX(),
                        player.getY() + 1,
                        player.getZ(),
                        16,
                        0.5,
                        0.5,
                        0.5,
                        0.2
                );
            }

            player.playSound(
                    SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            return true; // success
        }

        return false; // failed after retries
    }
}
