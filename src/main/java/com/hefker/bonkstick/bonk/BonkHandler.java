package com.hefker.bonkstick.bonk;

import com.hefker.bonkstick.item.ModItems;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Turns a Bonk Stick swing at a Bonkable into a Bonk: a pure shove instead of vanilla's attack.
 *
 * <p>Vanilla's {@code Player.attack} skips knockback entirely when the hit does no damage, so the Bonk is done here
 * from Fabric's {@link AttackEntityCallback} and the vanilla attack is cancelled. Nothing here calls
 * {@code hurt}, so there's no damage event: no health loss, red flash, hurt sound, damage ticks, anger or kill credit.
 *
 * <p>The callback fires on both sides. The client only cancels its local attack (Fabric still sends the attack
 * packet); the server does the Bonk.
 */
public final class BonkHandler {
	/** Set while the server replays a swing at a non-Bonkable as an empty-hand hit, so the callback lets it through. */
	private static boolean emptyHandHitInProgress;

	private BonkHandler() {
	}

	public static void initialize() {
		AttackEntityCallback.EVENT.register(BonkHandler::onAttack);
		BonkSound.initialize();
	}

	private static InteractionResult onAttack(
			Player player, Level level, InteractionHand hand, Entity target, @Nullable EntityHitResult hitResult) {
		// The callback runs before vanilla's spectator check.
		if (player.isSpectator() || !player.getMainHandItem().is(ModItems.BONK_STICK)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			if (Bonkables.resolve(target, false) == null) {
				return InteractionResult.PASS;
			}

			// SUCCESS makes Fabric send the attack packet and skip the local attack. Vanilla resets the Charge right
			// after that local attack, so do it here to keep the cooldown bar in step with the server.
			player.resetAttackStrengthTicker();
			return InteractionResult.SUCCESS;
		}

		if (emptyHandHitInProgress || !(player instanceof ServerPlayer bonker)) {
			return InteractionResult.PASS;
		}

		BonkSettings settings = BonkSettings.current();
		boolean playersProtected = settings.respectPvpSetting() && !bonker.server.isPvpAllowed();
		LivingEntity bonkable = Bonkables.resolve(target, playersProtected);

		if (bonkable == null) {
			emptyHandHit(bonker, target);
		} else {
			bonk(bonker, bonkable, settings);
		}

		return InteractionResult.SUCCESS;
	}

	private static void bonk(ServerPlayer bonker, LivingEntity bonkable, BonkSettings settings) {
		// Same early outs as vanilla's attack (e.g. an armor stand under spawn protection), before the Charge resets.
		if (!bonkable.isAttackable() || bonkable.skipAttackInteraction(bonker)) {
			return;
		}

		// Read the Charge before resetting it, as vanilla does.
		float charge = bonker.getAttackStrengthScale(0.5F);
		bonker.resetAttackStrengthTicker();

		// Only used to ask vanilla questions (invulnerability, shield facing); it never reaches hurt().
		DamageSource source = bonker.damageSources().playerAttack(bonker);

		if (!bonkable.isAlive() || isUnbonkableRightNow(bonkable, source)) {
			return;
		}

		// A raised shield facing the Bonker stops the Bonk completely. Vanilla's own facing check decides, and entity
		// event 29 makes nearby clients play the shield-block sound, exactly as for a blocked vanilla hit. Unlike a
		// blocked hit, the shield takes no durability damage and the Bonker isn't bounced back.
		if (bonkable.isDamageSourceBlocked(source)) {
			bonkable.level().broadcastEntityEvent(bonkable, EntityEvent.ATTACK_BLOCKED);
			BonkCallback.EVENT.invoker().onBonk(bonker, bonkable, BonkCallback.Outcome.BLOCKED);
			return;
		}

		BonkStrength.Push push = BonkStrength.compute(charge, bonker.isSprinting(), settings.strength());
		Vec3 motionBefore = bonkable.getDeltaMovement();
		boolean respectResistance = settings.respectKnockbackResistance();

		// Away from the Bonker, like the push every vanilla hit gives from hurt()...
		knockback(bonkable, push.hit(), bonker.getX() - bonkable.getX(), bonker.getZ() - bonkable.getZ(), respectResistance);
		// ...then along the Bonker's facing, like vanilla's Knockback enchantment and sprint bonus.
		float yaw = bonker.getYRot() * Mth.DEG_TO_RAD;
		knockback(bonkable, push.facing(), Mth.sin(yaw), -Mth.cos(yaw), respectResistance);

		// Vanilla slows the attacker and ends the sprint after any knockback hit.
		bonker.setDeltaMovement(bonker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
		bonker.setSprinting(false);

		sendMotion(bonkable, motionBefore);

		BonkCallback.EVENT.invoker().onBonk(bonker, bonkable, BonkCallback.Outcome.LANDED);
	}

	/**
	 * Makes sure clients see the push. A player's client owns its own movement, so, like vanilla after a hit, the Bonked
	 * player is sent the new motion directly and the server's copy goes back to what it was. For everything else,
	 * {@code hurtMarked} (normally set by {@code hurt()}) makes the server broadcast the new motion on its next update.
	 */
	private static void sendMotion(LivingEntity bonkable, Vec3 motionBefore) {
		if (bonkable instanceof ServerPlayer bonkedPlayer) {
			bonkedPlayer.connection.send(new ClientboundSetEntityMotionPacket(bonkedPlayer));
			bonkedPlayer.setDeltaMovement(motionBefore);
		} else {
			bonkable.hurtMarked = true;
		}
	}

	/** Targets vanilla hits can't touch either: creative-mode players and entities tagged invulnerable. */
	private static boolean isUnbonkableRightNow(LivingEntity bonkable, DamageSource source) {
		return bonkable.isInvulnerableTo(source)
				|| bonkable instanceof Player player && player.getAbilities().invulnerable;
	}

	/**
	 * Pushes like {@link LivingEntity#knockback}. That method always applies knockback resistance, so when the settings
	 * say to ignore it this repeats vanilla's formula without the reduction.
	 */
	private static void knockback(LivingEntity target, double strength, double x, double z, boolean respectResistance) {
		if (respectResistance) {
			target.knockback(strength, x, z);
			return;
		}

		if (strength <= 0.0) {
			return;
		}

		target.hasImpulse = true;
		Vec3 motion = target.getDeltaMovement();

		while (x * x + z * z < 1.0E-5F) {
			x = (Math.random() - Math.random()) * 0.01;
			z = (Math.random() - Math.random()) * 0.01;
		}

		Vec3 push = new Vec3(x, 0.0, z).normalize().scale(strength);
		target.setDeltaMovement(
				motion.x / 2.0 - push.x,
				target.onGround() ? Math.min(0.4, motion.y / 2.0 + strength) : motion.y,
				motion.z / 2.0 - push.z);
	}

	/**
	 * Swinging the Bonk Stick at anything that isn't Bonkable works like an empty-hand hit, so boats and minecarts
	 * still break. The stick itself has 0 attack damage, and vanilla ignores a 0-damage hit entirely, so this takes the
	 * stick's damage modifier off for the length of one vanilla attack.
	 */
	private static void emptyHandHit(ServerPlayer bonker, Entity target) {
		AttributeInstance attackDamage = bonker.getAttribute(Attributes.ATTACK_DAMAGE);
		AttributeModifier stickDamage = attackDamage == null ? null : attackDamage.getModifier(Item.BASE_ATTACK_DAMAGE_ID);

		if (stickDamage != null) {
			attackDamage.removeModifier(stickDamage.id());
		}

		emptyHandHitInProgress = true;

		try {
			bonker.attack(target);
		} finally {
			emptyHandHitInProgress = false;

			if (stickDamage != null) {
				attackDamage.addTransientModifier(stickDamage);
			}
		}
	}
}
