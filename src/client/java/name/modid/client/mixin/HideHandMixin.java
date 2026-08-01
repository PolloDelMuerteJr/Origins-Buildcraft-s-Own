package name.modid.client.mixin;

import name.modid.client.ClientChargingData;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class HideHandMixin {

    @Inject(
            method = "renderHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hideHand(
            MatrixStack matrices,
            Camera camera,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (ClientChargingData.isCharging()) {
            ci.cancel();
        }
    }
}