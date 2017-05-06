package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public class OakTree extends BaseTree implements INormalTree {
    public static Debugger debugger;

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        return player.hasPermission("treeassist.destroy.oak");
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG) {
                counter++;
            } else {

                bottom = block.getRelative(0, 1 - counter, 0);
                if (bottom.getRelative(BlockFace.DOWN).getType() != Material.DIRT &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.GRASS &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.CLAY) {
                    return null; // the tree is already broken.
                }
                return bottom;
            }
        } while (block.getY() - counter > 0);

        bottom = null;
        return bottom;
    }

    @Override
    protected Block getTop(Block block) {
        int maxY = block.getWorld().getMaxHeight() + 10;
        int counter = 1;

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() == Material.LEAVES) {
                top = block.getRelative(0, counter - 1, 0);
                break;
            } else {
                counter++;
            }
        }
        return (top != null && leafCheck(top)) ? top.getRelative(0, 1, 0) : null;
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();
        checkBlock(list, bottom, top, true);
        return list;
    }

    @Override
    protected int isLeaf(Block block) {
        if (block.getType() == Material.LEAVES) {
            return 1;
        }
        return 0;
    }

    @Override
    protected void getTrunks() {
    }

    @Override
    protected boolean willBeDestroyed() {
        return Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.Oak");
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(TreeSpecies.GENERIC);
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        replaceSapling(delay, bottom);
    }

    private void replaceSapling(int delay, Block bottom) {
        if (bottom == null) {
            debugger.i("no null sapling !!!");
            return;
        }
        // make sure that the block is not being removed later

        removeBlocks.remove(bottom);
        totalBlocks.remove(bottom);

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, TreeSpecies.GENERIC);
        Utils.plugin.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(Utils.plugin, b,
                        20 * delay);

        if (Utils.plugin.getConfig()
                .getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
            Utils.plugin.saplingLocationList.add(bottom.getLocation());
            Runnable X = new TreeAssistProtect(Utils.plugin,
                    bottom.getLocation());

            Utils.plugin.getServer()
                    .getScheduler()
                    .scheduleSyncDelayedTask(
                            Utils.plugin,
                            X,
                            20 * Utils.plugin.getConfig()
                                    .getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
        }
    }

    @Override
    public void checkBlock(List<Block> list, Block block,
                           Block top, boolean deep) {
        this.debugCount++;

		debug.i("cB " + Debugger.parse(block.getLocation()));
        if (block.getType() != Material.LOG) {
//			debug.i("no log: " + block.getType().name());
            if (isLeaf(block) > 0) {
                if (!list.contains(block)) {
                    list.add(block);
//					debug.i("cB: adding leaf " + block.getY());
                }
            }
//			debug.i("out!");
            return;
        }

        Tree tree = (Tree) block.getState().getData();
        if (tree.getSpecies() != TreeSpecies.GENERIC) {
            return;
        }

        if (block.getX() == top.getX() && block.getZ() == top.getZ()) {
//			debug.i("main trunk!");
            if (!deep) {
                // something else caught the main, return, this will be done later!
//				debug.i("not deep; out!");
                return;
            }
        }


        if (block.getRelative(0, 1, 0).getType() == Material.LOG) { // might
            // be a
            // trunk
//			debug.i("trunk?");
            // one above is a tree block
            if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");
                if (checkFail(block)) {
                    return;
                }
            }
        }

        if (list.contains(block)) {
//			debug.i("already added!");
            return;
        } else {
//			debug.i(">>>>>>>>>> adding! <<<<<<<<<<<");
            list.add(block);
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            checkBlock(list, block.getRelative(face), top, false);

            checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false);
            checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false);
        }

        if (!deep) {
//			debug.i("not deep, out!");
            return;
        }

        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }

        checkBlock(list, block.getRelative(0, 1, 0), top, true);
    }

    protected boolean checkFail(Block block) {
        int failCount = 0;
        for (int cont = -4; cont < 5; cont++) {
            if (block.getRelative(0, cont, 0).getType() == Material.LOG) {
                failCount++;
            }
        }
        if (failCount > 4) {
//			debug.i("fail count "+failCount+"! out!");
            return true;
        }
        return false;
    }

    @Override
    protected boolean isBottom(Block block) {
        return block.equals(bottom);
    }

    @Override
    protected void debug() {
        System.out.print("Tree: OakTree");
        System.out.print("TreeSpecies: " + TreeSpecies.GENERIC);

        System.out.print("bottom: " + (bottom == null ? "null" : bottom.toString()));
        System.out.print("top: " + (top == null ? "null" : top.toString()));
        System.out.print("valid: " + valid);

        System.out.print("removeBlocks: " + removeBlocks.size());
        System.out.print("totalBlocks: " + totalBlocks.size());
    }
}
