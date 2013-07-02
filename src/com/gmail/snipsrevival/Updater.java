package com.gmail.snipsrevival;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Updater {
	
	ModeratorNotes plugin;
	
	public Updater(ModeratorNotes plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Checks if the current version of ModeratorNotes is the most recent version that is on Bukkit
	 * 
	 * @return <tt>true</tt> if this is the latest version. If there is a newer version available, this
	 * will return <tt>false</tt>
	 */
	
	public boolean isLatest() {
		plugin.getLogger().info("Checking for newer versions...");
		try {
			InputStream input = new URL("http://dev.bukkit.org/bukkit-plugins/moderatornotes/files.rss").openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();
			String[] updateVersion = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "").split("\\.");
			int updateMajorRelease = Integer.parseInt(updateVersion[0]);
			int updateMinorRelease = Integer.parseInt(updateVersion[1]);
			int updateBuild = Integer.parseInt(updateVersion[2]);
	
			PluginDescriptionFile pdf = plugin.getDescription();
			String[] currentVersion = pdf.getVersion().split("\\.");
			int currentMajorRelease = Integer.parseInt(currentVersion[0]);
			int currentMinorRelease = Integer.parseInt(currentVersion[1]);
			int currentBuild = Integer.parseInt(currentVersion[2]);
			
			if(updateMajorRelease > currentMajorRelease) return false;
			else {
				if((updateMinorRelease > currentMinorRelease) && updateMajorRelease == currentMajorRelease) return false;
				else {
					if((updateBuild > currentBuild) && updateMinorRelease == currentMinorRelease) return false;
					else return true;
				}
			}
		}
		catch (Exception e) {
			plugin.getLogger().warning("Something is wrong with the update checker. This can probably be ignored");
		}
		return true;
	}
	
	/**
	 * Gets the download link of the latest version of ModeratorNotes found on Bukkit
	 * @return the link to the download. If there was some sort of error, the link to ModeratorNotes'
	 * main page will be returned instead
	 */
	
	public String getDownloadLink() {
		try {
			InputStream input = new URL("http://dev.bukkit.org/bukkit-plugins/moderatornotes/files.rss").openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();
			String updateLink = children.item(3).getTextContent();
			return updateLink;
		}
		catch (Exception e) {
			return "http://dev.bukkit.org/server-mods/moderatornotes/";
		}
	}
}
