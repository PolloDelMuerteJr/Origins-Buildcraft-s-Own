package name.modid.client;

public class ClientChargingData {

    private static int chargeTicks = 0;

    private static boolean charging;

    public static void tickCharging() {
        chargeTicks++;
    }

    public static int getChargeTicks() {
        return chargeTicks;
    }

    public static void resetChargingTicks() {
        chargeTicks = 0;
    }

    public static boolean isCharging() {
        return charging;
    }

    public static void setCharging(boolean value) {
        charging = value;
    }

    // --- INSTABILITY --- //

    private static double instability;

    public static double getInstability() {
        return instability;
    }
    public static void setInstability(double value) {
        instability = value;
    }

}