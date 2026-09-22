package com.hefker.bonkstick;

import java.nio.file.Path;

import com.hefker.bonkstick.bonk.BonkHandler;
import com.hefker.bonkstick.bonk.BonkSettings;
import com.hefker.bonkstick.config.BonkConfig;
import com.hefker.bonkstick.config.BonkConfigFile;
import com.hefker.bonkstick.item.ModItems;
import com.hefker.bonkstick.sound.ModSounds;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BonkStick implements ModInitializer {
	public static final String MOD_ID = "bonkstick";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Read once here, so it's in place on a dedicated server and on a single-player host alike. Changes to the
		// file apply after a restart.
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve(BonkConfigFile.FILE_NAME);
		BonkConfig config = BonkConfigFile.load(configFile);
		BonkSettings.use(config.bonk());

		ModItems.initialize();
		ModSounds.initialize();
		BonkHandler.initialize();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
