package me.itsatacoshop247.TreeAssist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

//to do

//make tree brekaing better - add leaf checks

//fucking jungle trees/oak trees-check straight up

// sapling drop percents.

//sapling replant percent

//custom leaf drops

public class TreeAssist extends JavaPlugin {
	
	public List<String> playerList = new ArrayList<String>();
	
	public void onEnable() 
	{
		new TreeAssistBlockListener(this);
		getServer().getPluginManager().registerEvents(new TreeAssistBlockListener(this), this);
		loadConfiguration();
	}

	private void loadConfiguration() {
		/*
		this.getConfig().addDefault("Main.Automatic Tree Destruction", true);
		this.getConfig().addDefault("Main.Use Permissions?", false);
		this.getConfig().addDefault("Main.Custom Leaf Decay", false);
		this.getConfig().addDefault("Main.Custom Leaf Decay Percent", 5);
		this.getConfig().addDefault("Main.Sapling Replant", true);
		this.getConfig().addDefault("Automatic Tree Destruction.Tree Types.Birch", true);
		this.getConfig().addDefault("Automatic Tree Destruction.Tree Types.Jungle", true);
		this.getConfig().addDefault("Automatic Tree Destruction.Tree Types.Oak", true);
		this.getConfig().addDefault("Automatic Tree Destruction.Tree Types.Spruce", true);
		String[] needTools = {"DIAMOND_AXE", "WOOD_AXE", "GOLD_AXE", "IRON_AXE", "STONE_AXE", "AIR"};
		this.getConfig().addDefault("Automatic Tree Destruction.Require Tools", false);
		this.getConfig().set("Automatic Tree Destruction.Tools", Arrays.asList(needTools));
		*/
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public void onDisable() 
	{
		//NOTHING
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("TreeAssist"))
		{
			if(args.length > 0)
			{
				if(args[0].equalsIgnoreCase("Toggle"))
				{
					if(sender.hasPermission("treeassist.toggle") || !this.getConfig().getBoolean("Main.Use Permissions"))
					{
						if(playerList.contains(sender.getName()))
						{
							playerList.remove(sender.getName());
							sender.sendMessage(ChatColor.GREEN + "Auto Tree Destruction Turned on!");
						}
						else
						{
							playerList.add(sender.getName());
							sender.sendMessage(ChatColor.GREEN + "Auto Tree Destruction Turned off!");
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}
	