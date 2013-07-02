package com.gmail.snipsrevival.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;

public class CommandRules implements CommandExecutor {
		
	ModeratorNotes plugin;
	
	public CommandRules(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("rules")) {
			if(!sender.hasPermission("moderatornotes.rules")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
			else {
				if(args.length > 1) {
					sender.sendMessage(ChatColor.RED + "Too many arguments!");
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/rules [page #] " + ChatColor.RED + "to show the rules");
				}
				else {
					List<String> inputList = plugin.getConfig().getStringList("Rules");
					double configNumber = plugin.getConfig().getDouble("MessagesPerPage.Rules");
					List<String> outputList;
					int totalPages = common.getTotalPages(inputList, configNumber);
					int page;
					try{
						if(args.length == 0) {
							outputList = common.getListPage(inputList, "1", configNumber);
							page = 1;
						}
						else {
							outputList = common.getListPage(inputList, args[0], configNumber);
							page = Integer.parseInt(args[0]);
						}
						
						sender.sendMessage(ChatColor.GOLD + "Rule Page " + page + " of " + totalPages);
						for(String output : outputList) {
							sender.sendMessage(output);
						}
					}
					catch (IllegalArgumentException e) { //ie args[0] is not a natural Number (1, 2, 3 etc)
						sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/rules [page #] " + ChatColor.RED + "to show the rules");
					}
					catch (IllegalStateException e) { //ie the original list is empty
						sender.sendMessage(ChatColor.RED + "No rules have been listed");
					}
					catch (IndexOutOfBoundsException e) { //ie args[0] is higher than the amount of pages
						sender.sendMessage(ChatColor.RED + "There are only " + totalPages + " pages of rules");
					}
				}
			}
		}
		return true;
	}
}
