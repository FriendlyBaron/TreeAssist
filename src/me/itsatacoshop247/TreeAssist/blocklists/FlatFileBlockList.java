package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.TreeBlock;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatFileBlockList implements BlockList {
    private Map<RegionKey, Map<TreeBlock, Long>> mymap = new HashMap<RegionKey, Map<TreeBlock, Long>>();

    /**
     * A small class to ease access to the file name and to mapping of the files' content
     */
    class RegionKey {
        String world;
        int x;
        int z;

        RegionKey(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (world == null ? 0 : world.hashCode());
            result = prime * result + (x ^ x >>> 32);
            result = prime * result + (z ^ z >>> 32);
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            final RegionKey theOther = (RegionKey) other;
            if (this.x != theOther.x) {
                return false;
            }
            if (this.z != theOther.z) {
                return false;
            }
            return this.world.equals(theOther.world);
        }
    }

    /**
     * Get the TreeBlock Map by Bukkit Block
     *
     * @param block the block to check
     * @return the TreeBlock Map
     */
    private synchronized Map<TreeBlock, Long> getChunkContent(Block block) {
        int cx = block.getX() >> 4;
        int cz = block.getZ() >> 4;

        int rx = cx >> 5;
        int rz = cz >> 5;

        String world = block.getWorld().getName();
        RegionKey rKey = new RegionKey(world, rx, rz);

        return getChunkContent(rKey);
    }

    /**
     * Get the TreeBlock Map by RegionKey
     *
     * @param rKey the RegionKey to check
     * @return the TreeBlock Map
     */
    private synchronized Map<TreeBlock, Long> getChunkContent(RegionKey rKey) {
        if (!mymap.containsKey(rKey)) {
            Map<TreeBlock, Long> map = new HashMap<TreeBlock, Long>();
            try {
                File path = new File(Utils.plugin.getDataFolder(), rKey.world);
                if (path.isDirectory() && path.exists()) {
                    File file = new File(path, rKey.x + "." + rKey.z + ".txt");
                    if (file.exists()) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            TreeBlock tBlock = new TreeBlock(line);
                            map.put(tBlock, tBlock.time);
                        }
                        reader.close();
                    }
                }
            } catch (Exception e) {

            }
            mymap.put(rKey, map);
        }

        return mymap.get(rKey);
    }

    @Override
    public void addBlock(Block block) {
        final long time = System.currentTimeMillis();
        Map<TreeBlock, Long> cc = getChunkContent(block);
        cc.put(new TreeBlock(block, time), time);
    }

    @Override
    public void initiate() {
        Bukkit.getScheduler().runTaskTimer(Utils.plugin, new Runnable() {
            @Override
            public void run() {
                FlatFileBlockList.this.save(false);
            }
        }, 1200, 1200);
    }

    @Override
    public boolean isPlayerPlaced(final Block block) {
        if (block == null) {
            return false;
        }
        Map<TreeBlock, Long> cc = getChunkContent(block);
        return cc.containsKey(new TreeBlock(block, 0));
    }

    @Override
    public void logBreak(Block block, Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(final Block block) {
        if (block == null) {
            return;
        }
        Map<TreeBlock, Long> cc = getChunkContent(block);
        Object rem = cc.remove(new TreeBlock(block, 0));
    }

    /**
     * Globally purge blocks - remove non LOG blocks
     *
     * @return the amount of blocks purged
     */
    public int purge() {
        int total = 0;

        List<RegionKey> keyRemovals = new ArrayList<RegionKey>();

        for (World world : Bukkit.getWorlds()) {
            File path = new File(Utils.plugin.getDataFolder(), world.getName());
            if (!path.isDirectory() || !path.exists()) {
                continue;
            }
            for (File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    String[] vals = file.getName().split("\\.");
                    try {
                        int x = Integer.parseInt(vals[0]);
                        int z = Integer.parseInt(vals[1]);

                        RegionKey rKey = new RegionKey(world.getName(), x, z);

                        Map<TreeBlock, Long> blockMap = getChunkContent(rKey);

                        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
                        for (TreeBlock block : blockMap.keySet()) {
                            Block bukkitBlock = block.getBukkitBlock();
                            if (bukkitBlock.getType() != Material.LOG &&
                                    !bukkitBlock.getType().name().equals(Material.LOG_2)) {
                                removals.add(block);
                            }
                        }
                        if (removals.size() > 0) {
                            for (TreeBlock block : removals) {
                                blockMap.remove(block);
                            }
                            saveData(rKey, true);
                            total += removals.size();
                            if (blockMap.size() < 1) {
                                keyRemovals.add(rKey);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        for (RegionKey key : keyRemovals) {
            mymap.remove(key);
        }
        return total;
    }

    /**
     * Purge a world, remove all blocks
     * @param worldname
     * @return the amount of blocks purged
     */
    public int purge(final String worldname) {
        int total = 0;

        File path = new File(Utils.plugin.getDataFolder(), worldname);
        if (path.isDirectory() && path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    String[] vals = file.getName().split("\\.");
                    try {
                        int x = Integer.parseInt(vals[0]);
                        int z = Integer.parseInt(vals[1]);

                        RegionKey rKey = new RegionKey(worldname, x, z);

                        Map<TreeBlock, Long> blockMap = getChunkContent(rKey);

                        if (blockMap.size() > 0) {
                            total += blockMap.size();
                            blockMap.clear();
                            saveData(rKey, true);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        List<RegionKey> removals = new ArrayList<RegionKey>();

        for (RegionKey key : mymap.keySet()) {
            if (key.world.equalsIgnoreCase(worldname)) {
                Map<TreeBlock, Long> blockMap = getChunkContent(key);
                if (blockMap.size() > 0) {
                    total += blockMap.size();
                    blockMap.clear();
                    saveData(key, true);
                }
                removals.add(key);
            }
        }
        for (RegionKey key : removals) {
            mymap.remove(key);
        }
        return total;
    }

    /**
     * Globally purge blocks - remove blocks that are older than the input
     * @param days the age to allow
     * @return the amount of blocks purged
     */
    public int purge(final int days) {

        int total = 0;

        List<RegionKey> keyRemovals = new ArrayList<RegionKey>();

        for (World world : Bukkit.getWorlds()) {
            File path = new File(Utils.plugin.getDataFolder(), world.getName());
            if (!path.isDirectory() || !path.exists()) {
                continue;
            }
            for (File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    String[] vals = file.getName().split("\\.");
                    try {
                        int x = Integer.parseInt(vals[0]);
                        int z = Integer.parseInt(vals[1]);

                        RegionKey rKey = new RegionKey(world.getName(), x, z);

                        Map<TreeBlock, Long> blockMap = getChunkContent(rKey);

                        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
                        for (TreeBlock block : blockMap.keySet()) {
                            if (block.time < (System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)) {
                                removals.add(block);
                            }
                        }
                        if (removals.size() > 0) {
                            for (TreeBlock block : removals) {
                                blockMap.remove(block);
                            }
                            saveData(rKey, true);
                            total += removals.size();
                            if (blockMap.size() < 1) {
                                keyRemovals.add(rKey);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        for (RegionKey key : keyRemovals) {
            mymap.remove(key);
        }
        return total;
    }

    @Override
    public void save() {
    }

    @Override
    public void save(boolean force) {
        for (RegionKey key : mymap.keySet()) {
            saveData(key, force);
        }
    }

    /**
     * Save a certain RegionKey file
     *
     * @param rKey  the RegionKey to save
     * @param force are we shutting down, so must we not use async?
     */
    private void saveData(final RegionKey rKey, final boolean force) {
        final File path = new File(Utils.plugin.getDataFolder(), rKey.world);
        final Map<TreeBlock, Long> map = new HashMap<TreeBlock, Long>(mymap.get(rKey));

        class RunLater implements Runnable {
            @Override
            public void run() {
                try {
                    if (!path.exists() || !path.isDirectory()) {
                        path.mkdir();
                    }
                    File file = new File(path, rKey.x + "." + rKey.z + ".txt");
                    if (!file.exists() && map != null && map.size() > 0) {
                        file.createNewFile();
                    } else if (map == null || map.size() < 1) {
                        file.delete();
                        return;
                    }
                    PrintWriter pw = new PrintWriter(file);
                    for (TreeBlock block : map.keySet()) {
                        pw.println(block.toString());
                    }
                    pw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (force) {
            new RunLater().run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Utils.plugin, new RunLater());
        }
    }
}
