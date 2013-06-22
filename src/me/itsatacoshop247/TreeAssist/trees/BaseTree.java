package me.itsatacoshop247.TreeAssist.trees;

import java.util.ArrayList;
import java.util.List;

import me.itsatacoshop247.TreeAssist.TreeAssist;
import me.itsatacoshop247.TreeAssist.core.Debugger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BaseTree implements Tree {
	protected enum TreeType {
		OAK, BIRCH, SPRUCE, JUNGLE, SHROOM, CUSTOM;
	}

	public static Debugger debug;
	
	protected boolean valid = false;
	protected List<Block> removeBlocks = new ArrayList<Block>();
	protected List<Block> totalBlocks = new ArrayList<Block>();
	
	protected Block bottom;
	protected Block top;


	private static void checkAndDoSaplingProtect(Player player, Block block, BlockBreakEvent event) {
		int typeid = block.getTypeId();
		if (typeid != 17 && !CustomTree.isCustomLog(block)) {
			if (typeid == 6) {
				if (Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Block all breaking of Saplings")) {
					player.sendMessage(ChatColor.GREEN
							+ "You cannot break saplings on this server!");
					event.setCancelled(true);
				} else if (Utils.plugin.blockList.contains(block.getLocation())) {
					player.sendMessage(ChatColor.GREEN
							+ "This sapling is protected!");
					event.setCancelled(true);
				}
			} else if (typeid == 2 || typeid == 3 || typeid == 82) {
				if (Utils.plugin.blockList.contains(block
						.getRelative(BlockFace.UP, 1).getLocation())) {
					player.sendMessage(ChatColor.GREEN
							+ "This sapling is protected!");
					event.setCancelled(true);
				}
			} else if (typeid != 6
					&& Utils.plugin.blockList.contains(block.getLocation())) {
				Utils.plugin.blockList.remove(block.getLocation());
			}
		}
	}


	private static BaseTree getTreeByBlockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		TreeType type = getTreeTypeByBlock(block);
		if (type == null) {
			checkAndDoSaplingProtect(event.getPlayer(), block, event);
			return null;
		}
		switch (type) {
			case OAK:
			case BIRCH:
			case SPRUCE:
			case JUNGLE:
				return new VanillaTree((byte) type.ordinal());
			case SHROOM:
				return new MushroomTree(block.getTypeId());
			case CUSTOM:
				return new CustomTree(block.getTypeId(), block.getData());
			default:
				return null;
		}
	}

	private static TreeType getTreeTypeByBlock(Block block) {
		if (block.getType() == Material.LOG) {
			switch (block.getData()) {
			case 0:
			case 1:
			case 2:
			case 3:
				return TreeType.values()[block.getData()];
			default:
				return null;
			}
		} else if (CustomTree.isCustomLog(block)) {
			return TreeType.CUSTOM;
		}
		switch (block.getTypeId()) {

			case 99:
			case 100:
				return TreeType.SHROOM;
			default:
				return null;
		}
	}
	public static BaseTree calculate(BlockBreakEvent event) {

		
		TreeAssist plugin = Utils.plugin;
		
		if (!plugin.isActive(event.getPlayer().getWorld())) {
			return new InvalidTree();
		}
		debug.i("BlockBreak!");
		
		BaseTree resultTree = getTreeByBlockBreakEvent(event);
		
		if (resultTree == null) {
			debug.i("getTreeByBlockBreakEvent == null");
			return new InvalidTree(); // not a tree block!
		}

		Block block = event.getBlock();
		
		if (plugin.getConfig().getBoolean("Main.Ignore User Placed Blocks")) {
			String check = "" + block.getX() + ";" + block.getY() + ";"
					+ block.getZ() + ";" + block.getWorld().getName();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.getData().getStringList("Blocks");
	
			if (list != null && list.contains(check)) {
				debug.i("placed blocks. Removing!");
				plugin.getData().getList("Blocks").remove(check);
				plugin.saveData();
				return new InvalidTree(); // placed block. ignore!
			}
		}

		Player player = event.getPlayer();
		
		if (!resultTree.hasPerms(player)) {
			debug.i("No permission!");
			if (plugin.isForceAutoDestroy()) {
				resultTree.findYourBlocks(block);
				debug.i("But still, remove later, maybe");
				if (resultTree.isValid()) {
					resultTree.removeLater();
					debug.i("Not maybe. For sure!");
				}
				return resultTree;
			}
			return new InvalidTree();
		}

		Block bottom = block;
		Block top = block;
		if (Utils.mcMMOTreeFeller(player)) {
			debug.i("MCMMO Tree Feller!");
			if (plugin.isForceAutoDestroy()) {
				resultTree.findYourBlocks(block);
				debug.i("But still, remove later, maybe");
				if (resultTree.isValid()) {
					resultTree.removeLater();
					debug.i("Not maybe. For sure!");
				}
				return resultTree;
			}
			return new InvalidTree();
		}
	
		if (!plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
			bottom = resultTree.getBottom(block);
		}
		top = resultTree.getTop(block);
		if (bottom == null) {
			debug.i("bottom is null!");
			return new InvalidTree();// not a valid tree
		}
		
		if (plugin.getConfig().getBoolean("Main.Automatic Tree Destruction")) {
			if (top == null) {
				debug.i("and not a tree anyways...");
				return new InvalidTree(); // not a valid tree
			}
			if (top.getY() - bottom.getY() < 3) {
				debug.i("and too short anyways...");
				return new InvalidTree(); // not a valid tree
			}
		}

		resultTree.getTrunks();

		boolean success = false;
		boolean damage = false;
	
		if (!event.isCancelled()
				&& plugin.getConfig().getBoolean("Main.Automatic Tree Destruction")) {
			if (plugin.getConfig()
					.getBoolean("Tools.Tree Destruction Require Tools")) {
				ItemStack inHand = player.getItemInHand();
				if (!Utils.isRequiredTool(inHand)) {
					debug.i("Player has not the right tool!");
					if (plugin.isForceAutoDestroy()) {
						resultTree.findYourBlocks(block);
						debug.i("But still, remove later, maybe");
						if (resultTree.isValid()) {
							resultTree.removeLater();
							debug.i("Not maybe. For sure!");
						}
						return resultTree;
					}
					return new InvalidTree();
				}
			}
	
			String[] directions = { "NORTH", "SOUTH", "EAST", "WEST",
					"NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST" };
			
			for (int x = 0; x < directions.length; x++) {
				if (!Utils.validTypes.contains(block.getRelative(
						BlockFace.valueOf(directions[x])).getTypeId())) {
					if (!(block.getRelative(BlockFace.valueOf(directions[x]))
							.getTypeId() == 17 && block.getData() == 3)) {
						return new InvalidTree(); // not a valid tree
					}
				}
			}
	
			if (!plugin.playerList.contains(player.getName())) {
				success = resultTree.willBeDestroyed();
				damage = plugin.getConfig().getBoolean("Main.Apply Full Tool Damage");
			}
		}
		
	
		if (plugin.getConfig().getBoolean("Main.Sapling Replant")
				&& !event.isCancelled()
				&& (resultTree.willReplant())) {
			
			if (!(plugin.getConfig().getBoolean("Main.Use Permissions")
					|| player.hasPermission("treeassist.replant"))) {
				
				if (plugin.getConfig()
						.getBoolean("Tools.Sapling Replant Require Tools")) {
					ItemStack inHand = player.getItemInHand();
					if (!Utils.isRequiredTool(inHand)) {
						if (plugin.isForceAutoDestroy()) {
							resultTree.findYourBlocks(block);
							if (resultTree.isValid()) {
								resultTree.removeLater();
							}
							return resultTree;
						}
						return new InvalidTree();
					}
				}
				int delay = plugin.getConfig()
						.getInt("Delay until Sapling is replanted (seconds) (minimum 1 second)");
				if (delay < 1) {
					delay = 1;
				}
				if (block == bottom) {
					// block is bottom
					resultTree.handleSaplingReplace(delay);
				} else if (!plugin.getConfig().getBoolean(
								"Sapling Replant.Bottom Block has to be Broken First")) {
					// block is not bottom, but not needed
					resultTree.handleSaplingReplace(delay);
				} // else: no sapling, because bottom block was needed and wasnt destroyed
			}
		}
		if (success) {
			debug.i("success!");
			event.setCancelled(true);
	
			if (player.getItemInHand().getDurability() > player.getItemInHand()
					.getType().getMaxDurability()
					&& Utils.isVanillaTool(player.getItemInHand())) {
				player.setItemInHand(new ItemStack(0));
			}
			resultTree.findYourBlocks(block);
			if (resultTree.isValid()) {
				debug.i("removing...");
				resultTree.removeLater(player, damage);
				return resultTree;
			}
			debug.i("... but invalid -.-");
			return new InvalidTree();
		}
		debug.i("no success!");
		if (plugin.isForceAutoDestroy()) {
			resultTree.findYourBlocks(block);
			debug.i("But still, remove later, maybe");
			if (resultTree.isValid()) {
				resultTree.removeLater();
				debug.i("Not maybe. For sure!");
			}
			return resultTree;
		}
		return new InvalidTree();
	}

	abstract protected List<Block> calculate(Block bottom, Block top);
	abstract protected boolean checkFail(Block block);
	abstract protected void checkBlock(List<Block> list, Block block,
			Block top, boolean deep, byte origData);
	abstract protected Block getBottom(Block block);
	abstract protected Block getTop(Block block);
	abstract protected void getTrunks();
	abstract protected void handleSaplingReplace(int delay);
	abstract protected boolean hasPerms(Player player);
	abstract protected int isLeaf(Block block);
	abstract protected boolean willBeDestroyed();
	abstract protected boolean willReplant();

	abstract public boolean isValid();

	/**
	 * Break a block and apply damage to the tool
	 * 
	 * @param block
	 *            the block to break
	 * @param tool
	 *            the item held to break with
	 * @param player
	 *            the breaking player
	 */
	private void breakBlock(final Block block, final ItemStack tool,
			final Player player) {

		boolean leaf = isLeaf(block)>0;

		if (!leaf && Utils.plugin.mcMMO && player != null) {
			Utils.mcMMOaddExp(player, block);
		}
		block.breakNaturally(tool);

		if (!leaf && tool != null && player != null) {
			if (Utils.toolgood.contains(player.getItemInHand().getTypeId())) {
				player.getItemInHand().setDurability(
						(short) (player.getItemInHand().getDurability() + 1));
			} else if (Utils.toolbad.contains(player.getItemInHand().getTypeId())) {
				player.getItemInHand().setDurability(
						(short) (player.getItemInHand().getDurability() + 2));
			}
		}
	}
	
	/**
	 * remove the blocks being removed from the total blocks list
	 * 
	 * @param removeBlocks
	 *            the blocks being removed
	 * @param totalBlocks
	 *            all blocks
	 */
	private void removeRemovals(List<Block> removeBlocks,
			List<Block> totalBlocks) {
		for (Block block : removeBlocks) {
			totalBlocks.remove(block);
		}
	}
	
	protected void findYourBlocks(Block block) {
		bottom = getBottom(block);
		top = getTop(block);

		totalBlocks = new ArrayList<Block>();

		if (bottom == null) {
			debug.i("bottom null!");
			removeBlocks = new ArrayList<Block>();
			return;
		}
		if (top == null) {
			debug.i("top null!");
			removeBlocks = new ArrayList<Block>();
			return;
		}
		valid = true;
	}
	
	protected boolean leafCheck(final Block block) {
		if (block.getData() > 2) {
			return true;
		}

		int total = 0;

		for (int x = -1; x < 2; x++) {
			for (int z = -1; z < 2; z++) {
				for (int y = -1; y < 1; y++) {
					total += isLeaf(block.getRelative(x, y, z));
				}
			}
			if (total > 3) {
				return true;
			}
		}

		return total > 3;
	}

	protected void removeLater() {
		removeBlocks = calculate(bottom, top);

		final int delay = Utils.plugin.getConfig()
				.getInt("Automatic Tree Destruction.Initial Delay (seconds)") * 20;
		final int offset = Utils.plugin.getConfig()
				.getInt("Automatic Tree Destruction.Delay (ticks)");

		class RemoveRunner extends BukkitRunnable {

			@Override
			public void run() {
				for (Block block : removeBlocks) {
					block.breakNaturally();
					removeBlocks.remove(block);
					return;
				}
				try {
					this.cancel();
				} catch (Exception e) {

				}
			}

		}

		Bukkit.getScheduler().runTaskTimer(Utils.plugin, new RemoveRunner(), delay,
				offset + 1);
	}

	protected void removeLater(final Player player, final boolean damage) {
		if (!valid) {
			(new Exception("invalid tree!!")).printStackTrace();
			return;
		}

		// valid tree, first calculate all blocks to remove
		if (removeBlocks.size() == 0) {
			removeBlocks = calculate(bottom, top);
		}

		if (totalBlocks.size() == 0) {
			totalBlocks = (bottom == getBottom(bottom)) ? new ArrayList<Block>()
					: calculate(getBottom(bottom), top);
		}
		
		if (totalBlocks.size() > 1) {
			removeRemovals(removeBlocks, totalBlocks);
		}

		final int delay = Utils.plugin.getConfig()
				.getInt("Automatic Tree Destruction.Initial Delay (seconds)") * 20;
		final int offset = Utils.plugin.getConfig()
				.getInt("Automatic Tree Destruction.Delay (ticks)");
		
		final ItemStack tool = (damage && player.getGameMode() != GameMode.CREATIVE) ? player.getItemInHand() : null;

		class InstantRunner extends BukkitRunnable {

			@Override
			public void run() {
				for (Block block : removeBlocks) {
					if (tool == null) {
						block.breakNaturally();
					} else {
						breakBlock(block, tool, player);
					}
					removeBlocks.remove(block);
					return;
				}
				try {
					this.cancel();
				} catch (Exception e) {

				}
			}

		}

		Bukkit.getScheduler().runTaskTimer(Utils.plugin, new InstantRunner(), offset,
				offset);

		class CleanRunner extends BukkitRunnable {

			@Override
			public void run() {
				for (Block block : totalBlocks) {
					breakBlock(block, null, null);
					totalBlocks.remove(block);
					return;
				}
				try {
					this.cancel();
				} catch (Exception e) {

				}
			}

		}

		Bukkit.getScheduler().runTaskTimer(Utils.plugin, new CleanRunner(), delay,
				offset);
	}

	public boolean contains(Block block) {
		return removeBlocks.contains(block) || totalBlocks.contains(block);
	}

}
