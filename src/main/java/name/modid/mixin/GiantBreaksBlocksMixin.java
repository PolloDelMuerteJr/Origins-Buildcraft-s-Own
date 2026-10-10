package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBreaksBlocksPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(PlayerEntity.class)
public class GiantBreaksBlocksMixin {

    private static final Map<BlockPos, Integer> DAMAGE = new HashMap<>();

    private static final Map<BlockPos, Integer> LAST_TICK = new HashMap<>();

    private static final Set<Block> WEAK_BLOCKS = Set.of(
            Blocks.ICE,
            Blocks.FROSTED_ICE,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.MANGROVE_LEAVES,
            Blocks.CHERRY_LEAVES,
            Blocks.AZALEA_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.FLOWER_POT,
            Blocks.DECORATED_POT,
            Blocks.GLASS,
            Blocks.GLASS_PANE,
            Blocks.TINTED_GLASS,
            Blocks.WHITE_STAINED_GLASS,
            Blocks.WHITE_STAINED_GLASS_PANE,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.RED_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS_PANE,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS_PANE,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS_PANE,
            Blocks.LIME_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS_PANE,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS_PANE
    );
    private static final Set<Block> INSTANT_BREAK_BLOCKS = Set.of(
            Blocks.LILY_PAD,
            Blocks.SMALL_DRIPLEAF,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.VINE,
            Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.TWISTING_VINES_PLANT
    );
    private static final Set<Block> NO_COLLISION_BREAK_BLOCKS = Set.of(
            Blocks.VINE,
            Blocks.SMALL_DRIPLEAF,
            Blocks.TWISTING_VINES,
            Blocks.WEEPING_VINES
    );

    @Inject(method = "tick", at = @At("HEAD"))
    private void giantBreaksBlocksMixin(CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.isSpectator()) return;
        if (player.isCreative()) return;
        if (!player.isAlive()) return;

        if (player.getWorld().isClient()) return;
        if (!PowerHolderComponent.hasPower(player, GiantBreaksBlocksPower.class)) return;

        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        int featherFallingLevel = EnchantmentHelper.getEquipmentLevel(
                Enchantments.FEATHER_FALLING,
                player
        );

        if (featherFallingLevel >= 4) return;

        if (featherFallingLevel > player.getRandom().nextInt(7)) return; //Sets the impact of feather falling on how slow blocks break

        World world = player.getWorld();

        Box box = player.getBoundingBox();

// A thin slice immediately below the Giant's feet
        double epsilon = 0.01;

        Box footBox = new Box(
                box.minX,
                box.minY - epsilon,
                box.minZ,
                box.maxX,
                box.minY,
                box.maxZ
        );

// Find the block coordinates covered by the feet
        int minX = MathHelper.floor(footBox.minX);
        int maxX = MathHelper.floor(footBox.maxX);
        int y = MathHelper.floor(box.minY - epsilon);
        int minZ = MathHelper.floor(footBox.minZ);
        int maxZ = MathHelper.floor(footBox.maxZ);

        List<BlockPos> positions = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(pos);

                // Get the block's actual collision shape
                VoxelShape shape = state.getCollisionShape(world, pos);

                // Convert the shape into world coordinates
                Box blockBox = shape.isEmpty()
                        ? null
                        : shape.getBoundingBox().offset(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                );

                // Only include blocks whose collision shape touches the feet
                boolean hasCollision = !shape.isEmpty()
                        && shape.getBoundingBox()
                        .offset(pos.getX(), pos.getY(), pos.getZ())
                        .intersects(footBox);

                boolean isSpecialPlant =
                        NO_COLLISION_BREAK_BLOCKS.contains(state.getBlock());

                boolean overlapsFeet =
                        pos.getX() + 1.0 > box.minX
                                && pos.getX() < box.maxX
                                && pos.getZ() + 1.0 > box.minZ
                                && pos.getZ() < box.maxZ;

                if (overlapsFeet && (hasCollision || isSpecialPlant)) {
                    positions.add(pos);
                }
            }
        }

// Phase 2: process safely
        for (BlockPos pos : positions) {

            BlockState state = world.getBlockState(pos);

            //intabreak lilypads, and the sort
            if (INSTANT_BREAK_BLOCKS.contains(state.getBlock()) || NO_COLLISION_BREAK_BLOCKS.contains(state.getBlock())){

                world.breakBlock(pos, true, player);

                DAMAGE.remove(pos);

                continue;
            }

            if (!WEAK_BLOCKS.contains(state.getBlock())) {
                DAMAGE.remove(pos);
                continue;
            }

            int progress = DAMAGE.getOrDefault(pos, 0);

            progress += 1 + player.getRandom().nextInt(2); // 1–2 per tick
            DAMAGE.put(pos, progress);

            int stage = Math.min(progress / 2, 9);

            if (world instanceof ServerWorld) {

                serverWorld.getServer().getPlayerManager().sendToAround(
                        null,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        32,
                        serverWorld.getRegistryKey(),
                        new net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket(
                                player.getId(),
                                pos,
                                stage
                        )
                );
            }

            if (progress >= 20) {
                world.breakBlock(pos, true, player);
                DAMAGE.remove(pos);
            }
            LAST_TICK.put(pos, player.age);
        }
        // Clean up of old/distant blocks to prevent memory draining !! UNTESTED GPT CODE !! //TODO test code
        DAMAGE.entrySet().removeIf(entry -> {

            BlockPos pos = entry.getKey();

            boolean tooFar =
                    pos.getX() < minX - 5 ||
                    pos.getX() > maxX + 5 ||
                    pos.getZ() < minZ - 5 ||
                    pos.getZ() > maxZ + 5;

            boolean tooOld =
                    LAST_TICK.getOrDefault(pos, player.age)
                            < player.age - 40;

            return tooFar || tooOld;
        });
    }
}