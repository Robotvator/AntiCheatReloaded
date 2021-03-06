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

package com.rammelkast.anticheatreloaded.check.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * This check should be replaced It works, but not great
 */
public class WaterWalkCheck {

	public static final List<UUID> IS_IN_WATER = new ArrayList<UUID>();
	public static final List<UUID> IS_IN_WATER_CACHE = new ArrayList<UUID>();
	public static final Map<UUID, Integer> WATER_SPEED_VIOLATIONS = new HashMap<UUID, Integer>();
	public static final Map<UUID, Integer> WATER_ASCENSION_VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult runCheck(Player player, double x, double y, double z) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		Block block = player.getLocation().getBlock();
		UUID uuid = player.getUniqueId();

		if (player.getVehicle() == null && !player.isFlying() && !VersionUtil.isFrostWalk(player)) {
			if (block.isLiquid()) {
				if (IS_IN_WATER.contains(uuid)) {
					if (IS_IN_WATER_CACHE.contains(uuid)) {
						if (player.getNearbyEntities(1, 1, 1).isEmpty()) {
							boolean violation;
							if (!Utilities.sprintFly(player)) {
								violation = x > backend.getMagic().XZ_SPEED_MAX_WATER()
										|| z > backend.getMagic().XZ_SPEED_MAX_WATER();
							} else {
								violation = x > backend.getMagic().XZ_SPEED_MAX_WATER_SPRINT()
										|| z > backend.getMagic().XZ_SPEED_MAX_WATER_SPRINT();
							}
							if (!violation && !Utilities.isFullyInWater(player.getLocation())
									&& Utilities.isHoveringOverWater(player.getLocation(), 1) && y == 0D
									&& !block.getType().equals(Utilities.LILY_PAD)) {
								violation = true;
							}
							if (violation) {
								if (WATER_SPEED_VIOLATIONS.containsKey(uuid)) {
									int v = WATER_SPEED_VIOLATIONS.get(uuid);
									if (v >= backend.getMagic().WATER_SPEED_VIOLATION_MAX()) {
										WATER_SPEED_VIOLATIONS.put(uuid, 0);
										return new CheckResult(CheckResult.Result.FAILED,
												"stood on water " + v + " times (can't stand on " + block.getType()
														+ " or " + block.getRelative(BlockFace.DOWN).getType() + ")");
									} else {
										WATER_SPEED_VIOLATIONS.put(uuid, v + 1);
									}
								} else {
									WATER_SPEED_VIOLATIONS.put(uuid, 1);
								}
							}
						}
					} else {
						IS_IN_WATER_CACHE.add(uuid);
						return PASS;
					}
				} else {
					IS_IN_WATER.add(uuid);
					return PASS;
				}
			} else if (block.getRelative(BlockFace.DOWN).isLiquid() && !backend.isAscending(player)
					&& Utilities.cantStandAt(block) && Utilities.cantStandAt(block.getRelative(BlockFace.DOWN))
					&& (y < 0.025 || y == 0.09999999999999964 || y == 0.14999999999999947
							|| y == 0.04999999999999982)) {
				if (WATER_ASCENSION_VIOLATIONS.containsKey(uuid)) {
					int v = WATER_ASCENSION_VIOLATIONS.get(uuid);
					if (v >= backend.getMagic().WATER_ASCENSION_VIOLATION_MAX()) {
						WATER_ASCENSION_VIOLATIONS.put(uuid, 0);
						return new CheckResult(CheckResult.Result.FAILED,
								"stood on water " + v + " times (can't stand on " + block.getType() + " or "
										+ block.getRelative(BlockFace.DOWN).getType() + ")");
					} else {
						WATER_ASCENSION_VIOLATIONS.put(uuid, v + 1);
					}
				} else {
					WATER_ASCENSION_VIOLATIONS.put(uuid, 1);
				}
			} else {
				IS_IN_WATER.remove(uuid);
				IS_IN_WATER_CACHE.remove(uuid);
			}
		}
		return PASS;
	}

}
