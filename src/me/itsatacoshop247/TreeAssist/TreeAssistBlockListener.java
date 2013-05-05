package me.itsatacoshop247.TreeAssist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;

public class TreeAssistBlockListener implements Listener  
{
	public TreeAssist plugin;
	
	public int derp = 0;
	
	public List<Integer> toolgood = Arrays.asList(271, 275, 258, 286,279);
	public List<Integer> toolbad = Arrays.asList(256,257,267,268,269,270,272,273,274,276,277,278,283,284,285,290,291,292,293,294);
	public List<?> customTreeBlocks = null;
	public List<?> customLogs = null;
	public List<?> customSaplings = null;
	
	public TreeAssistBlockListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		if(plugin.config.getBoolean("Leaf Decay.Fast Leaf Decay") && plugin.Enabled)
		{
			Block block = event.getBlock();
			World world = block.getWorld();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(world.getName()))
				{
					return;
				}
			}
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ(); 

			// only do the 8 edges instead of all 26 surrounding blocks
			
			leafBreak(world.getBlockAt(x-1, y-1, z-1));
			leafBreak(world.getBlockAt(x-1, y-1, z+1));
			leafBreak(world.getBlockAt(x-1, y+1, z-1));
			leafBreak(world.getBlockAt(x-1, y+1, z+1));
			leafBreak(world.getBlockAt(x+1, y-1, z-1));
			leafBreak(world.getBlockAt(x+1, y-1, z+1));
			leafBreak(world.getBlockAt(x+1, y+1, z-1));
			leafBreak(world.getBlockAt(x+1, y+1, z+1));
		}
		
		
		/*
		if(plugin.config.getBoolean("Leaf Decay.Enable Custom Drops"))
		{
			List<?> listOfDrops = plugin.config.getList("Leaf Decay.Drops");
			String[] dropList = (String[]) listOfDrops.toArray(new String[0]);

			for(int y = 0; y < dropList.length; y++)
			{
				String[] line = dropList[y].split(";");
				if((int)(Math.random()*300) < Integer.parseInt(line[1]))
				{
					ItemStack item =  new ItemStack(Integer.parseInt(line[0]), 1);
					if(item.getTypeId() == 18 || item.getTypeId() == 17)
					{
						//MaterialData data = new MaterialData(block.getData());
						//data.setData(block.getData());
						plugin.getServer().broadcastMessage("" + item.getDurability());
						plugin.getServer().broadcastMessage("" + block.getData());
						item.setDurability(block.getData());
						
						//item.setData(data);
						plugin.getServer().broadcastMessage("" + item.getDurability());
					}
					block.getWorld().dropItem(block.getLocation(), item);
					//y = 100;
				}
			}
		}
		*/	
	}
	
	/**
	 * if the block is a leaf block, enforces
	 * a 8 block radius FloatingLeaf removal
	 * 
	 * @param blockAt the block to check
	 */
	private void leafBreak(Block blockAt) 
	{
		if(blockAt.getTypeId() == 18 || isCustomTreeBlock(blockAt))
		{
			blockAt.breakNaturally();
			World world = blockAt.getWorld();
			int x = blockAt.getX();
			int y = blockAt.getY();
			int z = blockAt.getZ();
			for(int x2 = -8; x2 < 9; x2++)
			{
				for(int z2 = -8; z2 < 9; z2++)
				{
					FloatingLeaf(world.getBlockAt(x+x2, y+2, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y+1, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y-1, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y-2, z+z2));
				}
			}
		}
	}

	private boolean isCustomTreeBlock(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (customTreeBlocks.contains(blockAt.getTypeId())) {
				return true;
			}
			return customTreeBlocks.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return customTreeBlocks.contains(blockAt.getTypeId());
	}

	private boolean isCustomLog(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (customLogs.contains(blockAt.getTypeId())) {
				return true;
			}
			return customLogs.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return customLogs.contains(blockAt.getTypeId());
	}

	/**
	 * Checks if the block is a leaf block and drops it
	 * if no log is in 2 block radius around
	 * @param blockAt the block to check
	 */
	private void FloatingLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() != 18 && !isCustomTreeBlock(blockAt))
		{
			return;
		}
		World world = blockAt.getWorld();
		
		int fail = -1; // because we will fail once, when finding blockAt
		
		for (int x = blockAt.getX()-2; x<=blockAt.getX()+2; x++) {
			for (int y = blockAt.getY()-2; y<=blockAt.getY()+2; y++) {
				for (int z = blockAt.getZ()-2; z<=blockAt.getZ()+2; z++) {
					fail+=Air(world.getBlockAt(x, y, z));
					if (fail > 4) {
						return; // fail threshold -> out!
					}
				}
			}
		}

		blockAt.breakNaturally();
	}

	private int Air(Block blockAt) 
	{
		if(blockAt.getTypeId() == 0 || blockAt.getTypeId() == 106)
		{
			return 0;
		}
		else if(blockAt.getTypeId() == 17 || isCustomLog(blockAt))
		{
			return 5;
		}
		else
		{
			return 1;
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(plugin.config.getBoolean("Main.Ignore User Placed Blocks") && (event.getBlock().getTypeId() == 17 || isCustomLog(event.getBlock())))
		{
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(event.getBlock().getWorld().getName()))
				{
					return;
				}
			}
			Block block = event.getBlock();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.data.getList("Blocks");
			list.add("" + block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName());
			plugin.data.set("Blocks", list);
			plugin.saveData();
		}
	}
	
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if(plugin.config.getBoolean("Sapling Replant.Replant When Tree Burns Down") && plugin.Enabled)
		{
			Block block = event.getBlock();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(block.getWorld().getName()))
				{
					return;
				}
			}
			if(block.getType() == Material.LOG) 
			{
				Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
				Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
				if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
				{
					if(oneabove.getType() == Material.AIR || oneabove.getType() == Material.LOG)
					{
						Runnable b = new TreeAssistReplant(plugin, block, Material.LOG.getId(), block.getData());
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
					}
				}	
			}
		}
	}
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event)
	{
		if(plugin.config.getBoolean("Sapling Replant.Replant When Tree Burns Down") && plugin.Enabled)
		{
			Block block = event.getBlock();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(block.getWorld().getName()))
				{
					return;
				}
			}
			if(block.getType() == Material.LOG) 
			{
				Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
				Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
				if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
				{
					if(oneabove.getType() == Material.LOG)
					{
						Runnable b = new TreeAssistReplant(plugin, block, Material.LOG.getId(), block.getData());
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
					}
				}	
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{		
		Block block = event.getBlock();
		Block bottom = block;
		Block top = block;
		Player player = event.getPlayer();
		World world = player.getWorld();
		int typeid = block.getTypeId();
		byte data = block.getData();
		Block[] jungle = new Block[4];
		int j = 1;
		boolean success = false;
		
		if(plugin.config.getBoolean("Worlds.Enable Per World"))
		{
			if(!plugin.config.getList("Worlds.Enabled Worlds").contains(world.getName()))
			{
				return;
			}
		}
		
		if (!hasPerms(player, data)) {
			return;
		}
		
		if(plugin.config.getBoolean("Main.Ignore User Placed Blocks"))
		{
			String check = "" + block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.data.getList("Blocks");
			
			if(list != null && list.contains(check))
			{
				plugin.data.getList("Blocks").remove(check);
				plugin.saveData();
				return;
			}
		}
		
		if(typeid != 17 && !isCustomLog(block))
		{
			if(typeid == 6)
			{
				if(plugin.config.getBoolean("Sapling Replant.Block all breaking of Saplings"))
				{
					player.sendMessage(ChatColor.GREEN + "You cannot break saplings on this server!");
					event.setCancelled(true);
				}
				else if(plugin.blockList.contains(block.getLocation()))
				{
					player.sendMessage(ChatColor.GREEN + "This sapling is protected!");
					event.setCancelled(true);
				}
			}
			else if(typeid == 2 || typeid == 3 || typeid == 82)
			{
				if(plugin.blockList.contains(block.getRelative(BlockFace.UP, 1).getLocation()))
				{
					player.sendMessage(ChatColor.GREEN + "This sapling is protected!");
					event.setCancelled(true);
				}
			}
			else if(typeid != 6 && plugin.blockList.contains(block.getLocation()))
			{
				plugin.blockList.remove(block.getLocation());
			}
			return;
		}
		
		if(this.mcMMOTreeFeller(player))
		{
			return;
		}
		
		if (!plugin.config.getBoolean("Main.Destroy Only Blocks Above")) {
			bottom = getBottom(block);
		}
		top = getTop(block);
		if(bottom == null)
		{
			return;
		}
		if(plugin.config.getBoolean("Main.Automatic Tree Destruction"))
		{
			if(top == null)
			{
				return;
			}
			if(top.getY() - bottom.getY() < 3)
			{
				return;
			}
		}
		
		if(data == 3)
		{
			jungle[0] = bottom;
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ());
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ());
				j++;
			}
			if(world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()-1);
				j++;
			}
			if(world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()-1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()-1);
				j++;
			}
		}
		
		if(!event.isCancelled() && plugin.config.getBoolean("Main.Automatic Tree Destruction"))
		{
			if(plugin.config.getBoolean("Tools.Tree Destruction Require Tools"))
			{
				ItemStack inHand = player.getItemInHand();
				if (!isRequiredTool(inHand)) {
					return;
				}
			}
				
			String[] directions = {"NORTH", "SOUTH", "EAST", "WEST", "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST"};
			List<Integer> validTypes = new ArrayList<Integer>(Arrays.asList(0, 2, 3, 6, 8, 9, 18, 37, 38, 39, 40, 31, 32, 83, 106, 111, 78, 12, 50, 66)); //if it's not one of these blocks, it's safe to assume its a house/building
			for (Object obj : plugin.config.getList("Modding.Custom Logs")) {
				if (obj instanceof Integer) {
					validTypes.add((Integer) obj);
					continue;
				}
				if (obj.equals("LIST ITEMS GO HERE")) {
					List<Object> list = new ArrayList<Object>();
					list.add(-1);
					plugin.config.set("Modding.Custom Logs", list);
					plugin.saveConfig();
					break;
				}
				validTypes.add(Integer.parseInt(((String)obj).split(":")[0]));
			}
			for (Object obj : plugin.config.getList("Modding.Custom Tree Blocks")) {
				if (obj instanceof Integer) {
					validTypes.add((Integer) obj);
					continue;
				}
				if (obj.equals("LIST ITEMS GO HERE")) {
					List<Object> list = new ArrayList<Object>();
					list.add(-1);
					plugin.config.set("Modding.Custom Tree Blocks", list);
					plugin.saveConfig();
					break;
				}
				validTypes.add(Integer.parseInt(((String)obj).split(":")[0]));
			}
			for(int x = 0; x < directions.length; x++)
			{
				if(!validTypes.contains(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId()))
				{
					if(!(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId() == 17 && block.getData() == 3))
					{
						return; 
					}
				}
			}
				
			if(!plugin.playerList.contains(player.getName()))
			{
				byte blockdata = block.getData();
				/*
				if((blockdata == 1 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Spruce")) || (blockdata == 2 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Birch"))) //simpler calculation for birch and pine trees :D
				{
					
					blocksToRemove[0] = bottom;
					Block NextBlockUp = world.getBlockAt(bottom.getX(), bottom.getY() + 1, bottom.getZ());
					for(int q = 1; NextBlockUp.getTypeId() == 17 || isCustomLog(NextBlockUp); q++)
					{
						blocksToRemove[q] = NextBlockUp;
						NextBlockUp = world.getBlockAt(NextBlockUp.getX(), NextBlockUp.getY() + 1, NextBlockUp.getZ());
					}
					
					int total = removeBlocks(blocksToRemove, player);
					if(plugin.config.getBoolean("Main.Apply Full Tool Damage"))
					{
						int type = player.getItemInHand().getTypeId();
						if(type == 258 || type == 271 || type == 275 || type == 279 || type == 286)
						{
							player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+total));
						}
					}
					success = true;
					
				}
				*/
				if(isCustomLog(bottom) ||
						(blockdata == 0 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Oak")) ||
						(blockdata == 1 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Spruce")) ||
						(blockdata == 2 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Birch")) ||
						(blockdata == 3 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Jungle")) || blockdata > 3) //ugly branch messes
				{
					if(plugin.config.getBoolean("Main.Apply Full Tool Damage"))
					{
						if(player.getItemInHand() != null)
						{
							if(this.toolgood.contains(player.getItemInHand().getTypeId()))
							{
								checkBlock(bottom, top, player.getItemInHand(), player, top.getData());
								success = true;
							}
							else if(this.toolbad.contains(player.getItemInHand().getTypeId()))
							{
								checkBlock(bottom, top, player.getItemInHand(), player, top.getData());
								success = true;
							}
							else
							{
								checkBlock(bottom, top, null, player, top.getData());
								success = true;
							}
						}
					}
					else
					{
						checkBlock(bottom, top, null, player, top.getData());
						success = true;
					}
					
					if(blockdata == 3 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.BigJungle"))
					{
						int x = bottom.getX();
						int y = bottom.getY();
						int z = bottom.getZ();
						
						logBreak(world.getBlockAt(x-1, y-1, z+1));
						logBreak(world.getBlockAt(x, y-1, z+1));
						logBreak(world.getBlockAt(x+1, y-1, z+1));
						logBreak(world.getBlockAt(x+1, y-1, z));
						logBreak(world.getBlockAt(x+1, y-1, z-1));
						logBreak(world.getBlockAt(x, y-1, z-1));
						logBreak(world.getBlockAt(x-1, y-1, z-1));
						logBreak(world.getBlockAt(x-1, y-1, z));
						logBreak(world.getBlockAt(x, y-1, z));
					}
				}
			}
		}
		
		if(plugin.config.getBoolean("Main.Sapling Replant") && !event.isCancelled() && (isCustomLog(bottom) || replantType(data))) 
		{
			if((plugin.config.getBoolean("Main.Use Permissions") && player.hasPermission("treeassist.replant")) ||  !(plugin.config.getBoolean("Main.Use Permissions")))
			{
				if(plugin.config.getBoolean("Tools.Sapling Replant Require Tools"))
				{
					ItemStack inHand = player.getItemInHand();
					if (!isRequiredTool(inHand)) {
						return;
					}
				}
				if(plugin.config.getBoolean("Main.Automatic Tree Destruction"))
				{
					int delay = plugin.config.getInt("Delay until Sapling is replanted (seconds) (minimum 1 second)");
					if(delay < 1)
					{
						delay = 1;
					}
					if(block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.DIRT || block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.GRASS || block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.CLAY)
					{
						Runnable b = new TreeAssistReplant(plugin, bottom, typeid, data);
						
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20*delay);
						
						if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
						{
							plugin.blockList.add(bottom.getLocation());
							Runnable X = new TreeAssistProtect(plugin, bottom.getLocation());
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
						}
						if(data == 3 && j > 1 && plugin.config.getBoolean("Sapling Replant.Tree Types to Replant.BigJungle"))
						{
							for(int rts = 1; rts < 4; rts++)
							{
								if(jungle[rts] != null)
								{
									Runnable t = new TreeAssistReplant(plugin, jungle[rts], typeid, data);
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20*delay);
								
									if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
									{
										plugin.blockList.add(jungle[rts].getLocation());
										Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
								}
							}
						}
					}
				}
				else
				{
					int delay = plugin.config.getInt("Delay until Sapling is replanted (seconds) (minimum 1 second)");
					if(delay < 1)
					{
						delay = 1;
					}
					Block onebelow1 = event.getBlock().getRelative(BlockFace.DOWN, 1);
					Block oneabove1 = event.getBlock().getRelative(BlockFace.UP, 1);
					if(onebelow1.getType() == Material.DIRT || onebelow1.getType() == Material.GRASS || onebelow1.getType() == Material.CLAY)
					{
						if(!plugin.getConfig().getBoolean("Sapling Replant.Bottom Block has to be Broken First"))
						{
							Runnable b = new TreeAssistReplant(plugin, block, typeid, data);
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20*delay);
							
							if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
							{
								plugin.blockList.add(block.getLocation());
								Runnable X = new TreeAssistProtect(plugin, block.getLocation());
								
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
							}
							if(block.getData() == 3 && j == 4)
							{
								for(int rts = 1; rts < 4; rts++)
								{
									if(jungle[rts] != null && jungle[rts].getTypeId() == 0)
									{
										Runnable t = new TreeAssistReplant(plugin, jungle[rts], typeid, data);
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20*delay);
									
										if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
										{
											plugin.blockList.add(jungle[rts].getLocation());
											Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
											plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
										}
									}
								}
							}
						}
						else
						{
							if(oneabove1.getType() == Material.LOG || isCustomLog(oneabove1)) 
							{
								int x = 1;
								int extrablockcount = 1; 
								Block blockAbove = player.getWorld().getBlockAt(block.getX(), (block.getY() + 1), block.getZ());
								
								int typeId = oneabove1.getTypeId();
								
								for(x = 1;x < 4; x++)
								{
									if(block.getRelative(BlockFace.NORTH, x).getTypeId() == typeId || block.getRelative(BlockFace.SOUTH, x).getTypeId() == typeId || block.getRelative(BlockFace.EAST, x).getTypeId() == typeId || block.getRelative(BlockFace.WEST, x).getTypeId() == typeId)
									{
										extrablockcount++;
									}
									if(blockAbove.getRelative(BlockFace.NORTH, x).getTypeId() == typeId || blockAbove.getRelative(BlockFace.SOUTH, x).getTypeId() == typeId || blockAbove.getRelative(BlockFace.EAST, x).getTypeId() == typeId || blockAbove.getRelative(BlockFace.WEST, x).getTypeId() == typeId)
									{
										extrablockcount++;
									}
								}
								if (block.getData() == 3)
								{
									extrablockcount = 0;
								}
								if(extrablockcount < 3)
								{
									Runnable b = new TreeAssistReplant(plugin, block, typeid, data);
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20*delay);
									if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
									{
										plugin.blockList.add(block.getLocation());
										Runnable X = new TreeAssistProtect(plugin, block.getLocation());
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
									if(block.getData() == 3 && j == 4)
									{
										for(int rts = 1; rts < 4; rts++)
										{
											if(jungle[rts] != null && jungle[rts].getTypeId() == 0)
											{
												Runnable t = new TreeAssistReplant(plugin, jungle[rts], typeid, data);
												plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20*delay);
											
												if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
												{
													plugin.blockList.add(jungle[rts].getLocation());
													Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
													plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if(success)
		{
			event.setCancelled(true);
			
			if(player.getItemInHand().getDurability() > player.getItemInHand().getType().getMaxDurability())
			{
				if(isTool(player.getItemInHand()))
				{
					player.setItemInHand(new ItemStack(0));
				}
			}
		}
	}

	private boolean isRequiredTool(ItemStack inHand) {
		List<?> fromConfig = plugin.config.getList("Tools.Tools List");
		if(fromConfig.contains(inHand.getType().name()) || fromConfig.contains(inHand.getTypeId()))
		{
			return true;
		}
		
		for (Object obj : fromConfig) {
			if (!(obj instanceof String)) {
				continue; // skip item IDs
			}
			String tool = (String) obj;
			if (!tool.startsWith(inHand.getType().name())) {
				continue; // skip other names
			}
			
			String[] values = tool.split(":");
			
			if (values.length < 2) {
				return true; // no enchantment found, defaulting to plain (found) name
			}
			
			for (Enchantment ench : inHand.getEnchantments().keySet()) {
				if (!ench.getName().equalsIgnoreCase(values[1])) {
					continue; // skip other enchantments
				}
				int level = 0;
				if (values.length < 3) {
					return true; // has correct enchantment, no level needed
				}
				try {
					level = Integer.parseInt(values[2]);
				} catch (Exception e) {
					return true; // invalid level defined, defaulting to no level
				}
				
				if (level > inHand.getEnchantments().get(ench)) {
					continue; // enchantment too low
				}
				return true;
			}
		}
		
		return false;
	}

	private boolean replantType(byte data) 
	{
		if(data == 0 && plugin.config.getBoolean("Sapling Replant.Tree Types to Replant.Oak"))
		{
			return true;
		}
		if(data == 1 && plugin.config.getBoolean("Sapling Replant.Tree Types to Replant.Spruce"))
		{
			return true;
		}
		if(data == 2 && plugin.config.getBoolean("Sapling Replant.Tree Types to Replant.Birch"))
		{
			return true;
		}
		if(data == 3 && plugin.config.getBoolean("Sapling Replant.Tree Types to Replant.Jungle"))
		{
			return true;
		}
		return false;
	}

	private boolean hasPerms(Player player, byte data) 
	{
		if (!plugin.config.getBoolean("Main.Use Permissions")) {
			return true;
		}
		if(data == 0)
		{
			return player.hasPermission("treeassist.destroy.oak");
		}
		if(data == 1)
		{
			return player.hasPermission("treeassist.destroy.spruce");
		}
		if(data == 2)
		{
			return player.hasPermission("treeassist.destroy.birch");
		}
		if(data == 3)
		{
			return player.hasPermission("treeassist.destroy.jungle");
		}
		return false;
	}

	private boolean isTool(ItemStack itemInHand) 
	{
		if(this.toolbad.contains(itemInHand.getTypeId()))
		{
			return true;
		}
		else if(this.toolgood.contains(itemInHand.getTypeId()))
		{
			return true;
		}
		else if(plugin.config.getList("Tools.Tools List").contains(itemInHand.getTypeId()) || plugin.config.getList("Tools.Tools List").contains(itemInHand.getType().name()))
		{
			return true;
		}
		return false;
	}

	private void logBreak(Block blockAt) 
	{
		if(blockAt.getTypeId() == 17 || isCustomLog(blockAt))
		{
			blockAt.breakNaturally();
		}	
	}

	private void checkBlock(Block block, Block Top, ItemStack tool, Player p, Byte OrigData) 
	{
		if(block.getTypeId() != 17 && !isCustomLog(block))
		{
			if((block.getTypeId() == 18 || isCustomTreeBlock(block) )&& plugin.config.getBoolean("Leaf Decay.Fast Leaf Decay"))
			{
				block.breakNaturally();
				return;
			}
			else
			{
				return;
			}
		}
		
		if(!isCustomLog(block) && block.getData() != OrigData)
		{
			if(OrigData == 0 && block.getData() > 3)
			{
				//
			}
			else
			{
				return;
			}
		}
		
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		World world = block.getWorld();
		
		if(world.getBlockAt(x, y+1, z).getTypeId() == 17 || isCustomLog(world.getBlockAt(x, y+1, z)) || 
				world.getBlockAt(x, y-1, z).getTypeId() == 17 || isCustomLog(world.getBlockAt(x, y-1, z))) //might be a trunk
		{
			if(block.getX() != Top.getX() && block.getZ() != Top.getZ())
			{
				if(block.getData() < 3)
				{
					int failCount = 0;
					for(int cont = -4; cont < 5; cont++)
					{
						if(world.getBlockAt(x, y+cont, z).getTypeId() == 17 ||
								isCustomLog(world.getBlockAt(x, y+cont, z)))
						{
							failCount++;
						}
					}
					if(failCount > 3)
					{
						return;
					}
				}
				else 
				{
					boolean diff = true;
					for(int Cx = -1; Cx < 2; Cx++)
					{
						for(int Cz = -1; Cz < 2; Cz++)
						{
							if(block.getX()-Cx == Top.getX() && block.getZ()-Cz == Top.getZ())
							{
								diff = false;
								Cx = 2;
								Cz = 2;
							}
						}
					}
					if(diff)
					{
						int failCount = 0;
						for(int cont = -4; cont < 5; cont++)
						{
							if(world.getBlockAt(x, y+cont, z).getTypeId() == 17
									|| isCustomLog(world.getBlockAt(x, y+cont, z)))
							{
								failCount++;
							}
						}
						if(failCount > 3)
						{
							return;
						}
					}
				}
			}
		}
		
		boolean isBig = block.getData() == 3 && isBig(block);
		
		breakBlock(block, tool, p);
			
		if(block.getData() == 3  && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.BigJungle"))
		{
			checkBlock(world.getBlockAt(x-2, y+1, z-2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-1, y+1, z-2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x, y+1, z-2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+1, y+1, z-2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+2, y+1, z-2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+2, y+1, z-1), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+2, y+1, z), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+2, y+1, z+1), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+2, y+1, z+2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x+1, y+1, z+2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x, y+1, z+2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-1, y+1, z+2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-2, y+1, z+2), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-2, y+1, z+1), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-2, y+1, z), Top, tool, p, OrigData);
			checkBlock(world.getBlockAt(x-2, y+1, z-1), Top, tool, p, OrigData);
		} else if (isBig) {
			return;
		}
		checkBlock(world.getBlockAt(x-1, y, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x, y, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y, z), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x, y, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x-1, y, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x-1, y, z), Top, tool, p, OrigData);
		
		checkBlock(world.getBlockAt(x-1, y+1, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x, y+1, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y+1, z+1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y+1, z), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x+1, y+1, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x, y+1, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x-1, y+1, z-1), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x-1, y+1, z), Top, tool, p, OrigData);
		checkBlock(world.getBlockAt(x, y+1, z), Top, tool, p, OrigData);
	}

	private boolean isBig(Block block) {
		BlockFace field[] = {BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
		
		for (BlockFace face : field) {
			if (block.getRelative(face).getTypeId() == block.getTypeId()) {
				return true;
			}
		}
		
		return false;
	}

	private void breakBlock(Block block, ItemStack tool, Player play) 
	{
		if(plugin.mcMMO)
		{
			this.mcMMOFake(play, block);
		}
		block.breakNaturally();
				
		if (tool != null) 
		{
			if(this.toolgood.contains(play.getItemInHand().getTypeId()))
			{
				play.getItemInHand().setDurability((short) (play.getItemInHand().getDurability() + 1));
			}
			else if(this.toolbad.contains(play.getItemInHand().getTypeId()))
			{
				play.getItemInHand().setDurability((short) (play.getItemInHand().getDurability() + 2));
			}
		}
	}

	private Block getTop(Block block) 
	{
		int  x = block.getX();
		int  y = block.getY();
		int  z = block.getZ();
		Block above = null;
		Block top = null;
		while(top == null)
		{
			y++;
			above = block.getWorld().getBlockAt(x, y, z);
			
			if(above.getTypeId() == 18 || isCustomTreeBlock(above))
			{
				top = block.getWorld().getBlockAt(x, y - 1, z);
			}
			else if(above.getTypeId() != 17 && !isCustomLog(above))
			{
				return null;
			}
		}
		if(leafCheck(top))
		{
			return top;
		}
		else
		{
			return null;
		}
	}

	private boolean leafCheck(Block top) 
	{
		if(top.getData() > 2)
		{
			return true;
		}
		int x = top.getX();
		int y = top.getY();
		int z = top.getZ();
		World world = top.getWorld();
		int total = 0;
		
		total += isLeaf(world.getBlockAt(x-1, y, z+1));
		total += isLeaf(world.getBlockAt(x, y, z+1));
		total += isLeaf(world.getBlockAt(x+1, y, z+1));
		total += isLeaf(world.getBlockAt(x+1, y, z));
		total += isLeaf(world.getBlockAt(x+1, y, z-1));
		total += isLeaf(world.getBlockAt(x, y, z-1));
		total += isLeaf(world.getBlockAt(x-1, y, z-1));
		total += isLeaf(world.getBlockAt(x-1, y, z));

		total += isLeaf(world.getBlockAt(x-1, y-1, z+1));
		total += isLeaf(world.getBlockAt(x, y-1, z+1));
		total += isLeaf(world.getBlockAt(x+1, y-1, z+1));
		total += isLeaf(world.getBlockAt(x+1, y-1, z));
		total += isLeaf(world.getBlockAt(x+1, y-1, z-1));
		total += isLeaf(world.getBlockAt(x, y-1, z-1));
		total += isLeaf(world.getBlockAt(x-1, y-1, z-1));
		total += isLeaf(world.getBlockAt(x-1, y-1, z));
		total += isLeaf(world.getBlockAt(x, y-1, z));

		return total > 3;
	}

	private int isLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() == 18 || isCustomTreeBlock(blockAt))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	private Block getBottom(Block block) 
	{
		boolean found = false;
		int counter = 1;
		do
		{
			if(block.getWorld().getBlockAt(block.getX(), block.getY() - counter, block.getZ()).getTypeId() == 17
					|| isCustomLog(block.getWorld().getBlockAt(block.getX(), block.getY() - counter, block.getZ())))
			{
				counter++;
			}
			else
			{
				block = block.getWorld().getBlockAt(block.getX(), block.getY() - (counter - 1), block.getZ());
				found = true;
			}
		}
		while(!found);
		
		Block onebelow = block.getRelative(BlockFace.DOWN, 1);
		
		if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS || onebelow.getType() == Material.CLAY)
		{
			return block;
		}
		else
		{
			return null;
		}
	}
	
	public boolean mcMMOTreeFeller(Player player) 
	{
        boolean isMcMMOEnabled = plugin.getServer().getPluginManager().isPluginEnabled("mcMMO");
        
        if(!isMcMMOEnabled) 
        {
                return false;
        }
        
        if(AbilityAPI.treeFellerEnabled(player)) 
        {
                return true;
        } 
        else 
        {
                return false;
        }
	}

	public void mcMMOFake(Player player, Block block) 
	{
		Plugin mcmmo = plugin.getServer().getPluginManager().getPlugin("mcMMO");

        if(block.getData() == 0) 
        {
            ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig().getInt("Experience.Woodcutting.Oak"));
        }
        else if(block.getData() == 1) 
        {
            ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig().getInt("Experience.Woodcutting.Spruce"));
        }
        else if(block.getData() == 2) 
        {
            ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig().getInt("Experience.Woodcutting.Birch"));
        }
        else if(block.getData() == 3) 
        {
            ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig().getInt("Experience.Woodcutting.Jungle"));
        }
	}

	private int removeBlocks(Block[] blocksToRemove, Player player) 
	{
		int count = 0;
		for(int x = 0; x < blocksToRemove.length; x++)
		{
			if(blocksToRemove[x] != null)
			{
				if(plugin.mcMMO)
				{
					mcMMOFake(player, blocksToRemove[x]);
				}
				blocksToRemove[x].breakNaturally();
				count++;
			}
		}
		return count;
	}
}
