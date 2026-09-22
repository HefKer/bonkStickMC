package com.hefker.bonkstick.item;

import java.util.List;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * The Bonk Stick: an unbreakable, unenchantable, unstackable, inedible stale baguette.
 *
 * <p>Bonk behaviour lives elsewhere; this class only defines the item's look and stats.
 */
public class BonkStickItem extends Item {
	/** Tooltip shows the modifier plus the player's base value (1.0), so -1.0 reads as "0 Attack Damage". */
	private static final double ATTACK_DAMAGE_MODIFIER = -1.0;
	/** Player base attack speed is 4.0; -2.4 gives sword speed (1.6). */
	private static final double ATTACK_SPEED_MODIFIER = -2.4;

	public BonkStickItem(Properties properties) {
		super(properties);
	}

	/** The default properties for the Bonk Stick. */
	public static Properties properties() {
		return new Properties()
				.stacksTo(1)
				.component(DataComponents.UNBREAKABLE, new Unbreakable(true))
				.attributes(createAttributes());
	}

	private static ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder()
				.add(
						Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE_MODIFIER, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(
						Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build();
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}
