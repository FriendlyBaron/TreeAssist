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

public class VanillaDarkOakTree extends BaseTree implements ISpecialTree {
    public static Debugger debugger;
    private final TreeSpecies species = TreeSpecies.DARK_OAK;
    Block[] bottoms = null;
    Material logMat = Material.LOG_2;

    public VanillaDarkOakTree() {
    }


    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        return player.hasPermission("treeassist.destroy.darkoak");
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG_2) {
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

        // debug.i("trying to calculate the TOP block of a 1.7 tree!");

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() != Material.LOG_2) {
                top = block.getRelative(0, counter, 0);
                break;
            } else {
                counter++;
            }
        }

        // debug.i("> straight trunk for " + counter + " blocks; y="+top.getY());

        if (bottoms != null) {
            for (Block other : bottoms) {
                if (other == null || other.equals(block)) {
                    continue;
                }
                while (other.getY() + counter < maxY) {
                    // debug.i("checking "+other.getRelative(0, counter, 0));
                    if (other.getRelative(0, counter, 0).getType() != Material.LOG_2) {
                        if (other.equals(bottom)) {
                            // debug.i("has not been bottom yet");
                            top = other.getRelative(0, counter, 0);
                        }
                        break;
                    } else {
                        bottom = other;
                        // debug.i("new bottom: "+other);
                        counter++;
                    }
                }
            }
        }

        // debug.i("final top should be at y="+top.getY());

        return (top != null && leafCheck(top)) ? top : null;
    }

    private Block getDiagonal(Block block) {
        // always remember, the block block is the TOP, and the TOP is one ABOVE the last found LOG!

        // debug.i("getting diagonal at y="+block.getY());

        if (Material.LOG_2 == block.getType()) {
            // debug.i("> UP");
            return block.getRelative(BlockFace.UP);
        }
        for (BlockFace bf : Utils.NEIGHBORFACES) {
            if (Material.LOG_2 == block.getRelative(bf).getType()) {
                // debug.i("> "+bf.name());
                return block.getRelative(bf).getRelative(BlockFace.UP);
            }
        }
        // debug.i("> NULL");
        return null; // should have had a diagonal, but didn't find it. derp!
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
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();

        if (bottoms == null) {
            checkBlock(list, bottom, top, true, 0);
        } else {
            // debug.i("-------- checking bottoms!! --------");
            for (Block bBlock : bottoms) {
                if (bBlock == null) {
                    continue;
                }
                // debug.i("-------- checking bottom!! --------");
                checkBlock(list, bBlock, top, true, 0);
            }
        }
        // debug.i("steps: "+steps);
        return list;
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
    protected void getTrunks() {
        // debug.i("calculating trunks");
        bottoms = new Block[4];
        bottoms[0] = bottom;
        int j = 1;

        if (bottom == null) {
            // debug.i("bottom is null!");
            return;
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            if (bottom.getRelative(face).getType() == Material.LOG_2 && j < 4) {
                bottoms[j] = bottom.getRelative(face);
                j++;
            }
            if (j == 4) {
                break;
            }
        }
        // debug.i("found "+j+" trunks!");
    }

    @Override
    protected boolean willBeDestroyed() {
        // debug.i("willbedestroyedcheck: " + Utils.plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak"));
        return Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak");
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(species);
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        if (bottoms != null) {
            for (Block bottom : bottoms) {
                replaceSapling(delay, bottom);
            }
        }
        replaceSapling(delay, bottom);
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

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, Material.SAPLING, species.getData());
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
                           Block top, boolean deep, byte origData) {
        checkBlock(list, block, top, deep, 0);
    }

    public void checkBlock(List<Block> list, Block block,
                           Block top, boolean deep, int step) {
        steps = Math.max(steps, step);
        if (++step > 500) {
            // debug.i("OUT BECAUSE TOO MANY STEPS!");
            return;
        }
        // debug.i("["+block.hashCode()+"]"+ "cB " + Debugger.parse(block.getLocation()));
        if (block.getType() != this.logMat) {

            if (hasDiagonals(block)) {
                checkBlock(list, getDiagonal(block), top, deep, step);
                return;
            }

            // debug.i("["+block.hashCode()+"]"+ "no log!");
            if (isLeaf(block) > 0) {
                if (!list.contains(block)) {
                    list.add(block);
                    // debug.i("["+block.hashCode()+"]"+ "cB: adding leaf " + block.getY());
                }
            }
            // debug.i("["+block.hashCode()+"]"+ "out!");
            return;
        }

        Tree tree = (Tree) block.getState().getData();

        if (tree.getSpecies() != species) {
            // debug.i("["+block.hashCode()+"]"+ "cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
            if (top.getData() != 0 || block.getData() <= 1) {
                // debug.i("["+block.hashCode()+"]"+ "out!");
                return;
            }
        }

        boolean isMain = false;

        if (bottoms != null) {
            for (Block bBottom : bottoms) {
                if (bBottom == null) {
                    continue;
                }
                if (block.getX() == bBottom.getX() && block.getZ() == bBottom.getZ()) {
                    // debug.i("["+block.hashCode()+"]"+ "main trunk!");
                    if (!deep) {
                        // something else caught the main, return, this will be done later!
                        // debug.i("["+block.hashCode()+"]"+ "not deep; out!");
                        return;
                    }
                    isMain = true;
                    break;
                }
            }
        } else {
            if (block.getX() == bottom.getX() && block.getZ() == bottom.getZ()) {
                // debug.i("["+block.hashCode()+"]"+ "main trunk!");
                if (!deep) {
                    // something else caught the main, return, this will be done later!
                    // debug.i("["+block.hashCode()+"]"+ "not deep; out!");
                    return;
                }
                isMain = true;
            }
        }

        if (!isMain) {
            // we have a fat block outside of the trunk, make sure it is not another tree's trunk!
            Block iBlock = block.getRelative(BlockFace.DOWN);
            while (iBlock.getType().name().equals("LOG_2")) {
                iBlock = iBlock.getRelative(BlockFace.DOWN);
                // check what is UNDER the maybe-trunk we have here
            }

            switch (iBlock.getType()) {
                case AIR:
                    break; // we're just hanging in there, it'll be fine
                case GRASS:
                case DIRT:
                case CLAY:
                    return; // another trunk - OUT!!!
                default:
                    break; // I dunno - should be fine, I guess?
            }
        }

        if (block.getRelative(0, 1, 0).getType() == logMat) { // might
            // be a
            // trunk
            // debug.i("["+block.hashCode()+"]"+ "trunk?");
            // one above is a tree block
            if (!isMain) {
                // debug.i("["+block.hashCode()+"]"+ "not main!");

                // debug.i("["+block.hashCode()+"]"+ "big!");

                boolean awayFromTrunk = true;

                if (bottoms == null) {
                    for (int Cx = -1; Cx < 2; Cx++) {
                        for (int Cz = -1; Cz < 2; Cz++) {
                            if (block.getX() - Cx == top.getX()
                                    && block.getZ() - Cz == top.getZ()) {
                                awayFromTrunk = false;
                                Cx = 2;
                                Cz = 2;
                                break;
                            }
                        }
                    }
                } else {
                    for (Block myBottom : bottoms) {
                        if (myBottom == null) {
                            continue;
                        }
                        if ((Math.abs(block.getX() - myBottom.getX()) < 2)
                                && (Math.abs(block.getZ() - myBottom.getZ()) < 2)) {
                            awayFromTrunk = false;
                            break;
                        }
                    }
                }

                if (awayFromTrunk) {
                    if (checkFail(block)) {
                        return;
                    }
                }
            }
        }

        boolean isBig = bottoms != null;

        if (list.contains(block)) {
            // debug.i("["+block.hashCode()+"]"+ "already added!");
            return;
        } else {
            // debug.i("["+block.hashCode()+"]"+ ">>>>>>>>>> adding! <<<<<<<<<<<");
            list.add(block);
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            checkBlock(list, block.getRelative(face), top, false, step);

            checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false, step);
            checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false, step);
            if (isBig) {
                checkBlock(list, block.getRelative(face, 2), top, false, step);
            }
        }

        if (!deep) {
            // debug.i("["+block.hashCode()+"]"+ "not deep, check UP and DOWN then OUT!");
            checkBlock(list, block.getRelative(0, 1, 0), top, false, step);
            checkBlock(list, block.getRelative(0, -1, 0), top, false, step);
            return;
        }

        if (block.getY() > top.getY()) {
            // debug.i("["+block.hashCode()+"]"+ "over the top! (hah) out!");
            return;
        }

        if (isBig) {
            checkBlock(list, block.getRelative(-2, 0, -2), top, false, step);
            checkBlock(list, block.getRelative(-1, 0, -2), top, false, step);
            checkBlock(list, block.getRelative(0, 0, -2), top, false, step);
            checkBlock(list, block.getRelative(1, 0, -2), top, false, step);
            checkBlock(list, block.getRelative(2, 0, -2), top, false, step);
            checkBlock(list, block.getRelative(2, 0, -1), top, false, step);
            checkBlock(list, block.getRelative(2, 0, 0), top, false, step);
            checkBlock(list, block.getRelative(2, 0, 1), top, false, step);
            checkBlock(list, block.getRelative(2, 0, 2), top, false, step);
            checkBlock(list, block.getRelative(1, 0, 2), top, false, step);
            checkBlock(list, block.getRelative(0, 0, 2), top, false, step);
            checkBlock(list, block.getRelative(-1, 0, 2), top, false, step);
            checkBlock(list, block.getRelative(-2, 0, 2), top, false, step);
            checkBlock(list, block.getRelative(-2, 0, 1), top, false, step);
            checkBlock(list, block.getRelative(-2, 0, 0), top, false, step);
            checkBlock(list, block.getRelative(-2, 0, -1), top, false, step);
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, true, step);
    }

    protected boolean checkFail(Block block) {
        //Tree tree = (Tree) block.getState().getData();
        // debug.i("["+block.hashCode()+"]"+ "checkFail!");

        if (bottoms == null) {
            if (Math.abs(block.getX() - bottom.getX()) > 3 ||
                    Math.abs(block.getZ() - bottom.getZ()) > 3) {
                // debug.i("["+block.hashCode()+"]"+ "checkFail single bottom true");
                return true;
            }
        } else {

            boolean wefail = true;

            for (Block bottom : bottoms) {
                if (bottom == null) {
                    continue;
                }
                if (Math.abs(block.getX() - bottom.getX()) <= 3 &&
                        Math.abs(block.getX() - bottom.getX()) <= 3) {
                    wefail = false;
                }
            }

            if (wefail) {
                // debug.i("["+block.hashCode()+"]"+ "checkFail multi bottom true");
                return true;
            }
        }

        while (block.getType().name().equals("LOG_2")) {
            block = block.getRelative(BlockFace.DOWN);
        }
        // debug.i("["+block.hashCode()+"]"+ "checkFail result based on type: "+block.getType());
        switch (block.getType()) {
            case AIR:
                return false; // we're just hanging in there, it'll be fine
            case GRASS:
            case DIRT:
            case CLAY:
                return true; // another trunk - OUT!!!
            default:
                return false; // I dunno - should be fine, I guess?
        }
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
    protected void debug() {
        System.out.print("Tree: VanillaOneSevenTree");
        System.out.print("logMat: " + (logMat == null ? "null" : logMat.name()));
        System.out.print("species: " + species);
        System.out.print("bottoms: ");
        if (bottoms == null) {
            System.out.print("null");
        } else {
            for (Block b : bottoms) {
                if (b == null) {
                    System.out.print("null");
                } else {
                    System.out.print(b.toString());
                }
            }
        }


        System.out.print("bottom: " + (bottom == null ? "null" : bottom.toString()));
        System.out.print("top: " + (top == null ? "null" : top.toString()));
        System.out.print("valid: " + valid);

        System.out.print("removeBlocks: " + removeBlocks.size());
        System.out.print("totalBlocks: " + totalBlocks.size());
    }
}
