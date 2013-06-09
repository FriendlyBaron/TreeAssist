package me.itsatacoshop247.TreeAssist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.itsatacoshop247.TreeAssist.modding.ModUtils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;

public class TreeAssistBlockListener implements Listener  
{
	public TreeAssist plugin;
	
	public int derp = 0;
	
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
			
			breakRadiusIfLeaf(world.getBlockAt(x-1, y-1, z-1));
			breakRadiusIfLeaf(world.getBlockAt(x-1, y-1, z+1));
			breakRadiusIfLeaf(world.getBlockAt(x-1, y+1, z-1));
			breakRadiusIfLeaf(world.getBlockAt(x-1, y+1, z+1));
			breakRadiusIfLeaf(world.getBlockAt(x+1, y-1, z-1));
			breakRadiusIfLeaf(world.getBlockAt(x+1, y-1, z+1));
			breakRadiusIfLeaf(world.getBlockAt(x+1, y+1, z-1));
			breakRadiusIfLeaf(world.getBlockAt(x+1, y+1, z+1));
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

	@SuppressWarnings("unchecked")
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(plugin.config.getBoolean("Main.Ignore User Placed Blocks") && (event.getBlock().getTypeId() == 17 || ModUtils.isCustomLog(event.getBlock())))
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
			list = (List<String>) plugin.data.getList("Blocks", new ArrayList<String>());
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
	
	protected final static Set<TreeAssistTree> trees = new HashSet<TreeAssistTree>();
	
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		for (TreeAssistTree tree : trees) {
			if (tree.contains(event.getBlock())) {
				return;
			}
		}
		
		TreeAssistTree tree = TreeAssistTree.calculate(plugin, this, event);
		
		if (tree != null) {
			trees.add(tree);
		}
	}

	/**
	 * Checks if the block is a leaf block and drops it
	 * if no log is in 2 block radius around
	 * @param blockAt the block to check
	 */
	private void breakIfLonelyLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() != 18 && !ModUtils.isCustomTreeBlock(blockAt))
		{
			return;
		}
		World world = blockAt.getWorld();
		
		int fail = -1; // because we will fail once, when finding blockAt
		
		for (int x = blockAt.getX()-2; x<=blockAt.getX()+2; x++) {
			for (int y = blockAt.getY()-2; y<=blockAt.getY()+2; y++) {
				for (int z = blockAt.getZ()-2; z<=blockAt.getZ()+2; z++) {
					fail+=calcAir(world.getBlockAt(x, y, z));
					if (fail > 4) {
						return; // fail threshold -> out!
					}
				}
			}
		}

		blockAt.breakNaturally();
	}
	
	/**
	 * if the block is a leaf block, enforces
	 * a 8 block radius FloatingLeaf removal
	 * 
	 * @param blockAt the block to check
	 */
	private void breakRadiusIfLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() == 18 || ModUtils.isCustomTreeBlock(blockAt))
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
					breakIfLonelyLeaf(world.getBlockAt(x+x2, y+2, z+z2));
					breakIfLonelyLeaf(world.getBlockAt(x+x2, y+1, z+z2));
					breakIfLonelyLeaf(world.getBlockAt(x+x2, y, z+z2));
					breakIfLonelyLeaf(world.getBlockAt(x+x2, y-1, z+z2));
					breakIfLonelyLeaf(world.getBlockAt(x+x2, y-2, z+z2));
				}
			}
		}
	}

	private int calcAir(Block blockAt) 
	{
		if(blockAt.getTypeId() == 0 || blockAt.getTypeId() == 106)
		{
			return 0;
		}
		else if(blockAt.getTypeId() == 17 || ModUtils.isCustomLog(blockAt))
		{
			return 5;
		}
		else
		{
			return 1;
		}
	}
}
