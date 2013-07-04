package com.gmail.snipsrevival.commands;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;
import com.gmail.snipsrevival.Prefix;

public class CommandKick implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandKick(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);
		
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("kick")) {
			if(!sender.hasPermission("moderatornotes.kick")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
				return true;
			}
			if(args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Too few arguments!");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/kick <playername> <reason> " + ChatColor.RED + "to kick player");
				return true;
			}
						
			final Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
			
			if(targetPlayer == null) {
				sender.sendMessage(ChatColor.RED + args[0] + " is not online");
				return true;
			}
			
			File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
			YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
			List<String> noteList = userFile.getStringList("notes");
			
			if(userFile.getBoolean("KickExempt") == true) {
				sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is exempt from being kicked");
				return true;
			}
			
			common.createNewFile(file);
			
			StringBuilder strBuilder = new StringBuilder();			
			String prefix = new Prefix(plugin).getPrefix(sender);
			
			for(int arg = 1; arg < args.length; arg = arg+1) {
				strBuilder.append(args[arg] + " ");
			}
			String message = strBuilder.toString().trim();
			
			sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been kicked for this reason: " + message);
			targetPlayer.kickPlayer("You were kicked for this reason: " + message);

			if(plugin.getConfig().getBoolean("AutoRecordKicks") == true) {
				noteList.add(prefix + "has been kicked for this reason: " + message);
				common.addStringStaffList(prefix + targetPlayer.getName() + " has been kicked for this reason: " + message);
				userFile.set("notes", noteList);
			}
			common.saveYamlFile(userFile, file);
		}
		return true;
	}
}