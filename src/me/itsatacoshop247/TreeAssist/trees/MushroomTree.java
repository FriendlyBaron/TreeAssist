package me.itsatacoshop247.TreeAssist.trees;

import java.util.ArrayList;
import java.util.List;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class MushroomTree extends BaseTree {
	public static Debugger debugger;
	private final Material mat;

	public MushroomTree(Material material) {
		this.mat = material;
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
		if (mat == Material.HUGE_MUSHROOM_1) {
			return player.hasPermission("treeassist.destroy.brownshroom");
		}
		if (mat == Material.HUGE_MUSHROOM_2) {
			return player.hasPermission("treeassist.destroy.redshroom");
		}
		return false;
	}

	@Override
	protected Block getBottom(Block block) {
		int counter = 1;
		do {
			if (block.getRelative(0, 0 - counter, 0).getType() == mat) {
				counter++;
			} else {
				bottom = block.getRelative(0, 1 - counter, 0);
				if (bottom.getRelative(BlockFace.DOWN).getType() == Material.AIR ||
						bottom.getRelative(BlockFace.DOWN).getType() == Material.HUGE_MUSHROOM_1 ||
						bottom.getRelative(BlockFace.DOWN).getType() == Material.HUGE_MUSHROOM_2) {
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
		
		debug.i("getting top; type " + mat.name());

		while (block.getY() + counter < maxY) {
			if (block.getRelative(0, counter, 0).getType() != mat || counter > 6) {
				top = block.getRelative(0, counter - 1, 0);
				debug.i("++");
				break;
			} else {
				counter++;
			}
		}
		debug.i("counter == " + counter);
		return (top != null) ? top.getRelative(0, 1, 0) : null;
	}

	@Override
	protected List<Block> calculate(final Block bottom, final Block top) {
		List<Block> list = new ArrayList<Block>();
		checkBlock(list, bottom, top, true, bottom.getData());
		return list;
	}

	@Override
	protected int isLeaf(Block block) {
		return 0;
	}

	@Override
	protected void getTrunks() {
	
	}

	@Override
	protected boolean willBeDestroyed() {
		switch (mat) {
		case HUGE_MUSHROOM_1:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Brown Shroom");
		case HUGE_MUSHROOM_2:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Red Shroom");
		default:
			return true; // ugly branch messes
		}
	}

	@Override
	protected boolean willReplant() {
		if (!Utils.replantType((byte) mat.getId())) {
			return false;
		}
		return true;
	}

	@Override
	protected void handleSaplingReplace(int delay) {
		replaceSapling(delay, bottom);
	}

	private void replaceSapling(int delay, Block bottom) {
		// make sure that the block is not being removed later
		
		removeBlocks.remove(bottom);
		totalBlocks.remove(bottom);
		
		Material saplingMat = (mat == Material.HUGE_MUSHROOM_1) ? Material.BROWN_MUSHROOM: Material.RED_MUSHROOM;
		
		Runnable b = new TreeAssistReplant(Utils.plugin, bottom, saplingMat, (byte) 0);
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
	protected void checkBlock(List<Block> list, Block block,
			Block top, boolean deep, byte origData) {

//		debug.i("cB " + Debugger.parse(block.getLocation()));
		if (block.getType() != mat) {
//			debug.i("out!");
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

		if (top.getY() < block.getY()) {
			return;
		}
		
		int margin = mat == Material.HUGE_MUSHROOM_1 ? 3 : 2;
		
		if (Math.abs(bottom.getX() - block.getX()) > margin
				|| Math.abs(bottom.getZ() - block.getZ()) > margin) {
			// more than 3 off. That's probably the next shroom already.
			return;
		}
		
		if (mat == Material.HUGE_MUSHROOM_2 && block.getRelative(0, -1, 0).getType() == mat) {
			// overhanging red blabla
			if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");
				if (block.getY() < bottom.getY() || block.getY() > top.getY()) {
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
			checkBlock(list, block.getRelative(face), top, false, origData);

			checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false, origData);
			checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false, origData);
		}

		if (!deep) {
//			debug.i("not deep, out!");
			return;
		}

		if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
			return;
		}

		checkBlock(list, block.getRelative(0, 1, 0), top, true, origData);
	}
	protected boolean checkFail(Block block) {
		return false;
	}


	@Override
	protected boolean isBottom(Block block) {
		return block.equals(bottom);
	}
}
