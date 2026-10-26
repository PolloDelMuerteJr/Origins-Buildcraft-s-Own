package name.modid.powers;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import name.modid.ModPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.*;

public class JumperActive extends Power implements Active {

    public static final Set<UUID> CHARGING = new HashSet<>();
    boolean charging = false;

    private static final Map<UUID, Vec3d> STORED_VELOCITY = new HashMap<>();
    private static final Map<UUID, Vec3d> LOCKED_POSITIONS = new HashMap<>();

    GameMode gameMode = (GameMode.CREATIVE);

    private int storedAir = 0;

    float playerHealth = 20;
    private int savedHunger;
    private float savedSaturation;
    private float savedExhaustion;

    private final Map<UUID, List<StatusEffectInstance>> savedEffects =
            new HashMap<>();

    private RegistryKey<World> lastDimension;

    private final Active.Key key;

    float playerYaw = 0;
    float playerPitch = 0;

    private int charge = 0;
    private int ticksSinceUse = 0;

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

        ServerWorld world = (ServerWorld) player.getWorld();
        if (world.isClient()) return;

        UUID id = player.getUuid();

        if(!(CHARGING.contains(id))) {
            instability = (Math.min(instability, 100));
            instability = (Math.max((instability - 0.03), 0));
        }

        // If tick runs and onUse() did NOT run this tick,
        // we treat it as "released"
        if (ticksSinceUse > 2 && CHARGING.contains(id)) {
            onRelease();
        }
        // first press detection
        if (ticksSinceUse == 0 && !CHARGING.contains(id) && cooldown == 0) {
            ItemStack mainHand = player.getMainHandStack();
            if (mainHand.isOf(Items.COMPASS) || mainHand.isOf(Items.RECOVERY_COMPASS)) {
                compassTeleport(player, mainHand, world);
            } else {
                onFirstPress();
            }
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

        if (timeCharged >= 5) {

            double chance = Math.min(Math.pow((timeCharged / 230.0),1.5)+0.01, 0.3);

            if (Math.random() < chance) {
                player.playSound(
                        SoundEvents.PARTICLE_SOUL_ESCAPE,
                        SoundCategory.PLAYERS,
                        3.0f,
                        1.0f
                );
            }

            if (timeCharged >= 23) {

                chance = Math.min(Math.pow((timeCharged / 650.0),2)+0.02, 0.08);

                if (Math.random() < chance) {
                    player.playSound(
                            SoundEvents.ENTITY_PHANTOM_AMBIENT,
                            SoundCategory.PLAYERS,
                            2.2f,
                            0.9f
                    );
                }
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

    private void onFirstPress() {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        UUID id = player.getUuid();

        CHARGING.add(id);
        charging = true;

        gameMode = player.interactionManager.getGameMode();
        playerHealth = player.getHealth();

        savedHunger = player.getHungerManager().getFoodLevel();
        savedSaturation = player.getHungerManager().getSaturationLevel();
        savedExhaustion = player.getHungerManager().getExhaustion();

        PacketByteBuf chargingBuf = PacketByteBufs.create();
        chargingBuf.writeBoolean(charging);

        ServerPlayNetworking.send(
                player,
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
        player.changeGameMode(GameMode.SPECTATOR);

        List<StatusEffectInstance> effects = new ArrayList<>();

        for (StatusEffectInstance effect : player.getStatusEffects()) {
            effects.add(new StatusEffectInstance(effect));
        }
        savedEffects.put(player.getUuid(), effects);
    }

    private void onRelease() {
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        if (!(cooldown == 0)) return;

        UUID id = player.getUuid();

        // stop freeze
        CHARGING.remove(id);
        charging = false;

        PacketByteBuf chargingBuf = PacketByteBufs.create();
        chargingBuf.writeBoolean(charging);

        ServerPlayNetworking.send(
                player,
                ModPackets.CHARGING_SYNC,
                chargingBuf
        );

        LOCKED_POSITIONS.remove(id);

        // Undo player invulnerability
        player.setSilent(false);
        player.setInvulnerable(false);
        player.setCustomNameVisible(true);
        player.changeGameMode(gameMode);

        //restore physics
        player.setNoGravity(false);
        player.setSprinting(false);
        player.setJumping(false);

        Vec3d velocity = STORED_VELOCITY.getOrDefault(id, player.getVelocity());
        STORED_VELOCITY.remove(id);

        double distance = 3.2 * Math.pow(charge / 10.0, 1.2) + 10;
        if (charge >= 400)
            distance = 2.2 * Math.pow((double) charge / 10, 1.4) - 107;

        Vec3d look = player.getRotationVec(1.0F);
        Vec3d target = player.getPos().add(look.multiply(distance));

        Vec3d safePos = findSafePosition(
                (ServerWorld) player.getWorld(),
                player,
                target
        );

        player.setYaw(playerYaw);
        player.setPitch(playerPitch);

        //Final Teleport
        if (instability < 20) {
            if (safePos != null) {
                player.teleport(
                        (ServerWorld) player.getWorld(),
                        safePos.x,
                        safePos.y,
                        safePos.z,
                        playerYaw,
                        playerPitch
                );
            } else {
                player.damage(player.getDamageSources().inWall(), 2);
            }

        } else if (instability < 50) { //If instability between 20 and 50, look for a safe place to teleport, else damage.
            if (safePos != null) {
                player.teleport(
                        (ServerWorld) player.getWorld(),
                        safePos.x,
                        safePos.y,
                        safePos.z,
                        playerYaw,
                        playerPitch
                );
            } else {
                player.damage(player.getDamageSources().inWall(), 5);
            }
        } else if (instability < 80) { //If instability between 60 and 80, look for a safe place to teleport, but dont require one.
            if (safePos != null) {
                player.teleport(
                        (ServerWorld) player.getWorld(),
                        safePos.x,
                        safePos.y,
                        safePos.z,
                        playerYaw,
                        playerPitch
                );
            } else {
                player.teleport(
                        (ServerWorld) player.getWorld(),
                        target.x,
                        target.y,
                        target.z,
                        playerYaw,
                        playerPitch
                );
            }
        } else { // If instability over 80, just teleport them, safe or not.
            player.teleport(
                    (ServerWorld) player.getWorld(),
                    target.x,
                    target.y,
                    target.z,
                    playerYaw,
                    playerPitch
            );
        }

        player.setVelocity(velocity);
        player.velocityModified = true;

        List<StatusEffectInstance> effects =
                savedEffects.remove(player.getUuid());

        if (effects != null) {
            // Remove all effects currently active.
            for (StatusEffectInstance effect :
                    new ArrayList<>(player.getStatusEffects())) {
                player.removeStatusEffect(effect.getEffectType());
            }

            // Restore the original effects.
            for (StatusEffectInstance effect : effects) {
                player.addStatusEffect(
                        new StatusEffectInstance(effect)
                );
            }
        }

        player.setHealth(playerHealth);

        player.getHungerManager().setFoodLevel(savedHunger);
        player.getHungerManager().setSaturationLevel(savedSaturation);
        player.getHungerManager().setExhaustion(savedExhaustion);

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

        //instability Event
        if (instability >= 40) {
            if ((Math.random() * 100) < instability - 20) {
                swapSelectedSlot(player);
                instability -= 1;
            }
        }

        player.playSound(
                SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );

        if (player.isCreative() || player.isSpectator()) {
            instability = 0;
        } else if (safePos != null) {
            instability = instability + (Math.pow(distance, 1/1.4));
        }

        PacketByteBuf instabilityBuf = PacketByteBufs.create();
        instabilityBuf.writeDouble(instability);

        ServerPlayNetworking.send(
                player,
                ModPackets.INSTABILITY_SYNC,
                instabilityBuf
        );

        charge = 0;
        cooldown = 4;
        CHARGING.remove(id);
    }

    private Vec3d findSafePosition(
            ServerWorld world,
            PlayerEntity player,
            Vec3d origin
    ) {
        int radius = (int) Math.max(20 - (instability/100), 0);

        // Search positions in order of increasing distance.
        for (int distance = 0; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    for (int dz = -distance; dz <= distance; dz++) {

                        // Only examine positions on the current search shell.
                        if (Math.max(
                                Math.max(Math.abs(dx), Math.abs(dy)),
                                Math.abs(dz)
                        ) != distance) {
                            continue;
                        }

                        BlockPos candidate = BlockPos.ofFloored(
                                origin.x + dx,
                                origin.y + dy,
                                origin.z + dz
                        );

                        if (isSafePosition(world, player, candidate)) {
                            return new Vec3d(
                                    candidate.getX() + 0.5,
                                    candidate.getY(),
                                    candidate.getZ() + 0.5
                            );
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafePosition(
            ServerWorld world,
            PlayerEntity player,
            BlockPos feetPos
    ) {
        if (instability < 70) {
            if (feetPos.getY() < world.getBottomY()) {return false;}
        }

        if (instability < 10) {
            if (!world.isChunkLoaded(feetPos)) {return false;}
        }

        // Ensure the player has enough room to fit.
        Box playerBox = player.getBoundingBox()
                .offset(
                        feetPos.getX() + 0.5 - player.getX(),
                        feetPos.getY() - player.getY(),
                        feetPos.getZ() + 0.5 - player.getZ()
                );

        // Reject positions intersecting solid collision shapes.
        if (!world.isSpaceEmpty(player, playerBox)) {return false;}

        // Check the block directly below the player's feet.
        BlockPos floorPos = feetPos.down();
        BlockState floor = world.getBlockState(floorPos);

        if (instability < 60) {
            if (floor.getCollisionShape(world, floorPos).isEmpty()) {return false;}
        }
        if (instability < 50) {
            if (isHazardous(world, feetPos)
                    || isHazardous(world, feetPos.up())
                    || isHazardous(world, floorPos)) {
                return false;
            }
        }
        return true;
    }

    private void compassTeleport (PlayerEntity player, ItemStack compass, ServerWorld world) {
        int x;
        int y;
        int z;

        String playerDimension = player.getWorld()
                .getRegistryKey()
                .getValue()
                .toString();

        if (compass.isOf(Items.RECOVERY_COMPASS)) {
            Optional<GlobalPos> deathPos = player.getLastDeathPos();
            if (deathPos.isPresent()) {
                GlobalPos target = deathPos.get();

                // Get the block coordinates
                x = target.getPos().getX();
                y = target.getPos().getY();
                z = target.getPos().getZ();

                // Get the dimension
                String dimension = target.getDimension().getValue().toString();

                if (Objects.equals(dimension, playerDimension)) {
                    world.spawnParticles(
                        ParticleTypes.PORTAL,
                            x,
                            y,
                            z,
                            32,
                            0.5, 0.5, 0.5,
                            0.2
                    );
                    world.spawnParticles(
                            ParticleTypes.PORTAL,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            32,
                            0.5, 0.5, 0.5,
                            0.2
                    );
                    BlockPos targetPos = target.getPos();
                    double distance = Math.sqrt(
                            player.getBlockPos().getSquaredDistance(targetPos)
                    );
                    player.teleport(
                            x + 0.5,
                            y,
                            z + 0.5
                    );
                    player.playSound(
                            SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                    instability += (Math.pow(distance, 1/1.4))/2;
                    return;
                }
            }
        } else if (compass.isOf(Items.COMPASS)) {
            if (CompassItem.hasLodestone(compass)) {
                GlobalPos target = CompassItem.createLodestonePos(compass.getNbt());
                if (target != null) {
                    x = target.getPos().getX();
                    y = target.getPos().getY();
                    z = target.getPos().getZ();

                    String dimension = target.getDimension().getValue().toString();

                    if (Objects.equals(dimension, playerDimension)) {
                        world.spawnParticles(
                                ParticleTypes.PORTAL,
                                x,
                                y,
                                z,
                                32,
                                0.5, 0.5, 0.5,
                                0.2
                        );
                        world.spawnParticles(
                                ParticleTypes.PORTAL,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                32,
                                0.5, 0.5, 0.5,
                                0.2
                        );
                        BlockPos targetPos = target.getPos();
                        double distance = Math.sqrt(
                                player.getBlockPos().getSquaredDistance(targetPos)
                        );
                        player.teleport(
                                x + 0.5,
                                y + 1,
                                z + 0.5
                        );
                        player.playSound(
                                SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
                                SoundCategory.PLAYERS,
                                1.0f,
                                1.0f
                        );
                        instability += (Math.pow(distance, 1/1.4))/2;
                        return;
                    }
                }
            } else {
                if (player.getWorld().getRegistryKey() == World.OVERWORLD) {
                    BlockPos spawnPos = player.getWorld().getSpawnPos();

                    x = spawnPos.getX();
                    y = spawnPos.getY();
                    z = spawnPos.getZ();

                    world.spawnParticles(
                            ParticleTypes.PORTAL,
                            x,
                            y,
                            z,
                            32,
                            0.5, 0.5, 0.5,
                            0.2
                    );
                    world.spawnParticles(
                            ParticleTypes.PORTAL,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
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
                    BlockPos targetPos = player.getWorld().getSpawnPos();
                    double distance = Math.sqrt(
                            player.getBlockPos().getSquaredDistance(targetPos)
                    );
                    player.teleport(
                            x + 0.5,
                            y,
                            z + 0.5
                    );
                    instability += (Math.pow(distance, 1/1.4))/2;
                    return;
                }
            }
        }
        player.damage(player.getDamageSources().outOfWorld(), 2);
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
    public void resetInstability() {instability = 0;}

    public void setInstability(double amount) {instability = amount;}

    public double getInstability() {return instability;}

    private static boolean isHazardous(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        return state.isOf(net.minecraft.block.Blocks.LAVA)
                || state.isOf(net.minecraft.block.Blocks.FIRE)
                || state.isOf(net.minecraft.block.Blocks.SOUL_FIRE)
                || state.isOf(net.minecraft.block.Blocks.MAGMA_BLOCK)
                || state.isOf(net.minecraft.block.Blocks.CACTUS)
                || state.isOf(net.minecraft.block.Blocks.CAMPFIRE)
                || state.isOf(net.minecraft.block.Blocks.SOUL_CAMPFIRE)
                || state.isOf(net.minecraft.block.Blocks.SWEET_BERRY_BUSH)
                || state.isOf(net.minecraft.block.Blocks.WITHER_ROSE)
                || state.isOf(net.minecraft.block.Blocks.POWDER_SNOW);
    }

    public void swapSelectedSlot(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        int selectedSlot = inventory.selectedSlot;

        Random random = new Random();
        int randomSlot;

        do {
            randomSlot = random.nextInt(9);
        } while (randomSlot == selectedSlot);

        // Swap the item stacks
        ItemStack selectedItem = inventory.getStack(selectedSlot);
        ItemStack randomItem = inventory.getStack(randomSlot);

        inventory.setStack(selectedSlot, randomItem);
        inventory.setStack(randomSlot, selectedItem);

        // Select the slot containing the original item
        inventory.selectedSlot = randomSlot;
    }
}