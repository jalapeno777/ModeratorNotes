package com.gmail.snipsrevival.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.snipsrevival.ModeratorNotes;

public class CommandReloadConfig implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandReloadConfig(ModeratorNotes plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("moderatornotes")) {
			if(sender.hasPermission("moderatornotes.reload")) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("reload")) {
						plugin.reloadConfig();
						sender.sendMessage(ChatColor.GREEN + "ModeratorNotes config reloaded!");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/moderatornotes reload " + ChatColor.RED + "to reload the config");
					}
				}
				if (args.length > 1) {
					sender.sendMessage(ChatColor.RED + "Too many arguments!");
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/moderatornotes reload " + ChatColor.RED + "to reload the config");
				}
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/moderatornotes reload " + ChatColor.RED + "to reload the config");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
		}
		return true;
	}
}
