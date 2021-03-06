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
import com.gmail.snipsrevival.Prefix;

public class CommandUnmute implements CommandExecutor {
	
	ModeratorNotes plugin;
	CommonUtilities common;
	
	public CommandUnmute(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
		
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		
		this.common = new CommonUtilities(plugin);

		if(!sender.hasPermission("moderatornotes.unmute")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			return true;
		}
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/unmute <playername> <reason> " + ChatColor.RED + "to unmute player");
			return true;
		}
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
		
		if(userFile.get("permamute") == null && userFile.get("tempmute") == null) {
			sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is not muted");
			return true;
		}
		
		userFile.set("permamute", null);
		userFile.set("tempmute", null);
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = new Prefix(plugin).getPrefix(sender);
		
		for(int arg = 1; arg < args.length; arg = arg+1) {
			strBuilder.append(args[arg] + " ");
		}
		String message = strBuilder.toString().trim();
		
		sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been unmuted for this reason: " + message);
		
		if(plugin.getConfig().getBoolean("AutoRecordUnmutes") == true) {
			noteList.add(prefix + "has been unmuted for this reason: " + message);
			common.addStringStaffList(prefix + targetPlayer.getName() + " has been unmuted for this reason: " + message);
			userFile.set("notes", noteList);
		}
		common.saveYamlFile(userFile, file);
		return true;
	}
}