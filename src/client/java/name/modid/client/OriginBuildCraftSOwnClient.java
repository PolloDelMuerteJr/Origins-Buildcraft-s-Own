package name.modid.client;

import name.modid.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import static name.modid.ModPackets.CHARGING_SYNC;

public class OriginBuildCraftSOwnClient implements ClientModInitializer {

	public static void registerS2C() {

		ClientPlayNetworking.registerGlobalReceiver(
				CHARGING_SYNC,
				(client, handler, buf, responseSender) -> {

					boolean charging = buf.readBoolean();

					client.execute(() -> {
						ClientChargingData.setCharging(charging);
					});

				});

	}

	@Override
	public void onInitializeClient() {

		ClientPackets.registerS2C();

		JumperRenderer.register();

		HyperSpaceRenderer.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tick(client);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null) {
				ClientSkyTime.tick(client.world.getTimeOfDay());
			}
		});

	}

	private void tick(MinecraftClient client) {
		if (ClientChargingData.isCharging()) {
			ClientChargingData.tickCharging();
		} else {
			ClientChargingData.resetChargingTicks();
		}
	}
}