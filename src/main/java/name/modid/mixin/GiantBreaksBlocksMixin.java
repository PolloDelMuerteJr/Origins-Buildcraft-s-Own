package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBreaksBlocksPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
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
            Blocks.FLOWERING_AZALEA_LEAVES
    );
    private static final Set<Block> INSTANT_BREAK_BLOCKS = Set.of(
            Blocks.LILY_PAD,
            Blocks.SMALL_DRIPLEAF,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.VINE,
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

        World world = player.getWorld();

        Box box = player.getBoundingBox();

// Slightly below the player's feet
        int y = MathHelper.floor(box.minY - 0.05);

        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.floor(box.maxX);

        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.floor(box.maxZ);

// Phase 1: snapshot positions
        List<BlockPos> positions = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                positions.add(new BlockPos(x, y, z));

                // Also check one block higher for thin blocks
                positions.add(new BlockPos(x, y + 1, z));
            }
        }

// Phase 2: process safely
        for (BlockPos pos : positions) {

            BlockState state = world.getBlockState(pos);

            //intabreak lilypads, and the sort
            if (INSTANT_BREAK_BLOCKS.contains(state.getBlock())) {

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