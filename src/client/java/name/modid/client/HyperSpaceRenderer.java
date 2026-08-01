package name.modid.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

public class HyperSpaceRenderer {

    private static final int MAX_STARS = 300;

    private static final float[] x = new float[MAX_STARS];
    private static final float[] y = new float[MAX_STARS];
    private static final float[] depth = new float[MAX_STARS];

    private static final Random random = new Random();

    public static void register() {

        resetStars();

        HudRenderCallback.EVENT.register(HyperSpaceRenderer::render);

    }


    private static void resetStars() {

        for (int i = 0; i < MAX_STARS; i++) {

            x[i] = random.nextFloat() * 2 - 1;
            y[i] = random.nextFloat() * 2 - 1;
            depth[i] = random.nextFloat();

        }
    }


    private static void render(DrawContext context, float tickDelta) {

        if (!ClientChargingData.isCharging())
            return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        float centerX = width / 2f;
        float centerY = height / 2f;

        double speed = Math.min((ClientChargingData.getChargeTicks() * 0.00025f), 0.8);

        double progress = speed / 0.8f;
        progress = Math.min(progress, 1.0f);

        int starCount = (int)(50 + 250 * Math.pow(progress, 0.5));

        for (int i = 0; i < starCount; i++) {

            // Move star towards viewer
            depth[i] = (float) (depth[i] - (speed * tickDelta * 0.1f));

            if (depth[i] <= 0.05f) {
                depth[i] = 1.0f;

                x[i] = random.nextFloat() * 2 - 1;
                y[i] = random.nextFloat() * 2 - 1;
            }

            float streakLength = 0.08f + (float)Math.pow(progress, 2);

            float oldDepth = depth[i] + streakLength;

            float x1 = centerX + x[i] / oldDepth * width;
            float y1 = centerY + y[i] / oldDepth * height;

            float x2 = centerX + x[i] / depth[i] * width;
            float y2 = centerY + y[i] / depth[i] * height;

            context.getMatrices().push();

            depth[i] -= speed;

            int alpha = (int)(255 * (1-depth[i]));
            int color = (alpha << 24) | 0x88CCFF;

            drawLine(
                    context,
                    x1,
                    y1,
                    x2,
                    y2,
                    color
            );

            context.getMatrices().pop();
        }
    }


    private static void drawLine(
            DrawContext context,
            float x1,
            float y1,
            float x2,
            float y2,
            int color
    ) {

        context.fill(
                (int)x1,
                (int)y1,
                (int)x2,
                (int)y2,
                color
        );

    }
}