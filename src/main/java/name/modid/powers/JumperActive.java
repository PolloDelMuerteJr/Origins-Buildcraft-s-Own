package name.modid.powers;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import name.modid.ModPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class JumperActive extends Power implements Active {

    public static final Set<UUID> CHARGING = new HashSet<>();
    boolean charging = false;

    private static final Map<UUID, Vec3d> STORED_VELOCITY = new HashMap<>();
    private static final Map<UUID, Vec3d> LOCKED_POSITIONS = new HashMap<>();

    private int storedAir = 0;

    private RegistryKey<World> lastDimension;

    private final Active.Key key;

    float playerYaw = 0;
    float playerPitch = 0;

    private int charge = 0;
    private int ticksSinceUse = 0;

    private int instabilityTimer = 0;
    private int packetTimer = 0;

    int cooldown = 20;

    public JumperActive(PowerType<?> type, LivingEntity entity, Active.Key key) {
        super(type, entity);
        this.key = key;
        setTicking(true);
    }

    public static boolean CHARGING(PlayerEntity player) {
        return CHARGING.contains(player.getUuid());
    }

    private double instability = 0;

    @Override
    public void onUse() {
        ticksSinceUse = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!(entity instanceof PlayerEntity player)) return;

        World world = player.getWorld();
        if (world.isClient()) return;

        UUID id = player.getUuid();

        if(!(CHARGING.contains(id))) {
            instability = (Math.min(instability, 100));
            instability = (Math.max((instability - 0.03), 0));
        }

        // If tick runs and onUse() did NOT run this tick,
        // we treat it as "released"
        if (ticksSinceUse > 1 && CHARGING.contains(id)) {
            onRelease();
        }
        // first press detection
        if (ticksSinceUse == 0 && !CHARGING.contains(id) && cooldown == 0) {
            onFirstPress();
        }

        if (CHARGING.contains(id)) {
            charge++;

            player.setAir(storedAir);

            // Prevent nether portal use
            player.resetPortalCooldown();

            // Freeze Player during charge
            player.setVelocity(0, 0, 0);
            player.fallDistance = 0;
            player.setInvulnerable(true);
            player.setSprinting(false);
            player.setJumping(false);
            player.setSneaking(false);
            player.setNoGravity(true);
            player.setSilent(true);
            player.setCustomNameVisible(false);

            // Soft prevent item use
            player.clearActiveItem();

            // kill movement inputs
            player.sidewaysSpeed = 0;
            player.forwardSpeed = 0;

            // hard position lock
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            player.requestTeleport(x, y, z);

            Vec3d locked = LOCKED_POSITIONS.get(id);

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }

            serverPlayer.teleport(
                    serverPlayer.getServerWorld(),
                    locked.x,
                    locked.y,
                    locked.z,
                    playerYaw,
                    playerPitch
            );
            // Particles
            serverPlayer.getServerWorld().spawnParticles(
                    ParticleTypes.PORTAL,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    16,
                    0.5, 0.5, 0.5,
                    0.2
            );
        }

        //Random Instability teleports when instability greater then 10
        instabilityTimer++;

        if (instabilityTimer >= 20) {
            instabilityTimer = 0;

            if (instability > 10) {

                if (cooldown == 0) {

                double chance = (instability / 180.0);

                    if (Math.random() < chance) {
                        long radius = Math.round((instability) / 4);

                        boolean successfulTeleport = safeRandomTeleport(player, (int) radius);
                        if (successfulTeleport) instability -= 5;
                    }
                }
            }
        }
        packetTimer++;
        if (packetTimer >= 5) {
            packetTimer = 0;

            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeDouble(instability);

            ServerPlayNetworking.send(
                    (ServerPlayerEntity) entity,
                    ModPackets.INSTABILITY_SYNC,
                    buf
            );
        }

        //TODO check if sounds are client side, or not. If not, make them client side.
        int timeCharged = (charge/20) - 20; //Charge increases every tick, timeCharged is in seconds

        if (timeCharged >= 2) {

            double chance = Math.max(Math.pow((timeCharged / 130.0),2)+0.01, 0.3);

            if (Math.random() < chance) {
                player.playSound(
                        SoundEvents.PARTICLE_SOUL_ESCAPE,
                        SoundCategory.PLAYERS,
                        2.0f,
                        1.0f
                );
            }

            if (timeCharged >= 20) {

                chance = Math.max(Math.pow((timeCharged / 650.0),2)+0.02, 0.08);

                if (Math.random() < chance) {
                    player.playSound(
                            SoundEvents.ENTITY_PHANTOM_AMBIENT,
                            SoundCategory.PLAYERS,
                            2.5f,
                            0.9f
                    );
                }
            }
        }

        if (instability > 20) {

            List<EndermanEntity> endermen = world.getEntitiesByClass(
                    EndermanEntity.class,
                    player.getBoundingBox().expand((5*(instability/15))),
                    e -> true
            );

            for (EndermanEntity enderman : endermen) {
                enderman.setAngryAt(player.getUuid());
                enderman.setTarget(player);
                instability -= 1;
            }
        }

        RegistryKey<World> currentDimension = entity.getWorld().getRegistryKey();

        if (lastDimension == null) {
            lastDimension = currentDimension;
            return;
        }

        if (!currentDimension.equals(lastDimension)) {
            instability += 10;
            lastDimension = currentDimension;
        }

        ticksSinceUse++;
        cooldown = (Math.max((cooldown-1), 0));
    }

    //TODO make the safe teleport only check for lava, not water
    //Random instability teleport method
    public static boolean safeRandomTeleport(PlayerEntity player, int radius) {

        if (CHARGING(player)) return false; //Dont run during charging

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
            float pitch = (float) MathHelper.clamp((player.getPitch() + ((random.nextFloat() - 0.5) * radius * 1.3)), -90.0F, 90.0F);

            if (!player.isFallFlying()) {
                yaw = player.getYaw();
                pitch = player.getPitch();
            }

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

    private void onFirstPress() {
        if (!(entity instanceof PlayerEntity player)) return;

        UUID id = player.getUuid();

        CHARGING.add(id);
        charging = true;

        //TODO set player into spectator during charge

        PacketByteBuf chargingBuf = PacketByteBufs.create();
        chargingBuf.writeBoolean(charging);

        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                ModPackets.CHARGING_SYNC,
                chargingBuf
        );

        STORED_VELOCITY.put(id, player.getVelocity());
        LOCKED_POSITIONS.put(id, player.getPos());

        playerYaw = player.getYaw();
        playerPitch = player.getPitch();

        storedAir = player.getAir();

        player.setNoGravity(true);
        player.setSilent(true); // removes footstep sounds
        player.setGlowing(false);
        player.setCustomNameVisible(false);
        player.setInvulnerable(true);
        player.setVelocity(0, 0, 0);
        player.fallDistance = 0;
        //player.setInvisible(true);  //not sure if I want/can have the player fully disappear
    }

    private void onRelease() {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        if (!(cooldown == 0)) return;

        UUID id = player.getUuid();

        // stop freeze
        CHARGING.remove(id);
        charging = false;

        PacketByteBuf chargingBuf = PacketByteBufs.create();
        chargingBuf.writeBoolean(charging);

        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                ModPackets.CHARGING_SYNC,
                chargingBuf
        );

        LOCKED_POSITIONS.remove(id);

        // Undo player invulnerability
        player.setSilent(false);
        player.setInvulnerable(false);
        player.setCustomNameVisible(true);
        //player.setInvisible(false); //not sure if I want/can have the player fully disappear

        //restore physics
        player.setNoGravity(false);
        player.setSprinting(false);
        player.setJumping(false);

        Vec3d velocity = STORED_VELOCITY.getOrDefault(id, player.getVelocity());
        STORED_VELOCITY.remove(id);

        double distance = 1.2 * Math.pow(charge / 10.0, 2);

        Vec3d look = player.getRotationVec(1.0F);
        Vec3d target = player.getPos().add(look.multiply(distance));

        //Final Teleport
        player.requestTeleport(
                target.x,
                target.y,
                target.z);

        player.setVelocity(velocity);
        player.velocityModified = true;

        //Spawn Particles for effect
        world.spawnParticles(
                ParticleTypes.PORTAL,
                target.x,
                target.y,
                target.z,
                32,
                0.5, 0.5, 0.5,
                0.2
        );

        player.playSound(
                SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        if(player.isCreative() || player.isSpectator()) {
            instability = 0;
        } else {
            instability = (instability + (distance / 100) + 10);
        }

        PacketByteBuf instabilityBuf = PacketByteBufs.create();
        instabilityBuf.writeDouble(instability);

        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                ModPackets.INSTABILITY_SYNC,
                instabilityBuf
        );

        charge = 0;
        cooldown = 3;
        CHARGING.remove(id);
    }

    @Override
    public Active.Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        // not needed for implementation, just a requirement for the power to exist
    }
    @Override
    public NbtElement toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putDouble("instability", instability);

        return tag;
    }
    @Override
    public void fromTag(NbtElement tag) {
        NbtCompound compound = (NbtCompound) tag;

        instability = compound.getDouble("instability");
    }
    public void resetInstability() {
        instability = 0;
    }
}