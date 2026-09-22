package com.hefker.bonkstick.loot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

/**
 * Which Loot Chests can hold a Bonk Stick, and how likely each one is to have it.
 *
 * <p>They come from the {@code "loot"} section of {@code config/bonkstick.json}, loaded once when the mod starts.
 * Any loot table works, vanilla or modded; a table that isn't listed, or is listed with a chance of 0, never gets a
 * Bonk Stick.
 *
 * @param chances loot-table id to the chance (0 to 1) that one of its chests holds a single Bonk Stick, in file order
 */
public record LootChests(Map<ResourceLocation, Double> chances) {
	/** Default chance for each default Loot Chest. */
	public static final double DEFAULT_CHANCE = 0.2;

	/** Dungeon (spawner-room) chests and stronghold corridor chests. */
	public static final LootChests DEFAULTS = new LootChests(defaultChances());

	public LootChests {
		chances = Collections.unmodifiableMap(new LinkedHashMap<>(chances));
	}

	/**
	 * The chance that a chest from loot table {@code id} holds a Bonk Stick, or 0 if it's not a Loot Chest.
	 */
	public double chanceFor(ResourceLocation id) {
		return chances.getOrDefault(id, 0.0);
	}

	/** Whether chests from loot table {@code id} should get a Bonk Stick pool at all. */
	public boolean isLootChest(ResourceLocation id) {
		return chanceFor(id) > 0.0;
	}

	private static Map<ResourceLocation, Double> defaultChances() {
		Map<ResourceLocation, Double> chances = new LinkedHashMap<>();
		chances.put(ResourceLocation.withDefaultNamespace("chests/simple_dungeon"), DEFAULT_CHANCE);
		chances.put(ResourceLocation.withDefaultNamespace("chests/stronghold_corridor"), DEFAULT_CHANCE);
		return chances;
	}
}
