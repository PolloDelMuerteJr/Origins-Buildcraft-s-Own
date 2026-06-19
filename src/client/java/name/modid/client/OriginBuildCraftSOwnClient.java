package name.modid.client;

import net.fabricmc.api.ClientModInitializer;

public class OriginBuildCraftSOwnClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		System.out.println("Hello Client Fabric world!"); //debug line
	}
}