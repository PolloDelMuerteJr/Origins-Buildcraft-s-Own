package name.modid.mixin;



import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBigBedsPower;
import name.modid.powers.GiantSizePower;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;

import net.minecraft.util.dynamic.Codecs;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerTrySleepMixin {

    @Inject(
            method = "trySleep",
            at = @At("RETURN"),
            cancellable = true
    )
    private void giant$forceSleep(BlockPos pos,
                                  CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {

        PlayerEntity player = (PlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantBigBedsPower.class)) return;

        if (cir.getReturnValue().left().isEmpty()) {
            return; // already success or failure handled
        }

        BlockState state = player.getWorld().getBlockState(pos);

        if (isWool(state)) {
            System.out.println("TRY_SLEEP MIXIN ACTIVE"); //debug line
            cir.setReturnValue(Either.right(Unit.INSTANCE));
        }
    }

    private boolean isWool(BlockState state) {
        return state.isOf(Blocks.WHITE_WOOL)
                || state.isOf(Blocks.ORANGE_WOOL)
                || state.isOf(Blocks.BLUE_WOOL)
                || state.isOf(Blocks.RED_WOOL);
    }
}