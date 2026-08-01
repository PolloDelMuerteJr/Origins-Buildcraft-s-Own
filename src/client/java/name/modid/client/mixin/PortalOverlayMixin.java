package name.modid.client.mixin;

import name.modid.client.ClientChargingData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class PortalOverlayMixin {

    @Inject(
            method = "renderPortalOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hidePortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (ClientChargingData.isCharging()) {
            ci.cancel();
        }
    }
}