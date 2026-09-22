package com.hefker.bonkstick.sound;

import com.hefker.bonkstick.BonkStick;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/** Registers the mod's sound events. Their files and subtitles are listed in {@code assets/bonkstick/sounds.json}. */
public final class ModSounds {
	/** The Bonk Sound. */
	public static final SoundEvent BONK = register("bonk");

	private ModSounds() {
	}

	private static SoundEvent register(String path) {
		ResourceLocation id = BonkStick.id(path);
		// Variable range, like vanilla's own sounds: audible out to 16 blocks at volume 1.
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	/** Runs the static registrations above. */
	public static void initialize() {
	}
}
