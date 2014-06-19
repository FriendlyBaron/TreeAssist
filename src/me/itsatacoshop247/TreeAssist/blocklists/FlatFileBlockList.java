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
    private List<String> list = new ArrayList<String>();

    @Override
    public void addBlock(Block block) {
        String check = toString(block);

        list.add(check);
        data.set("Blocks", list);
    }

	@Override
	public void initiate() {
		this.dataFile = new File(Utils.plugin.getDataFolder(), "data.yml");
		data.options().copyDefaults(true);
        System.out.print("Initiating!");
		try {
			this.data.load(this.dataFile);
            list = data.getStringList("Blocks");

            final String first = list.get(0);
            final String[] split = first.split(";");
            if (split.length < 5) {
                List<String> newList = new ArrayList<String>();
                StringBuffer buff = new StringBuffer();
                for (String def : list) {
                    String[] defSplit = def.split(";");
                    buff.setLength(0);
                    buff.append(defSplit[0]);
                    buff.append(';');
                    buff.append(defSplit[1]);
                    buff.append(';');
                    buff.append(defSplit[2]);
                    buff.append(';');
                    buff.append(System.currentTimeMillis());
                    buff.append(';');
                    buff.append(defSplit[3]);

                    newList.add(buff.toString());
                }
                list = newList;
                save();
            }
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
            list = new ArrayList<String>();
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		String check = toString(block);

        return (list != null && list.contains(check));
	}

    @Override
    public void logBreak(Block block, Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(Block block) {
        String check = toString(block);
        list.remove(check);
        data.set("Blocks", list);
    }

    public int purge(final String worldname) {
        final List<String> removals = new ArrayList<String>();
        for (String def : list) {
            if (def.endsWith(worldname)) {
                removals.add(def);
            }
        }
        list.removeAll(removals);
        save();
        return removals.size();
    }

    public int purge(final int days) {
        final List<String> removals = new ArrayList<String>();
        for (String def : list) {
            int i = Integer.valueOf(def.split(";")[3]);
            if (i < (System.currentTimeMillis() - days*24*60*60*1000)) {
                removals.add(def);
            }
        }
        list.removeAll(removals);
        save();
        return removals.size();
    }

    @Override
    public void save() {
        this.saveData();
    }
	
	private String toString(Block block) {
		return block.getX() + ";" + block.getY() + ";"
				+ block.getZ() + ";" + System.currentTimeMillis() + ";" + block.getWorld().getName();
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
            data.set("Blocks", list);
			data.save(dataFile);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
