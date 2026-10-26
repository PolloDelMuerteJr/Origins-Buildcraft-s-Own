package name.modid.mixin;

import name.modid.powers.JumperActive;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class SpectatorTeleportMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "onSpectatorTeleport",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventTeleportWhileCharging(
            SpectatorTeleportC2SPacket packet,
            CallbackInfo ci
    ) {
        if (JumperActive.CHARGING(player)) {
            ci.cancel();
        }
    }
}