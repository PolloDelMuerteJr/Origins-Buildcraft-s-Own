package name.modid.client;

import name.modid.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPackets {

    public static void registerS2C() {

        ClientPlayNetworking.registerGlobalReceiver(
                ModPackets.CHARGING_SYNC,
                (client, handler, buf, responseSender) -> {

                    boolean charging = buf.readBoolean();

                    client.execute(() -> {
                        ClientChargingData.setCharging(charging);
                    });

                }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ModPackets.INSTABILITY_SYNC,
                (client, handler, buf, responseSender) -> {

                    double instability = buf.readDouble();

                    client.execute(() -> {
                        ClientChargingData.setInstability(instability);
                    });

                }
        );
    }
}