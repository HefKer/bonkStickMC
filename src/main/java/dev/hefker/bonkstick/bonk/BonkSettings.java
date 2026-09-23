package dev.hefker.bonkstick.bonk;

/**
 * The server-owner knobs for a Bonk.
 *
 * <p>They come from the {@code "bonk"} section of {@code config/bonkstick.json}, loaded once when the mod starts (see
 * {@link #use(BonkSettings)}). Until then, and whenever the file can't be used, they're the defaults below. The Bonk
 * logic asks {@link #current()} on each Bonk.
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

	private static volatile BonkSettings current = DEFAULTS;

	/** The settings in effect right now. */
	public static BonkSettings current() {
		return current;
	}

	/** Makes {@code settings} the ones every following Bonk uses. Called once at startup with the loaded config. */
	public static void use(BonkSettings settings) {
		current = settings;
	}
}
