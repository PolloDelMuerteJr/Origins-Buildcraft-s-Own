package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantFootstepsPower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO test if louder footsteps effect wardens

@Mixin(Entity.class)
public class GiantFootstepsMixin {

    //Makes the Giant's footsteps louder/deeper
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void onStep(CallbackInfo ci) {

        Entity self = (Entity)(Object)this;

        BlockState state = self.getSteppingBlockState();

        BlockSoundGroup sounds = state.getSoundGroup();
        SoundEvent stepSound = sounds.getStepSound();

        if (self instanceof PlayerEntity player) {

            if (PowerHolderComponent.hasPower(player, GiantFootstepsPower.class)) {
                self.getWorld().playSound(
                        null,
                        self.getX(),
                        self.getY(),
                        self.getZ(),
                        stepSound,
                        SoundCategory.PLAYERS,
                        1.5f,   // louder
                        0.4f    // deeper
                );
                ci.cancel();
            }
        }
    }
}