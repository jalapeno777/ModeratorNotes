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

public class CommandWarn implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandWarn(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("warn")) {
			if(sender.hasPermission("moderatornotes.warn")) {
				if(args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/warn <playername> <reason> " + ChatColor.RED + "to warn player");
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
					List<String> noteList = (List<String>) userFile.getStringList("notes");
					List<String> mailListNew = (List<String>) userFile.getStringList("mail.new");
					
					for(int arg = 1; arg < args.length; arg++) {
						strBuilder.append(args[arg] + " ");
					}
					String message = strBuilder.toString().trim();
					sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been warned for this reason: " + message);
					common.createNewFile(file);
					
					if(plugin.getConfig().getBoolean("AutoRecordWarns") == true) {
						noteList.add(prefix + "has been warned for this reason: " + message);
						common.addStringStaffList(prefix + targetPlayer.getName() + " has been warned for this reason: " + message);
						userFile.set("notes", noteList);
					}
					if(targetPlayer.isOnline()) {
						Bukkit.getServer().getPlayer(args[0]).sendMessage(ChatColor.RED + "You have been warned for this reason: " + message);
					}
					mailListNew.add(prefix + ChatColor.RED + "You have been warned for this reason: " + message);
					userFile.set("mail.new", mailListNew);
					common.saveYamlFile(userFile, file);
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
		}
		return true;
	}
}