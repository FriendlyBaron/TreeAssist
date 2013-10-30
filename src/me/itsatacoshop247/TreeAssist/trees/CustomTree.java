package me.itsatacoshop247.TreeAssist.trees;

import java.util.ArrayList;
import java.util.List;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CustomTree extends BaseTree {
	public static List<?> customTreeBlocks = null;
	public static List<?> customLogs = null;
	public static List<?> customSaplings = null;
	public static Debugger debugger;
	private final int typeID;
	private final byte dataValue;

	public CustomTree(int typeId, byte data) {
		typeID = typeId;
		dataValue = data;
	}

	public static boolean isCustomLog(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (CustomTree.customLogs.contains(blockAt.getTypeId())) {
				return true;
			}
			return CustomTree.customLogs.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return CustomTree.customLogs.contains(blockAt.getTypeId());
	}

	public static boolean isCustomTreeBlock(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (CustomTree.customTreeBlocks.contains(blockAt.getTypeId())) {
				return true;
			}
			return CustomTree.customTreeBlocks.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return CustomTree.customTreeBlocks.contains(blockAt.getTypeId());
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	protected boolean hasPerms(Player player) {
		return true; //TODO implement custom 
	}

	@Override
	protected Block getBottom(Block block) {
		int counter = 1;
		do {
			if (CustomTree.isCustomLog(block.getRelative(0, 0 - counter, 0))) {
				counter++;
			} else {
				bottom = block.getRelative(0, 1 - counter, 0);
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
			if (CustomTree.isCustomTreeBlock(block.getRelative(0, counter, 0))) {
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
		checkBlock(list, bottom, top, true, bottom.getData());
		return list;
	}

	@Override
	protected int isLeaf(Block block) {
		if (CustomTree.isCustomTreeBlock(block)) {
			return 1;
		}
		return 0;
	}

	@Override
	protected void getTrunks() {
	}

	@Override
	protected boolean willBeDestroyed() {
		return true;
	}

	@Override
	protected boolean willReplant() {
		if (!CustomTree.isCustomLog(bottom)) {
			return false;
		}
		
		return true;
	}

	@Override
	protected void handleSaplingReplace(int delay) {
		// TODO Auto-generated method stub
		// make sure that the block is not being removed later
		bottom.setType(Material.AIR);
		removeBlocks.remove(bottom);
		totalBlocks.remove(bottom);
		
		//TODO add custom saplings!
	}

	@Override
	protected void checkBlock(List<Block> list, Block block,
			Block top, boolean deep, byte origData) {

//		debug.i("cB " + Debugger.parse(block.getLocation()));
		if (block.getTypeId() != typeID) {
//			debug.i("no log!");
			if (isLeaf(block) > 0) {
				if (!list.contains(block)) {
					list.add(block);
//					debug.i("cB: adding leaf " + block.getY());
				}
			}
//			debug.i("out!");
			return;
		}

		if (!CustomTree.isCustomLog(block)) {
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

		
		
		if (block.getRelative(0, 1, 0).getTypeId() == typeID) { // might
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
		int failCount = 0;
		for (int cont = -4; cont < 5; cont++) {
			if (block.getRelative(0, cont, 0).getTypeId() == typeID) {
				failCount++;
			}
		}
		if (failCount > 3) {
//			debug.i("fail count "+failCount+"! out!");
			return true;
		}
		return false;
	}
}
