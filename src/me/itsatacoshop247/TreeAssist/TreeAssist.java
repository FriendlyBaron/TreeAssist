package me.itsatacoshop247.TreeAssist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


public class TreeAssist extends JavaPlugin 
{
	public List<String> playerList = new ArrayList<String>();
	public List<Location> blockList = new ArrayList<Location>();
	
	public boolean Enabled = true;
	public boolean mcMMO = false;
	
	File configFile;
	File dataFile;
	FileConfiguration config;
	FileConfiguration data;
	
	public void onEnable() 
	{
		checkMcMMO();
		
		new TreeAssistBlockListener(this);
		getServer().getPluginManager().registerEvents(new TreeAssistBlockListener(this), this);
		
		this.configFile = new File(getDataFolder(), "config.yml");
		this.dataFile = new File(getDataFolder(), "data.yml");
		try 
		{
			firstRun();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		this.config = new YamlConfiguration();
		this.data = new YamlConfiguration();
		loadYamls();
		config.options().copyDefaults(true);
		data.options().copyDefaults(true);
	}

	private void checkMcMMO() 
	{
        if(getConfig().getBoolean("Main.Use mcMMO if Available")) 
        {
            boolean isMcMMOEnabled = getServer().getPluginManager().isPluginEnabled("mcMMO");
            if(isMcMMOEnabled) 
            {
                    this.mcMMO = true;
            } 
            else 
            {
            	    this.mcMMO = false;
            }
        } 
        else 
        {
        	this.mcMMO = false;
        }
    }

	public WorldGuardPlugin getWorldGuard() 
	{
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) 
	    {
	        return null;
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public void onDisable() 
	{
		this.getServer().getScheduler().cancelTasks(this);
	}
	
	private void firstRun() throws Exception 
	{
		if (!this.dataFile.exists()) 
		{
			this.dataFile.getParentFile().mkdirs();
			copy(getResource("data.yml"), this.dataFile);
			
			configFile.delete();
		}
		if (!this.configFile.exists()) 
		{
			this.configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), this.configFile);
		}
	}
	
	private void copy(InputStream in, File file) 
	{
		try 
		{
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) 
			{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void loadYamls() 
	{
		try 
		{
			this.config.load(this.configFile);
			this.data.load(this.dataFile);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void saveConfig() 
	{
		try 
		{
			this.config.save(this.configFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void saveData() 
	{
		try 
		{
			this.data.save(this.dataFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("TreeAssist"))
		{
			if(args.length > 0)
			{
				if(args[0].equalsIgnoreCase("Reload"))
				{
					if(!sender.hasPermission("treeassist.reload"))
					{
						sender.sendMessage("You don't have treeassist.reload");
						return true;
					}
					this.saveData();
					this.loadYamls();
					sender.sendMessage(ChatColor.GREEN + "TreeAssist has been reloaded.");
					return true;
				}
				if(args[0].equalsIgnoreCase("Toggle"))
				{
					if(!sender.hasPermission("treeassist.toggle"))
					{
						sender.sendMessage("You don't have treeassist.toggle");
						return true;
					}
					if(playerList.contains(sender.getName()))
					{
						playerList.remove(sender.getName());
						sender.sendMessage(ChatColor.GREEN + "TreeAssist functions are now on for you!");
					}
					else
					{
						playerList.add(sender.getName());
						sender.sendMessage(ChatColor.GREEN + "TreeAssist functions turned off for you!");
					}
					return true;
				}
				if(args[0].equalsIgnoreCase("Global"))
				{
					if(!sender.hasPermission("treeassist.toggle.global"))
					{
						sender.sendMessage("You don't have treeassist.toggle.global");
						return true;
					}
					if(!this.Enabled)
					{
						this.Enabled = true;
						sender.sendMessage(ChatColor.GREEN + "TreeAssist functions are globally back on!");
					}
					else
					{
						this.Enabled = false;
						sender.sendMessage(ChatColor.GREEN + "TreeAssist functions turned off globally!");
					}
					return true;
				}
			}
		}
		return false;
	}
}
	