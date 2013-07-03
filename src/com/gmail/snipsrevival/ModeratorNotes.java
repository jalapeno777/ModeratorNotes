package com.gmail.snipsrevival;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.snipsrevival.commands.*;
import com.gmail.snipsrevival.listeners.*;

public class ModeratorNotes extends JavaPlugin {
	
	public static ModeratorNotes plugin;
	
	CommonUtilities common = new CommonUtilities();	
	
	private static Object getPrivateField(Object object, String field) throws SecurityException,
    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		Field objectField = clazz.getDeclaredField(field);
		objectField.setAccessible(true);
		Object result = objectField.get(object);
		objectField.setAccessible(false);
		return result;
	}
 
	public static void unRegisterBukkitCommand(PluginCommand cmd) {
		try {
			Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
			SimpleCommandMap commandMap = (SimpleCommandMap) result;
			Object map = getPrivateField(commandMap, "knownCommands");
			@SuppressWarnings("unchecked")
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
			knownCommands.remove(cmd.getName());
			for (String alias : cmd.getAliases()) {
				if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(Bukkit.getName())) {
					knownCommands.remove(alias);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void onEnable() {
		
		plugin = this;
				
		if(getConfig().getBoolean("EnableUpdateChecker") == true) {
			Updater updater = new Updater(this);
			if(!updater.isLatest()) {
				this.getLogger().warning("There is a newer version of ModeratorNotes available");
				this.getLogger().warning("Download it at " + updater.getDownloadLink());
			}
			else {
				this.getLogger().info("You have the latest version of ModeratorNotes!");
			}
		}
						
		//old directories
		//delete people's staffnotes and exemptplayers
		//rename people's playernotes to userdata
		
		//all player info will be stored in userdata
		
		File staffNoteDir = new File(this.getDataFolder() + "/StaffNotes/");
		File playerNoteDir = new File(this.getDataFolder() + "/PlayerNotes/");
		File exemptPlayerDir = new File(this.getDataFolder() + "/ExemptPlayers/");
		
		File userDataDir = new File(this.getDataFolder() + "/userdata/");
		
		getCommand("note").setExecutor(new CommandNote(this));
		getCommand("mail").setExecutor(new CommandMail(this));
		getCommand("moderatornotes").setExecutor(new CommandReloadConfig(this));
		getCommand("rules").setExecutor(new CommandRules(this));
		getCommand("info").setExecutor(new CommandInfo(this));
		getCommand("ban").setExecutor(new CommandBan(this));
		getCommand("tempban").setExecutor(new CommandTempban(this));
		getCommand("unban").setExecutor(new CommandUnban(this));
		getCommand("kick").setExecutor(new CommandKick(this));
		getCommand("mute").setExecutor(new CommandMute(this));
		getCommand("tempmute").setExecutor(new CommandTempmute(this));
		getCommand("unmute").setExecutor(new CommandUnmute(this));
		getCommand("warn").setExecutor(new CommandWarn(this));
		
		new PlayerListener(this);
		
		if(getConfig().getBoolean("UseBan") == false) {
			PluginCommand ban = this.getCommand("ban");
			unRegisterBukkitCommand(ban);
		}
		if(getConfig().getBoolean("UseTempban") == false) {
			PluginCommand tempban = this.getCommand("tempban");
			unRegisterBukkitCommand(tempban);
		}
		if(getConfig().getBoolean("UseUnban") == false) {
			PluginCommand unban = this.getCommand("unban");
			unRegisterBukkitCommand(unban);
		}
		if(getConfig().getBoolean("UseKick") == false) {
			PluginCommand kick = this.getCommand("kick");
			unRegisterBukkitCommand(kick);
		}
		if(getConfig().getBoolean("UseMute") == false) {
			PluginCommand mute = this.getCommand("mute");
			unRegisterBukkitCommand(mute);
		}
		if(getConfig().getBoolean("UseTempmute") == false) {
			PluginCommand tempmute = this.getCommand("tempmute");
			unRegisterBukkitCommand(tempmute);
		}
		if(getConfig().getBoolean("UseUnmute") == false) {
			PluginCommand unmute = this.getCommand("unmute");
			unRegisterBukkitCommand(unmute);
		}
		if(getConfig().getBoolean("UseInfo") == false) {
			PluginCommand info = this.getCommand("info");
			unRegisterBukkitCommand(info);
		}
		if(getConfig().getBoolean("UseRules") == false) {
			PluginCommand rules = this.getCommand("rules");
			unRegisterBukkitCommand(rules);
		}
		if(getConfig().getBoolean("UseWarn") == false) {
			PluginCommand warn = this.getCommand("warn");
			unRegisterBukkitCommand(warn);
		}
		
		common.createNewDir(userDataDir);
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		//remove stuff below after a few releases and months go by
		
		if(staffNoteDir.exists()) {
			for(File f : staffNoteDir.listFiles()) {
				new File(this.getDataFolder() + "/StaffNotes/" + f.getName()).delete();
			}
			staffNoteDir.delete();
		}
		if(playerNoteDir.exists()) {
			for(File f : playerNoteDir.listFiles()) {
				File childFile = new File(this.getDataFolder() + "/PlayerNotes/" + f.getName());
				childFile.renameTo(new File(this.getDataFolder() + "/userdata/" + f.getName()));
			}
			playerNoteDir.delete();
		}
		if(exemptPlayerDir.exists()) {
			for(File f : exemptPlayerDir.listFiles()) {
				new File(this.getDataFolder() + "/ExemptPlayers/" + f.getName()).delete();
			}
			exemptPlayerDir.delete();
		}
	}
	
	@Override
	public void onDisable() {
		plugin = null;
	}
}
