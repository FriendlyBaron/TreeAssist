package me.itsatacoshop247.TreeAssist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;


//Running changelog

//- Tree destruction will only destory the main blocks Type ID. E.G. if a Oak Tree branch hits a jungle tree branch, the Oak Tree will not destroy the jungle Branch and vice-versa
//- Fixed Blocks dissapearing when used to destroy a tree.
//- Config autoupdates

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
	
	public Logger log = Logger.getLogger("Minecraft");
	
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
		//check for defaults to set newly
		data.options().copyDefaults(true);
		
		this.updateConfig();
	}

	private void updateConfig() 
	{
		HashMap<String, String> items = new HashMap<String, String>();
		
		items = loadConfigurables(items);
		
		int num = 0;
		for(Map.Entry<String, String> item : items.entrySet())
		{
			if(this.config.get(item.getKey()) == null)
			{
				this.log.info(item.getKey());
				if(item.getValue().equalsIgnoreCase("LIST"))
				{
					List<String> list = Arrays.asList("LIST ITEMS GO HERE");
					this.config.addDefault(item.getKey(), list);
				}
				else if(item.getValue().equalsIgnoreCase("true"))
				{
					this.config.addDefault(item.getKey(), true);
				}
				else if(item.getValue().equalsIgnoreCase("false"))
				{
					this.config.addDefault(item.getKey(), false);
				}
				else if(isInteger(item.getValue()))
				{
					this.config.addDefault(item.getKey(), Integer.parseInt(item.getValue()));
				}
				else
				{
					this.config.addDefault(item.getKey(), item.getValue());
				}
				num++;
			}
		}
		if(num > 0)
		{
			this.log.info("[TreeAssist] " + num + " missing items added to config file.");
		}
		this.saveConfig();
	}

	public boolean isInteger(String input)  
	{  
	   try  
	   {  
	      Integer.parseInt(input);  
	      return true;  
	   }  
	   catch(Exception e)  
	   {  
	      return false; 
	   }  
	} 

	private HashMap<String, String> loadConfigurables(HashMap<String, String> items) 
	{
		//Pre-5.0
		items.put("Main.Automatic Tree Destruction", "true");
		items.put("Main.Use Permissions", "false");
		items.put("Main.Sapling Replant", "true");
		items.put("Main.Apply Full Tool Damage", "true");
		items.put("Main.Ignore User Placed Blocks", "false");
		items.put("Main.Use mcMMO if Available", "true");
		items.put("Automatic Tree Destruction.Tree Types.Birch", "true");
		items.put("Automatic Tree Destruction.Tree Types.Jungle", "true");
		items.put("Automatic Tree Destruction.Tree Types.Oak", "true");
		items.put("Automatic Tree Destruction.Tree Types.Spruce", "true");
		items.put("Leaf Decay.Fast Leaf Decay", "true");
		items.put("Sapling Replant.Bottom Block has to be Broken First", "true");
		items.put("Sapling Replant.Time to Protect Sapling (Seconds)", "0");
		items.put("Sapling Replant.Replant When Tree Burns Down", "true");
		items.put("Sapling Replant.Block all breaking of Saplings", "false");
		items.put("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)", "1");
		items.put("Tools.Sapling Replant Require Tools", "true");
		items.put("Tools.Tree Destruction Require Tools", "true");
		items.put("Tools.Tools List", "LIST");
		items.put("Worlds.Enable Per World", "false");
		items.put("Worlds.Enabled Worlds", "LIST");
		items.put("Config Help", "dev.bukkit.org/server-mods/tree-assist/pages/config-walkthrough/");
		
		//5.0 additions
		items.put("Sapling Replant.Tree Types to Replant.Birch", "true");
		items.put("Sapling Replant.Tree Types to Replant.Jungle", "true");
		items.put("Sapling Replant.Tree Types to Replant.Oak", "true");
		items.put("Sapling Replant.Tree Types to Replant.Spruce", "true");
		return items;
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
	