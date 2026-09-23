package dev.hefker.bonkstick.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class LootChestsTest {
	private static final ResourceLocation DUNGEON = ResourceLocation.withDefaultNamespace("chests/simple_dungeon");
	private static final ResourceLocation TYPO = ResourceLocation.withDefaultNamespace("chests/simple_dungon");
	private static final ResourceLocation MODDED = ResourceLocation.fromNamespaceAndPath("somemod", "chests/tower");

	@Test
	void unknownTablesAreTheListedIdsThatDontExist() {
		Map<ResourceLocation, Double> chances = new LinkedHashMap<>();
		chances.put(TYPO, 0.2);
		chances.put(DUNGEON, 0.2);
		chances.put(MODDED, 0.0);
		LootChests lootChests = new LootChests(chances);

		assertEquals(List.of(TYPO, MODDED), lootChests.unknownTables(Set.of(DUNGEON)::contains));
	}

	@Test
	void noUnknownTablesWhenEveryListedTableExists() {
		assertTrue(LootChests.DEFAULTS.unknownTables(id -> true).isEmpty());
	}
}
