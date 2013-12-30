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

import me.itsatacoshop247.TreeAssist.blocklists.BlockList;
import me.itsatacoshop247.TreeAssist.blocklists.EmptyBlockList;
import me.itsatacoshop247.TreeAssist.blocklists.FlatFileBlockList;
import me.itsatacoshop247.TreeAssist.blocklists.PrismBlockList;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.metrics.MetricsLite;
import me.itsatacoshop247.TreeAssist.trees.BaseTree;
import me.itsatacoshop247.TreeAssist.trees.CustomTree;
import me.itsatacoshop247.TreeAssist.trees.InvalidTree;
import me.itsatacoshop247.TreeAssist.trees.MushroomTree;
import me.itsatacoshop247.TreeAssist.trees.VanillaOneSevenTree;
import me.itsatacoshop247.TreeAssist.trees.VanillaTree;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


//Running changelog

//- Tree destruction will only destory the main blocks Type ID. E.G. if a Oak Tree branch hits a jungle tree branch, the Oak Tree will not destroy the jungle Branch and vice-versa
//- Fixed Blocks dissapearing when used to destroy a tree.
//- Config autoupdates

public class TreeAssist extends JavaPlugin 
{
	public List<String> playerList = new ArrayList<String>();
	public List<Location> saplingLocationList = new ArrayList<Location>();
	
	public boolean Enabled = true;
	public boolean mcMMO = false;
	
	File configFile;
	FileConfiguration config;
	
	public BlockList blockList;
	
	TreeAssistBlockListener listener;
	
	public void onEnable() 
	{
		checkMcMMO();
		
		Utils.plugin = this;
		
		this.configFile = new File(getDataFolder(), "config.yml");
		try 
		{
			firstRun();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		this.config = new YamlConfiguration();
		
		this.listener = new TreeAssistBlockListener(this);
		
		loadYamls();
		config.options().copyDefaults(true);
		//check for defaults to set newly
		
		this.updateConfig();
		
		getServer().getPluginManager().registerEvents(listener, this);
		if (config.getBoolean("Main.Auto Plant Dropped Saplings")) {
			getServer().getPluginManager().registerEvents(new TreeAssistSpawnListener(this), this);
		}
		reloadLists();
		
		try {
		    MetricsLite metrics = new MetricsLite(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}

		BaseTree.debug = new Debugger(this, 1);
		CustomTree.debugger = new Debugger(this, 2);
		InvalidTree.debugger = new Debugger(this, 3);
		MushroomTree.debugger = new Debugger(this, 4);
		VanillaTree.debugger = new Debugger(this, 5);
		VanillaOneSevenTree.debugger = new Debugger(this, 6);
		Debugger.load(this, Bukkit.getConsoleSender());
		

		initiateList("Modding.Custom Logs", Utils.validTypes);
		initiateList("Modding.Custom Tree Blocks", Utils.validTypes);
		
		if (getConfig().getBoolean("Main.Ignore User Placed Blocks")) {
			String pluginName = getConfig().getString(
					"Placed Blocks.Handler Plugin Name", "TreeAssist");
			if ("TreeAssist".equalsIgnoreCase(pluginName)) {
				blockList = new FlatFileBlockList();
			} else if ("Prism".equalsIgnoreCase(pluginName)) {
				blockList = new PrismBlockList();
			}
		} else {
			blockList = new EmptyBlockList();
		}
		blockList.initiate();
	}

	private void reloadLists() {
		CustomTree.customTreeBlocks = config.getList("Modding.Custom Tree Blocks");
		CustomTree.customLogs = config.getList("Modding.Custom Logs");
		CustomTree.customSaplings = config.getList("Modding.Custom Saplings");
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
				getLogger().info(item.getKey());
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
				else if(isDouble(item.getValue()))
				{
					this.config.addDefault(item.getKey(), Double.parseDouble(item.getValue()));
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
			getLogger().info(num + " missing items added to config file.");
		}
		this.saveConfig();
	}

	public boolean isDouble(String input)  
	{  
	   try  
	   {  
		   Double.parseDouble(input);  
	      return true;  
	   }  
	   catch(Exception e)  
	   {  
	      return false; 
	   }  
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
		
		//5.2 additions
		items.put("Main.Destroy Only Blocks Above", "false");
		
		//5.3 additions
		items.put("Modding.Custom Logs", "LIST");
		items.put("Modding.Custom Tree Blocks", "LIST");
		items.put("Modding.Custom Saplings", "LIST");
		
		//5.4 additions
		items.put("Automatic Tree Destruction.Tree Types.BigJungle", "true");
		items.put("Sapling Replant.Tree Types to Replant.BigJungle", "true");
		
		//5.5 additions
		items.put("Automatic Tree Destruction.Delay (ticks)", "0");
		items.put("Automatic Tree Destruction.Forced Removal", "false");
		items.put("Automatic Tree Destruction.Initial Delay (seconds)", "10");
		
		//5.6 additions
		items.put("Main.Auto Plant Dropped Saplings", "false");
		items.put("Auto Plant Dropped Saplings.Chance (percent)", "10");
		items.put("Auto Plant Dropped Saplings.Delay (seconds)", "5");
		
		//5.7 additions
		items.put("Automatic Tree Destruction.Tree Types.Brown Shroom", "true");
		items.put("Automatic Tree Destruction.Tree Types.Red Shroom", "true");

		//5.7.1 additions
		items.put("Tools.Drop Chance.DIAMOND_AXE", "100");
		items.put("Tools.Drop Chance.WOOD_AXE", "100");
		items.put("Tools.Drop Chance.GOLD_AXE", "100");
		items.put("Tools.Drop Chance.IRON_AXE", "100");
		items.put("Tools.Drop Chance.STONE_AXE", "100");
		
		//5.7.2 additions
		items.put("Automatic Tree Destruction.Cooldown (seconds)", "0");
		
		//5.7.3 additions
		items.put("Custom Drops.APPLE", "0.1");
		items.put("Custom Drops.GOLDEN_APPLE", "0.0");
		
		//5.8 additions
		items.put("Placed Blocks.Handler Plugin Name","TreeAssist");
		items.put("Automatic Tree Destruction.Tree Types.Acacia", "true");
		items.put("Automatic Tree Destruction.Tree Types.Dark Oak", "true");
		items.put("Sapling Replant.Tree Types to Replant.Acacia", "true");
		items.put("Sapling Replant.Tree Types to Replant.Dark Oak", "true");
		items.put("Automatic Tree Destruction.Tree Types.BigSpruce", "true");
		items.put("Sapling Replant.Tree Types to Replant.BigSpruce", "true");
		
		items.put("Sapling Replant.Enforce", "true");
		return items;
	}

	private void checkMcMMO() 
	{
        if(getConfig().getBoolean("Main.Use mcMMO if Available")) 
        {
            this.mcMMO = getServer().getPluginManager().isPluginEnabled("mcMMO");
        } 
        else 
        {
        	this.mcMMO = false;
        }
    }
	
	public void onDisable() 
	{
		this.getServer().getScheduler().cancelTasks(this);
		Debugger.destroy();
	}
	
	private void firstRun() throws Exception 
	{
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
					blockList.save();
					reloadConfig();
					this.loadYamls();
					reloadLists();
					sender.sendMessage(ChatColor.GREEN + "TreeAssist has been reloaded.");
					return true;
				} else if(args[0].equalsIgnoreCase("Toggle")) {
					
					if (sender.hasPermission("treeassist.toggle.other") && args.length > 2) {
						if(playerList.contains(args[1]))
						{
							playerList.remove(args[1]);
							sender.sendMessage(ChatColor.GREEN + "TreeAssist functions are now on for "+args[1]+"!");
						}
						else
						{
							playerList.add(args[1]);
							sender.sendMessage(ChatColor.GREEN + "TreeAssist functions turned off for "+args[1]+"!");
						}
						return true;
					}
					
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
				} else if(args[0].equalsIgnoreCase("Global")) {
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
				} else if (args[0].equalsIgnoreCase("Debug")) {
					if (args.length < 2) {
						getConfig().set("Debug", "none");
						Debugger.load(this, sender);
					} else {
						getConfig().set("Debug", args[1]);
						Debugger.load(this, sender);
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean isActive(World world) {
		return (!config.getBoolean("Worlds.Enable Per World")) ||
		config.getList("Worlds.Enabled Worlds").contains(
			world.getName());
	}

	public boolean isForceAutoDestroy() {
		return getConfig().getBoolean("Main.Automatic Tree Destruction")
				&& getConfig().getBoolean("Automatic Tree Destruction.Forced Removal");
	}
	private void initiateList(String string, List<Integer> validTypes) {
		for (Object obj : config.getList(string)) {
			if (obj instanceof Integer) {
				validTypes.add((Integer) obj);
				continue;
			}
			if (obj.equals("LIST ITEMS GO HERE")) {
				List<Object> list = new ArrayList<Object>();
				list.add(-1);
				config.set(string, list);
				saveConfig();
				break;
			}
			validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
		}
	}
	
	private Map<String, BukkitTask> coolDowns = new HashMap<String, BukkitTask>();
	private Map<String, Long> coolDownTime = new HashMap<String, Long>();

	long ticksToMillisecs (final long ticks) {
		return ticks * 1000 /20L; 
	}
	long millisecsToTicks (final long ms) {
		return ms * 20L /1000L; 
	}

	public boolean hasCoolDown(Player player) {
		return coolDowns.containsKey(player.getName());
	}
	// returns the number of seconds that player must still wait
	public float getCoolDownSecondsRemaining (Player player) {
		String name = player.getName();
		long cooldown = 0;
		if ( !coolDownTime.containsKey (name))
			return 0F;
		else {
			cooldown = coolDownTime.get (name) - System.currentTimeMillis();
	
			return (cooldown > 0 ? cooldown / 1000.0F : 0F);
		}
	}
	public void setCoolDown(Player player) {
		final int coolDown = getConfig().getInt("Automatic Tree Destruction.Cooldown (seconds)", 0);

		setCoolDown (player, coolDown);
	}
	public void setCoolDown(Player player, int coolDown) {
		if (coolDown < 1) {
			return;
		}

		coolDownTime.put (player.getName(), System.currentTimeMillis() + ticksToMillisecs(coolDown * 20L));
		player.sendMessage(ChatColor.GREEN + "Wait " + coolDown + " secs for TreeAssist cooldown");

		class RemoveRunner extends BukkitRunnable {
			private final String name;
			RemoveRunner(Player player) {
				name = player.getName();
			}
			@Override
			public void run() {
				coolDowns.remove(name);
				coolDownTime.remove (name);
				Player player = Bukkit.getPlayer(name);
				if (player != null) {
					player.sendMessage(ChatColor.GREEN + "TreeAssist cooled down!");
				}
			}
			
		}
		coolDowns.put(player.getName(), ((new RemoveRunner(player)).runTaskLater(this, coolDown * 20L)));
	}
}
	