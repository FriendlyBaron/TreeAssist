package me.itsatacoshop247.TreeAssist;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

public class TreeAssistBlockListener implements Listener  {
	public TreeAssist plugin;
	
	public TreeAssistBlockListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		Block block = event.getBlock();
		Block[] blocksToRemove  = new Block[20];
		int removeCount = 1;
		blocksToRemove[0] = block;
		int randomint = (int)(Math.random()*100);
		String[] directions = {"UP", "NORTH", "SOUTH", "EAST", "WEST", "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST", "WEST_NORTH_WEST", "NORTH_NORTH_WEST", "NORTH_NORTH_EAST", "EAST_NORTH_EAST", "EAST_SOUTH_EAST", "SOUTH_SOUTH_EAST", "SOUTH_SOUTH_WEST", "WEST_SOUTH_WEST"};
		if(plugin.getConfig().getInt("Main.Leaf Decay Rate") != 100 && plugin.getConfig().getInt("Main.Leaf Decay Rate") >= 0)
		{
			int rate = plugin.getConfig().getInt("Main.Leaf Decay Rate");
			if(rate < 100) //decresing amount of leaf decay
			{
				if(randomint > rate)
				{
					event.setCancelled(true);
					return;
				}
			}
			if(rate > 100) //increasing. I guess I can just get nearby leaf blocks? 200% would mean one more block, I assume
			{
				int modRate = rate%100; //gives leftover over 100th %
				int forSure = rate/100; //how many blocks to break for sure.
				int x = 0;
				while(modRate > 1 && x < directions.length && removeCount < forSure)
				{
					if(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId() == 18)
					{
						blocksToRemove[removeCount] = block.getRelative(BlockFace.valueOf(directions[x]));
						removeCount++;
					}
					x++;
				}
				if(modRate != 0 && x < (directions.length-1))
				{
					if(randomint < modRate)
					{
						if(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId() == 18)
						{
							blocksToRemove[removeCount] = block.getRelative(BlockFace.valueOf(directions[x]));
						}
					}
				}
				removeBlocks(blocksToRemove);
			}
			
		}
		/*
		if(plugin.getConfig().getBoolean("Leaf Decay.Enable Custom Drops"))
		{
			List<?> listOfDrops = plugin.getConfig().getList("Leaf Decay.Drops");
			String[] dropList = (String[]) listOfDrops.toArray(new String[0]);

			for(int y = 0; y < dropList.length; y++)
			{
				String[] line = dropList[y].split(";");
				if((int)(Math.random()*300) < Integer.parseInt(line[1]))
				{
					ItemStack item =  new ItemStack(Integer.parseInt(line[0]), 1);
					if(item.getTypeId() == 18 || item.getTypeId() == 17)
					{
						//MaterialData data = new MaterialData(block.getData());
						//data.setData(block.getData());
						plugin.getServer().broadcastMessage("" + item.getDurability());
						plugin.getServer().broadcastMessage("" + block.getData());
						item.setDurability(block.getData());
						
						//item.setData(data);
						plugin.getServer().broadcastMessage("" + item.getDurability());
					}
					block.getWorld().dropItem(block.getLocation(), item);
					//y = 100;
				}
			}
		}
		*/	
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		Block block = event.getBlock();
		if(block.getType() == Material.LOG) 
		{
			Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
			Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
			if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
			{
				if(oneabove.getType() == Material.AIR || oneabove.getType() == Material.LOG)
				{
					Runnable b = new TreeAssistReplant(plugin, block, block.getData());
					new Thread(b).start();
				}
			}	
		}
	}
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		Block block = event.getBlock();
		if(block.getType() == Material.LOG) 
		{
			Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
			Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
			if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
			{
				if(oneabove.getType() == Material.LOG)
				{
					Runnable b = new TreeAssistReplant(plugin, block, block.getData());
					new Thread(b).start();
				}
			}	
		}
	}
	
	@EventHandler(priority= EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}
		Block block = event.getBlock();
		Block origblock = event.getBlock();
		Player player = event.getPlayer();
		Block[] blocksToRemove  = new Block[400];
		World world = player.getWorld();
		int typeid = block.getTypeId();
		//original replace of the sapling
		if(plugin.getWorldGuard() != null)
		{
			if(!plugin.getWorldGuard().canBuild(player, block))
			{
				return;
			}
		}
		if(typeid == 17 && plugin.getConfig().getBoolean("Main.Sapling Replant") && !event.isCancelled()) 
		{
			if((plugin.getConfig().getBoolean("Main.Use Permissions") && player.hasPermission("treeassist.replant")) ||  !(plugin.getConfig().getBoolean("Main.Use Permissions")))
			{
				if(plugin.getConfig().getBoolean("Tools.Sapling Replant Require Tools"))
				{
					ItemStack inHand = player.getItemInHand();
					List<?> fromConfig = plugin.getConfig().getList("Tools.Tools List");
					if(!fromConfig.contains(inHand.getType().toString()))
					{
						return;
					}
				}
				Block onebelow1 = event.getBlock().getRelative(BlockFace.DOWN, 1);
				Block oneabove1 = event.getBlock().getRelative(BlockFace.UP, 1);
				if(onebelow1.getType() == Material.DIRT || onebelow1.getType() == Material.GRASS || onebelow1.getType() == Material.CLAY)
				{
					
					if(!plugin.getConfig().getBoolean("Sapling Replant.Bottom Block has to be Broken First"))
					{
						Runnable b = new TreeAssistReplant(plugin, origblock, origblock.getData());
						new Thread(b).start();
					}
					else
					{
						if(oneabove1.getType() == Material.LOG) 
						{
							int x = 1;
							int extrablockcount = 1;
							Block blockAbove = player.getWorld().getBlockAt(block.getX(), (block.getY() + 1), block.getZ());
							for(x = 1;x < 4; x++)
							{
								if(block.getRelative(BlockFace.NORTH, x).getTypeId() == 17 || block.getRelative(BlockFace.SOUTH, x).getTypeId() == 17 || block.getRelative(BlockFace.EAST, x).getTypeId() == 17 || block.getRelative(BlockFace.WEST, x).getTypeId() == 17)
								{
									extrablockcount++;
								}
								if(blockAbove.getRelative(BlockFace.NORTH, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.SOUTH, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.EAST, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.WEST, x).getTypeId() == 17)
								{
									extrablockcount++;
								}
							}
							if (block.getData() == 3)
							{
								extrablockcount = 0;
							}
							if(extrablockcount < 3)
							{
								Runnable b = new TreeAssistReplant(plugin, origblock, origblock.getData());
								new Thread(b).start();
							}
						}
					}
				}	
			}
		}
		if(!event.isCancelled() && (plugin.getConfig().getBoolean("Main.Automatic Tree Destruction") && (player.hasPermission("treeassist.autoremove") || (!plugin.getConfig().getBoolean("Main.Use Permissions")))))
			{
			if(plugin.getConfig().getBoolean("Tools.Tree Destruction Require Tools"))
			{
				ItemStack inHand = player.getItemInHand();
				List<?> fromConfig = plugin.getConfig().getList("Tools.Tools List");
				if(!fromConfig.contains(inHand.getType().toString()))
				{
					return;
				}
			}
			if(IsPartOfTree(block, player) && block.getTypeId() == 17 && !plugin.playerList.contains(player.getName())) //checking if its really a tree, based on nearby blocks and if it's at least 4 blocks high stack. Should fix running into buildings problem
			{
				byte blockdata = block.getData();
				if((blockdata == 1 && plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Spruce")) || (blockdata == 2 && plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Birch"))) //simpler calculation for birch and pine trees :D
				{
					blocksToRemove[0] = block;
					Block NextBlockUp = world.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
					for(int q = 1;NextBlockUp.getTypeId() == 17; q++)
					{
						blocksToRemove[q] = NextBlockUp;
						NextBlockUp = world.getBlockAt(NextBlockUp.getX(), NextBlockUp.getY() + 1, NextBlockUp.getZ());
					}
				}
				if((blockdata == 0 && plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Oak")) || (blockdata == 3 && plugin.getConfig().getBoolean("Automatic Tree Destruction.Tree Types.Jungle"))) //ugly branch messes
				{
					int y = 0;		
					int p = 0;
					int h = 0;
					int heightUp = 0;
					int heightDown = 0;
					blocksToRemove[0] = block;
					Block NextBlockUp = world.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
					for(y = 1;NextBlockUp.getTypeId() == 17; y++)
					{
						blocksToRemove[y] = NextBlockUp;
						NextBlockUp = world.getBlockAt(NextBlockUp.getX(), NextBlockUp.getY() + 1, NextBlockUp.getZ());
					}
					if(blockdata == 3)
					{
						if(block.getRelative(BlockFace.NORTH).getTypeId() == 17)
						{
							block.getRelative(BlockFace.NORTH).breakNaturally();
						}
						if(block.getRelative(BlockFace.SOUTH).getTypeId() == 17)
						{
							block.getRelative(BlockFace.SOUTH).breakNaturally();
						}
						if(block.getRelative(BlockFace.EAST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.EAST).breakNaturally();
						}
						if(block.getRelative(BlockFace.WEST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.WEST).breakNaturally();
						}
						if(block.getRelative(BlockFace.NORTH_EAST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.NORTH_EAST).breakNaturally();
						}
						if(block.getRelative(BlockFace.NORTH_WEST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.NORTH_WEST).breakNaturally();
						}
						if(block.getRelative(BlockFace.SOUTH_WEST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.SOUTH_WEST).breakNaturally();
						}
						if(block.getRelative(BlockFace.SOUTH_EAST).getTypeId() == 17)
						{
							block.getRelative(BlockFace.SOUTH_EAST).breakNaturally();
						}
					}
					for(y = 1; y < 400 ; y++)
					{
						for(p = 1; p < 4; p++)
						{
							if(blocksToRemove[y] != null)
							{
								if(blocksToRemove[y].getRelative(BlockFace.NORTH, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.EAST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.EAST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.EAST, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.WEST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.WEST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.WEST, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p);
											h = 501;
										}
									}
								}
								if(blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getTypeId() == 17)
								{
									for(h=0;h < 400; h++)
									{
										if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p))
										{
											h = 501;
										}
										if(h != 501 && blocksToRemove[h] == null)
										{
											blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p);
											h = 501;
										}
									}
								}
								for(heightUp = 1; heightUp < 3; heightUp++)
								{
									if(blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.UP, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.UP, heightUp))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.UP, heightUp);
												h = 501;
											}
										}
									}
								}
								for(heightDown = 1; heightDown < 3; heightDown++)
								{
									if(blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.DOWN, heightDown).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.EAST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.WEST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_EAST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.SOUTH_WEST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_WEST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
									if(blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.DOWN, heightUp).getTypeId() == 17)
									{
										for(h=0;h < 400; h++)
										{
											if(blocksToRemove[h] == blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.DOWN, heightDown))
											{
												h = 501;
											}
											if(h != 501 && blocksToRemove[h] == null)
											{
												blocksToRemove[h] = blocksToRemove[y].getRelative(BlockFace.NORTH_EAST, p).getRelative(BlockFace.DOWN, heightDown);
												h = 501;
											}
										}
									}
								}
							}
						}
					}
				}
				removeBlocks(blocksToRemove);
			}
		}
	}

	private void removeBlocks(Block[] blocksToRemove) 
	{
		for(int x = 0; x < blocksToRemove.length; x++)
		{
			if(blocksToRemove[x] != null)
			{
				blocksToRemove[x].breakNaturally();
			}
		}
		
	}
	
	private boolean IsPartOfTree(Block blockA, Player playerA)
	{
		int extrablockcount = 0;
		int x = 1;
		Block blockAbove = playerA.getWorld().getBlockAt(blockA.getX(), (blockA.getY() + 1), blockA.getZ());
		if(!((blockA.getRelative(BlockFace.DOWN, 1).getTypeId() == 2  || blockA.getRelative(BlockFace.DOWN, 1).getTypeId() == 3 || blockA.getRelative(BlockFace.DOWN, 1).getTypeId() == 82) && blockA.getRelative(BlockFace.UP, 1).getTypeId() == 17))
		{
			return false;
		}
		if(blockA.getData() != 3)
		{
			for(x = 1;x < 4; x++)
			{
				if(blockA.getRelative(BlockFace.NORTH, x).getTypeId() == 17 || blockA.getRelative(BlockFace.SOUTH, x).getTypeId() == 17 || blockA.getRelative(BlockFace.EAST, x).getTypeId() == 17 || blockA.getRelative(BlockFace.WEST, x).getTypeId() == 17)
				{
					extrablockcount++;
				}
				if(blockAbove.getRelative(BlockFace.NORTH, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.SOUTH, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.EAST, x).getTypeId() == 17 || blockAbove.getRelative(BlockFace.WEST, x).getTypeId() == 17)
				{
					extrablockcount++;
					if(extrablockcount > 2)
					{
						return false;
					}
				}
			}
		}
		for(x = 1; x < 4; x++) {
			if(blockA.getRelative(BlockFace.UP, x).getTypeId() != 17)
			{
				return false;
			}
		}
		return true;
	}
}