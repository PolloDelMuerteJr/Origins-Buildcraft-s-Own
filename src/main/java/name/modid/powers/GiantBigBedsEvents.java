package name.modid.powers;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

//TODO Add arm swing
//TODO make giant only about to sleep at night/thunderstorms, and when not in danger
//TODO make bed blow up in other dimentions, bigger boom then normal
//TODO Limit range of bed interaction
//TODO Set spawn point

public class GiantBigBedsEvents {

    public static void register() {
        UseBlockCallback.EVENT.register(GiantBigBedsEvents::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player,
                                           World world,
                                           Hand hand,
                                           BlockHitResult hit) {

        if (world.isClient) {
            return ActionResult.PASS;
        }

        // IMPORTANT: this is the Origins check
        if (!hasGiantBedPower(player)) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(hit.getBlockPos());
        Block block = state.getBlock();

        boolean isWool = isWool(block);
        boolean isBed = isBed(block);
        boolean isCarpet = isCarpet(block);

        if (isWool || isBed || isCarpet) {
            if (player.isSneaking()) {
                return ActionResult.PASS;
            }
            System.out.println("[GiantBigBeds] interaction with: " + block); //debug line

            //Stop Giant from using beds
            if (isBed && hasGiantBedPower(player)) {
                player.sendMessage(Text.literal("That bed is too small!"), true);
                return ActionResult.FAIL;
            }
            if (player.isSleeping()) return ActionResult.PASS;
            if (!world.isNight()) return ActionResult.FAIL;

            //Run bed detection
            if (isWool) {
                BlockPos anchor = getGiantBedAnchor(world, hit.getBlockPos());
                if (anchor != null) {
                    System.out.println("This wool is part of a giant bed!"); //debug line

                    BlockPos sleepPos = new BlockPos(
                            anchor.getX(),
                            anchor.getY(),
                            anchor.getZ()
                    );

                    //TODO implement custom sleep logic

                    return ActionResult.SUCCESS;
                }
            }

            //TODO update carpet bed logic
            if (isCarpet) {
                if (isPartOfGiantCarpetBed(world, hit.getBlockPos())) {
                    System.out.println("This carpet is part of a giant bed!"); //debug line
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    private static boolean isWoolAt(World world, BlockPos pos) {
        return isWool(world.getBlockState(pos).getBlock());
    }
    //Version for carpet detection
    private static boolean isCarpetAt(World world, BlockPos pos) {
        return isCarpet(world.getBlockState(pos).getBlock());
    }

    private static boolean isAllWoolRectangle(
            World world,
            BlockPos start,
            int width,
            int height) {

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                BlockPos pos = start.add(x, 0, z);

                if (!isWoolAt(world, pos)) {
                    return false;
                }
                // Must have air above it
                BlockState aboveState = world.getBlockState(pos.up());

                if (!aboveState.isAir() && !(aboveState.getBlock() instanceof CarpetBlock)) {
                    return false;
                }
            }
        }
        return true;
    }
    //Carpet bed detection
    private static boolean isAllCarpetRectangle(
            World world,
            BlockPos start,
            int width,
            int height) {

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                BlockPos pos = start.add(x, 0, z);
                BlockState aboveState = world.getBlockState(pos.up());

                if (!world.getBlockState(pos.up()).isAir()) {
                    return false;
                }

                // Must have wool below it
                BlockState belowState = world.getBlockState(pos.down());
                Block belowBlock = belowState.getBlock();

                if (!isWool(belowBlock)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockPos getGiantBedAnchor(World world, BlockPos clickedPos) {

        int[][] sizes = {
                {2, 4},
                {4, 2}
        };

        for (int[] size : sizes) {
            int width = size[0];
            int height = size[1];

            // Try every possible position of the clicked block
            // within this rectangle.
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < height; zOffset++) {

                    BlockPos start =
                            clickedPos.add(-xOffset, 0, -zOffset);

                    if (isAllWoolRectangle(
                            world,
                            start,
                            width,
                            height)) {
                        return start;
                    }
                }
            }
        }
        return null;
    }
    //Version for carpet on top detection
    private static boolean isPartOfGiantCarpetBed(World world, BlockPos clickedPos) {

        int[][] sizes = {
                {2, 4},
                {4, 2}
        };

        for (int[] size : sizes) {
            int width = size[0];
            int height = size[1];

            // Try every possible position of the clicked block
            // within this rectangle.
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < height; zOffset++) {

                    BlockPos start =
                            clickedPos.add(-xOffset, 0, -zOffset);

                    if (isAllCarpetRectangle(
                            world,
                            start,
                            width,
                            height)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasGiantBedPower(PlayerEntity player) {
        return PowerHolderComponent.getPowers(player, GiantBigBedsPower.class)
                .stream()
                .anyMatch(p -> true);
    }

    private static boolean isWool(Block block) {
        return block == Blocks.WHITE_WOOL
                || block == Blocks.ORANGE_WOOL
                || block == Blocks.MAGENTA_WOOL
                || block == Blocks.LIGHT_BLUE_WOOL
                || block == Blocks.YELLOW_WOOL
                || block == Blocks.LIME_WOOL
                || block == Blocks.PINK_WOOL
                || block == Blocks.GRAY_WOOL
                || block == Blocks.LIGHT_GRAY_WOOL
                || block == Blocks.CYAN_WOOL
                || block == Blocks.PURPLE_WOOL
                || block == Blocks.BLUE_WOOL
                || block == Blocks.BROWN_WOOL
                || block == Blocks.GREEN_WOOL
                || block == Blocks.RED_WOOL
                || block == Blocks.BLACK_WOOL;
    }
    private static boolean isCarpet(Block block) {
        return block == Blocks.WHITE_CARPET
                || block == Blocks.ORANGE_CARPET
                || block == Blocks.MAGENTA_CARPET
                || block == Blocks.LIGHT_BLUE_CARPET
                || block == Blocks.YELLOW_CARPET
                || block == Blocks.LIME_CARPET
                || block == Blocks.PINK_CARPET
                || block == Blocks.GRAY_CARPET
                || block == Blocks.LIGHT_GRAY_CARPET
                || block == Blocks.CYAN_CARPET
                || block == Blocks.PURPLE_CARPET
                || block == Blocks.BLUE_CARPET
                || block == Blocks.BROWN_CARPET
                || block == Blocks.GREEN_CARPET
                || block == Blocks.RED_CARPET
                || block == Blocks.BLACK_CARPET;
}
    private static boolean isBed(Block block) {
        return block == Blocks.WHITE_BED
                || block == Blocks.ORANGE_BED
                || block == Blocks.MAGENTA_BED
                || block == Blocks.LIGHT_BLUE_BED
                || block == Blocks.YELLOW_BED
                || block == Blocks.LIME_BED
                || block == Blocks.PINK_BED
                || block == Blocks.GRAY_BED
                || block == Blocks.LIGHT_GRAY_BED
                || block == Blocks.CYAN_BED
                || block == Blocks.PURPLE_BED
                || block == Blocks.BLUE_BED
                || block == Blocks.BROWN_BED
                || block == Blocks.GREEN_BED
                || block == Blocks.RED_BED
                || block == Blocks.BLACK_BED;
    }
}