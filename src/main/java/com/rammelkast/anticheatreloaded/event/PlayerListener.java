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

package com.rammelkast.anticheatreloaded.event;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.check.movement.AimbotCheck;
import com.rammelkast.anticheatreloaded.check.movement.ElytraCheck;
import com.rammelkast.anticheatreloaded.check.movement.FastLadderCheck;
import com.rammelkast.anticheatreloaded.check.movement.FlightCheck;
import com.rammelkast.anticheatreloaded.check.movement.GlideCheck;
import com.rammelkast.anticheatreloaded.check.movement.SpeedCheck;
import com.rammelkast.anticheatreloaded.check.movement.WaterWalkCheck;
import com.rammelkast.anticheatreloaded.check.movement.YAxisCheck;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class PlayerListener extends EventListener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (getCheckManager().willCheck(player, CheckType.COMMAND_SPAM) && !Permission.getCommandExempt(player, event.getMessage().split(" ")[0])) {
            CheckResult result = getBackend().checkCommandSpam(player, event.getMessage());
            if (result.failed()) {
                event.setCancelled(!silentMode());
				if (!silentMode())
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
                getBackend().processCommandSpammer(player);
                log(null, player, CheckType.COMMAND_SPAM);
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            getBackend().logEnterExit(event.getPlayer());
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() != GameMode.CREATIVE) {
            getBackend().logEnterExit(event.getPlayer());
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            if (event.getEntity() instanceof Arrow) {
                return;
            }

            if (getCheckManager().willCheck(player, CheckType.FAST_PROJECTILE)) {
                CheckResult result = getBackend().checkProjectile(player);
                if (result.failed()) {
                    event.setCancelled(!silentMode());
                    log(result.getMessage(), player, CheckType.FAST_PROJECTILE);
                }
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.PLUGIN) {
            getBackend().logTeleport(event.getPlayer());
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerChangeWorlds(PlayerChangedWorldEvent event) {
        getBackend().logTeleport(event.getPlayer());

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    	Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (getCheckManager().willCheck(player, CheckType.SNEAK)) {
                CheckResult result = getBackend().checkSneakToggle(player);
                if (result.failed()) {
                    event.setCancelled(!silentMode());
                    log(result.getMessage(), player, CheckType.SNEAK);
                }
            }
           // getBackend().logToggleSneak(event.getPlayer());
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        if (getCheckManager().willCheck(player, CheckType.FLIGHT)) {
            if (getBackend().justVelocity(player) && getBackend().extendVelocityTime(player)) {
                event.setCancelled(!silentMode());
                return;
            }
            getBackend().logVelocity(player);
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (getCheckManager().willCheck(player, CheckType.CHAT_SPAM)) {
            CheckResult result = getBackend().checkChatSpam(player, event.getMessage());
            if (result.failed()) {
                event.setCancelled(!silentMode());
                if (!result.getMessage().equals("") && !silentMode()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
                }
                getBackend().processChatSpammer(player);
                AntiCheatReloaded.sendToMainThread(new Runnable() {
					@Override
					public void run() {
						log(null, player, CheckType.CHAT_SPAM);
					}
				});
            }
        }
        
        if (getCheckManager().willCheck(player, CheckType.CHAT_UNICODE)) {
            CheckResult result = getBackend().checkChatUnicode(player, event.getMessage());
            if (result.failed()) {
            	event.setCancelled(true);
            	player.sendMessage(ChatColor.translateAlternateColorCodes('&', result.getMessage()));
                AntiCheatReloaded.sendToMainThread(new Runnable() {
					@Override
					public void run() {
						log(null, player, CheckType.CHAT_UNICODE);
					}
				});
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        getBackend().garbageClean(event.getPlayer());

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getBackend().garbageClean(event.getPlayer());

        User user = getUserManager().getUser(event.getPlayer().getUniqueId());

        getConfig().getLevels().saveLevelFromUser(user);

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (!event.isSprinting()) {
            getBackend().logEnterExit(player);
        }
        if (getCheckManager().willCheck(player, CheckType.SPRINT)) {
            CheckResult result = getBackend().checkSprintHungry(event);
            if (result.failed()) {
                event.setCancelled(!silentMode());
                log(result.getMessage(), player, CheckType.SPRINT);
            } else {
                decrease(player);
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        	ItemStack itemInHand;
        	if (VersionUtil.isBountifulUpdate()) {
        		itemInHand = VersionUtil.getItemInHand(player);
        	} else {
        		itemInHand = ((event.getHand() == EquipmentSlot.HAND) ? inv.getItemInMainHand() : inv.getItemInOffHand());
        	}
        	
            if (itemInHand.getType() == Material.BOW) {
                getBackend().logBowWindUp(player);
            } else if (Utilities.isFood(itemInHand.getType()) || Utilities.isFood(itemInHand.getType())) {
                getBackend().logEatingStart(player);
            }
            
            if (!VersionUtil.isBountifulUpdate()) {
                if (itemInHand.getType() == Material.FIREWORK_ROCKET) {
                    ElytraCheck.JUMP_Y_VALUE.remove(player.getUniqueId());
                    if (player.isGliding()) {
                    	// TODO config max elytra height?
                    	ElytraCheck.JUMP_Y_VALUE.put(player.getUniqueId(), 9999.99D);
                    }
                }
            }
        }
        Block block = event.getClickedBlock();

        if (block != null) {
            Distance distance = new Distance(player.getLocation(), block.getLocation());
            getBackend().checkLongReachBlock(player, distance.getXDifference(), distance.getYDifference(), distance.getZDifference());
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (getCheckManager().willCheck(player, CheckType.ITEM_SPAM)) {
            CheckResult result = getBackend().checkFastDrop(player);
            if (result.failed()) {
                event.setCancelled(!silentMode());
                log(result.getMessage(), player, CheckType.ITEM_SPAM);
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        if (event.getBed().getType().name().endsWith("BED")) return;
        getBackend().logEnterExit(event.getPlayer());

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerExitBed(PlayerBedLeaveEvent event) {
        if (event.getBed().getType().name().endsWith("BED")) return;
        getBackend().logEnterExit(event.getPlayer());

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getBackend().logJoin(player);

        User user = new User(player.getUniqueId());
        user.setIsWaitingOnLevelSync(true);
        getConfig().getLevels().loadLevelToUser(user);
        getUserManager().addUser(user);

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
        
        if (player.hasPermission("anticheat.admin") && !AntiCheatReloaded.getUpdateManager().isLatest()) {
        	player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.GRAY + "Your version of AntiCheatReloaded is outdated! You can download " + AntiCheatReloaded.getUpdateManager().getLatestVersion() + " from the Spigot forums or DevBukkit.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (getCheckManager().checkInWorld(player) && !getCheckManager().isOpExempt(player)) {
            final Location from = event.getFrom();
            final Location to = event.getTo();

            final Distance distance = new Distance(from, to);
            final double y = distance.getYDifference();
            getBackend().logAscension(player, from.getY(), to.getY());

            final User user = getUserManager().getUser(player.getUniqueId());
            user.setTo(to.getX(), to.getY(), to.getZ());
            
            if (getCheckManager().willCheckQuick(player, CheckType.FLIGHT) && !VersionUtil.isFlying(player)) {
                CheckResult result = FlightCheck.runCheck(player, distance);
                if (result.failed()) {
                    if (!silentMode()) {
                        event.setTo(user.getGoodLocation(from.clone()));
                    }
                    log(result.getMessage(), player, CheckType.FLIGHT);
                }
            }
            if (getCheckManager().willCheckQuick(player, CheckType.FLIGHT) && !VersionUtil.isFlying(player)) {
                CheckResult result = GlideCheck.runCheck(player, distance);
                if (result.failed()) {
                    log(result.getMessage(), player, CheckType.FLIGHT);
                }
            }
            if (getCheckManager().willCheckQuick(player, CheckType.ELYTRAFLY)) {
            	 CheckResult result = ElytraCheck.runCheck(player, distance);
                 if (result.failed()) {
                     log(result.getMessage(), player, CheckType.ELYTRAFLY);
                 }
            }
            if (getCheckManager().willCheckQuick(player, CheckType.VCLIP) && event.getFrom().getY() > event.getTo().getY()) {
                CheckResult result = getBackend().checkVClip(player, new Distance(event.getFrom(), event.getTo()));
                if (result.failed()) {
                    if (!silentMode()) {
                        int data = result.getData() > 3 ? 3 : result.getData();
                        Location newloc = new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY() + data, event.getFrom().getZ());
                        if (newloc.getBlock().getType() == Material.AIR) {
                            event.setTo(newloc);
                        } else {
                            event.setTo(user.getGoodLocation(from.clone()));
                        }
                        player.damage(3);
                    }
                    log(result.getMessage(), player, CheckType.VCLIP);
                }
            }
            if (getCheckManager().willCheckQuick(player, CheckType.NOFALL) && getCheckManager().willCheck(player, CheckType.FLIGHT) && !Utilities.isClimbableBlock(player.getLocation().getBlock()) && event.getFrom().getY() > event.getTo().getY()) {
                CheckResult result = getBackend().checkNoFall(player, y);
                if (result.failed()) {
                    if (!silentMode()) {
                        event.setTo(user.getGoodLocation(from.clone()));
                        // TODO better handling of this
                    }
                    log(result.getMessage(), player, CheckType.NOFALL);
                }
            }

            boolean changed = false;
            if (event.getTo() != event.getFrom()) {
                double x = distance.getXDifference();
                double z = distance.getZDifference();
                if (getCheckManager().willCheckQuick(player, CheckType.SPEED) && getCheckManager().willCheck(player, CheckType.FLIGHT)) {
                    if (event.getFrom().getY() < event.getTo().getY()) {
                        CheckResult result = SpeedCheck.checkYSpeed(player, distance);
                        if (result.failed()) {
                            if (!silentMode()) {
                                event.setTo(user.getGoodLocation(from.clone()));
                            }
                            log(result.getMessage(), player, CheckType.SPEED);
                            changed = true;
                        }
                    }
                    CheckResult result = SpeedCheck.checkXZSpeed(player, x, z);
                    if (result.failed()) {
                        if (!silentMode()) {
                            event.setTo(user.getGoodLocation(from.clone()));
                        }
                        log(result.getMessage(), player, CheckType.SPEED);
                        changed = true;
                    }
                }
                if (getCheckManager().willCheckQuick(player, CheckType.WATER_WALK)) {
                    CheckResult result = WaterWalkCheck.runCheck(player, x, y, z);
                    if (result.failed()) {
                        if (!silentMode()) {
                            player.teleport(user.getGoodLocation(player.getLocation().add(0, -1.5, 0)));
                        }
                        log(result.getMessage(), player, CheckType.WATER_WALK);
                        changed = true;
                    }
                }
                if (getCheckManager().willCheckQuick(player, CheckType.SNEAK)) {
                    CheckResult result = getBackend().checkSneak(player, event.getTo(), x, z);
                    if (result.failed()) {
                        if (!silentMode()) {
                            event.setTo(user.getGoodLocation(from.clone()));
                            player.setSneaking(false);
                        }
                        log(result.getMessage(), player, CheckType.SNEAK);
                        changed = true;
                    }
                }
                if (getCheckManager().willCheckQuick(player, CheckType.SPIDER)) {
                    CheckResult result = getBackend().checkSpider(player, y);
                    if (result.failed()) {
                        if (!silentMode()) {
                            event.setTo(user.getGoodLocation(from.clone()));
                        }
                        log(result.getMessage(), player, CheckType.SPIDER);
                        changed = true;
                    }
                }
                if (getCheckManager().willCheckQuick(player, CheckType.FASTLADDER)) {
                	// Does not use y value created before because that value is absolute
                    CheckResult result = FastLadderCheck.runCheck(player, event.getTo().getY() - event.getFrom().getY());
                    if (result.failed()) {
                        if (!silentMode()) {
                            event.setTo(user.getGoodLocation(from.clone()));
                        }
                        log(result.getMessage(), player, CheckType.FASTLADDER);
                        changed = true;
                    }
                }
                if (!changed) {
                    user.setGoodLocation(event.getFrom());
                }
            }
            if (getCheckManager().willCheckQuick(player, CheckType.AIMBOT)) {
            	CheckResult result = AimbotCheck.runCheck(player, event);
                if (result.failed()) {
                    log(result.getMessage(), player, CheckType.AIMBOT);
                }
            }
        }
        
        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
    	getBackend().logTeleport(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void checkFly(PlayerMoveEvent event) {
        // Check flight on highest to make sure other plugins have a chance to change the values first.
        final Player player = event.getPlayer();
        final User user = getUserManager().getUser(player.getUniqueId());
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (!user.checkTo(to.getX(), to.getY(), to.getZ())) {
            // The to value has been modified by another plugin
            return;
        }

        if (getCheckManager().willCheck(player, CheckType.FLIGHT) && !player.isFlying()) {
            CheckResult result1 = YAxisCheck.runCheck(player, new Distance(from, to));
            CheckResult result2 = FlightCheck.checkAscension(player, from.getY(), to.getY());
            String log = result1.failed() ? result1.getMessage() : result2.failed() ? result2.getMessage() : "";
            if (!log.equals("")) {
                if (!silentMode()) {
                    event.setTo(user.getGoodLocation(from.clone()));
                }
                log(log, player, CheckType.FLIGHT);
            }
        }
    }
}
