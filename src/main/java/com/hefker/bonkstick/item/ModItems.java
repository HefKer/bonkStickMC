package com.hefker.bonkstick.item;

import com.hefker.bonkstick.BonkStick;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Registers the mod's items. */
public final class ModItems {
	public static final BonkStickItem BONK_STICK = register("bonk_stick", new BonkStickItem(BonkStickItem.properties()));

	private ModItems() {
	}

	private static <T extends Item> T register(String path, T item) {
		return Registry.register(BuiltInRegistries.ITEM, BonkStick.id(path), item);
	}

	/** Runs the static registrations above and adds the items to their creative tabs. */
	public static void initialize() {
		// Next to the swords, since it's held and swung like one. No crafting recipe on purpose.
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.addAfter(Items.NETHERITE_SWORD, BONK_STICK));
	}
}
