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

public class CommandMail implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandMail(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		if(cmd.getName().equalsIgnoreCase("mail")) {	
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
		}
		return true;
	}
	
	public boolean readMail(CommandSender sender, String[] args) {
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(sender instanceof Player) {	
			if(args.length == 1 || args.length == 2) {
				if(file.exists()) {
					if(!mailListNew.isEmpty()) {
						sender.sendMessage(ChatColor.GREEN + "These are your unread mail messages:");
						for(int note = 0; note < mailListNew.size(); note++) {
							int noteNumberPrefix = note+1;
							sender.sendMessage(mailListNew.get(note).replace("<index>", Integer.toString(noteNumberPrefix)));
						}
						sender.sendMessage(ChatColor.GREEN + "Reread your mail if you would like to see all of your mail");
						
						mailListRead.addAll(mailListNew);
						mailListNew.clear();
						userFile.set("mail.new", mailListNew);
						userFile.set("mail.read", mailListRead);
						common.saveYamlFile(userFile, file);
					}
					else {
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
							sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail read [page #] " + ChatColor.RED + "to read mail");
						}
						catch (IllegalStateException e) { //ie the inputList is empty
							sender.sendMessage(ChatColor.RED + "You don't have any mail");
						}
						catch (IndexOutOfBoundsException e) { //ie args[1] is higher than the amount of pages
							sender.sendMessage(ChatColor.RED + "You only have " + totalPages + " pages of mail");
						}
					}
				}
			}

			if(args.length > 2) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail read [page #] " + ChatColor.RED + "to read mail");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
		}
		return true;
	}
	
	public boolean removeSingleMail(CommandSender sender, String[] args) {
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(sender instanceof Player) {
			if(args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail remove <note #> " + ChatColor.RED + "to remove mail message");
			}
				
			if(args.length == 2 && sender.hasPermission("moderatornotes.mail.remove")) {		
				if(common.isInt(args[1])) {	
					int number = Integer.parseInt(args[1]);
					if(!mailListNew.isEmpty()) {
						sender.sendMessage(ChatColor.RED + "You have unread mail! Read your mail first.");
					}
					else {
						if(file.exists() && !mailListRead.isEmpty()) {
							if(number <= mailListRead.size() && number > 0) {
								mailListRead.remove(number-1);
								userFile.set("mail.read", mailListRead);
								common.saveYamlFile(userFile, file);
								if(mailListRead.isEmpty()) {
									sender.sendMessage(ChatColor.GREEN + "Message " + args[1] + " has been removed from your mail");
									sender.sendMessage(ChatColor.GREEN + "Your mail is now empty");
									return true;
								}
								sender.sendMessage(ChatColor.GREEN + "Message " + args[1] + " has been removed from your mail");
							}
							else {
								sender.sendMessage(ChatColor.RED + "That message does not exist");
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "That message does not exist");
						}
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "That message does not exist");
				}
			}
			if(args.length > 2) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail remove <message #> " + ChatColor.RED + "to remove mail message");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
		}
		return true;
	}
	
	public boolean removeAllMail(CommandSender sender, String[] args) {
		
		File file = new File(plugin.getDataFolder() + "/userdata/" + sender.getName() + ".yml");
		YamlConfiguration userFile = YamlConfiguration.loadConfiguration(file);
		List<String> mailListNew = userFile.getStringList("mail.new");
		List<String> mailListRead = userFile.getStringList("mail.read");
		
		if(sender instanceof Player) {	
			if(args.length == 1) {
				if(!mailListNew.isEmpty()) {
					sender.sendMessage(ChatColor.RED + "You have unread mail! Read your mail first.");
				}
				else {
					mailListRead.clear();
					userFile.set("mail.read", mailListRead);
					common.saveYamlFile(userFile, file);
					sender.sendMessage(ChatColor.GREEN + "All mail has been removed from your mail");
				}
			}
			
			if(args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail removeall " + ChatColor.RED + "to remove all mail");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "The console does not have a mailbox");
		}
		return true;
	}
	
	public boolean sendMail(CommandSender sender, String[] args) {
		
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail send <playername> <message> " + ChatColor.RED + "to send message to player");
		}
		
		else {
			StringBuilder strBuilder = new StringBuilder();			
			String prefix = common.getPrefix();
			prefix = prefix.replace("<playername>", sender.getName());
			
			File dir = new File(plugin.getDataFolder() + "/userdata/");
			File[] children = dir.listFiles();
			
			if(common.nameContainsInvalidCharacter(args[1])) {
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
			if(children != null) {				
				File childFile = new File(plugin.getDataFolder() + "/userdata/" + targetPlayer.getName().toLowerCase() + ".yml");
				YamlConfiguration userFile = YamlConfiguration.loadConfiguration(childFile);
				List<String> mailListNew = userFile.getStringList("mail.new");
				if(childFile.exists()) {
					mailListNew.add(prefix + message);
					userFile.set("mail.new", mailListNew);
					common.saveYamlFile(userFile, childFile);
					sender.sendMessage(ChatColor.GREEN + "Mail sent to " + targetPlayer.getName());
				}
				else {
					sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " has not played on this server before");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "There are no players to send mail to");
			}
		}
		return true;
	}
	
	public boolean sendAllMail(CommandSender sender, String[] args) {
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = common.getPrefix();
		prefix = prefix.replace("<playername>", sender.getName());
		
		File dir = new File(plugin.getDataFolder() + "/userdata/");
		File[] children = dir.listFiles();

		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendall <message> " + ChatColor.RED + "to send message to all players");
		}
		
		else {
			for(int arg = 1; arg < args.length; arg = arg+1) {
				strBuilder.append(args[arg] + " ");
			}
			String message = strBuilder.toString().trim();	
			if(children != null) {
				for(int i = 0; i < children.length; i++) {
					File childFile = new File(plugin.getDataFolder() + "/userdata/" + children[i].getName());
					YamlConfiguration userFile = YamlConfiguration.loadConfiguration(childFile);
					List<String> mailListNew = userFile.getStringList("mail.new");
					mailListNew.add(prefix + message);
					userFile.set("mail.new", mailListNew);
					common.saveYamlFile(userFile, childFile);	
				}
				sender.sendMessage(ChatColor.GREEN + "Mail sent to all players");
			}
			else {
				sender.sendMessage(ChatColor.RED + "There are no players to send mail to");
			}
		}
		return true;
	}
	
	public boolean sendStaffMail(CommandSender sender, String[] args) {
		
		StringBuilder strBuilder = new StringBuilder();			
		String prefix = common.getPrefix();
		prefix = prefix.replace("<playername>", sender.getName());
		
		File dir = new File(plugin.getDataFolder() + "/userdata/");
		File[] children = dir.listFiles();

		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/mail sendstaff <message> " + ChatColor.RED + "to send message to all staff");
		}
		
		else {
			for(int arg = 1; arg < args.length; arg = arg+1) {
				strBuilder.append(args[arg] + " ");
			}
			String message = strBuilder.toString().trim();
			if(children != null) {
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
			}
		}
		return true;
	}
}
