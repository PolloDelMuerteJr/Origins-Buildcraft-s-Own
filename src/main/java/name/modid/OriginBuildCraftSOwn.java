package name.modid;

import name.modid.config.ModConfig;
import name.modid.powers.GiantBigBedsEvents;
import name.modid.registry.ModPowers;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO fix bed use? (Need mixins)
//TODO Let Giant pick up mobs?

// I-WISHLIST
//Make Giant eat food faster. (Not doable, No clean Mixin, ChatGPT cant put something fuctional together, neither can I. I tried #LivingEntity, to no scuccess. #Item worked, but has no per-player context)
//Increase throw strength? (Need mixins)
//Let Giants use riptide tridents only (I could, but right now the power is a nice, small, clean .json, and I don't want to change it.)
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

		ModConfig.load();

		GiantBigBedsEvents.register();

		LOGGER.info("Origins: BuildCraft's Own loaded!");
	}
}