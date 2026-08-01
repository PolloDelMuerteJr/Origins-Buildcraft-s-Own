package name.modid.client.mixin;

import name.modid.client.ClientChargingData;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public abstract class DisableDistortionMixin {

    @ModifyVariable(
            method = "renderWorld",
            at = @At(value = "STORE"),
            ordinal = 1
    )
    private float disablePortalDistortion(float g) {
        if (ClientChargingData.isCharging()) {
            return 0.0F;
        }
        return g;
    }
}