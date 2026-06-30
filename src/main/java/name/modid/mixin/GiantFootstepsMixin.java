package name.modid.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.GiantFootstepsPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO find/make better sound effect
//TODO test if louder footsteps effect wardens
//TODO Fix footstep timing errors. (Will need mixins into player limb swing amount)

@Mixin(PlayerEntity.class)
public class GiantFootstepsMixin {

    private boolean giantFootstepToggle = false;

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void onStep(CallbackInfo ci) {

        Entity self = (Entity)(Object)this;

        if (!(self instanceof PlayerEntity player)) return;

        if (!PowerHolderComponent.hasPower(player, GiantFootstepsPower.class)) return;

        giantFootstepToggle = !giantFootstepToggle;

        if (!giantFootstepToggle) {
            ci.cancel(); // skip every second step
            return;
        }

        self.getWorld().playSound(
                null,
                self.getX(),
                self.getY(),
                self.getZ(),
                SoundEvents.ENTITY_RAVAGER_STEP,
                SoundCategory.PLAYERS,
                1.5f,
                0.8f
        );
    }
}