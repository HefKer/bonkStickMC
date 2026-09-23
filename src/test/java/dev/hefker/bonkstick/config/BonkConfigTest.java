package dev.hefker.bonkstick.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.hefker.bonkstick.bonk.BonkSettings;

class BonkConfigTest {
	@Test
	void defaultsRoundTripWithoutWarnings() {
		BonkConfig.Parsed parsed = BonkConfig.parse(BonkConfig.DEFAULTS.toJson());

		assertEquals(BonkConfig.DEFAULTS, parsed.config());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void defaultFileNestsTheBonkKeysUnderABonkSection() {
		String json = BonkConfig.DEFAULTS.toJson();

		assertTrue(json.contains("\"bonk\": {"), json);
		assertTrue(json.contains("\"bonkStrength\": 1.0"), json);
		assertTrue(json.contains("\"respectKnockbackResistance\": true"), json);
		assertTrue(json.contains("\"respectPvpSetting\": false"), json);
	}

	@Test
	void readsEveryBonkKey() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkStrength": 3, "respectKnockbackResistance": false, "respectPvpSetting": true}}
				""");

		assertEquals(new BonkSettings(3.0, false, true), parsed.config().bonk());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void missingKeysFallBackOneByOne() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"respectPvpSetting": true}}
				""");

		assertEquals(new BonkSettings(BonkSettings.DEFAULT_STRENGTH, BonkSettings.DEFAULT_RESPECT_KNOCKBACK_RESISTANCE,
				true), parsed.config().bonk());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void missingSectionOrEmptyObjectMeansDefaults() {
		BonkConfig.Parsed parsed = BonkConfig.parse("{}");

		assertEquals(BonkConfig.DEFAULTS, parsed.config());
		assertTrue(parsed.warnings().isEmpty(), parsed.warnings().toString());
	}

	@Test
	void strengthAboveFiveIsClampedWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkStrength": 12.5}}
				""");

		assertEquals(5.0, parsed.config().bonk().strength());
		assertEquals(1, parsed.warnings().size());
		assertTrue(parsed.warnings().get(0).contains("clamped"), parsed.warnings().toString());
	}

	@Test
	void negativeStrengthIsClampedToZero() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkStrength": -1}}
				""");

		assertEquals(0.0, parsed.config().bonk().strength());
		assertEquals(1, parsed.warnings().size());
	}

	@Test
	void strengthsInsideTheRangeAreKeptExactly() {
		assertEquals(0.0, BonkConfig.parse("{\"bonk\": {\"bonkStrength\": 0}}").config().bonk().strength());
		assertEquals(5.0, BonkConfig.parse("{\"bonk\": {\"bonkStrength\": 5}}").config().bonk().strength());
		assertEquals(0.25, BonkConfig.parse("{\"bonk\": {\"bonkStrength\": 0.25}}").config().bonk().strength());
	}

	@Test
	void wrongTypesFallBackPerKeyWithWarnings() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkStrength": "3", "respectKnockbackResistance": "no", "respectPvpSetting": true}}
				""");

		assertEquals(new BonkSettings(BonkSettings.DEFAULT_STRENGTH, BonkSettings.DEFAULT_RESPECT_KNOCKBACK_RESISTANCE,
				true), parsed.config().bonk());
		assertEquals(2, parsed.warnings().size(), parsed.warnings().toString());
	}

	@Test
	void sectionThatIsNotAnObjectFallsBackWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("{\"bonk\": 3}");

		assertEquals(BonkConfig.DEFAULTS, parsed.config());
		assertEquals(1, parsed.warnings().size());
	}

	@Test
	void unknownKeysAreIgnoredWithAWarning() {
		BonkConfig.Parsed parsed = BonkConfig.parse("""
				{"bonk": {"bonkstrength": 3}, "extra": 1}
				""");

		assertEquals(BonkConfig.DEFAULTS, parsed.config());
		assertEquals(2, parsed.warnings().size(), parsed.warnings().toString());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"   ",
			"not json",
			"{\"bonk\": {\"bonkStrength\": 3,}}",
			"{\"bonk\": {\"bonkStrength\": 3}",
			"{\"bonk\": {\"bonkStrength\": NaN}}",
			"// comment\n{}",
			"{} {}",
			"[]",
			"null",
			"3",
	})
	void brokenFilesGiveAWarningAndTheDefaults(String json) {
		BonkConfig.Parsed parsed = BonkConfig.parse(json);

		assertEquals(BonkConfig.DEFAULTS, parsed.config());
		assertFalse(parsed.warnings().isEmpty(), "expected a warning for: " + json);
	}
}
