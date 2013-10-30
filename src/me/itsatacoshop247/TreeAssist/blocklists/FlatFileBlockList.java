package me.itsatacoshop247.TreeAssist.blocklists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.itsatacoshop247.TreeAssist.core.Utils;

import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class FlatFileBlockList implements BlockList {
	FileConfiguration data = new YamlConfiguration();
	File dataFile;

	@Override
	public void initiate() {
		this.dataFile = new File(Utils.plugin.getDataFolder(), "data.yml");
		data.options().copyDefaults(true);
		try {
			this.data.load(this.dataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		if (!this.dataFile.exists()) 
		{
			this.dataFile.getParentFile().mkdirs();
			copy(Utils.plugin.getResource("data.yml"), this.dataFile);
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		String check = toString(block);
		List<String> list = new ArrayList<String>();
		list = (List<String>) data.getStringList("Blocks");

		if (list != null && list.contains(check)) {
			return true;
		}
		return false;
	}
	
	private String toString(Block block) {
		return block.getX() + ";" + block.getY() + ";"
				+ block.getZ() + ";" + block.getWorld().getName();
	}

	@Override
	public void addBlock(Block block) {
		String check = toString(block);
		List<String> list = new ArrayList<String>();
		list = data.getStringList("Blocks");
		list.add(check);
		data.set("Blocks", list);
	}

	@Override
	public void removeBlock(Block block) {
		String check = toString(block);
		List<String> list = new ArrayList<String>();
		list = data.getStringList("Blocks");
		list.remove(check);
		data.set("Blocks", list);
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

	
	private synchronized void saveData() 
	{
		try 
		{
			data.save(dataFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		this.saveData();
	}

	@Override
	public void logBreak(Block block, Player player) {
	}
}
