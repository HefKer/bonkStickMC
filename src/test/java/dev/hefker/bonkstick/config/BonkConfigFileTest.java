package dev.hefker.bonkstick.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.hefker.bonkstick.bonk.BonkSettings;

class BonkConfigFileTest {
	@TempDir
	Path dir;

	@Test
	void missingFileIsWrittenWithTheDefaults() throws IOException {
		Path file = dir.resolve("config").resolve(BonkConfigFile.FILE_NAME);

		BonkConfig config = BonkConfigFile.load(file);

		assertEquals(BonkConfig.DEFAULTS, config);
		assertTrue(Files.exists(file));
		assertEquals(BonkConfig.DEFAULTS.toJson(), Files.readString(file, StandardCharsets.UTF_8));
	}

	@Test
	void existingFileIsReadAndNotRewritten() throws IOException {
		Path file = dir.resolve(BonkConfigFile.FILE_NAME);
		String json = "{\"bonk\": {\"bonkStrength\": 3}}";
		Files.writeString(file, json);

		BonkConfig config = BonkConfigFile.load(file);

		assertEquals(new BonkSettings(3.0, BonkSettings.DEFAULT_RESPECT_KNOCKBACK_RESISTANCE,
				BonkSettings.DEFAULT_RESPECT_PVP_SETTING), config.bonk());
		assertEquals(json, Files.readString(file));
	}

	@Test
	void brokenFileGivesTheDefaultsAndIsLeftAlone() throws IOException {
		Path file = dir.resolve(BonkConfigFile.FILE_NAME);
		String broken = "{\"bonk\": {\"bonkStrength\": 3,";
		Files.writeString(file, broken);

		BonkConfig config = BonkConfigFile.load(file);

		assertEquals(BonkConfig.DEFAULTS, config);
		assertEquals(broken, Files.readString(file));
	}

	@Test
	void unreadableFileGivesTheDefaults() throws IOException {
		// A directory where the file should be can't be read as text.
		Path file = Files.createDirectory(dir.resolve(BonkConfigFile.FILE_NAME));

		assertEquals(BonkConfig.DEFAULTS, BonkConfigFile.load(file));
	}

	@Test
	void nonUtf8FileGivesTheDefaults() throws IOException {
		Path file = dir.resolve(BonkConfigFile.FILE_NAME);
		Files.write(file, new byte[] {'{', (byte) 0xFF, '}'});

		assertEquals(BonkConfig.DEFAULTS, BonkConfigFile.load(file));
	}
}
