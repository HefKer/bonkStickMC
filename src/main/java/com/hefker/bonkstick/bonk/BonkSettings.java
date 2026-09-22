package com.hefker.bonkstick.bonk;

/**
 * The server-owner knobs for a Bonk.
 *
 * <p>For now these are fixed defaults. The config file will replace {@link #current()} with values read from disk;
 * everything else asks {@code current()} on each Bonk, so nothing else needs to change when that happens.
 *
 * @param strength how hard a Bonk pushes, as a multiplier on the default Bonk Strength (1.0 = between Knockback I and
 *     Knockback II at full Charge)
 * @param respectKnockbackResistance whether knockback resistance (iron golems, netherite…) reduces a Bonk
 * @param respectPvpSetting whether players stop being Bonkable when the server has PvP turned off
 */
public record BonkSettings(double strength, boolean respectKnockbackResistance, boolean respectPvpSetting) {
	/** Default Bonk Strength multiplier. */
	public static final double DEFAULT_STRENGTH = 1.0;
	/** By default knockback resistance counts, as it does for vanilla knockback. */
	public static final boolean DEFAULT_RESPECT_KNOCKBACK_RESISTANCE = true;
	/** By default players can be Bonked even with PvP off: a Bonk isn't an attack. */
	public static final boolean DEFAULT_RESPECT_PVP_SETTING = false;

	public static final BonkSettings DEFAULTS =
			new BonkSettings(DEFAULT_STRENGTH, DEFAULT_RESPECT_KNOCKBACK_RESISTANCE, DEFAULT_RESPECT_PVP_SETTING);

	/** The settings in effect right now. */
	public static BonkSettings current() {
		return DEFAULTS;
	}
}
