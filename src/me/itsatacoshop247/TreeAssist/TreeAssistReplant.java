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
/*
		Object[] logs;
		Object[] saplings;
		
		logs = new Object[CustomTree.customLogs.size()];
		int pos = 0;
		for (Object o : CustomTree.customLogs) {
			logs[pos++] = o;
		}

		saplings = new Object[CustomTree.customSaplings.size()];
		pos = 0;
		for (Object o : CustomTree.customSaplings) {
			saplings[pos++] = o;
		}
* /
		if (typeid == Material.LOG.getId() || (!CustomTree.customLogs.contains(typeid) && !CustomTree.customLogs.contains(typeid+":"+importData))) {
			type = Material.SAPLING.getId();
			return;
		}
		/*
		for (pos = 0; pos < logs.length && pos < saplings.length; pos++) {
			if (logs[pos].equals(typeid) || logs[pos].equals(typeid+':'+importData)) {
				Object o = saplings[pos];
				
				if (o instanceof Integer) {
					type = (Integer) o;
					return;
				}
				
				String value = (String) o;
				
				String[] split = value.split(":");

				type = Integer.parseInt(split[0]);
				importData = Byte.parseByte(split[1]);
				return;
			}
		}*/
		
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
