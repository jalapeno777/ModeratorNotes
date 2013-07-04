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
import org.bukkit.entity.Player;

import com.gmail.snipsrevival.CommonUtilities;
import com.gmail.snipsrevival.ModeratorNotes;
import com.gmail.snipsrevival.Prefix;

public class CommandMail implements CommandExecutor {
	
	ModeratorNotes plugin;
	CommonUtilities common;
	
	public CommandMail(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
			
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		
		this.common = new CommonUtilities(plugin);

		if(args.length == 0) {
			if(sender.hasPermission("moderatornotes.mail.send")) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail send <playername> <message> " + ChatColor.RED + "to send mail");
			}
			if(sender.hasPermission("moderatornotes.mail.sendall")) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendall <message> " + ChatColor.RED + "to send mail to all players");
			}
			if(sender.hasPermission("moderatornotes.mail.sendstaff")) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendstaff <message> " + ChatColor.RED + "to send mail to staff members");
			}
			if(sender.hasPermission("moderatornotes.mail.read")) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail read " + ChatColor.RED + "to read mail");
			}
			if(sender.hasPermission("moderatornotes.mail.remove")) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail removeall " + ChatColor.RED + "to remove all mail");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail remove <note #> " + ChatColor.RED + "to remove mail message");
			}
			if (!sender.hasPermission("moderatornotes.mail.send") &&
					!sender.hasPermission("moderatornotes.mail.sendall") &&
					!sender.hasPermission("moderatornotes.mail.sendstaff") &&
					!sender.hasPermission("moderatornotes.mail.read") &&
					!sender.hasPermission("moderatornotes.mail.remove")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
		}
		else {
			if(args[0].equalsIgnoreCase("read") && sender.hasPermission("moderatornotes.mail.read")) {
				readMail(sender, args);
			}
			else if(args[0].equalsIgnoreCase("remove") && sender.hasPermission("moderatornotes.mail.remove")) {
				removeSingleMail(sender, args);
			}
			else if(args[0].equalsIgnoreCase("removeall") && sender.hasPermission("moderatornotes.mail.remove")) {
				removeAllMail(sender, args);
			}
			else if(args[0].equalsIgnoreCase("send") && sender.hasPermission("moderatornotes.mail.send")) {
				sendMail(sender, args);
			}
			else if(args[0].equalsIgnoreCase("sendall") && sender.hasPermission("moderatornotes.mail.sendall")) {
				sendAllMail(sender, args);
			}
			else if(args[0].equalsIgnoreCase("sendstaff") && sender.hasPermission("moderatornotes.mail.sendstaff")) {
				sendStaffMail(sender, args);
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
			}
		}
		return true;
	}
	
	public boolean readMail(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
			return true;
		}
		if(args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail read [page #] " + ChatColor.RED + "to read mail");
			return true;
		}
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(!mailListNew.isEmpty()) {
			sender.sendMessage(ChatColor.GOLD + "All Unread Mail for " + sender.getName());
			for(int i = 0; i < mailListNew.size(); i++) {
				int noteNumberPrefix = i+1;
				sender.sendMessage(mailListNew.get(i).replace("<index>", Integer.toString(noteNumberPrefix)));
			}
			sender.sendMessage(ChatColor.GREEN + "Reread your mail if you would like to see all of your mail");
			
			mailListRead.addAll(mailListNew);
			mailListNew.clear();
			userFile.set("mail.new", mailListNew);
			userFile.set("mail.read", mailListRead);
			common.saveYamlFile(userFile, file);
			return true;
		}
		double configNumber = plugin.getConfig().getDouble("MessagesPerPage.Mail");
		List<String> outputList;
		int totalPages = common.getTotalPages(mailListRead, configNumber);
		int page;
		try{
			if(args.length == 1) {
				outputList = common.getListPage(mailListRead, "1", configNumber);
				page = 1;
			}
			else {
				outputList = common.getListPage(mailListRead, args[1], configNumber);
				page = Integer.parseInt(args[1]);
			}
			
			sender.sendMessage(ChatColor.GOLD + "Mail Page " + page + " of " + totalPages + " for " + sender.getName());
			int messageNumberPrefix = (int) ((page*configNumber)-configNumber)+1;
			for(String output : outputList) {
				output = output.replace("<index>", Integer.toString(messageNumberPrefix));
				sender.sendMessage(output);
				messageNumberPrefix++;
			}
		}
		catch (IllegalArgumentException e) { //ie args[1] is not a natural Number (1, 2, 3 etc)
			sender.sendMessage(ChatColor.RED + "That is an invalid page number");
		}
		catch (IllegalStateException e) { //ie the inputList is empty
			sender.sendMessage(ChatColor.RED + "You don't have any mail");
		}
		catch (IndexOutOfBoundsException e) { //ie args[1] is higher than the amount of pages
			sender.sendMessage(ChatColor.RED + "You only have " + totalPages + " pages of mail");
		}
		return true;
	}
	
	public boolean removeSingleMail(CommandSender sender, String[] args) {	
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
			return true;
		}
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail remove <note #> " + ChatColor.RED + "to remove mail message");
			return true;
		}
		if(args.length > 2) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail remove <message #> " + ChatColor.RED + "to remove mail message");
			return true;
		}
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(!mailListNew.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You have unread mail! Read your mail first.");
			return true;
		}	
		if(!common.isInt(args[1])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid message number");
			return true;
		}
		
		int number = Integer.parseInt(args[1]);
		
		if(!file.exists() || mailListRead.isEmpty() || 
				number > mailListRead.size() || number <= 0) {
			sender.sendMessage(ChatColor.RED + "That message does not exist");
			return true;
		}

		mailListRead.remove(number-1);
		userFile.set("mail.read", mailListRead);
		common.saveYamlFile(userFile, file);
		if(mailListRead.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "Message " + args[1] + " has been removed from your mailbox");
			sender.sendMessage(ChatColor.GREEN + "Your mailbox is now empty");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Message " + args[1] + " has been removed from your mail");
		return true;
	}
	
	public boolean removeAllMail(CommandSender sender, String[] args) {	
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
			return true;
		}
		if(args.length > 1) {
			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail removeall " + ChatColor.RED + "to remove all mail");
			return true;
		}
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(!mailListNew.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You have unread mail! Read your mail first.");
			return true;
		}
		mailListRead.clear();
		userFile.set("mail.read", mailListRead);
		common.saveYamlFile(userFile, file);
		sender.sendMessage(ChatColor.GREEN + "All mail has been removed from your mailbox");
		return true;
	}
	
	public boolean sendMail(CommandSender sender, String[] args) {
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail send <playername> <message> " + ChatColor.RED + "to send message to player");
			return true;
		}
		
		if(common.nameContainsInvalidCharacter(args[1])) {
			sender.sendMessage(ChatColor.RED + "That is an invalid playername");
			return true;
		}
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = new Prefix(plugin).getPrefix(sender);
		
		final OfflinePlayer targetPlayer;
		if(Bukkit.getServer().getPlayer(args[1]) != null) targetPlayer = Bukkit.getServer().getPlayer(args[1]);
		else targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
	
		for(int arg = 2; arg < args.length; arg = arg+1) {
			strBuilder.append(args[arg] + " ");
		}
		String message = strBuilder.toString().trim();
		
		File childFile = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(childFile);
		List<String> mailListNew = userFile.getStringList("mail.new");
		
		if(!childFile.exists()) {
			sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " has not played on this server before");
			return true;
		}
		mailListNew.add(prefix + message);
		userFile.set("mail.new", mailListNew);
		common.saveYamlFile(userFile, childFile);
		sender.sendMessage(ChatColor.GREEN + "Mail sent to " + targetPlayer.getName());
		return true;
	}
	
	public boolean sendAllMail(CommandSender sender, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendall <message> " + ChatColor.RED + "to send message to all players");
			return true;
		}
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = new Prefix(plugin).getPrefix(sender);
		
		File dir = new File(plugin.getDataFolder() + "/userdata/");
		File[] children = dir.listFiles();
		
		for(int arg = 1; arg < args.length; arg = arg+1) {
			strBuilder.append(args[arg] + " ");
		}
		String message = strBuilder.toString().trim();	
		
		for(int i = 0; i < children.length; i++) {
			File childFile = new File(plugin.getDataFolder() + "/userdata/" + children[i].getName());
			YamlConfiguration userFile = YamlConfiguration.loadConfiguration(childFile);
			List<String> mailListNew = userFile.getStringList("mail.new");
			mailListNew.add(prefix + message);
			userFile.set("mail.new", mailListNew);
			common.saveYamlFile(userFile, childFile);	
		}
		sender.sendMessage(ChatColor.GREEN + "Mail sent to all players");
		return true;
	}
	
	public boolean sendStaffMail(CommandSender sender, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendstaff <message> " + ChatColor.RED + "to send message to all staff");
			return true;
		}
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = new Prefix(plugin).getPrefix(sender);
		
		File dir = new File(plugin.getDataFolder() + "/userdata/");
		File[] children = dir.listFiles();
		
		for(int arg = 1; arg < args.length; arg = arg+1) {
			strBuilder.append(args[arg] + " ");
		}
		String message = strBuilder.toString().trim();
		
		for(int i = 0; i < children.length; i++) {
			File childFile = new File(plugin.getDataFolder() + "/userdata/" + children[i].getName());
			YamlConfiguration userFile = YamlConfiguration.loadConfiguration(childFile);
			List<String> mailListNew = userFile.getStringList("mail.new");
			if(userFile.getBoolean("staffmember") == true) {
				mailListNew.add(prefix + message);
				userFile.set("mail.new", mailListNew);
				common.saveYamlFile(userFile, childFile);	
			}
		}
		sender.sendMessage(ChatColor.GREEN + "Mail sent to all staff");
		return true;
	}
}
