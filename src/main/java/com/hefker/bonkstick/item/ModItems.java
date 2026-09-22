package com.hefker.bonkstick.item;

import com.hefker.bonkstick.BonkStick;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/** Registers the mod's items. */
public final class ModItems {
	public static final BonkStickItem BONK_STICK = register("bonk_stick", new BonkStickItem(BonkStickItem.properties()));

	private ModItems() {
	}

	private static <T extends Item> T register(String path, T item) {
		return Registry.register(BuiltInRegistries.ITEM, BonkStick.id(path), item);
	}

	/** Forces class loading so the static registrations above run during mod init. */
	public static void initialize() {
	}
}
