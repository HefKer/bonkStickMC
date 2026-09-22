package com.hefker.bonkstick.item;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * The Bonk Stick: an unbreakable, unenchantable, unstackable, inedible stale baguette.
 *
 * <p>Bonk behaviour lives elsewhere; this class only defines the item's look and stats.
 */
public class BonkStickItem extends Item {
	public BonkStickItem(Properties properties) {
		super(properties);
	}

	/** The default properties for the Bonk Stick. */
	public static Properties properties() {
		return new Properties()
				.stacksTo(1)
				.component(DataComponents.UNBREAKABLE, new Unbreakable(true));
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return false;
	}
}
