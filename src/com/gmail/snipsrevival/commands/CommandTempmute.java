package com.gmail.snipsrevival.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class CommandTempmute implements CommandExecutor {
	
	ModeratorNotes plugin;
	
	public CommandTempmute(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	CommonUtilities common = new CommonUtilities(ModeratorNotes.plugin);
	
	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("tempmute")) {
			if(sender.hasPermission("moderatornotes.tempmute")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
				return true;
			}
			if(args.length < 3) {
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/tempmute <playername> <time> <reason> " + ChatColor.RED + "to tempmute player");
				return true;
			}
			
			StringBuilder strBuilder = new StringBuilder();			
			String prefix = new Prefix(plugin).getPrefix(sender);
			
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
			
			if(userFile.getBoolean("MuteExempt") == true) {
				sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is exempt from being muted");
				return true;
			}
			if(userFile.getBoolean("permamute.muted") == true) {
				sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " is already permanently muted");
				return true;
			}
			double unmuteTime = 0;

			Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)([wdhms]{1})");
			Matcher matcher = pattern.matcher(args[1]);

			if(matcher.matches()) {
				
				int pos = matcher.start(2);
				String number = args[1].substring(0, pos);
				String letter = args[1].substring(pos);
				double i = Double.parseDouble(number);
				
				if(letter.equalsIgnoreCase("w")) {
					unmuteTime = i*604800;
				}
				if(letter.equalsIgnoreCase("d")) {
					unmuteTime = i*86400;
				}
				if(letter.equalsIgnoreCase("h")) {
					unmuteTime = i*3600;
				}
				if(letter.equalsIgnoreCase("m")) {
					unmuteTime = i*60;
				}
				if(letter.equalsIgnoreCase("s")) {
					unmuteTime = i;
				}				
			}
								
			if(unmuteTime <= 0) {
				sender.sendMessage(ChatColor.RED + "That is an invalid time argument");
				return true;
			}
			
			Date unmuteDateUnformatted = new Date((long) (System.currentTimeMillis() + unmuteTime*1000));
			String unmuteDate = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a z").format(unmuteDateUnformatted);
			
			for(int x = 2; x < args.length; x++) {
				strBuilder.append(args[x] + " ");
			}
			String message = strBuilder.toString().trim();
			
			common.createNewFile(file);
			userFile.set("tempmute.end", (System.currentTimeMillis()/1000) + unmuteTime);
			userFile.set("tempmute.reason", "You are tempmuted until " + unmuteDate + " for this reason: " + message);
			sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has been tempmuted until " + unmuteDate + " " + message);
			
			if(plugin.getConfig().getBoolean("AutoRecordTempmutes") == true) {
				noteList.add(prefix + "has been tempmuted until " + unmuteDate + " for this reason: " + message);
				common.addStringStaffList(prefix + targetPlayer.getName() + " has been tempmuted until " + unmuteDate + " for this reason: " + message);
				userFile.set("notes", noteList);
			}
			common.saveYamlFile(userFile, file);
		}
		return true;
	}
}