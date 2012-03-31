package me.itsatacoshop247.TreeAssist;

import org.bukkit.block.Block;

public class TreeAssistReplant implements Runnable {
	public final TreeAssist plugin;
	public Block block;
	public byte data;
	
	public TreeAssistReplant(TreeAssist instance, Block importBlock, byte importData)
	{
		this.plugin = instance;
		this.block = importBlock;
		this.data = importData;
	}

	@Override
	public void run() {
		if(plugin.isEnabled())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		this.block.setTypeId(6);
		this.block.setData(this.data);
	}
}
