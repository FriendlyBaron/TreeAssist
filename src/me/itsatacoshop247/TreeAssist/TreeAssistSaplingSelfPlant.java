package me.itsatacoshop247.TreeAssist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

public class TreeAssistSaplingSelfPlant implements Runnable {
	private final TreeAssist plugin;
	private Item drop;
	
	public TreeAssistSaplingSelfPlant(TreeAssist instance, Item item)
	{
		this.plugin = instance;
		drop = item;
		
		int delay = plugin.getConfig().getInt("Auto Plant Dropped Saplings.Delay (seconds)", 5);
		if (delay < 1) {
			delay = 1;
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, this, 20L * delay);
	}

	@Override
	public void run() 
	{
		Block block = drop.getLocation().getBlock();
		
		if ((block.getType() == Material.AIR || block.getType() == Material.SNOW) &&
				(block.getRelative(BlockFace.DOWN).getType() == Material.DIRT ||
						block.getRelative(BlockFace.DOWN).getType() == Material.MYCEL ||
						block.getRelative(BlockFace.DOWN).getType() == Material.GRASS)) {

			block.setTypeId(drop.getItemStack().getTypeId());
			block.setData(drop.getItemStack().getData().getData());
			drop.remove();
		}
	}
}
