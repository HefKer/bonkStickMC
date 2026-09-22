package com.hefker.bonkstick.bonk;

/**
 * Works out how hard a Bonk pushes from the Charge and whether the Bonker is sprinting.
 *
 * <p>A vanilla melee hit with Knockback pushes twice: {@code LivingEntity.hurt} applies a fixed 0.4 away from the
 * attacker, then {@code Player.attack} adds {@code 0.5} per Knockback level (and 0.5 more for a charged sprint hit)
 * along the attacker's facing. A Bonk pushes the same two ways, with 0.75 in place of the enchantment's share, which
 * puts a fully charged Bonk between Knockback I (0.5) and Knockback II (1.0).
 *
 * <p>Unlike vanilla knockback, both pushes scale with the Charge along vanilla's damage curve
 * ({@code 0.2 + 0.8 * charge²}), so a spam-click only taps.
 */
public final class BonkStrength {
	/** The push every vanilla melee hit gives, from {@code LivingEntity.hurt}. */
	static final double HIT_PUSH = 0.4;
	/** The Bonk's extra push on top of that at full Charge: between Knockback I (0.5) and Knockback II (1.0). */
	static final double BONK_PUSH = 0.75;
	/** Vanilla's sprint-hit bonus: one extra Knockback level, times 0.5. */
	static final double SPRINT_PUSH = 0.5;
	/** Vanilla only counts a sprint hit above this Charge. */
	static final float SPRINT_MIN_CHARGE = 0.9F;

	private BonkStrength() {
	}

	/**
	 * The two pushes of a Bonk, as strengths for {@code LivingEntity.knockback}.
	 *
	 * @param hit the push away from the Bonker, like the one every vanilla hit gives
	 * @param facing the push along the Bonker's facing, like vanilla's Knockback enchantment and sprint bonus
	 */
	public record Push(double hit, double facing) {
	}

	/**
	 * @param charge how full the attack-cooldown bar was, 0 to 1
	 * @param sprinting whether the Bonker was sprinting
	 * @param strength the Bonk Strength multiplier from {@link BonkSettings#strength()}
	 */
	public static Push compute(float charge, boolean sprinting, double strength) {
		float clamped = Math.max(0.0F, Math.min(1.0F, charge));
		double chargeScale = 0.2 + 0.8 * clamped * clamped;
		double sprintBonus = sprinting && clamped > SPRINT_MIN_CHARGE ? SPRINT_PUSH : 0.0;

		return new Push(
				HIT_PUSH * chargeScale * strength,
				(BONK_PUSH * chargeScale + sprintBonus) * strength);
	}
}
