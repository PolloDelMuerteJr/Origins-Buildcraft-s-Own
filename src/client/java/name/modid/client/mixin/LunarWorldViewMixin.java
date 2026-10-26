package name.modid.client.mixin;

import name.modid.client.ClientSkyTime;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.LunarWorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LunarWorldView.class)
public interface LunarWorldViewMixin {

    @Redirect(
            method = "getSkyAngle(F)F",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/LunarWorldView;getLunarTime()J"
            )
    )
    private long slowSkyRotation(LunarWorldView instance) {
        if (instance instanceof ClientWorld) {
            return ClientSkyTime.getVisualTime();
        }
        return instance.getLunarTime();
    }
}