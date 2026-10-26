package name.modid.client;

public class ClientSkyTime {
    private static double visualTime;
    private static boolean initialized;

    public static void tick(long worldTime) {
        if (!initialized) {
            visualTime = worldTime;
            initialized = true;
            return;
        }

        double instability = ClientChargingData.getInstability();
        instability = Math.max(0, Math.min(100, instability));

        double speed = 1.0 - (instability / 100.0) * 0.3;

        visualTime += speed;
    }
    public static void reset(long worldTime) {
        visualTime = worldTime;
        initialized = true;
    }

    public static long getVisualTime() {
        return (long) visualTime;
    }
}
