package name.modid.client.mixin;

import name.modid.client.ClientSkyTime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(
            method = "onWorldTimeUpdate",
            at = @At("TAIL")
    )
    private void onTimeUpdate(
            WorldTimeUpdateS2CPacket packet,
            CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world != null) {
            ClientSkyTime.reset(client.world.getTimeOfDay());
        }
    }
}