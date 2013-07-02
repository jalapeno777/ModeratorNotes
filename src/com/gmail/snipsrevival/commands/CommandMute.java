package com.gmail.snipsrevival.commands;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;

public class CommandMute implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandMute(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);
	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("mute")) {
			if(!sender.hasPermission("moderatornotes.mute")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
			else {
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mute <playername> <reason> " + ChatColor.RED + "to mute player");
				}
				
				else {
					
					StringBuilder strBuilder = new StringBuilder();			
					String prefix = common.getPrefix();
					prefix = prefix.replace("<playername>", sender.getName());
					
					if(common.nameContainsInvalidCharacter(args[0])) {
						sender.sendMessage(ChatColor.RED + "That is an invalid playername");
						return true;
					}
					
					final OfflinePlayer targetPlayer;
					if(Bukkit.getServer().getPlayer(args[0]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[0]);
					else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[0]);
					
					File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
					YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
					List<String> noteList = userFile.getStringList("notes");
					
					if(userFile.getBoolean("MuteExempt") == false) {

						if(userFile.getBoolean("permamute.muted") == false || !file.exists()) {
							common.createNewFile(file);
							userFile.set("permamute.muted", true);	
							for(int arg = 1; arg < args.length; arg = arg+1) {
								strBuilder.append(args[arg] + " ");
							}
							String message = strBuilder.toString().trim();
							
							userFile.set("permamute.reason", "You are muted for this reason: " + message);
							sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been muted for this reason: " + message);
							
							if(plugin.getConfig().getBoolean("AutoRecordMutes") == true) {
								noteList.add(prefix + "has been muted for this reason: " + message);
								common.addStringStaffList(prefix + targetPlayer.getName() + " has been muted for this reason: " + message);
								userFile.set("notes", noteList);
							}
							common.saveYamlFile(userFile, file);
						}
				
						else {
							sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is already muted");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is exempt from being muted");
					}
				}
			}
		}
		return true;
	}
}
