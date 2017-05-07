package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public class DarkOakTree extends BaseTree implements INormalTree {
    public static Debugger debugger;
    Block[] bottoms = null;
    private final List<Block> leaves = new ArrayList<>();

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();
        int x = Math.min(bottoms[0].getX(), Math.min(bottoms[1].getX(), bottoms[2].getX()));
        int z = Math.min(bottoms[0].getZ(), Math.min(bottoms[1].getZ(), bottoms[2].getZ()));

        checkBlock(list,
                bottom.getWorld().getBlockAt(x, bottom.getY(), z),
                bottom.getWorld().getBlockAt(x, top.getY(), z),
                BlockFace.WEST, BlockFace.NORTH, true);
        checkBlock(list,
                bottom.getWorld().getBlockAt(x+1, bottom.getY(), z),
                bottom.getWorld().getBlockAt(x+1, top.getY(), z),
                BlockFace.EAST, BlockFace.NORTH, true);
        checkBlock(list,
                bottom.getWorld().getBlockAt(x, bottom.getY(), z+1),
                bottom.getWorld().getBlockAt(x, top.getY(), z+1),
                BlockFace.WEST, BlockFace.SOUTH, true);
        checkBlock(list,
                bottom.getWorld().getBlockAt(x+1, bottom.getY(), z+1),
                bottom.getWorld().getBlockAt(x+1, top.getY(), z+1),
                BlockFace.EAST, BlockFace.SOUTH, true);
        list.addAll(leaves);
        return list;
    }

    // north = towards negative z
    // west = towards negative x

    private void checkBlock(List<Block> list, Block block, Block top, BlockFace x, BlockFace z, boolean deep) {

        this.debugCount++;

        if (!Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak")) {
            return;
        }
        debug.i("cB " + Debugger.parse(block.getLocation()));

        if (block.getType() != Material.LOG_2) {
//			debug.i("no log: " + block.getType().name());
            if (isLeaf(block) > 0) {
                if (!leaves.contains(block)) {
                    leaves.add(block);
//					debug.i("cB: adding leaf " + block.getY());
                }
            }
//			debug.i("out!");
            return;
        }

        Tree tree = (Tree) block.getState().getData();
        if (tree.getSpecies() != TreeSpecies.DARK_OAK) {
//			debug.i("cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
            return;
        }

        if (block.getRelative(0, 1, 0).getType() == Material.LOG_2) { // might
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

        if (!deep) {
            checkBlock(list, block.getRelative(x), top, x, z, false);
            checkBlock(list, block.getRelative(z), top, x, z, false);
            checkBlock(list, block.getRelative(BlockFace.UP), top, x, z, false);
//			debug.i("not deep, out!");
            return;
        }

        checkBlock(list, block.getRelative(x).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(x).getRelative(z).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(z).getRelative(BlockFace.UP), top, x, z, false);

        // distinct changing of TOP support, because OAK
        if (block.getY() > top.getY() +3) {
//			debug.i("over the top! (hah) out!");
            return;
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, x, z,true);
    }

    @Override
    public void checkBlock(List<Block> list, Block block, Block top, boolean deep) {

    }

    protected boolean checkFail(Block block) {
        // Tree tree = (Tree) block.getState().getData();
        // debug.i("["+block.hashCode()+"]"+ "checkFail!");

        while (block.getType() == Material.LOG_2) {
            block = block.getRelative(BlockFace.DOWN);
        }
        // debug.i("["+block.hashCode()+"]"+ "checkFail result based on type: "+block.getType());
        switch (block.getType()) {
            case AIR:
                return false; // we're just hanging in there, it'll be fine
            case GRASS:
            case DIRT:
            case CLAY:
            case SAND:
                return true; // another trunk - OUT!!!
            default:
                return false; // I dunno - should be fine, I guess?
        }
    }

    @Override
    protected void debug() {
        System.out.print("Tree: DarkOakTree");
        System.out.print("logMat: " + Material.LOG_2);
        System.out.print("species: " + TreeSpecies.DARK_OAK);
        System.out.print("bottoms: ");
        for (Block b : bottoms) {
            if (b == null) {
                System.out.print("null");
            } else {
                System.out.print(b.toString());
            }
        }

        System.out.print("bottom: " + (bottom == null ? "null" : bottom.toString()));
        System.out.print("top: " + (top == null ? "null" : top.toString()));
        System.out.print("valid: " + valid);

        System.out.print("removeBlocks: " + removeBlocks.size());
        System.out.print("totalBlocks: " + totalBlocks.size());
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG_2) {
                counter++;
            } else {
                if (block.getRelative(0, 0, -1).getType() == Material.LOG_2) {
                    block = block.getRelative(0, 0, -1);
                }
                if (block.getRelative(-1, 0, 0).getType() == Material.LOG_2) {
                    block = block.getRelative(-1, 0, 0);
                }

                bottom = block.getRelative(0, 1 - counter, 0);
                if (bottom.getRelative(BlockFace.DOWN).getType() != Material.DIRT &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.GRASS &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.CLAY &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.SAND) {
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
            if (block.getRelative(0, counter, 0).getType() == Material.LEAVES_2) {
                top = block.getRelative(0, counter - 1, 0);
                break;
            } else {
                counter++;
            }
        }
        // distinct difference from other tops:
        // non-trunk blocks can be higher than the top
        // and thus mess up the leafCheck check
        return (top != null) ? top.getRelative(0, 1, 0) : null;
    }

    @Override
    protected void getTrunks() {
        bottoms = new Block[4];
        bottoms[0] = bottom;
        int j = 1;

        if (bottom == null) {
            return;
        }

        int foundsum = 0;

        for (BlockFace face : Utils.NEIGHBORFACES) {
            if (bottom.getRelative(face).getType() == Material.LOG_2 && j < 4) {
                bottoms[j] = bottom.getRelative(face);
                j++;
                foundsum++;
            }
            if (j == 4) {
                break;
            }
        }
        if (foundsum < 1) {
            bottoms = null;
            valid = false;
        }
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        if (!Utils.plugin.getConfig().getBoolean(
                "Sapling Replant.Tree Types to Replant.Dark Oak")) {
            debugger.i("no big spruce sapling !!!");
            return;
        }
        for (Block bottom : bottoms) {
            replaceSapling(delay, bottom);
            debugger.i("go !!!");
        }
    }

    private boolean hasDiagonals(Block block) {
        // always remember, the block block is the TOP, and the TOP is one ABOVE the last found LOG!

        // debug.i("checking for diagonal at y="+block.getY());

        if (Material.LOG_2 == block.getType()) {
            // debug.i("> UP");
            return true;
        }
        for (BlockFace bf : Utils.NEIGHBORFACES) {
            if (Material.LOG_2 == block.getRelative(bf).getType()) {
                // debug.i("> "+bf.name());
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        return player.hasPermission("treeassist.destroy.darkoak");
    }
    @Override
    protected boolean isBottom(Block block) {
        if (bottoms != null) {
            for (Block b : bottoms) {
                if (b != null && b.equals(block)) {
                    return true;
                }
            }
        }
        return block.equals(bottom);
    }

    @Override
    protected int isLeaf(Block block) {
        if (block.getType() == Material.LEAVES_2) {
            Location bottomLoc = block.getLocation().clone();
            if (bottoms == null) {
                if (bottom == null) {
                    return 0;
                }
                bottomLoc.setY(bottom.getY());
                if (bottom.getLocation().distanceSquared(bottomLoc) > 25) {
                    return 0;
                }
            } else {
                for (Block bottom : bottoms) {
                    if (bottom == null) {
                        continue;
                    }
                    bottomLoc.setY(bottom.getY());
                    if (bottom.getLocation().distanceSquared(bottomLoc) > 25) {
                        return 0;
                    }
                }
            }

            return 1;
        }
        return 0;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    private void replaceSapling(int delay, Block bottom) {
        if (bottom == null) {
            return;
        }
        // make sure that the block is not being removed later

        if (bottoms != null) {
            for (Block b : bottoms) {
                removeBlocks.remove(b);
                totalBlocks.remove(b);
            }
        }

        removeBlocks.remove(bottom);
        totalBlocks.remove(bottom);

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, Material.SAPLING, TreeSpecies.DARK_OAK.getData());
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
    protected boolean willBeDestroyed() {
        // debug.i("willbedestroyedcheck: " + Utils.plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak"));
        return Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak");
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(TreeSpecies.DARK_OAK);
    }
}
