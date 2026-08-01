package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantBedBreakerPower;
import name.modid.powers.GiantBreaksBlocksPower;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PlayerEntity.class)
public class GiantBedBreakerMixin {

    private static final Random RANDOM = new Random();

    @Inject(method = "wakeUp", at = @At("HEAD"))
    private void onWakeUp(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.isSpectator()) return;
        if (player.isCreative()) return;
        if (!player.isAlive()) return;

        if (player.getWorld().isClient()) return;
        if (!PowerHolderComponent.hasPower(player, GiantBedBreakerPower.class)) return;

        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        int featherFallingLevel = EnchantmentHelper.getEquipmentLevel(
                Enchantments.FEATHER_FALLING,
                player
        );

        if (featherFallingLevel >= 4) return;

        // 1/4 chance
        if (RANDOM.nextInt(4) != 0) {
            return;
        }

        BlockPos bedPos = player.getSleepingPosition().orElse(null);

        if (bedPos == null) {
            return;
        }

        World world = player.getWorld();

        // Make sure there is still a bed there
        if (world.getBlockState(bedPos).getBlock() instanceof net.minecraft.block.BedBlock) {
            world.breakBlock(bedPos, true);
        }
    }
}