package com.hefker.bonkstick.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hefker.bonkstick.bonk.BonkSettings;
import com.hefker.bonkstick.loot.LootChests;

import net.minecraft.resources.ResourceLocation;

/** The {@code "loot"} section of {@code bonkstick.json}: which Loot Chests can hold a Bonk Stick. */
class LootSectionTest {
	private static final ResourceLocation DUNGEON = ResourceLocation.withDefaultNamespace("chests/simple_dungeon");
	private static final ResourceLocation CORRIDOR = ResourceLocation.withDefaultNamespace("chests/stronghold_corridor");
	private static final ResourceLocation LIBRARY = ResourceLocation.withDefaultNamespace("chests/stronghold_library");
	private static final ResourceLocation CROSSING = ResourceLocation.withDefaultNamespace("chests/stronghold_crossing");

	@Test
	void defaultsAreDungeonAndStrongholdCorridorAtOneInFive() {
		assertEquals(Map.of(DUNGEON, 0.2, CORRIDOR, 0.2), LootChests.DEFAULTS.chances());
		assertFalse(LootChests.DEFAULTS.isLootChest(LIBRARY));
		assertFalse(LootChests.DEFAULTS.isLootChest(CROSSING));
	}

	@Test
	void defaultFileListsTheDefaultLootChests() {
		String json = BonkConfig.DEFAULTS.toJson();

		assertTrue(json.contains("\"loot\": {"), json);
		assertTrue(json.contains("\"minecraft:chests/simple_dungeon\": 0.2"), json);
		assertTrue(json.contains("\"minecraft:chests/stronghold_corridor\": 0.2"), json);
	}

	@Test
	void fileWithoutALootSectionKeepsTheDefaultLootChests() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkStrength": 2}}
				""");

		assertEquals(LootChests.DEFAULTS, parsed.config().loot());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void lootSectionReplacesTheDefaultsAsAWhole() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"loot": {"minecraft:chests/stronghold_library": 0.5, "somemod:chests/tower": 1}}
				""");

		LootChests loot = parsed.config().loot();
		assertEquals(Map.of(LIBRARY, 0.5, ResourceLocation.fromNamespaceAndPath("somemod", "chests/tower"), 1.0),
				loot.chances());
		assertFalse(loot.isLootChest(DUNGEON));
		assertEquals(BonkSettings.DEFAULTS, parsed.config().bonk());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void emptyLootSectionMeansNoLootChests() {
		BonkConfig.Parsed parsed = BonkConfig.parse("{\"loot\": {}}");

		assertTrue(parsed.config().loot().chances().isEmpty());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void chanceOfZeroIsKeptButIsNotALootChest() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"loot": {"minecraft:chests/simple_dungeon": 0, "minecraft:chests/stronghold_corridor": 0.2}}
				""");

		LootChests loot = parsed.config().loot();
		assertEquals(0.0, loot.chanceFor(DUNGEON));
		assertFalse(loot.isLootChest(DUNGEON));
		assertTrue(loot.isLootChest(CORRIDOR));
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void idWithoutANamespaceMeansMinecraft() {
		BonkConfig.Parsed parsed = BonkConfig.parse("{\"loot\": {\"chests/stronghold_library\": 0.3}}");

		assertEquals(0.3, parsed.config().loot().chanceFor(LIBRARY));
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void chancesOutsideZeroToOneAreClampedWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"loot": {"minecraft:chests/simple_dungeon": 1.5, "minecraft:chests/stronghold_corridor": -0.1}}
				""");

		LootChests loot = parsed.config().loot();
		assertEquals(1.0, loot.chanceFor(DUNGEON));
		assertEquals(0.0, loot.chanceFor(CORRIDOR));
		assertEquals(2, parsed.warnings().size(), parsed.warnings().toString());
		assertTrue(parsed.warnings().stream().allMatch(w -> w.contains("clamped")), parsed.warnings().toString());
	}

	@Test
	void invalidIdsAndNonNumericChancesAreSkippedWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"loot": {
				  "Not A Valid Id!": 0.5,
				  "minecraft:chests/stronghold_library": "0.5",
				  "minecraft:chests/stronghold_crossing": true,
				  "minecraft:chests/igloo_chest": {"chance": 0.5},
				  "minecraft:chests/simple_dungeon": 0.25
				}}
				""");

		assertEquals(Map.of(DUNGEON, 0.25), parsed.config().loot().chances());
		List<String> warnings = parsed.warnings();
		assertEquals(4, warnings.size(), warnings.toString());
		assertTrue(warnings.get(0).contains("not a valid loot-table id"), warnings.toString());
	}

	@Test
	void sameTableListedTwiceKeepsTheLaterChanceWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"loot": {"chests/simple_dungeon": 0.1, "minecraft:chests/simple_dungeon": 0.4}}
				""");

		assertEquals(Map.of(DUNGEON, 0.4), parsed.config().loot().chances());
		assertEquals(1, parsed.warnings().size(), parsed.warnings().toString());
	}

	@Test
	void lootSectionThatIsNotAnObjectFallsBackToTheDefaultsWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("{\"loot\": [\"minecraft:chests/simple_dungeon\"]}");

		assertEquals(LootChests.DEFAULTS, parsed.config().loot());
		assertEquals(1, parsed.warnings().size(), parsed.warnings().toString());
	}

	@Test
	void customLootSectionRoundTrips() {
		BonkConfig.Parsed first = BonkConfig.parse("""
				{"loot": {"minecraft:chests/stronghold_library": 0.5, "minecraft:chests/simple_dungeon": 0}}
				""");
		BonkConfig.Parsed second = BonkConfig.parse(first.config().toJson());

		assertEquals(first.config(), second.config());
		assertTrue(second.warnings().isEmpty(), second.warnings().toString());
	}
}
