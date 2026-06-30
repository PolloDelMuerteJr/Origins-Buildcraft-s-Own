package name.modid.mixin;

//This mixin prevents Origins being shown when disabled

import io.github.apace100.origins.origin.Origin;
import name.modid.config.ModConfig;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Origin.class)
public class OriginChoosableMixin {

    @Inject(method = "isChoosable", at = @At("HEAD"), cancellable = true)
    private void giant$disableOrigins(CallbackInfoReturnable<Boolean> cir) {

        Origin self = (Origin)(Object)this;

        Identifier id = self.getIdentifier();

        if (id.equals(new Identifier("origin-buildcrafts-own", "giant"))) {
            if (!ModConfig.INSTANCE.enable_giant) {
                cir.setReturnValue(false);
            }
        }

        if (id.equals(new Identifier("origin-buildcrafts-own", "jumper"))) {
            if (!ModConfig.INSTANCE.enable_jumper) {
                cir.setReturnValue(false);
            }
        }
    }
}