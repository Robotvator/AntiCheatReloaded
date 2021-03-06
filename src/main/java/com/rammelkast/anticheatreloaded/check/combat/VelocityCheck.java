/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2020 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.rammelkast.anticheatreloaded.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;

/*
 * TODO improve
 */
public class VelocityCheck {

	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<UUID, Integer>();

	public static void runCheck(EntityDamageByEntityEvent e, final Player player) {
		if (!AntiCheatReloaded.getManager().getCheckManager().willCheck(player, CheckType.VELOCITY))
			return;
		final Location then = player.getLocation();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline())
					return;
				if (then.distance(player.getLocation()) < 0.125) {
					if (!VIOLATIONS.containsKey(player.getUniqueId()))
						VIOLATIONS.put(player.getUniqueId(), 1);
					else {
						VIOLATIONS.put(player.getUniqueId(), VIOLATIONS.get(player.getUniqueId()) + 1);
						if (VIOLATIONS.get(player.getUniqueId()) > AntiCheatReloaded.getManager().getBackend().getMagic()
								.VELOCITY_MAX_FLAGS()) {
							EventListener.log(
									new CheckResult(CheckResult.Result.FAILED,
											"had zero/low velocity " + VIOLATIONS.get(player.getUniqueId()) + " times (max="
													+ AntiCheatReloaded.getManager().getBackend().getMagic()
															.VELOCITY_MAX_FLAGS()
													+ ", dist=" + then.distance(player.getLocation()) + ")").getMessage(),
									player, CheckType.VELOCITY);
							VIOLATIONS.remove(player.getUniqueId());
						}
					}
				} else {
					VIOLATIONS.remove(player.getUniqueId());
				}
			}
		}.runTaskLater(AntiCheatReloaded.getPlugin(), 4);
	}

}
