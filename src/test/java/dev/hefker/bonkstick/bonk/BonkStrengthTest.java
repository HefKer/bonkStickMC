package dev.hefker.bonkstick.bonk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BonkStrengthTest {
	private static final double EPSILON = 1e-9;

	@Test
	void fullChargeIsVanillaHitPlusTheBonkPush() {
		BonkStrength.Push push = BonkStrength.compute(1.0F, false, 1.0);

		assertEquals(0.4, push.hit(), EPSILON);
		assertEquals(0.75, push.facing(), EPSILON);
	}

	@Test
	void fullChargeFallsBetweenKnockbackOneAndTwo() {
		double bonk = BonkStrength.compute(1.0F, false, 1.0).facing();

		assertTrue(bonk > 0.5, "stronger than Knockback I");
		assertTrue(bonk < 1.0, "weaker than Knockback II");
	}

	@Test
	void spamClickOnlyTaps() {
		BonkStrength.Push full = BonkStrength.compute(1.0F, false, 1.0);
		BonkStrength.Push spam = BonkStrength.compute(0.1F, false, 1.0);

		assertTrue(spam.hit() < full.hit() / 4);
		assertTrue(spam.facing() < full.facing() / 4);
	}

	@Test
	void emptyChargeStillGivesVanillasMinimum() {
		BonkStrength.Push push = BonkStrength.compute(0.0F, false, 1.0);

		assertEquals(0.4 * 0.2, push.hit(), EPSILON);
		assertEquals(0.75 * 0.2, push.facing(), EPSILON);
	}

	@Test
	void chargedSprintAddsVanillasSprintBonus() {
		BonkStrength.Push push = BonkStrength.compute(1.0F, true, 1.0);

		assertEquals(0.4, push.hit(), EPSILON);
		assertEquals(0.75 + 0.5, push.facing(), EPSILON);
	}

	@Test
	void sprintNeedsAlmostFullCharge() {
		BonkStrength.Push sprinting = BonkStrength.compute(0.8F, true, 1.0);
		BonkStrength.Push walking = BonkStrength.compute(0.8F, false, 1.0);

		assertEquals(walking, sprinting);
	}

	@Test
	void strengthScalesBothPushes() {
		BonkStrength.Push push = BonkStrength.compute(1.0F, true, 2.0);

		assertEquals(0.8, push.hit(), EPSILON);
		assertEquals(2.5, push.facing(), EPSILON);
	}

	@Test
	void chargeOutsideZeroToOneIsClamped() {
		assertEquals(BonkStrength.compute(1.0F, false, 1.0), BonkStrength.compute(1.5F, false, 1.0));
		assertEquals(BonkStrength.compute(0.0F, false, 1.0), BonkStrength.compute(-1.0F, false, 1.0));
	}
}
