package com.gmail.snipsrevival.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;

public class CommandInfo implements CommandExecutor {
		
	ModeratorNotes plugin;
	
	public CommandInfo(ModeratorNotes plugin) {
		this.plugin = plugin;
	}

	CommonUtilities common = new CommonUtilities(plugin);
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		if(cmd.getName().equalsIgnoreCase("info")) {
			if(!sender.hasPermission("moderatornotes.info")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
				return true;
			}
			Set<String> topicSet;
			if(sender.hasPermission("moderatornotes.staffmember")) {
				topicSet = plugin.getConfig().getConfigurationSection("StaffInfo").getKeys(false);
			}
			else {
				topicSet = plugin.getConfig().getConfigurationSection("Info").getKeys(false);
			}
			List<String> topicList = new ArrayList<String>(topicSet);
			
			if(args.length == 0) {
				if(!topicList.isEmpty()) {
					for(String topic : topicList) {
						sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/info " + topic.toLowerCase() + " [page #]" + ChatColor.GREEN + " for more information");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "There are no info topics");
				}
			}
			
			if((args.length == 1 || args.length == 2) && common.containsIgnoreCase(args[0], topicList)) {
				int topicIndex = common.getIndexOfString(args[0], topicList);
				String topic = topicList.get(topicIndex);
				
				List<String> inputList;
				if(sender.hasPermission("moderatornotes.staffmember")) {
					inputList = plugin.getConfig().getStringList("StaffInfo." + topic);
				}
				else {
					inputList = plugin.getConfig().getStringList("Info." + topic);
				}
				double configNumber = plugin.getConfig().getDouble("MessagesPerPage.Info");
				List<String> outputList;
				int totalPages = common.getTotalPages(inputList, configNumber);
				int page;
				try{
					if(args.length == 1) {
						outputList = common.getListPage(inputList, "1", configNumber);
						page = 1;
					}
					else {
						outputList = common.getListPage(inputList, args[1], configNumber);
						page = Integer.parseInt(args[1]);
					}
					
					sender.sendMessage(ChatColor.GOLD + "Info Page " + page + " of " + totalPages + " for Topic " + topic);
					for(String output : outputList) {
						sender.sendMessage(output);
					}
				}
				catch (IllegalArgumentException e) { //ie args[1] is not a natural Number (1, 2, 3 etc)
					sender.sendMessage(ChatColor.RED + "That is an invalid page number");
				}
				catch (IllegalStateException e) { //ie the inputList is empty
					sender.sendMessage(ChatColor.RED + "There is no information for the topic " + topic);
				}
				catch (IndexOutOfBoundsException e) { //ie args[1] is higher than the amount of pages
					sender.sendMessage(ChatColor.RED + "There are only " + totalPages + " pages of info for the topic " + topic);
				}
			}
			if(args.length > 2) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/info <topic> [page #] " + ChatColor.RED + "for more information");
			}
		}
		return true;
	}
}
