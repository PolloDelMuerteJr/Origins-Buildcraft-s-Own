package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantOnLandingPower;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class GiantTrapdoorMixin {

    @Inject(method = "onLanding", at = @At("HEAD"))
    private void onLanding(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantOnLandingPower.class)) return;

        if (!(player.getWorld() instanceof ServerWorld world))
            return;

        if (player.fallDistance > 2.4) {
            //Play Fun sound effects
            player.getWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE,
                    SoundCategory.PLAYERS,
                    0.2f,
                    0.5f
            );
            //Spawn more particles
            BlockState particleState = world.getBlockState(player.getBlockPos().down());

            world.spawnParticles(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, particleState),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    30,
                    0.3,
                    0.1,
                    0.3,
                    0.15
            );

            //Open wooden trapdoors
            Box box = player.getBoundingBox();

            // Slightly below the player's feet
            int y = MathHelper.floor(box.minY - 0.05);

            int minX = MathHelper.floor(box.minX);
            int maxX = MathHelper.floor(box.maxX);

            int minZ = MathHelper.floor(box.minZ);
            int maxZ = MathHelper.floor(box.maxZ);

            List<BlockPos> positions = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {

                    positions.add(new BlockPos(x, y, z));

                    // Also check one block higher for thin blocks
                    positions.add(new BlockPos(x, y + 1, z));
                }
            }
            for (BlockPos pos : positions) {

                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof TrapdoorBlock) {
                    if (state.getBlock() == Blocks.IRON_TRAPDOOR) return;

                    boolean isBottomTrapdoor =
                            state.get(TrapdoorBlock.HALF) == BlockHalf.BOTTOM;

                    BlockPos belowPos = pos.down();
                    BlockState belowState = world.getBlockState(belowPos);

                    boolean hasSolidBlockBelow =
                            belowState.isSideSolidFullSquare(
                                    world,
                                    belowPos,
                                    Direction.UP
                            );

                    if (isBottomTrapdoor && hasSolidBlockBelow)
                        return; //don't open trapdoors when they are lower trapdoors, and there is a block below them

                    world.setBlockState(
                            pos,
                            state.with(TrapdoorBlock.OPEN, true),
                            Block.NOTIFY_ALL
                    );
                }
            }
        }
    }
}
