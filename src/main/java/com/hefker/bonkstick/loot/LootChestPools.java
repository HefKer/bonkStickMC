package com.hefker.bonkstick.loot;

import com.hefker.bonkstick.item.ModItems;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Puts the Bonk Stick into Loot Chests.
 *
 * <p>Each Loot Chest's loot table gets one extra pool of its own: one roll that, with the configured chance, gives a
 * single Bonk Stick. So a chest holds at most one, and the table's own pools (including the dungeon's music-disc pool)
 * are left exactly as they were.
 *
 * <p>The pool is added whatever the table's source, including tables a data pack adds or replaces. Listing a table in
 * the config is already the server owner's explicit choice, and a data pack that should own a table outright can be
 * paired with removing that table from the config.
 */
public final class LootChestPools {
	private LootChestPools() {
	}

	/** The pool added to a Loot Chest's loot table: one roll, one Bonk Stick, {@code chance} of it appearing. */
	public static LootPool.Builder pool(double chance) {
		return LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0f))
				.when(LootItemRandomChanceCondition.randomChance((float) chance))
				.add(LootItem.lootTableItem(ModItems.BONK_STICK));
	}

	/** Adds the Bonk Stick pool to every loot table {@code lootChests} lists with a chance above 0, as tables load. */
	public static void initialize(LootChests lootChests) {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			double chance = lootChests.chanceFor(key.location());
			if (chance > 0.0) {
				tableBuilder.withPool(pool(chance));
			}
		});
	}
}
