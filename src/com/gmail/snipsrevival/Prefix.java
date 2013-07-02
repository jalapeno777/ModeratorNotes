package com.gmail.snipsrevival;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Prefix {
	
	ModeratorNotes plugin;
	
	public Prefix(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Gets the prefix as set in the config file
	 * 
	 * @param sender - <tt>CommandSender</tt> that be used in the prefix
	 * @return prefix in the form of a string
	 */
	
	public String getPrefix(CommandSender sender) {
		Date date = new Date();
		String prefix = plugin.getConfig().getString("Prefix");
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		prefix = prefix.replace("<MM>", new SimpleDateFormat("MM").format(date));
		prefix = prefix.replace("<MMM>", new SimpleDateFormat("MMM").format(date));
		prefix = prefix.replace("<MMMM>", new SimpleDateFormat("MMMM").format(date));
		prefix = prefix.replace("<dd>", new SimpleDateFormat("dd").format(date));
		prefix = prefix.replace("<yyyy>", new SimpleDateFormat("yyyy").format(date));
		prefix = prefix.replace("<yy>", new SimpleDateFormat("yy").format(date));
		prefix = prefix.replace("<HH>", new SimpleDateFormat("HH").format(date));
		prefix = prefix.replace("<hh>", new SimpleDateFormat("hh").format(date));
		prefix = prefix.replace("<mm>", new SimpleDateFormat("mm").format(date));
		prefix = prefix.replace("<ss>", new SimpleDateFormat("ss").format(date));
		prefix = prefix.replace("<a>", new SimpleDateFormat("a").format(date));
		prefix = prefix.replace("<Z>", new SimpleDateFormat("z").format(date));
		prefix = prefix.replace("<playername>", sender.getName());
		return prefix;
	}
}
