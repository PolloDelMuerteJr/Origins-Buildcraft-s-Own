package name.modid.client.mixin;

import name.modid.client.ClientChargingData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class HidePortalParticlesMixin {

    @Inject(
            method = "addParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hidePortalParticles(
            ParticleEffect parameters,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            CallbackInfoReturnable<Particle> cir
    ) {

        double distance = MinecraftClient.getInstance()
                .player
                .squaredDistanceTo(x, y, z);

        if (ClientChargingData.isCharging()
                && distance < 4 && parameters == ParticleTypes.PORTAL) {
            cir.setReturnValue(null);
        }
    }
}