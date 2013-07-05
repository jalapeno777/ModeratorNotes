package com.gmail.snipsrevival.listeners;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;
import com.gmail.snipsrevival.Updater;

public class PlayerListener implements Listener {
	
	ModeratorNotes plugin;
	
	public PlayerListener(ModeratorNotes plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	CommonUtilities common = new CommonUtilities();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		final Player player = event.getPlayer();
		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(player.isOp()) {
			Updater updater = new Updater(plugin);
			if(!updater.isLatest() && plugin.getConfig().getBoolean("EnableUpdateChecker") == true) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.sendMessage(ChatColor.RED + "There is a newer version of ModeratorNotes available");
					}
				});
			}
		}
		
		if(player.hasPermission("moderatornotes.banexempt")) userFile.set("BanExempt", true);
		if(player.hasPermission("moderatornotes.muteexempt")) userFile.set("MuteExempt", true);
		if(player.hasPermission("moderatornotes.kickexempt")) userFile.set("KickExempt", true);	
		if(player.hasPermission("moderatornotes.staffmember")) userFile.set("staffmember", true);
		if(!player.hasPermission("moderatornotes.banexempt")) userFile.set("BanExempt", false);
		if(!player.hasPermission("moderatornotes.muteexempt")) userFile.set("MuteExempt", false);
		if(!player.hasPermission("moderatornotes.kickexempt")) userFile.set("KickExempt", false);
		if(!player.hasPermission("moderatornotes.staffmember")) userFile.set("staffmember", false);
		userFile.set("mail.new", mailListNew);
		userFile.set("mail.read", mailListRead);
		common.saveYamlFile(userFile, file);
		
		if(player.hasPermission("moderatornotes.mail.read")) {
			if(!mailListNew.isEmpty()) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.sendMessage(ChatColor.GREEN + "You have unread mail in your mailbox");
						player.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/mail read " + ChatColor.GREEN + "to read your mail");
					}
				});
				return;
			}
			if(plugin.getConfig().getBoolean("AlwaysNotifyMailboxMessage") == true ) {
				if(!mailListRead.isEmpty()) {
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							player.sendMessage(ChatColor.GREEN + "You have mail in your mailbox");
							player.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/mail read " + ChatColor.GREEN + "to read your mail");
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
	
		if(player.hasPermission("moderatornotes.banexempt")) userFile.set("BanExempt", true);
		if(player.hasPermission("moderatornotes.muteexempt")) userFile.set("MuteExempt", true);
		if(player.hasPermission("moderatornotes.kickexempt")) userFile.set("KickExempt", true);	
		if(player.hasPermission("moderatornotes.staffmember")) userFile.set("staffmember", true);
		if(!player.hasPermission("moderatornotes.banexempt")) userFile.set("BanExempt", false);
		if(!player.hasPermission("moderatornotes.muteexempt")) userFile.set("MuteExempt", false);
		if(!player.hasPermission("moderatornotes.kickexempt")) userFile.set("KickExempt", false);
		if(!player.hasPermission("moderatornotes.staffmember")) userFile.set("staffmember", false);
		userFile.set("mail.new", mailListNew);
		userFile.set("mail.read", mailListRead);
		common.saveYamlFile(userFile, file);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		
		Player player = event.getPlayer();
		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		
		if(userFile.getBoolean("permamute.muted") == true) {
			event.setCancelled(true);
			if(userFile.getString("permamute.reason") != null) {
				player.sendMessage(ChatColor.RED + userFile.getString("permamute.reason"));
			}
			else {
				player.sendMessage(ChatColor.RED + "You are muted");
			}
			return;
		}
		
		if(userFile.get("tempmute.end") != null) {
			if((System.currentTimeMillis()/1000) >= userFile.getDouble("tempmute.end")) {
				userFile.set("tempmute", null);
				common.saveYamlFile(userFile, file);
			}
			else {
				event.setCancelled(true);
				if(userFile.getString("tempmute.reason") != null) {
					player.sendMessage(ChatColor.RED + userFile.getString("temp.reason"));
				}
				else {
					player.sendMessage(ChatColor.RED + "You are temporarily muted");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		
		Player player = event.getPlayer();
		File file = new File(plugin.getDataFolder() + "/userdata/" + player.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
				
		if(player.isBanned()) {
			if(userFile.get("permaban.reason") == null) {
				event.setKickMessage("You were banned from this server");
			}
			else {
				event.setKickMessage(userFile.getString("permaban.reason"));
			}
			return;
		}
		
		if(!player.isBanned()) {
			if(userFile.get("permaban.reason") != null) {
				userFile.set("permaban", null);
				common.saveYamlFile(userFile, file);
			}
			
			
		}
		
		if(userFile.get("tempban.end") != null) {
			if((System.currentTimeMillis()/1000) >= userFile.getDouble("tempban.end")) {
				userFile.set("tempban", null);
				common.saveYamlFile(userFile, file);
			}
			else {
				event.setResult(Result.KICK_BANNED);
				if(userFile.getString("tempban.reason") != null) {
					event.setKickMessage(userFile.getString("tempban.reason"));
				}
				else {
					event.setKickMessage("You have been temporarily banned from this server");
				}
			}
		}
	}
}
