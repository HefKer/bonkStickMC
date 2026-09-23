package dev.hefker.bonkstick.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import dev.hefker.bonkstick.bonk.BonkSettings;
import dev.hefker.bonkstick.loot.LootChests;

import net.minecraft.resources.ResourceLocation;

/**
 * Everything in {@code config/bonkstick.json}, and how it turns into and out of JSON.
 *
 * <p>The file is split into one object per section, so each feature (and a future config screen) owns its own block:
 *
 * <pre>{@code
 * {
 *   "bonk": {
 *     "bonkStrength": 1.0,
 *     "respectKnockbackResistance": true,
 *     "respectPvpSetting": false
 *   },
 *   "loot": {
 *     "minecraft:chests/simple_dungeon": 0.2,
 *     "minecraft:chests/stronghold_corridor": 0.2
 *   }
 * }
 * }</pre>
 *
 * <p>The {@code "loot"} section maps loot-table ids to the chance (0 to 1) that a chest from that table holds a Bonk
 * Stick. It's taken as a whole: listing it replaces the default Loot Chests, leaving it out keeps them.
 *
 * <p>Parsing never throws. Anything it can't use is reported in {@link Parsed#warnings()} and replaced by its default:
 * a broken file falls back as a whole, a bad or missing value falls back on its own.
 *
 * @param bonk the Bonk section
 * @param loot the Loot Chest section
 */
public record BonkConfig(BonkSettings bonk, LootChests loot) {
	public static final BonkConfig DEFAULTS = new BonkConfig(BonkSettings.DEFAULTS, LootChests.DEFAULTS);

	/** Section holding the Bonk knobs. */
	public static final String BONK = "bonk";
	public static final String BONK_STRENGTH = "bonkStrength";
	public static final String RESPECT_KNOCKBACK_RESISTANCE = "respectKnockbackResistance";
	public static final String RESPECT_PVP_SETTING = "respectPvpSetting";

	/** Lowest allowed {@value #BONK_STRENGTH}: 0 turns the push off. */
	public static final double MIN_BONK_STRENGTH = 0.0;
	/** Highest allowed {@value #BONK_STRENGTH}. */
	public static final double MAX_BONK_STRENGTH = 5.0;

	/** Section mapping Loot Chest loot-table ids to their Bonk Stick chance. */
	public static final String LOOT = "loot";

	/** Lowest allowed Loot Chest chance: 0 means the chest never holds a Bonk Stick. */
	public static final double MIN_CHANCE = 0.0;
	/** Highest allowed Loot Chest chance: 1 means every such chest holds one. */
	public static final double MAX_CHANCE = 1.0;

	private static final Set<String> SECTIONS = Set.of(BONK, LOOT);
	private static final Set<String> BONK_KEYS = Set.of(BONK_STRENGTH, RESPECT_KNOCKBACK_RESISTANCE, RESPECT_PVP_SETTING);

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The result of reading a config file.
	 *
	 * @param config the values to use, with defaults filled in wherever the file couldn't be used
	 * @param warnings one human-readable line per problem found, empty if the file was fine
	 */
	public record Parsed(BonkConfig config, List<String> warnings) {
	}

	/** Reads a config from the text of {@code bonkstick.json}. */
	public static Parsed parse(String json) {
		List<String> warnings = new ArrayList<>();
		JsonElement root;

		try {
			root = readStrict(json);
		} catch (IOException | JsonParseException | IllegalStateException e) {
			warnings.add("not valid JSON (" + e.getMessage() + "), using the defaults");
			return new Parsed(DEFAULTS, warnings);
		}

		if (!root.isJsonObject()) {
			warnings.add("expected a JSON object at the top level, using the defaults");
			return new Parsed(DEFAULTS, warnings);
		}

		JsonObject object = root.getAsJsonObject();
		warnUnknownKeys(object, SECTIONS, "", warnings);
		BonkSettings bonk = parseBonk(section(object, BONK, warnings), warnings);
		LootChests loot = parseLoot(section(object, LOOT, warnings), warnings);
		return new Parsed(new BonkConfig(bonk, loot), warnings);
	}

	/** The JSON written for this config, pretty-printed with a trailing newline. */
	public String toJson() {
		JsonObject bonkSection = new JsonObject();
		bonkSection.addProperty(BONK_STRENGTH, bonk.strength());
		bonkSection.addProperty(RESPECT_KNOCKBACK_RESISTANCE, bonk.respectKnockbackResistance());
		bonkSection.addProperty(RESPECT_PVP_SETTING, bonk.respectPvpSetting());

		JsonObject lootSection = new JsonObject();
		for (Map.Entry<ResourceLocation, Double> entry : loot.chances().entrySet()) {
			lootSection.addProperty(entry.getKey().toString(), entry.getValue());
		}

		JsonObject root = new JsonObject();
		root.add(BONK, bonkSection);
		root.add(LOOT, lootSection);
		return GSON.toJson(root) + "\n";
	}

	/** Clamps a Bonk Strength multiplier into the allowed range. */
	public static double clampBonkStrength(double strength) {
		return Math.max(MIN_BONK_STRENGTH, Math.min(MAX_BONK_STRENGTH, strength));
	}

	/** Clamps a Loot Chest chance into 0 to 1. */
	public static double clampChance(double chance) {
		return Math.max(MIN_CHANCE, Math.min(MAX_CHANCE, chance));
	}

	private static LootChests parseLoot(JsonObject section, List<String> warnings) {
		if (section == null) {
			return LootChests.DEFAULTS;
		}

		String path = LOOT + ".";
		Map<ResourceLocation, Double> chances = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
			String key = entry.getKey();
			ResourceLocation id = ResourceLocation.tryParse(key);
			if (id == null) {
				warnings.add(path + key + " is not a valid loot-table id, skipped");
				continue;
			}

			JsonElement element = entry.getValue();
			JsonPrimitive raw = element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
			if (raw == null || !raw.isNumber() || !Double.isFinite(raw.getAsDouble())) {
				warnings.add(path + key + " should be a chance from " + MIN_CHANCE + " to " + MAX_CHANCE + ", skipped");
				continue;
			}

			double value = raw.getAsDouble();
			double chance = clampChance(value);
			if (chance != value) {
				warnings.add(path + key + " " + value + " is outside " + MIN_CHANCE + " to " + MAX_CHANCE
						+ ", clamped to " + chance);
			}

			if (chances.put(id, chance) != null) {
				warnings.add(path + key + " lists loot table " + id + " a second time, using this later chance");
			}
		}

		return new LootChests(chances);
	}

	private static BonkSettings parseBonk(JsonObject section, List<String> warnings) {
		if (section == null) {
			return BonkSettings.DEFAULTS;
		}

		String path = BONK + ".";
		warnUnknownKeys(section, BONK_KEYS, path, warnings);

		double strength = BonkSettings.DEFAULT_STRENGTH;
		if (section.has(BONK_STRENGTH)) {
			JsonPrimitive raw = primitive(section, BONK_STRENGTH);
			if (raw != null && raw.isNumber() && Double.isFinite(raw.getAsDouble())) {
				double value = raw.getAsDouble();
				strength = clampBonkStrength(value);
				if (strength != value) {
					warnings.add(path + BONK_STRENGTH + " " + value + " is outside " + MIN_BONK_STRENGTH + " to "
							+ MAX_BONK_STRENGTH + ", clamped to " + strength);
				}
			} else {
				warnings.add(wrongType(path + BONK_STRENGTH, "a number", BonkSettings.DEFAULT_STRENGTH));
			}
		}

		boolean respectResistance = parseBoolean(section, RESPECT_KNOCKBACK_RESISTANCE,
				BonkSettings.DEFAULT_RESPECT_KNOCKBACK_RESISTANCE, path, warnings);
		boolean respectPvp = parseBoolean(section, RESPECT_PVP_SETTING,
				BonkSettings.DEFAULT_RESPECT_PVP_SETTING, path, warnings);

		return new BonkSettings(strength, respectResistance, respectPvp);
	}

	private static boolean parseBoolean(JsonObject section, String key, boolean fallback, String path,
			List<String> warnings) {
		if (!section.has(key)) {
			return fallback;
		}

		JsonPrimitive value = primitive(section, key);
		if (value != null && value.isBoolean()) {
			return value.getAsBoolean();
		}

		warnings.add(wrongType(path + key, "true or false", fallback));
		return fallback;
	}

	/** The named section, or {@code null} (with a warning if it's there but not an object) to use its defaults. */
	private static JsonObject section(JsonObject root, String name, List<String> warnings) {
		JsonElement element = root.get(name);
		if (element == null) {
			return null;
		}

		if (!element.isJsonObject()) {
			warnings.add("\"" + name + "\" should be an object, using its defaults");
			return null;
		}

		return element.getAsJsonObject();
	}

	private static JsonPrimitive primitive(JsonObject object, String key) {
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
	}

	private static String wrongType(String key, String expected, Object fallback) {
		return key + " should be " + expected + ", using the default " + fallback;
	}

	private static void warnUnknownKeys(JsonObject object, Set<String> known, String path, List<String> warnings) {
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			if (!known.contains(entry.getKey())) {
				warnings.add("unknown key " + path + entry.getKey() + " ignored");
			}
		}
	}

	/** Parses plain JSON only: no comments, unquoted strings, NaN or trailing content. */
	private static JsonElement readStrict(String json) throws IOException {
		JsonReader reader = new JsonReader(new StringReader(json));
		reader.setLenient(false);
		JsonElement element = GSON.getAdapter(JsonElement.class).read(reader);

		if (reader.peek() != JsonToken.END_DOCUMENT) {
			throw new JsonParseException("unexpected content after the top-level value");
		}

		return element;
	}
}
