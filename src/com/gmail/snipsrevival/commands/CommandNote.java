package com.gmail.snipsrevival.commands;

import java.io.File;
import java.util.ArrayList;
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

public class CommandNote implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandNote(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { 
		if(cmd.getName().equalsIgnoreCase("note")) {				
			if(args.length == 0) {
				if(sender.hasPermission("moderatornotes.note.add")) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note add <playername> <message> " + ChatColor.RED + "to add note");
				}
				if(sender.hasPermission("moderatornotes.note.read")) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note list [page #] " + ChatColor.RED + "to show players with notes");
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note read <playername> [page #] " + ChatColor.RED + "to read notes");
				}
				if(sender.hasPermission("moderatornotes.note.read.self") && !sender.hasPermission("moderatornotes.note.read")) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note read <playername> [page #] " + ChatColor.RED + "to read notes");
				}
				if(sender.hasPermission("moderatornotes.note.remove")) {
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note removeall <playername> " + ChatColor.RED + "to remove all notes");
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note remove <playername> <note #> " + ChatColor.RED + "to remove note");
				}
				if(!sender.hasPermission("moderatornotes.note.add") &&
						!sender.hasPermission("moderatornotes.note.read") &&
						!sender.hasPermission("moderatornotes.note.read.self") &&
						!sender.hasPermission("moderatornotes.note.remove")) {
					sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
				}
			}
			
			else {
				if(args[0].equalsIgnoreCase("add") && sender.hasPermission("moderatornotes.note.add")) {
					addNote(sender, args);
				}
				else if(args[0].equalsIgnoreCase("list") && sender.hasPermission("moderatornotes.note.read")) {
					listPlayers(sender, args);
				}
				else if(args[0].equalsIgnoreCase("read") && (sender.hasPermission("moderatornotes.note.read") || sender.hasPermission("moderatornotes.note.read.self"))) {
					readNotes(sender, args);
				}
				else if(args[0].equalsIgnoreCase("remove") && sender.hasPermission("moderatornotes.note.remove")) {
					removeSingleNote(sender, args);
				}
				else if(args[0].equalsIgnoreCase("removeall") && sender.hasPermission("moderatornotes.note.remove")) {
					removeAllNotes(sender, args);
				}
				else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
				}
			}
		}
		return true;
	}
	
	public boolean addNote(CommandSender sender, String[] args) { 
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note add <playername> <message> " + ChatColor.RED + "to add note");
			return true;
		}
				
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = new Prefix(plugin).getPrefix(sender);
		
		if(common.nameContainsInvalidCharacter(args[0])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid playername");
			return true;
		}
		
		final OfflinePlayer targetPlayer;
		if(Bukkit.getServer().getPlayer(args[1]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[1]);
		else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
		
		for(int arg = 2; arg < args.length; arg = arg+1) {
			strBuilder.append(args[arg] + " ");
		}
		String message = strBuilder.toString().trim();
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> notelist = userFile.getStringList("notes");
		
		notelist.add(prefix + message);
		common.addStringStaffList(prefix + "Note about " + targetPlayer.getName() + ": " + message);
		userFile.set("notes", notelist);
		common.saveYamlFile(userFile, file);
		sender.sendMessage(ChatColor.GREEN + "note added for " + targetPlayer.getName());
		return true;
	}
	
	public boolean listPlayers(CommandSender sender, String[] args) { 
		
		File dir = new File(plugin.getDataFolder() + "/userdata/");
		File[] children = dir.listFiles();
		List<String> inputList = new ArrayList<String>();
		
		if(args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note list [page #] " + ChatColor.RED + "to show players with notes");
			return true;
		}
		if(children != null) {
			for(File f : children) {
				File childFile = new File(plugin.getDataFolder() + "/userdata/" + f.getName());
				YamlConfiguration childUserFile = YamlConfiguration.loadConfiguration(childFile);
				List<String> noteList = childUserFile.getStringList("notes");
				String fileListWithExt = childFile.getName();
				int pos = fileListWithExt.lastIndexOf(".");
				if(pos != -1) {
					String fileList = fileListWithExt.substring(0, pos);
					if(childUserFile.contains("notes") && noteList.size() != 0) {
						inputList.add(fileList);
					}
				}
			}
			double configNumber = plugin.getConfig().getDouble("MessagesPerPage.Notes");
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
				
				sender.sendMessage(ChatColor.GOLD + "Players With Notes Page " + page + " of " + totalPages);
				for(String output : outputList) {
					sender.sendMessage(output);
				}
			}
			catch (IllegalArgumentException e) { //ie args[1] is not a natural Number (1, 2, 3 etc)
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note list [page #] " + ChatColor.RED + "to show players with notes");
			}
			catch (IllegalStateException e) { //ie the inputList is empty
				sender.sendMessage(ChatColor.RED + "There are no players with notes");
			}
			catch (IndexOutOfBoundsException e) { //ie args[1] is higher than the amount of pages
				sender.sendMessage(ChatColor.RED + "There are only " + totalPages + " pages of players with notes");
			}
		}
		return true;
	}

	public boolean readNotes(CommandSender sender, String[] args) {	
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note read <playername> [page #] " + ChatColor.RED + "to read notes");
			return true;
		}
		if(args.length > 3) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note read <playername> [page #] " + ChatColor.RED + "to read notes");
			return true;
		}			
		if(common.nameContainsInvalidCharacter(args[1])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid playername");
			return true;
		}
	
		final OfflinePlayer targetPlayer;
		if(Bukkit.getServer().getPlayer(args[1]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[1]);
		else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
		
		if(!targetPlayer.getName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission("moderatornotes.note.read")) {
			sender.sendMessage(ChatColor.RED + "You only have permission to read notes about yourself");
			return true;
		}
			
		File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		
		List<String> inputList = userFile.getStringList("notes");
		double configNumber = plugin.getConfig().getDouble("MessagesPerPage.Notes");
		List<String> outputList;
		int totalPages = common.getTotalPages(inputList, configNumber);
		int page;
		try{
			if(args.length == 2) {
				outputList = common.getListPage(inputList, "1", configNumber);
				page = 1;
			}
			else {
				outputList = common.getListPage(inputList, args[2], configNumber);
				page = Integer.parseInt(args[2]);
			}
			
			sender.sendMessage(ChatColor.GOLD + "Note Page " + page + " of " + totalPages + " for " + targetPlayer.getName());
			int messageNumberPrefix = (int) ((page*configNumber)-configNumber)+1;
			for(String output : outputList) {
				output = output.replace("<index>", Integer.toString(messageNumberPrefix));
				sender.sendMessage(output);
				messageNumberPrefix++;
			}
		}
		catch (IllegalArgumentException e) { //ie args[2] is not a natural Number (1, 2, 3 etc)
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note read <playername> [page #] " + ChatColor.RED + "to read notes");
		}
		catch (IllegalStateException e) { //ie the inputList is empty
			sender.sendMessage(ChatColor.RED + "There are no notes for " + targetPlayer.getName());
		}
		catch (IndexOutOfBoundsException e) { //ie args[2] is higher than the amount of pages
			sender.sendMessage(ChatColor.RED + "There are only " + totalPages + " pages of notes");
		}
		return true;
	}
	
	public boolean removeSingleNote(CommandSender sender, String[] args) { 
		
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note remove <playername> <note #> " + ChatColor.RED + "to remove note");
			return true;
		}
		if(args.length > 3) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note remove <playername> <note #> " + ChatColor.RED + "to remove note");
			return true;
		}
			
		if(common.nameContainsInvalidCharacter(args[0])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid playername");
			return true;
		}
		
		if(!common.isInt(args[2])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid note number");
			return true;
		}
		
		final OfflinePlayer targetPlayer;
		if(Bukkit.getServer().getPlayer(args[1]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[1]);
		else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
							
		File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> noteList = userFile.getStringList("notes");
		int number = Integer.parseInt(args[2]);
		
		if(!file.exists() || noteList.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "That note does not exist");
			return true;
		}
		if(number > noteList.size() || number <= 0) {
			sender.sendMessage(ChatColor.RED + "That note does not exist");
			return true;
		}
		noteList.remove(number-1);
		userFile.set("notes", noteList);
		common.saveYamlFile(userFile, file);
		sender.sendMessage(ChatColor.GREEN + "note " + args[2] + " for " + targetPlayer.getName() + " has been removed");
		if(noteList.isEmpty()) {
			noteList.clear();
			userFile.set("notes", noteList);
			common.saveYamlFile(userFile, file);
			sender.sendMessage(ChatColor.GREEN + "There are no more notes for " + targetPlayer.getName());
		}		
		return true;
	}
	
	public boolean removeAllNotes(CommandSender sender, String[] args) { 
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note removeall <playername> " + ChatColor.RED + "to remove all notes");
			return true;
		}
		if(args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/note removeall <playername> " + ChatColor.RED + "to remove all notes");
			return true;
		}
					
		if(common.nameContainsInvalidCharacter(args[0])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid playername");
			return true;
		}
	
		final OfflinePlayer targetPlayer;
		if(Bukkit.getServer().getPlayer(args[1]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[1]);
		else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
					
		File file = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> noteList = userFile.getStringList("notes");

		noteList.clear();
		userFile.set("notes", noteList);
		common.saveYamlFile(userFile, file);
		sender.sendMessage(ChatColor.GREEN + "All notes for " + targetPlayer.getName() + " have been removed");
		return true;
	}
}
