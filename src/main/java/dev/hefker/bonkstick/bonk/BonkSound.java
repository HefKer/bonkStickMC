package dev.hefker.bonkstick.bonk;

import dev.hefker.bonkstick.sound.ModSounds;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Plays the Bonk Sound at the Bonkable on every Bonk that lands. A blocked Bonk stays quiet here, since the shield plays
 * its own block sound; misses and non-Bonkables never fire {@link BonkCallback}, so they're silent too.
 */
public final class BonkSound {
	/** How far the pitch strays either way from normal on each Bonk. */
	private static final float PITCH_SPREAD = 0.1F;

	private BonkSound() {
	}

	public static void initialize() {
		BonkCallback.EVENT.register(BonkSound::onBonk);
	}

	private static void onBonk(ServerPlayer bonker, LivingEntity bonkable, BonkCallback.Outcome outcome) {
		if (outcome != BonkCallback.Outcome.LANDED) {
			return;
		}

		float pitch = 1.0F + (bonkable.getRandom().nextFloat() * 2.0F - 1.0F) * PITCH_SPREAD;
		// A null player sends it to everyone in range, the Bonker included. Passing the Bonker would leave them out,
		// since vanilla assumes that player already played the sound on their own client.
		bonkable.level().playSound(null, bonkable.getX(), bonkable.getY(), bonkable.getZ(), ModSounds.BONK,
				SoundSource.PLAYERS, 1.0F, pitch);
	}
}
