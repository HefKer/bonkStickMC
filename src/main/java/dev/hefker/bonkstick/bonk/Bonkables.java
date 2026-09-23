package dev.hefker.bonkstick.bonk;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;

/**
 * Decides what counts as a Bonkable: every living mob, every player and armor stands. Anything else (boats,
 * minecarts, item frames…) is not, and gets an ordinary empty-hand hit instead.
 */
public final class Bonkables {
	private Bonkables() {
	}

	/**
	 * Returns the Bonkable that swinging at {@code target} would Bonk, or {@code null} if it isn't Bonkable.
	 *
	 * <p>The ender dragon is hit through its parts, so a part resolves to the dragon itself.
	 *
	 * @param playersProtected true when players aren't Bonkable right now (PvP is off and the server respects that)
	 */
	@Nullable
	public static LivingEntity resolve(Entity target, boolean playersProtected) {
		Entity entity = target instanceof EnderDragonPart part ? part.parentMob : target;

		if (!(entity instanceof LivingEntity living) || entity.isSpectator()) {
			return null;
		}

		if (playersProtected && living instanceof Player) {
			return null;
		}

		return living;
	}
}
