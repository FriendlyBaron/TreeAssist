package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MushroomRedTree extends BaseTree implements ISpecialTree {
    public static Debugger debugger;

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();

        int x = bottom.getX();
        int z = bottom.getZ();

        for (int y = bottom.getY(); y < top.getY(); y++) {
            list.add(bottom.getWorld().getBlockAt(x, y, z));
        }

        for (x = -1; x < 2; x++) {
            for (z = -1; z < 2; z++) {
                list.add(top.getRelative(x, 0, z));

                list.add(top.getRelative(-2, x-2, z));
                list.add(top.getRelative(2, x-2, z));
                list.add(top.getRelative(z, x-2, -2));
                list.add(top.getRelative(z, x-2, 2));
            }
        }

        return list;
    }

    /**
     * thanks to filbert66 for this determination method!
     *
     * @param tool the itemstack being used
     * @return the seconds that it will take to destroy
     */
    @Override
    public int calculateCooldown(ItemStack tool) {

        Material element = (tool != null ? tool.getType() : null);

        float singleTime;

        switch (element) {
            case GOLD_AXE:
                singleTime = 0.05F;
                break;
            case DIAMOND_AXE:
                singleTime = 0.05F;
                break;
            case IRON_AXE:
                singleTime = 0.05F;
                break;
            case STONE_AXE:
                singleTime = 0.1F;
                break;
            case WOOD_AXE:
                singleTime = 0.15F;
                break;

            default:
                singleTime = 0.3F;
                break;
        }

        float efficiencyFactor = 1.0F;
        if (tool != null && tool.hasItemMeta()) {
            int efficiencyLevel = tool.getItemMeta().getEnchantLevel(
                    Enchantment.DIG_SPEED);
            for (int i = 0; i < efficiencyLevel; i++) {
                efficiencyFactor /= 1.3F;
            }
            debug.i("tool efficiency factor: " + efficiencyFactor);
        }

        int numLogs = 0;
        for (Block b : removeBlocks) {
            if (isLeaf(b) > 0) {
                numLogs++;
            }
        }

        debug.i("breakTime (" + removeBlocks.size() + " blocks): " + numLogs
                * singleTime * efficiencyFactor);

        return (int) (numLogs * singleTime * efficiencyFactor);
    }

    @Override
    public void checkBlock(List<Block> list, Block block, Block top,
                           boolean deep, byte origData) {
    }

    protected boolean checkFail(Block block) {
        return false;
    }

    @Override
    protected void debug() {
        System.out.print("Tree: MushroomRedTree");
        System.out.print("mat: " + Material.HUGE_MUSHROOM_2);


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
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.HUGE_MUSHROOM_2) {
                counter++;
            } else {
                bottom = block.getRelative(0, 1 - counter, 0);
                if (bottom.getRelative(BlockFace.DOWN).getType() == Material.AIR
                        || bottom.getRelative(BlockFace.DOWN).getType() == Material.HUGE_MUSHROOM_2) {
                    return null; // the shroom is already broken.
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

        //debug.i("getting top; type " + mat.name());

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() != Material.HUGE_MUSHROOM_2
                    || counter > 13) {
                top = block.getRelative(0, counter - 1, 0);
                debug.i("++");
                break;
            } else {
                counter++;
            }
        }
        debug.i("counter == " + counter);
        return top;
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
        return player.hasPermission("treeassist.destroy.redshroom");
    }

    @Override
    protected boolean isBottom(Block block) {
        return block.equals(bottom);
    }

    @Override
    protected int isLeaf(Block block) {
        return 0;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    private void replaceSapling(int delay, Block bottom) {
        // make sure that the block is not being removed later

        removeBlocks.remove(bottom);
        totalBlocks.remove(bottom);

        Material saplingMat = Material.RED_MUSHROOM;

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, saplingMat,
                (byte) 0);
        Utils.plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(Utils.plugin, b, 20 * delay);

        if (Utils.plugin.getConfig().getInt(
                "Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
            Utils.plugin.saplingLocationList.add(bottom.getLocation());
            Runnable X = new TreeAssistProtect(Utils.plugin,
                    bottom.getLocation());

            Utils.plugin
                    .getServer()
                    .getScheduler()
                    .scheduleSyncDelayedTask(
                            Utils.plugin,
                            X,
                            20 * Utils.plugin
                                    .getConfig()
                                    .getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
        }
    }

    @Override
    protected boolean willBeDestroyed() {
        return Utils.plugin.getConfig().getBoolean(
                "Automatic Tree Destruction.Tree Types.Red Shroom");
    }

    @Override
    protected boolean willReplant() {
        if (!Utils.replantType((byte) Material.HUGE_MUSHROOM_2.getId())) {
            return false;
        }
        return true;
    }
}
