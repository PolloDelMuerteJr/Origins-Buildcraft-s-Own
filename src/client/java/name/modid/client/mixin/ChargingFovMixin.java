package name.modid.client.mixin;

import name.modid.client.ClientChargingData;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class ChargingFovMixin {

    private static float chargingFovIncrease = 0.0f;

    private static final float MAX_FOV_INCREASE = 50.0f;
    private static final float CHARGE_SPEED = 0.1f;

    @Inject(
            method = "getFov",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyFov(
            net.minecraft.client.render.Camera camera,
            float tickDelta,
            boolean changingFov,
            CallbackInfoReturnable<Double> cir
    ) {

        if (ClientChargingData.isCharging()) {

            chargingFovIncrease = Math.min(
                    chargingFovIncrease + CHARGE_SPEED * tickDelta,
                    MAX_FOV_INCREASE
            );

            // Add onto Minecraft's existing FOV
            cir.setReturnValue(
                    cir.getReturnValue() + chargingFovIncrease
            );

        } else {
            chargingFovIncrease = Math.max(
                    chargingFovIncrease - (CHARGE_SPEED * 2) * tickDelta,
                    0.0f
            );
        }
    }
}