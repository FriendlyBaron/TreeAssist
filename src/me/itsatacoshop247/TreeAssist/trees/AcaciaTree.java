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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcaciaTree extends BaseTree {
    public static Debugger debugger;
    private final List<Block> blocks = new ArrayList<>();
    private final List<Block> leafTops = new ArrayList<>();

    public AcaciaTree() {
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        debugger.i("size: " + blocks.size());
        for (Block block : leafTops) {
            for (BlockFace face : Utils.NEIGHBORFACES) {
                blocks.add(block.getRelative(face));
                blocks.add(block.getRelative(face).getRelative(BlockFace.UP));
            }
            blocks.add(block.getRelative(BlockFace.UP));
        }
        return blocks;
    }

    protected boolean checkFail(Block block) {
        //Tree tree = (Tree) block.getState().getData();

        if (Math.abs(block.getX() - bottom.getX()) > 4 ||
                Math.abs(block.getZ() - bottom.getZ()) > 4) {
            return true;
        }

        int failCount = 0;
        for (int cont = -4; cont < 5; cont++) {
            if (block.getRelative(0, cont, 0).getType() == Material.LOG_2) {
                failCount++;
            }
        }
        if (failCount > 3) {
            debug.i("fail count " + failCount + "! out!");
            return true;
        }
        return false;
    }

    @Override
    protected void debug() {
        System.out.print("Tree: AcaciaTree");
        System.out.print("logMat: " + Material.LOG_2);
        System.out.print("species: " + TreeSpecies.ACACIA);
        System.out.print("bottoms: null");

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
                bottom = block.getRelative(0, 1 - counter, 0);

                boolean foundDiagonal = false;

                for (BlockFace face : Utils.NEIGHBORFACES) {
                    if (bottom.getRelative(BlockFace.DOWN).getRelative(face).getType() == Material.LOG_2) {
                        bottom = bottom.getRelative(BlockFace.DOWN).getRelative(face);
                        block = block.getRelative(face);
                        foundDiagonal = true;
                    }
                }

                if (foundDiagonal) {
                    counter++;
                    continue;
                }

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

    private Block getDiagonalTop(Block block, BlockFace face) {
        if (!blocks.contains(block)) {
            //debugger.i("adding "+face+": " + Debugger.parse(block.getLocation()));
            blocks.add(block);
        }
        if (block.getRelative(BlockFace.UP).getType() == Material.LOG_2) {
            return getDiagonalTop(block.getRelative(BlockFace.UP), face);
        }
        if (block.getRelative(BlockFace.UP).getRelative(face).getType() == Material.LOG_2) {
            return getDiagonalTop(block.getRelative(BlockFace.UP).getRelative(face), face);
        }
        // we are at the top
        leafTops.add(block);
        return block;
    }

    @Override
    protected Block getTop(Block block) {
        int maxY = block.getWorld().getMaxHeight() + 10;
        int counter = 1;

        //debug.i("trying to calculate the TOP block of a 1.7 tree!");


        Map<BlockFace, Block> checkMap = new HashMap<BlockFace, Block>();

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() != Material.LOG_2) {
                // reached non log,
                top = block.getRelative(0, counter-1, 0);
                if (!blocks.contains(top)) {
                    //debugger.i("adding top: " + Debugger.parse(top.getLocation()));
                    blocks.add(top);
                }
                break;
            } else {
                Block temp = block.getRelative(0, counter, 0);
                if (!blocks.contains(temp)) {
                    //debugger.i("adding trunk: " + Debugger.parse(temp.getLocation()));
                    blocks.add(temp);
                }
                if (counter == 1) {
                    // first trunk, let's double check that there is no INSTANT branch
                    for (BlockFace face : Utils.NEIGHBORFACES) {
                        Block check = temp.getRelative(face);
                        if (check.getType() == Material.LOG_2) {
                            check = getDiagonalTop(check, face);
                            checkMap.put(face, check);
                        }
                    }
                }

                for (BlockFace face : Utils.NEIGHBORFACES) {
                    Block check = temp.getRelative(face);
                    if (check.getRelative(BlockFace.UP).getType() == Material.LOG_2) {
                        check = getDiagonalTop(check.getRelative(BlockFace.UP), face);
                        checkMap.put(face, check);
                    }
                }
                counter++;
            }
        }

		debug.i("> straight trunk for " + counter + " blocks; y="+top.getY());

        leafTops.add(top);

        // top is the currently last trunk log
        // from here on we either go horizontally or diagonally

        for (BlockFace face : Utils.NEIGHBORFACES) {
            Block check = top.getRelative(face);
            if (check.getRelative(BlockFace.UP).getType() == Material.LOG_2) {
                check = getDiagonalTop(check.getRelative(BlockFace.UP), face);
                checkMap.put(face, check);
            }
        }

        for (BlockFace face : checkMap.keySet()) {
            Block check = checkMap.get(face);
            if  (check.getY() > top.getY()) {
                top = check;
            }
        }

        top = top.getRelative(BlockFace.UP);

		debug.i("final top should be at y="+top.getY());

        return (top != null && leafCheck(top)) ? top : null;
    }

    @Override
    protected void getTrunks() {
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        replaceSapling(delay, bottom);
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        return player.hasPermission("treeassist.destroy.acacia");
    }

    @Override
    protected boolean isBottom(Block block) {
        return block.equals(bottom);
    }

    @Override
    protected int isLeaf(Block block) {
        if (block.getType() == Material.LEAVES_2) {
            Location bottomLoc = block.getLocation().clone();
            if (bottom == null) {
                return 0;
            }
            bottomLoc.setY(bottom.getY());
            if (bottom.getLocation().distanceSquared(bottomLoc) > 25) {
                return 0;
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

        removeBlocks.remove(bottom);
        totalBlocks.remove(bottom);

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, Material.SAPLING, TreeSpecies.ACACIA.getData());
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
        return Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.Acacia");
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(TreeSpecies.ACACIA);
    }
}
