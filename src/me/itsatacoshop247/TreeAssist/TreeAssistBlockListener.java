package me.itsatacoshop247.TreeAssist;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.events.TALeafDecay;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import me.itsatacoshop247.TreeAssist.trees.CustomTree;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TreeAssistBlockListener implements Listener {
    public TreeAssist plugin;
    private final Map<String, Long> noreplace = new HashMap<String, Long>();
    private final TreeAssistAntiGrow antiGrow;

    public TreeAssistBlockListener(TreeAssist instance) {
        plugin = instance;
        antiGrow = new TreeAssistAntiGrow(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (plugin.config.getBoolean("Leaf Decay.Fast Leaf Decay") && plugin.Enabled) {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (!plugin.isActive(world)) {
                return;
            }
            breakRadiusLeaves(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.hasBlock()) {
            if (this.isProtectTool(event.getPlayer().getItemInHand())) {
                Block clicked = event.getClickedBlock();

                if (clicked.getType() == Material.SAPLING) {
                    if (plugin.saplingLocationList.contains(clicked.getLocation())) {
                        plugin.saplingLocationList.remove(clicked.getLocation());
                        event.getPlayer().sendMessage(
                                Language.parse(MSG.SUCCESSFUL_PROTECT_OFF));
                    } else {
                        plugin.saplingLocationList.add(clicked.getLocation());
                        event.getPlayer().sendMessage(
                                Language.parse(MSG.SUCCESSFUL_PROTECT_ON));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.config.getBoolean("Main.Ignore User Placed Blocks") &&
                (event.getBlock().getType() == Material.LOG || event.getBlock().getType() == Material.LOG_2 ||
                        CustomTree.isCustomLog(event.getBlock()))) {
            if (plugin.config.getBoolean("Worlds.Enable Per World")) {
                if (!plugin.config.getList("Worlds.Enabled Worlds").contains(event.getBlock().getWorld().getName())) {
                    return;
                }
            }
            Block block = event.getBlock();
            plugin.blockList.addBlock(block);
            plugin.blockList.save();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        checkFire(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        checkFire(event, event.getBlock());
    }

    private void checkFire(Cancellable event, Block block) {

        if (plugin.config.getBoolean("Sapling Replant.Replant When Tree Burns Down") && plugin.Enabled) {
            if (plugin.config.getBoolean("Worlds.Enable Per World")) {
                if (!plugin.config.getList("Worlds.Enabled Worlds").contains(block.getWorld().getName())) {
                    return;
                }
            }
            MaterialData data = block.getState().getData();
            if (data instanceof Tree) {
                Material logMat = block.getType();
                Tree tree = (Tree) data;
                Block onebelow = block.getRelative(BlockFace.DOWN, 1);
                Block oneabove = block.getRelative(BlockFace.UP, 1);
                if (onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS) {
                    if (oneabove.getType() == Material.AIR || oneabove.getType() == logMat) {
                        Runnable b = new TreeAssistReplant(plugin, block, tree.getSpecies());
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
                    }
                }
            }
        }
    }

    protected final static Set<AbstractGenericTree> trees = new HashSet<AbstractGenericTree>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.Enabled) {
            return;
        }

        Set<AbstractGenericTree> myTrees = new HashSet<AbstractGenericTree>();
        for (AbstractGenericTree tree : trees) {
            myTrees.add(tree);
        }

        for (AbstractGenericTree tree : myTrees) {
            if (tree.contains(event.getBlock())) {
                return;
            } else if (!tree.isValid()) {
                trees.remove(tree);
            }
        }

        AbstractGenericTree tree = AbstractGenericTree.calculate(event);

        if (tree.isValid()) {
            trees.add(tree);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("Main.Toggle Default")) {
            plugin.toggleGlobal(event.getPlayer().getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (antiGrow.contains(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks if the block is a leaf block and drops it
     * if no log is in 2 block radius around
     *
     * @param blockAt the block to check
     */
    private void breakIfLonelyLeaf(Block blockAt) {
        if (blockAt.getType() != Material.LEAVES && !blockAt.getType().name().equals("LEAVES_2") && !CustomTree.isCustomTreeBlock(blockAt)) {
            return;
        }
        World world = blockAt.getWorld();

        int fail = -1; // because we will fail once, when finding blockAt

        for (int x = blockAt.getX() - 2; x <= blockAt.getX() + 2; x++) {
            for (int y = blockAt.getY() - 2; y <= blockAt.getY() + 2; y++) {
                for (int z = blockAt.getZ() - 2; z <= blockAt.getZ() + 2; z++) {
                    fail += calcAir(world.getBlockAt(x, y, z));
                    if (fail > 4) {
                        return; // fail threshold -> out!
                    }
                }
            }
        }

        TALeafDecay event = new TALeafDecay(blockAt);
        Utils.plugin.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled())
        {
        	Utils.plugin.blockList.logBreak(blockAt, null);
        	blockAt.breakNaturally();
        }
    }

    /**
     * enforces an 8 block radius FloatingLeaf removal
     *
     * @param blockAt the block to check
     */
    public void breakRadiusLeaves(Block blockAt) {
        TALeafDecay event = new TALeafDecay(blockAt);
        Utils.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Utils.plugin.blockList.logBreak(blockAt, null);
        blockAt.breakNaturally();
        World world = blockAt.getWorld();
        int x = blockAt.getX();
        int y = blockAt.getY();
        int z = blockAt.getZ();
        for (int x2 = -8; x2 < 9; x2++) {
            for (int z2 = -8; z2 < 9; z2++) {
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 2, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 1, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 1, z + z2));
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 2, z + z2));
            }
        }
    }

    private int calcAir(Block blockAt) {
        if (blockAt.getType() == Material.AIR || blockAt.getType() == Material.VINE || blockAt.getType() == Material.LEAVES || blockAt.getType() == Material.LEAVES_2) {
            return 0;
        } else if (blockAt.getType() == Material.LOG || blockAt.getType() == Material.LOG_2 || CustomTree.isCustomLog(blockAt)) {
            return 5;
        } else {
            return 1;
        }
    }

    private final String displayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Protect";

    public boolean isProtectTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(displayName);
    }

    public TreeAssistAntiGrow getAntiGrow() {
        return antiGrow;
    }

    public ItemStack getProtectionTool() {
        ItemStack item = new ItemStack(Material.GOLD_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    public void noReplace(String name, int seconds) {
        noreplace.put(name, (Long) (System.currentTimeMillis() / 1000) + seconds);
    }

    public boolean isNoReplace(String name) {
        if (noreplace.containsKey(name)) {
            if (noreplace.get(name) < System.currentTimeMillis() / 1000) {
                noreplace.remove(name);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
