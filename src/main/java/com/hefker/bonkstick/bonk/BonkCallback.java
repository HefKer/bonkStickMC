package com.hefker.bonkstick.bonk;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fired on the server after every Bonk that reaches a Bonkable, whether it landed or a raised shield stopped it. A
 * swing that misses, or reaches something that isn't Bonkable, fires nothing.
 */
@FunctionalInterface
public interface BonkCallback {
	Event<BonkCallback> EVENT = EventFactory.createArrayBacked(BonkCallback.class,
			listeners -> (bonker, bonkable, outcome) -> {
				for (BonkCallback listener : listeners) {
					listener.onBonk(bonker, bonkable, outcome);
				}
			});

	void onBonk(ServerPlayer bonker, LivingEntity bonkable, Outcome outcome);

	enum Outcome {
		/** The Bonkable was pushed. */
		LANDED,
		/** A raised shield facing the Bonker stopped the Bonk; nothing was pushed. */
		BLOCKED
	}
}
