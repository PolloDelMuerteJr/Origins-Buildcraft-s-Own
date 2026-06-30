package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBigBedsPower;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class BedObstructionMixin {

    @Inject(method = "isBedObstructed", at = @At("HEAD"), cancellable = true)
    private void giant$allowWoolBeds(BlockPos pos, Direction direction,
                                     CallbackInfoReturnable<Boolean> cir) {

        PlayerEntity player = (PlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantBigBedsPower.class)) return;

        BlockState state = player.getWorld().getBlockState(pos);

        if (state.isOf(Blocks.WHITE_WOOL)
                || state.isOf(Blocks.ORANGE_WOOL)
                || state.isOf(Blocks.BLUE_WOOL)
                || state.isOf(Blocks.RED_WOOL)) {

            cir.setReturnValue(false); // never obstructed
        }
    }
}