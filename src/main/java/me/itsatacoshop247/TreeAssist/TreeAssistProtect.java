package me.itsatacoshop247.TreeAssist;

import org.bukkit.Location;

public class TreeAssistProtect implements Runnable {
	public final TreeAssist plugin;
	public Location location;
	
	public TreeAssistProtect(TreeAssist instance, Location importLocation)
	{
		this.plugin = instance;
		this.location = importLocation;
	}

	public void run() 
	{
		if(plugin.isEnabled() && plugin.blockList.contains(this.location))
		{
			plugin.blockList.remove(this.location);
		}
	}
}
