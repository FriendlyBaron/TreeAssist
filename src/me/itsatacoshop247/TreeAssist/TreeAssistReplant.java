package me.itsatacoshop247.TreeAssist;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class TreeAssistReplant implements Runnable {
	public final TreeAssist plugin;
	public Block block;
	public byte data;
	public Material mat;
	
	public TreeAssistReplant(TreeAssist instance, Block importBlock, Material logMat, byte importData)
	{
		this.plugin = instance;
		this.block = importBlock;
		this.data = importData;
		this.mat = logMat;
		
		if (mat == Material.LOG) {
			mat = Material.SAPLING;
		} else if (mat.name().equals("LOG_2")) {
			mat = Material.SAPLING;
			if (data < 4) {
				data += 4;
			}
		}
	}

	@Override
	public void run() 
	{
		if(plugin.isEnabled())
		{
			this.block.setType(mat);
			this.block.setData(this.data);
		}
	}
}
