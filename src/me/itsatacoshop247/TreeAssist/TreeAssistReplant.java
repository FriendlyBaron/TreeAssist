package me.itsatacoshop247.TreeAssist;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class TreeAssistReplant implements Runnable {
	public final TreeAssist plugin;
	public Block block;
	public byte data;
	public int type;
	
	public TreeAssistReplant(TreeAssist instance, Block importBlock, int typeid, byte importData)
	{
		this.plugin = instance;
		this.block = importBlock;
		this.data = importData;
		this.type = typeid;
		
		if (type == Material.LOG.getId()) {
			type = Material.SAPLING.getId();
		}
	}

	@Override
	public void run() 
	{
		if(plugin.isEnabled())
		{
			this.block.setTypeId(type);
			this.block.setData(this.data);
		}
	}
}
