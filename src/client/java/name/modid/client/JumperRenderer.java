package name.modid.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.JumperActive;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

//TODO more high instability visuals

public class JumperRenderer {

    private static float displayedInstability = 0;

    private static int tickCounter = 0;
    private static long lastWorldTick = -1;

    public static void register() {

        HudRenderCallback.EVENT.register(
                (drawContext, tickDelta) -> {

                    MinecraftClient client = MinecraftClient.getInstance();

                    if (client.player == null || client.interactionManager == null) {
                        return;
                    }

                    JumperActive power =
                            PowerHolderComponent
                                    .getPowers(client.player, JumperActive.class)
                                    .stream()
                                    .findFirst()
                                    .orElse(null);

                    if (power == null) return;

                    GameMode gameMode = client.interactionManager.getCurrentGameMode();

                    if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) {
                        return;
                    }

                    if (ClientChargingData.isCharging()) {
                        renderChargingEffect(drawContext);
                    } else {
                        tickCounter = 0;
                    }

                    renderInstabilityEffect(drawContext);
                }
        );
    }

    private static void renderInstabilityEffect(DrawContext context) {

        MinecraftClient client = MinecraftClient.getInstance();

        Identifier INSTABILITY_TEXTURE = new Identifier(
                "origin-buildcrafts-own",
                "insability.png"
        );

        double instability = ClientChargingData.getInstability();
        instability = Math.max(0, Math.min(100, instability));

        // Width to draw
        int textureWidth = 182;
        int textureHeight = 5;

        displayedInstability = (float) (displayedInstability + (instability - displayedInstability) * 0.15f);

        displayedInstability = Math.max(0, Math.min(100, displayedInstability));

        int filledWidth = (int)(textureWidth * (displayedInstability / 100f));

        int x = (client.getWindow().getScaledWidth() - textureWidth) / 2;
        int y = client.getWindow().getScaledHeight() - 29;

        RenderSystem.setShaderTexture(0, INSTABILITY_TEXTURE);

        context.drawTexture(
                INSTABILITY_TEXTURE,
                x,
                y,
                0,              // u
                0,              // v
                filledWidth,    // width to draw
                textureHeight,  // height
                textureWidth,   // texture width
                textureHeight   // texture height
        );
    }

    private static void renderChargingEffect(DrawContext context) {

        MinecraftClient client = MinecraftClient.getInstance();

        Identifier PREINSTABILITY_TEXTURE = new Identifier(
                "origin-buildcrafts-own",
                "preinstability.png"
        );

        double instability = ClientChargingData.getInstability();
        instability = Math.max(0, Math.min(100, instability));

        long worldTick = client.world.getTime();

        if (worldTick != lastWorldTick) {
            lastWorldTick = worldTick;
            tickCounter++;
        }

        // Width to draw
        int textureWidth = 182;
        int textureHeight = 5;

        float preInstabilityOffset = (float) ((1.2 * Math.pow(tickCounter / 10.0, 2)) / 100) + 10;

        displayedInstability = (float) (displayedInstability + (instability - displayedInstability) * 0.15f);

        float displayedBar = (displayedInstability + preInstabilityOffset);

        displayedBar = Math.max(0, Math.min(100, displayedBar));

        int filledWidth = (int)(textureWidth * (displayedBar / 100f));

        int x = (client.getWindow().getScaledWidth() - textureWidth) / 2;
        int y = client.getWindow().getScaledHeight() - 29;

        RenderSystem.setShaderTexture(0, PREINSTABILITY_TEXTURE);

        context.drawTexture(
                PREINSTABILITY_TEXTURE,
                x,
                y,
                0,              // u
                0,              // v
                filledWidth,    // width to draw
                textureHeight,  // height
                textureWidth,   // texture width
                textureHeight   // texture height
        );
    }
}