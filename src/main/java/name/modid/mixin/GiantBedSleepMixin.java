package name.modid.mixin;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBigBedsPower;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class GiantBedSleepMixin {

    @Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
    private void giant$preventWoolWake(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {

        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PowerHolderComponent.hasPower(player, GiantBigBedsPower.class)) return;

        if (player.isSleeping()) {

            BlockPos pos = player.getSleepingPosition().orElse(null);
            if (pos == null) return;

            BlockState state = player.getWorld().getBlockState(pos);

            if (state.isOf(Blocks.WHITE_WOOL)
                    || state.isOf(Blocks.ORANGE_WOOL)
                    || state.isOf(Blocks.BLUE_WOOL)
                    || state.isOf(Blocks.RED_WOOL)) {

                ci.cancel(); // block wake-up entirely
            }
        }
    }
}