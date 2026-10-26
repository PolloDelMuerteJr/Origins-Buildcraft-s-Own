package name.modid.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import name.modid.powers.JumperActive;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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

                    if (gameMode == GameMode.SPECTATOR && ClientChargingData.isCharging()) {
                        renderChargingBar(drawContext);
                    } else {
                        tickCounter = 0;
                    }

                    if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) {
                        return;
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

    private static void renderChargingBar(DrawContext context) {

        MinecraftClient client = MinecraftClient.getInstance();

        Identifier CHARGING_BAR_TEXTURE = new Identifier(
                "origin-buildcrafts-own",
                "shortchargebar.png"
        );
        Identifier LONG_CHARGING_BAR_TEXTURE = new Identifier(
                "origin-buildcrafts-own",
                "longchargebar.png"
        );
        Identifier OVER_CHARGE_BAR_TEXTURE = new Identifier(
                "origin-buildcrafts-own",
                "overchargebar.png"
        );

        long worldTick = client.world.getTime(); //TODO Test if still work after client side day/night changes

        if (worldTick != lastWorldTick) {
            lastWorldTick = worldTick;
            tickCounter++;
        }

        // Width to draw
        int textureWidth = 182;
        int textureHeight = 5;

        int filledWidth = (int)Math.min(textureWidth * (((float) tickCounter /4) / 100f), 182);

        int x = (client.getWindow().getScaledWidth() - textureWidth) / 2;
        int y = client.getWindow().getScaledHeight() - 10;

        RenderSystem.setShaderTexture(0, CHARGING_BAR_TEXTURE);

        context.drawTexture(
                CHARGING_BAR_TEXTURE,
                x,
                y,
                0,              // u
                0,              // v
                filledWidth,    // width to draw
                textureHeight,  // height
                textureWidth,   // texture width
                textureHeight   // texture height
        );
        if (tickCounter >= 400) {
            int longFilledWidth = Math.min((tickCounter - 400) / 2, 182);
            context.drawTexture(
                    LONG_CHARGING_BAR_TEXTURE,
                    x,
                    y,
                    0,              // u
                    0,              // v
                    longFilledWidth,    // width to draw
                    textureHeight,  // height
                    textureWidth,   // texture width
                    textureHeight   // texture height
            );
        }
        if (tickCounter >= 800) {
            float alpha;

            if (tickCounter < 820) {
                // Phase 1: Fade in from 0% to 100% over 1 second.
                alpha = (tickCounter - 800) / 20.0F;
            } else {
                // Phase 2: Pulse between 50% and 100% every 5 seconds.
                alpha = 0.75F + 0.25F * (float) Math.cos(
                        2.0 * Math.PI * (tickCounter - 820) / 100.0
                );
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            context.drawTexture(
                    OVER_CHARGE_BAR_TEXTURE,
                    x,
                    y,
                    0,  // u
                    0,  // v
                    textureWidth,
                    textureHeight,
                    textureWidth,
                    textureHeight
            );

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}