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

public class VanillaOneSevenTree extends BaseTree {
	public static Debugger debugger;
	private final byte data;
	Block[] bottoms = null;
	Material logMat = Material.LOG_2;

	public VanillaOneSevenTree(byte type) {
		this.data = type;
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
		if (data == 0) {
			return player.hasPermission("treeassist.destroy.acacia");
		}
		if (data == 1) {
			return player.hasPermission("treeassist.destroy.darkoak");
		}
		return false;
	}

	@Override
	protected Block getBottom(Block block) {
		int counter = 1;
		do {
			if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG_2) {
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
			if (block.getRelative(0, counter, 0).getTypeId() == 18) {
				top = block.getRelative(0, counter - 1, 0);
				break;
			} else {
				counter++;
			}
		}
		
		
		if (data == 0) {
			// acacia might get really messy now; check!
			while (hasDiagonals(top)) {
				top = getDiagonal(top);
			}
		}
		
		return (top != null && leafCheck(top)) ? top.getRelative(0, 1, 0) : null;
	}

	private Block getDiagonal(Block block) {
		// always remember, the block block is the TOP, and the TOP is one ABOVE the last found LOG!
		
		if (block.getType() == block.getRelative(BlockFace.UP).getType()) {
			return block.getRelative(BlockFace.UP);
		}
		for (BlockFace bf : Utils.NEIGHBORFACES) {
			if (block.getType() == block.getRelative(bf).getType()) {
				return block.getRelative(bf).getRelative(BlockFace.UP);
			}
		}
		return null; // should have had a diagonal, but didn't find it. derp!
	}

	private boolean hasDiagonals(Block block) {
		// always remember, the block block is the TOP, and the TOP is one ABOVE the last found LOG!
		
		if (block.getType() == block.getRelative(BlockFace.UP).getType()) {
			return true;
		}
		for (BlockFace bf : Utils.NEIGHBORFACES) {
			if (block.getType() == block.getRelative(bf).getType()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected List<Block> calculate(final Block bottom, final Block top) {
		List<Block> list = new ArrayList<Block>();
		checkBlock(list, bottom, top, true, bottom.getData());
		return list;
	}

	@Override
	protected int isLeaf(Block block) {
		if (block.getTypeId() == 18) {
			return 1;
		}
		return 0;
	}

	@Override
	protected void getTrunks() {
		if (data == 0) {
			return;
		}
		bottoms = new Block[4];
		bottoms[0] = bottom;
		int j = 1;
		
		if (bottom == null) {
			System.out.print("Warning! bottom is null!");
			return;
		}
		
		for (BlockFace face : Utils.NEIGHBORFACES) {
			if (bottom.getRelative(face).getType() == Material.LOG_2 && j<4) {
				bottoms[j] = bottom.getRelative(face);
				j++;
			}
			if (j == 4) {
				break;
			}
		}
	}

	@Override
	protected boolean willBeDestroyed() {
		switch (data) {
		case 0:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Acacia");
		case 1:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Dark Oak");
		default:
			return true; // ugly branch messes
		}
	}

	@Override
	protected boolean willReplant() {
		return Utils.replantType((byte) (data+4));
	}

	@Override
	protected void handleSaplingReplace(int delay) {
		if (data == 1) {
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
		bottom.setType(Material.AIR);
		removeBlocks.remove(bottom);
		totalBlocks.remove(bottom);
		
		Runnable b = new TreeAssistReplant(Utils.plugin, bottom, Material.SAPLING.getId(), (byte) (data+4));
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
		if (block.getType() != this.logMat) {
			
			if (deep && hasDiagonals(block)) {
				checkBlock(list, getDiagonal(block), top, deep, origData);
				return;
			}
			
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

		if (block.getData() != data) {
//			debug.i("cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
			if (top.getData() != 0 || block.getData() <= 1) {
//				debug.i("out!");
				return;
			}
		}
		
		if (block.getX() == top.getX() && block.getZ() == top.getZ()) {
//			debug.i("main trunk!");
			if (!deep) {
				// something else caught the main, return, this will be done later!
//				debug.i("not deep; out!");
				return;
			}
		}

		
		
		if (block.getRelative(0, 1, 0).getType() == logMat) { // might
																		// be a
																		// trunk
//			debug.i("trunk?");
			// one above is a tree block
			if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");
				
				if (block.getData() < 1) {
//					debug.i("no big!");
					
					if (checkFail(block)) {
						return;
					}


				} else {
//					debug.i("big!");

					boolean diff = true;
					for (int Cx = -1; Cx < 2; Cx++) {
						for (int Cz = -1; Cz < 2; Cz++) {
							if (block.getX() - Cx == top.getX()
									&& block.getZ() - Cz == top.getZ()) {
								diff = false;
								Cx = 2;
								Cz = 2;
							}
						}
					}
					if (diff) {
						if (checkFail(block)) {
							return;
						}
					}
				}
			}
		}

		boolean isBig = bottoms != null;

		boolean destroyBig = block.getData() == 1;

		if (!destroyBig && isBig) {
//			debug.i("!destroy & isBig; out!");
			return;
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
			if (isBig) {
				checkBlock(list, block.getRelative(face, 2), top, false, origData);
			}
		}

		if (!deep) {
//			debug.i("not deep, out!");
			return;
		}

		if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
			return;
		}

		if (destroyBig) {
			checkBlock(list, block.getRelative(-2, 0, -2), top, false, origData);
			checkBlock(list, block.getRelative(-1, 0, -2), top, false, origData);
			checkBlock(list, block.getRelative(0, 0, -2), top, false, origData);
			checkBlock(list, block.getRelative(1, 0, -2), top, false, origData);
			checkBlock(list, block.getRelative(2, 0, -2), top, false, origData);
			checkBlock(list, block.getRelative(2, 0, -1), top, false, origData);
			checkBlock(list, block.getRelative(2, 0, 0), top, false, origData);
			checkBlock(list, block.getRelative(2, 0, 1), top, false, origData);
			checkBlock(list, block.getRelative(2, 0, 2), top, false, origData);
			checkBlock(list, block.getRelative(1, 0, 2), top, false, origData);
			checkBlock(list, block.getRelative(0, 0, 2), top, false, origData);
			checkBlock(list, block.getRelative(-1, 0, 2), top, false, origData);
			checkBlock(list, block.getRelative(-2, 0, 2), top, false, origData);
			checkBlock(list, block.getRelative(-2, 0, 1), top, false, origData);
			checkBlock(list, block.getRelative(-2, 0, 0), top, false, origData);
			checkBlock(list, block.getRelative(-2, 0, -1), top, false, origData);
		}
		checkBlock(list, block.getRelative(0, 1, 0), top, true, origData);
	}
	protected boolean checkFail(Block block) {
		int failCount = 0;
		for (int cont = -4; cont < 5; cont++) {
			if (block.getRelative(0, cont, 0).getType() == logMat) {
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
