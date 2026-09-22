package com.hefker.bonkstick.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads {@code config/bonkstick.json} from disk, writing it with the defaults if it isn't there yet.
 *
 * <p>Never throws and never touches a file that already exists: a file the mod can't use is reported in the log and
 * left as it is for the server owner to fix, while the defaults are used in its place.
 */
public final class BonkConfigFile {
	/** The file's name inside the game's config directory. */
	public static final String FILE_NAME = "bonkstick.json";

	private static final Logger LOGGER = LoggerFactory.getLogger("bonkstick");

	private BonkConfigFile() {
	}

	/** Loads the config at {@code file}, creating it with the defaults first if it's missing. */
	public static BonkConfig load(Path file) {
		if (Files.notExists(file)) {
			writeDefaults(file);
			return BonkConfig.DEFAULTS;
		}

		String json;
		try {
			json = Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.warn("Couldn't read {}, using the default Bonk Stick config: {}", file, e.toString());
			return BonkConfig.DEFAULTS;
		}

		BonkConfig.Parsed parsed = BonkConfig.parse(json);
		for (String warning : parsed.warnings()) {
			LOGGER.warn("{}: {}", file, warning);
		}

		if (!parsed.warnings().isEmpty()) {
			LOGGER.warn("{} was left as it is. Fix it, or delete it to get a fresh default file on the next start.",
					file);
		}

		return parsed.config();
	}

	private static void writeDefaults(Path file) {
		try {
			Path dir = file.toAbsolutePath().getParent();
			if (dir != null) {
				Files.createDirectories(dir);
			}

			Files.writeString(file, BonkConfig.DEFAULTS.toJson(), StandardCharsets.UTF_8);
			LOGGER.info("Wrote the default Bonk Stick config to {}", file);
		} catch (IOException e) {
			LOGGER.warn("Couldn't write the default Bonk Stick config to {}, using the defaults: {}", file,
					e.toString());
		}
	}
}
