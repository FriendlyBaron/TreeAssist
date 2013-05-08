package me.itsatacoshop247.TreeAssist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.itsatacoshop247.TreeAssist.modding.ModUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;

public class TreeAssistTree {
	static TreeAssist plugin;
	final TreeAssistBlockListener listener;
	private static List<Integer> toolgood = Arrays.asList(271, 275, 258, 286,
			279);
	private static List<Integer> toolbad = Arrays.asList(256, 257, 267, 268,
			269, 270, 272, 273, 274, 276, 277, 278, 283, 284, 285, 290, 291,
			292, 293, 294);

	private final List<Block> removeBlocks;
	private final List<Block> totalBlocks;

	public static TreeAssistTree calculate(TreeAssist plugin,
			TreeAssistBlockListener listener, BlockBreakEvent event) {

		Block block = event.getBlock();
		Block bottom = block;
		Block top = block;
		Player player = event.getPlayer();
		World world = player.getWorld();
		int typeid = block.getTypeId();
		byte data = block.getData();
		Block[] jungle = new Block[4];
		int j = 1;
		boolean success = false;
		boolean damage = false;

		if (plugin.config.getBoolean("Worlds.Enable Per World")) {
			if (!plugin.config.getList("Worlds.Enabled Worlds").contains(
					world.getName())) {
				return null;
			}
		}

		if (!hasPerms(player, data)) {
			// no perms to use TA, still could be a tree

			if (plugin.config.getBoolean("Main.Automatic Tree Destruction")
					&& plugin.config
							.getBoolean("Automatic Tree Destruction.Forced Removal")) {
				// check if it is a tree we should restore
				return new TreeAssistTree(plugin, listener, block);
			}
			return null; // don't care
		}

		if (plugin.config.getBoolean("Main.Ignore User Placed Blocks")) {
			String check = "" + block.getX() + ";" + block.getY() + ";"
					+ block.getZ() + ";" + block.getWorld().getName();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.data.getList("Blocks");

			if (list != null && list.contains(check)) {
				plugin.data.getList("Blocks").remove(check);
				plugin.saveData();
				return null; // no tree. ignore!
			}
		}

		if (typeid != 17 && !ModUtils.isCustomLog(block)) {
			if (typeid == 6) {
				if (plugin.config
						.getBoolean("Sapling Replant.Block all breaking of Saplings")) {
					player.sendMessage(ChatColor.GREEN
							+ "You cannot break saplings on this server!");
					event.setCancelled(true);
				} else if (plugin.blockList.contains(block.getLocation())) {
					player.sendMessage(ChatColor.GREEN
							+ "This sapling is protected!");
					event.setCancelled(true);
				}
			} else if (typeid == 2 || typeid == 3 || typeid == 82) {
				if (plugin.blockList.contains(block
						.getRelative(BlockFace.UP, 1).getLocation())) {
					player.sendMessage(ChatColor.GREEN
							+ "This sapling is protected!");
					event.setCancelled(true);
				}
			} else if (typeid != 6
					&& plugin.blockList.contains(block.getLocation())) {
				plugin.blockList.remove(block.getLocation());
			}
			return null; // not a log triggering anything, ignore
		}

		if (mcMMOTreeFeller(player)) {
			if (plugin.config.getBoolean("Main.Automatic Tree Destruction")
					&& plugin.config
							.getBoolean("Automatic Tree Destruction.Forced Removal")) {
				// check if it is a tree we should restore
				return new TreeAssistTree(plugin, listener, block);
			}
			return null; // don't care
		}

		if (!plugin.config.getBoolean("Main.Destroy Only Blocks Above")) {
			bottom = getBottom(block);
		}
		top = getTop(block);
		if (bottom == null) {
			return null; // not a valid tree
		}
		if (plugin.config.getBoolean("Main.Automatic Tree Destruction")) {
			if (top == null) {
				return null; // not a valid tree
			}
			if (top.getY() - bottom.getY() < 3) {
				return null; // not a valid tree
			}
		}

		if (data == 3) {
			jungle[0] = bottom;
			if (world.getBlockAt(bottom.getX() - 1, bottom.getY(),
					bottom.getZ()).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() - 1, bottom.getY(),
						bottom.getZ());
				j++;
			}
			if (world.getBlockAt(bottom.getX() + 1, bottom.getY(),
					bottom.getZ()).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() + 1, bottom.getY(),
						bottom.getZ());
				j++;
			}
			if (world.getBlockAt(bottom.getX(), bottom.getY(),
					bottom.getZ() - 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(),
						bottom.getZ() - 1);
				j++;
			}
			if (world.getBlockAt(bottom.getX(), bottom.getY(),
					bottom.getZ() + 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(),
						bottom.getZ() + 1);
				j++;
			}
			if (world.getBlockAt(bottom.getX() - 1, bottom.getY(),
					bottom.getZ() + 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() - 1, bottom.getY(),
						bottom.getZ() + 1);
				j++;
			}
			if (world.getBlockAt(bottom.getX() + 1, bottom.getY(),
					bottom.getZ() - 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() + 1, bottom.getY(),
						bottom.getZ() - 1);
				j++;
			}
			if (world.getBlockAt(bottom.getX() + 1, bottom.getY(),
					bottom.getZ() + 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() + 1, bottom.getY(),
						bottom.getZ() + 1);
				j++;
			}
			if (world.getBlockAt(bottom.getX() - 1, bottom.getY(),
					bottom.getZ() - 1).getTypeId() == 17
					&& j < 4) {
				jungle[j] = world.getBlockAt(bottom.getX() - 1, bottom.getY(),
						bottom.getZ() - 1);
				j++;
			}
		}

		if (!event.isCancelled()
				&& plugin.config.getBoolean("Main.Automatic Tree Destruction")) {
			if (plugin.config
					.getBoolean("Tools.Tree Destruction Require Tools")) {
				ItemStack inHand = player.getItemInHand();
				if (!isRequiredTool(inHand)) {
					if (plugin.config
							.getBoolean("Main.Automatic Tree Destruction")
							&& plugin.config
									.getBoolean("Automatic Tree Destruction.Forced Removal")) {
						// check if it is a tree we should restore
						return new TreeAssistTree(plugin, listener, block);
					}
					return null; // don't care
				}
			}

			String[] directions = { "NORTH", "SOUTH", "EAST", "WEST",
					"NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST" };
			List<Integer> validTypes = new ArrayList<Integer>(Arrays.asList(0,
					2, 3, 6, 8, 9, 18, 37, 38, 39, 40, 31, 32, 83, 106, 111,
					78, 12, 50, 66)); // if it's not one of these blocks, it's
										// safe to assume its a house/building
			for (Object obj : plugin.config.getList("Modding.Custom Logs")) {
				if (obj instanceof Integer) {
					validTypes.add((Integer) obj);
					continue;
				}
				if (obj.equals("LIST ITEMS GO HERE")) {
					List<Object> list = new ArrayList<Object>();
					list.add(-1);
					plugin.config.set("Modding.Custom Logs", list);
					plugin.saveConfig();
					break;
				}
				validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
			}
			for (Object obj : plugin.config
					.getList("Modding.Custom Tree Blocks")) {
				if (obj instanceof Integer) {
					validTypes.add((Integer) obj);
					continue;
				}
				if (obj.equals("LIST ITEMS GO HERE")) {
					List<Object> list = new ArrayList<Object>();
					list.add(-1);
					plugin.config.set("Modding.Custom Tree Blocks", list);
					plugin.saveConfig();
					break;
				}
				validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
			}
			for (int x = 0; x < directions.length; x++) {
				if (!validTypes.contains(block.getRelative(
						BlockFace.valueOf(directions[x])).getTypeId())) {
					if (!(block.getRelative(BlockFace.valueOf(directions[x]))
							.getTypeId() == 17 && block.getData() == 3)) {
						return null; // not a valid tree
					}
				}
			}

			if (!plugin.playerList.contains(player.getName())) {
				byte blockdata = block.getData();
				/*
				 * if((blockdata == 1 && plugin.config.getBoolean(
				 * "Automatic Tree Destruction.Tree Types.Spruce")) ||
				 * (blockdata == 2 && plugin.config.getBoolean(
				 * "Automatic Tree Destruction.Tree Types.Birch"))) //simpler
				 * calculation for birch and pine trees :D {
				 * 
				 * blocksToRemove[0] = bottom; Block NextBlockUp =
				 * world.getBlockAt(bottom.getX(), bottom.getY() + 1,
				 * bottom.getZ()); for(int q = 1; NextBlockUp.getTypeId() == 17
				 * || listener.isCustomLog(NextBlockUp); q++) {
				 * blocksToRemove[q] = NextBlockUp; NextBlockUp =
				 * world.getBlockAt(NextBlockUp.getX(), NextBlockUp.getY() + 1,
				 * NextBlockUp.getZ()); }
				 * 
				 * int total = removeBlocks(blocksToRemove, player);
				 * if(plugin.config.getBoolean("Main.Apply Full Tool Damage")) {
				 * int type = player.getItemInHand().getTypeId(); if(type == 258
				 * || type == 271 || type == 275 || type == 279 || type == 286)
				 * { player.getItemInHand().setDurability((short)
				 * (player.getItemInHand().getDurability()+total)); } } success
				 * = true;
				 * 
				 * }
				 */
				if (ModUtils.isCustomLog(bottom)
						|| (blockdata == 0 && plugin.config
								.getBoolean("Automatic Tree Destruction.Tree Types.Oak"))
						|| (blockdata == 1 && plugin.config
								.getBoolean("Automatic Tree Destruction.Tree Types.Spruce"))
						|| (blockdata == 2 && plugin.config
								.getBoolean("Automatic Tree Destruction.Tree Types.Birch"))
						|| (blockdata == 3 && plugin.config
								.getBoolean("Automatic Tree Destruction.Tree Types.Jungle"))
						|| blockdata > 3) // ugly branch messes
				{
					if (plugin.config.getBoolean("Main.Apply Full Tool Damage")) {
						// checkBlock(bottom, top, player.getItemInHand(),
						// player, top.getData());
						success = true;
						damage = true;
					} else {
						// checkBlock(bottom, top, null, player, top.getData());
						success = true;
						damage = false;
					}
					/*
					 * if(blockdata == 3 && plugin.config.getBoolean(
					 * "Automatic Tree Destruction.Tree Types.BigJungle")) { int
					 * x = bottom.getX(); int y = bottom.getY(); int z =
					 * bottom.getZ();
					 * 
					 * breakLog(world.getBlockAt(x-1, y-1, z+1));
					 * breakLog(world.getBlockAt(x, y-1, z+1));
					 * breakLog(world.getBlockAt(x+1, y-1, z+1));
					 * breakLog(world.getBlockAt(x+1, y-1, z));
					 * breakLog(world.getBlockAt(x+1, y-1, z-1));
					 * breakLog(world.getBlockAt(x, y-1, z-1));
					 * breakLog(world.getBlockAt(x-1, y-1, z-1));
					 * breakLog(world.getBlockAt(x-1, y-1, z));
					 * breakLog(world.getBlockAt(x, y-1, z)); }
					 */
				}
			}
		}

		if (plugin.config.getBoolean("Main.Sapling Replant")
				&& !event.isCancelled()
				&& (ModUtils.isCustomLog(bottom) || replantType(data))) {
			if ((plugin.config.getBoolean("Main.Use Permissions") && player
					.hasPermission("treeassist.replant"))
					|| !(plugin.config.getBoolean("Main.Use Permissions"))) {
				if (plugin.config
						.getBoolean("Tools.Sapling Replant Require Tools")) {
					ItemStack inHand = player.getItemInHand();
					if (!isRequiredTool(inHand)) {
						if (plugin.config
								.getBoolean("Main.Automatic Tree Destruction")
								&& plugin.config
										.getBoolean("Automatic Tree Destruction.Forced Removal")) {
							// check if it is a tree we should restore
							return new TreeAssistTree(plugin, listener, block);
						}
						return null; // don't care
					}
				}
				if (plugin.config.getBoolean("Main.Automatic Tree Destruction")) {
					int delay = plugin.config
							.getInt("Delay until Sapling is replanted (seconds) (minimum 1 second)");
					if (delay < 1) {
						delay = 1;
					}
					if (block
							.getWorld()
							.getBlockAt(bottom.getX(), bottom.getY() - 1,
									bottom.getZ()).getType() == Material.DIRT
							|| block.getWorld()
									.getBlockAt(bottom.getX(),
											bottom.getY() - 1, bottom.getZ())
									.getType() == Material.GRASS
							|| block.getWorld()
									.getBlockAt(bottom.getX(),
											bottom.getY() - 1, bottom.getZ())
									.getType() == Material.CLAY) {
						Runnable b = new TreeAssistReplant(plugin, bottom,
								typeid, data);

						plugin.getServer().getScheduler()
								.scheduleSyncDelayedTask(plugin, b, 20 * delay);

						if (plugin.config
								.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
							plugin.blockList.add(bottom.getLocation());
							Runnable X = new TreeAssistProtect(plugin,
									bottom.getLocation());
							plugin.getServer()
									.getScheduler()
									.scheduleSyncDelayedTask(
											plugin,
											X,
											20 * plugin.config
													.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
						}
						if (data == 3
								&& j > 1
								&& plugin.config
										.getBoolean("Sapling Replant.Tree Types to Replant.BigJungle")) {
							for (int rts = 1; rts < 4; rts++) {
								if (jungle[rts] != null) {
									Runnable t = new TreeAssistReplant(plugin,
											jungle[rts], typeid, data);
									plugin.getServer()
											.getScheduler()
											.scheduleSyncDelayedTask(plugin, t,
													20 * delay);

									if (plugin.config
											.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
										plugin.blockList.add(jungle[rts]
												.getLocation());
										Runnable X2 = new TreeAssistProtect(
												plugin,
												jungle[rts].getLocation());
										plugin.getServer()
												.getScheduler()
												.scheduleSyncDelayedTask(
														plugin,
														X2,
														20 * plugin.config
																.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
								}
							}
						}
					}
				} else {
					int delay = plugin.config
							.getInt("Delay until Sapling is replanted (seconds) (minimum 1 second)");
					if (delay < 1) {
						delay = 1;
					}
					Block onebelow1 = event.getBlock().getRelative(
							BlockFace.DOWN, 1);
					Block oneabove1 = event.getBlock().getRelative(
							BlockFace.UP, 1);
					if (onebelow1.getType() == Material.DIRT
							|| onebelow1.getType() == Material.GRASS
							|| onebelow1.getType() == Material.CLAY) {
						if (!plugin
								.getConfig()
								.getBoolean(
										"Sapling Replant.Bottom Block has to be Broken First")) {
							Runnable b = new TreeAssistReplant(plugin, block,
									typeid, data);
							plugin.getServer()
									.getScheduler()
									.scheduleSyncDelayedTask(plugin, b,
											20 * delay);

							if (plugin.config
									.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
								plugin.blockList.add(block.getLocation());
								Runnable X = new TreeAssistProtect(plugin,
										block.getLocation());

								plugin.getServer()
										.getScheduler()
										.scheduleSyncDelayedTask(
												plugin,
												X,
												20 * plugin.config
														.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
							}
							if (block.getData() == 3 && j == 4) {
								for (int rts = 1; rts < 4; rts++) {
									if (jungle[rts] != null
											&& jungle[rts].getTypeId() == 0) {
										Runnable t = new TreeAssistReplant(
												plugin, jungle[rts], typeid,
												data);
										plugin.getServer()
												.getScheduler()
												.scheduleSyncDelayedTask(
														plugin, t, 20 * delay);

										if (plugin.config
												.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
											plugin.blockList.add(jungle[rts]
													.getLocation());
											Runnable X2 = new TreeAssistProtect(
													plugin,
													jungle[rts].getLocation());
											plugin.getServer()
													.getScheduler()
													.scheduleSyncDelayedTask(
															plugin,
															X2,
															20 * plugin.config
																	.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
										}
									}
								}
							}
						} else {
							if (oneabove1.getType() == Material.LOG
									|| ModUtils.isCustomLog(oneabove1)) {
								int x = 1;
								int extrablockcount = 1;
								Block blockAbove = player.getWorld()
										.getBlockAt(block.getX(),
												(block.getY() + 1),
												block.getZ());

								int typeId = oneabove1.getTypeId();

								for (x = 1; x < 4; x++) {
									if (block.getRelative(BlockFace.NORTH, x)
											.getTypeId() == typeId
											|| block.getRelative(
													BlockFace.SOUTH, x)
													.getTypeId() == typeId
											|| block.getRelative(
													BlockFace.EAST, x)
													.getTypeId() == typeId
											|| block.getRelative(
													BlockFace.WEST, x)
													.getTypeId() == typeId) {
										extrablockcount++;
									}
									if (blockAbove.getRelative(BlockFace.NORTH,
											x).getTypeId() == typeId
											|| blockAbove.getRelative(
													BlockFace.SOUTH, x)
													.getTypeId() == typeId
											|| blockAbove.getRelative(
													BlockFace.EAST, x)
													.getTypeId() == typeId
											|| blockAbove.getRelative(
													BlockFace.WEST, x)
													.getTypeId() == typeId) {
										extrablockcount++;
									}
								}
								if (block.getData() == 3) {
									extrablockcount = 0;
								}
								if (extrablockcount < 3) {
									Runnable b = new TreeAssistReplant(plugin,
											block, typeid, data);
									plugin.getServer()
											.getScheduler()
											.scheduleSyncDelayedTask(plugin, b,
													20 * delay);
									if (plugin.config
											.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
										plugin.blockList.add(block
												.getLocation());
										Runnable X = new TreeAssistProtect(
												plugin, block.getLocation());
										plugin.getServer()
												.getScheduler()
												.scheduleSyncDelayedTask(
														plugin,
														X,
														20 * plugin.config
																.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
									if (block.getData() == 3 && j == 4) {
										for (int rts = 1; rts < 4; rts++) {
											if (jungle[rts] != null
													&& jungle[rts].getTypeId() == 0) {
												Runnable t = new TreeAssistReplant(
														plugin, jungle[rts],
														typeid, data);
												plugin.getServer()
														.getScheduler()
														.scheduleSyncDelayedTask(
																plugin, t,
																20 * delay);

												if (plugin.config
														.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
													plugin.blockList
															.add(jungle[rts]
																	.getLocation());
													Runnable X2 = new TreeAssistProtect(
															plugin,
															jungle[rts]
																	.getLocation());
													plugin.getServer()
															.getScheduler()
															.scheduleSyncDelayedTask(
																	plugin,
																	X2,
																	20 * plugin.config
																			.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if (success) {
			event.setCancelled(true);

			if (player.getItemInHand().getDurability() > player.getItemInHand()
					.getType().getMaxDurability()
					&& isVanillaTool(player.getItemInHand())) {
				player.setItemInHand(new ItemStack(0));
			}
			return new TreeAssistTree(listener, bottom, top, player, damage);
		}
		if (plugin.config.getBoolean("Main.Automatic Tree Destruction")
				&& plugin.config
						.getBoolean("Automatic Tree Destruction.Forced Removal")) {
			// check if it is a tree we should restore
			return new TreeAssistTree(plugin, listener, block);
		}
		return null; // don't care
	}

	private TreeAssistTree(TreeAssist plugin, TreeAssistBlockListener listener,
			Block block) {
		TreeAssistTree.plugin = plugin;
		this.listener = listener;

		Block bottom = getBottom(block);
		Block top = getTop(block);

		totalBlocks = new ArrayList<Block>();

		if (bottom == null || top == null) {
			removeBlocks = new ArrayList<Block>();
			return;
		}

		// valid tree, should be removed automatically after a period of time!
		removeBlocks = this.calculate(bottom, top);

		final int delay = plugin.config
				.getInt("Automatic Tree Destruction.Initial Delay (seconds)") * 20;
		final int offset = plugin.config
				.getInt("Automatic Tree Destruction.Delay (ticks)");

		class RemoveRunner extends BukkitRunnable {

			@Override
			public void run() {
				for (Block block : removeBlocks) {
					if (block.getType() == Material.LOG) {
						block.setTypeIdAndData(Material.GLASS.getId(), (byte) 0, true);
					} else if (block.getType() != Material.GLASS) {
						block.breakNaturally();
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

		Bukkit.getScheduler().runTaskTimer(plugin, new RemoveRunner(), delay,
				offset + 1);

	}

	public TreeAssistTree(final TreeAssistBlockListener listener,
			final Block bottom, final Block top, final Player player,
			boolean damage) {
		this.listener = listener;

		if (bottom == null || top == null) {
			removeBlocks = new ArrayList<Block>();
			totalBlocks = new ArrayList<Block>();
			return;
		}

		// valid tree, first calculate all blocks to remove
		removeBlocks = calculate(bottom, top);
		totalBlocks = (bottom == getBottom(bottom)) ? new ArrayList<Block>()
				: calculate(getBottom(bottom), top);

		if (totalBlocks.size() > 1) {
			removeRemovals(removeBlocks, totalBlocks);
		}

		final int delay = plugin.config
				.getInt("Automatic Tree Destruction.Initial Delay (seconds)") * 20;
		final int offset = plugin.config
				.getInt("Automatic Tree Destruction.Delay (ticks)");
		
		final ItemStack tool = (damage && player.getGameMode() != GameMode.CREATIVE) ? player.getItemInHand() : null;

		class InstantRunner extends BukkitRunnable {

			@Override
			public void run() {
				for (Block block : removeBlocks) {
					if (block.getType() == Material.LOG) {
						block.setTypeIdAndData(Material.GLASS.getId(), (byte) 0, true);
					} else if (block.getType() != Material.GLASS) {
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

		Bukkit.getScheduler().runTaskTimer(plugin, new InstantRunner(), offset,
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

		Bukkit.getScheduler().runTaskTimer(plugin, new CleanRunner(), delay,
				offset);

	}

	public boolean contains(Block block) {
		return removeBlocks.contains(block) || totalBlocks.contains(block);
	}

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
	private static void breakBlock(final Block block, final ItemStack tool,
			final Player player) {

		boolean leaf = block.getTypeId() != 17 && !ModUtils.isCustomLog(block);

		if (!leaf && plugin.mcMMO && player != null) {
			mcMMOaddExp(player, block);
		}
		block.breakNaturally();

		if (!leaf && tool != null && player != null) {
			if (toolgood.contains(player.getItemInHand().getTypeId())) {
				player.getItemInHand().setDurability(
						(short) (player.getItemInHand().getDurability() + 1));
			} else if (toolbad.contains(player.getItemInHand().getTypeId())) {
				player.getItemInHand().setDurability(
						(short) (player.getItemInHand().getDurability() + 2));
			}
		}
	}

	/**
	 * Break a log at the block's position
	 * 
	 * @param block
	 *            the block to break / private void breakLog(Block block) { if
	 *            (block.getTypeId() == 17 || ModUtils.isCustomLog(block)) {
	 *            block.breakNaturally(); } }
	 */

	/**
	 * check a block and recursively check neighbors if the block is a log
	 * 
	 * @param list
	 * 
	 * @param block
	 *            the block to check
	 * @param top
	 *            the top block
	 * @param tool
	 *            the tool breaking the tree
	 * @param player
	 *            the player breaking the tree
	 * @param firstData
	 *            the initial broken block's data
	 */
	private static void checkBlock(List<Block> list, Block block,
			Block top, boolean deep) {

		if (block.getTypeId() != 17 && !ModUtils.isCustomLog(block)) {
			if (isLeaf(block) > 0) {
				if (!list.contains(block)) {
					list.add(block);
				}
			}
			return;
		}

		if (!ModUtils.isCustomLog(block) && block.getData() != top.getData()) {
			if (top.getData() != 0 || block.getData() <= 3) {
				return;
			}
		}
		
		if (block.getX() == top.getX() && block.getZ() == top.getZ()) {
			if (!deep) {
				// something else caught the main, return, this will be done later!
				return;
			}
		}

		/*
		
		if (block.getRelative(0, 1, 0).getTypeId() == 17
				|| ModUtils.isCustomLog(block.getRelative(0, 1, 0))
				|| block.getRelative(0, 1, 0).getTypeId() == 17
				|| ModUtils.isCustomLog(block.getRelative(0, 1, 0))) { // might
																		// be a
																		// trunk
			// either one below or above is a tree block
			if (block.getX() != top.getX() && block.getZ() != top.getZ()) {

				// unneeded calculations:
				/*
				if (block.getData() < 3) {
					
					int failCount = 0;
					for (int cont = -4; cont < 5; cont++) {
						if (world.getBlockAt(x, y + cont, z).getTypeId() == 17
								|| ModUtils.isCustomLog(world.getBlockAt(x, y
										+ cont, z))) {
							failCount++;
						}
					}
					if (failCount > 3) {
						return;
					}


				} else {

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
						int failCount = 0;
						for (int cont = -4; cont < 5; cont++) {
							if (world.getBlockAt(x, y + cont, z).getTypeId() == 17
									|| ModUtils.isCustomLog(world.getBlockAt(x,
											y + cont, z))) {
								failCount++;
							}
						}
						if (failCount > 3) {
							return;
						}
					}

					// same story, the bottom block calculation makes this
					// redundant

					Bukkit.getLogger().warning("[TreeAssist] Custom log fail!");
				}* /
			}
		}*/

		boolean isBig = block.getData() == 3 && isBig(block);

		boolean destroyBig = block.getData() == 3
				&& plugin.config
						.getBoolean("Automatic Tree Destruction.Tree Types.BigJungle");

		if (!destroyBig && isBig) {
			return;
		}

		if (list.contains(block)) {
			return;
		} else {
			list.add(block);
		}
		
		final BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,
				BlockFace.NORTH_EAST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.SOUTH_WEST};

		for (BlockFace face : faces) {
			checkBlock(list, block.getRelative(face), top, false);

			checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false);
			checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false);
			if (isBig) {
				checkBlock(list, block.getRelative(face, 2), top, false);
			}
		}

		if (!deep) {
			return;
		}

		if (block.getY() > top.getY()) {
			return;
		}

		if (destroyBig) {
			checkBlock(list, block.getRelative(-2, 0, -2), top, false);
			checkBlock(list, block.getRelative(-1, 0, -2), top, false);
			checkBlock(list, block.getRelative(0, 0, -2), top, false);
			checkBlock(list, block.getRelative(1, 0, -2), top, false);
			checkBlock(list, block.getRelative(2, 0, -2), top, false);
			checkBlock(list, block.getRelative(2, 0, -1), top, false);
			checkBlock(list, block.getRelative(2, 0, 0), top, false);
			checkBlock(list, block.getRelative(2, 0, 1), top, false);
			checkBlock(list, block.getRelative(2, 0, 2), top, false);
			checkBlock(list, block.getRelative(1, 0, 2), top, false);
			checkBlock(list, block.getRelative(0, 0, 2), top, false);
			checkBlock(list, block.getRelative(-1, 0, 2), top, false);
			checkBlock(list, block.getRelative(-2, 0, 2), top, false);
			checkBlock(list, block.getRelative(-2, 0, 1), top, false);
			checkBlock(list, block.getRelative(-2, 0, 0), top, false);
			checkBlock(list, block.getRelative(-2, 0, -1), top, false);
		}
		checkBlock(list, block.getRelative(0, 1, 0), top, true);
	}

	/**
	 * calculate the bottom tree block out of a block
	 * 
	 * @param block
	 *            a log block
	 * @return the bottom block
	 */
	private static Block getBottom(final Block block) {
		int counter = 1;
		do {
			if (block.getRelative(0, 0 - counter, 0).getTypeId() == 17
					|| ModUtils.isCustomLog(block
							.getRelative(0, 0 - counter, 0))) {
				counter++;
			} else {
				return block.getRelative(0, 1 - counter, 0);
			}
		} while (block.getY() - counter > 0);

		return null;
	}

	/**
	 * Calculate the top log block of a tree
	 * 
	 * @param block
	 *            a block of the tree to check
	 * @return the top block
	 */
	private static Block getTop(final Block block) {
		Block top = null;
		int maxY = block.getWorld().getMaxHeight() + 10;
		int counter = 1;

		while (block.getY() + counter < maxY) {
			if (block.getRelative(0, counter, 0).getTypeId() == 18
					|| ModUtils.isCustomTreeBlock(block.getRelative(0, counter,
							0))) {
				top = block.getRelative(0, counter - 1, 0);
				break;
			} else {
				counter++;
			}
		}
		return (top != null && leafCheck(top)) ? top.getRelative(0, 1, 0) : null;
	}

	/**
	 * Check if a player has the permission for this type
	 * 
	 * @param player
	 *            the player to check
	 * @param data
	 *            the data value to check
	 * @return if a player has the permission for this type
	 */
	private static boolean hasPerms(final Player player, final byte data) {
		if (!plugin.config.getBoolean("Main.Use Permissions")) {
			return true;
		}
		if (data == 0) {
			return player.hasPermission("treeassist.destroy.oak");
		}
		if (data == 1) {
			return player.hasPermission("treeassist.destroy.spruce");
		}
		if (data == 2) {
			return player.hasPermission("treeassist.destroy.birch");
		}
		if (data == 3) {
			return player.hasPermission("treeassist.destroy.jungle");
		}
		return true;
	}

	/**
	 * check if a block belongs to a big tree
	 * 
	 * @param block
	 *            the block to check
	 * @return if a block belongs to a big tree
	 */
	private static boolean isBig(final Block block) {
		BlockFace field[] = { BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
				BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

		for (BlockFace face : field) {
			if (block.getRelative(face).getTypeId() == block.getTypeId()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a given block is a leaf block
	 * 
	 * @param block
	 *            the block to check
	 * @return 1 if it is, 0 otherwise
	 */
	private static int isLeaf(final Block block) {
		if (block.getTypeId() == 18 || ModUtils.isCustomTreeBlock(block)) {
			return 1;
		}
		return 0;
	}

	/**
	 * Check if the player has a needed tool
	 * 
	 * @param inHand
	 *            the held item
	 * @return if the player has a needed tool
	 */
	private static boolean isRequiredTool(final ItemStack inHand) {
		List<?> fromConfig = plugin.config.getList("Tools.Tools List");
		if (fromConfig.contains(inHand.getType().name())
				|| fromConfig.contains(inHand.getTypeId())) {
			return true;
		}

		for (Object obj : fromConfig) {
			if (!(obj instanceof String)) {
				continue; // skip item IDs
			}
			String tool = (String) obj;
			if (!tool.startsWith(inHand.getType().name())) {
				continue; // skip other names
			}

			String[] values = tool.split(":");

			if (values.length < 2) {
				return true; // no enchantment found, defaulting to plain
								// (found) name
			}

			for (Enchantment ench : inHand.getEnchantments().keySet()) {
				if (!ench.getName().equalsIgnoreCase(values[1])) {
					continue; // skip other enchantments
				}
				int level = 0;
				if (values.length < 3) {
					return true; // has correct enchantment, no level needed
				}
				try {
					level = Integer.parseInt(values[2]);
				} catch (Exception e) {
					return true; // invalid level defined, defaulting to no
									// level
				}

				if (level > inHand.getEnchantments().get(ench)) {
					continue; // enchantment too low
				}
				return true;
			}
		}

		return false;
	}

	private static boolean isVanillaTool(final ItemStack itemStack) {
		return (toolbad.contains(itemStack.getTypeId()) || toolgood
				.contains(itemStack.getTypeId()));
	}

	/**
	 * Check if the block has enough leaves around (3 needed)
	 * 
	 * @param block
	 *            the block to check
	 * @return if the block has enough leaves
	 */
	private static boolean leafCheck(final Block block) {
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

	/**
	 * Add mcMMO exp for destroying a block
	 * 
	 * @param player
	 *            the player to give exp
	 * @param block
	 *            the block being destroyed
	 */
	private static void mcMMOaddExp(Player player, Block block) {
		Plugin mcmmo = plugin.getServer().getPluginManager().getPlugin("mcMMO");

		if (block.getData() == 0) {
			ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig()
					.getInt("Experience.Woodcutting.Oak"));
		} else if (block.getData() == 1) {
			ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig()
					.getInt("Experience.Woodcutting.Spruce"));
		} else if (block.getData() == 2) {
			ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig()
					.getInt("Experience.Woodcutting.Birch"));
		} else if (block.getData() == 3) {
			ExperienceAPI.addXP(player, "Woodcutting", mcmmo.getConfig()
					.getInt("Experience.Woodcutting.Jungle"));
		}
	}

	/**
	 * check if a player is using the tree feller ability atm
	 * 
	 * @param player
	 *            the player to check
	 * @return if a player is using tree feller
	 */
	private static boolean mcMMOTreeFeller(Player player) {
		boolean isMcMMOEnabled = plugin.getServer().getPluginManager()
				.isPluginEnabled("mcMMO");

		if (!isMcMMOEnabled) {
			return false;
		}

		return AbilityAPI.treeFellerEnabled(player);
	}

	/**
	 * Should the given data be replanted?
	 * 
	 * @param data
	 *            the log data
	 * @return if a sapling should be replanted
	 */
	private static boolean replantType(byte data) {
		if (data == 0
				&& plugin.config
						.getBoolean("Sapling Replant.Tree Types to Replant.Oak")) {
			return true;
		}
		if (data == 1
				&& plugin.config
						.getBoolean("Sapling Replant.Tree Types to Replant.Spruce")) {
			return true;
		}
		if (data == 2
				&& plugin.config
						.getBoolean("Sapling Replant.Tree Types to Replant.Birch")) {
			return true;
		}
		if (data == 3
				&& plugin.config
						.getBoolean("Sapling Replant.Tree Types to Replant.Jungle")) {
			return true;
		}
		return false;
	}

	private List<Block> calculate(final Block bottom, final Block top) {
		List<Block> list = new ArrayList<Block>();
		checkBlock(list, bottom, top, true);
		return list;
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

}
