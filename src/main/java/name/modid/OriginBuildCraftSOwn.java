package name.modid;

import name.modid.registry.ModPowers;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO Break rocks with bare hands?

// I-WISHLIST
//Increase throw strength? (Need mixins)
//fix bed use? (Need mixins)
//reduce giant acceleration? (not doable? GPT couldn't figure it out, neither can I)


//TODO set up Jumper

public class OriginBuildCraftSOwn implements ModInitializer {

	public static final String MOD_ID = "origin-buildcrafts-own";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModPowers.register();

		LOGGER.info("Origins: BuildCraft's Own loaded!");
	}
}