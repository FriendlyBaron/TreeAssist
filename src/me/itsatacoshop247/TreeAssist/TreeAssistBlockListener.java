package me.itsatacoshop247.TreeAssist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.gmail.nossr50.datatypes.AbilityType;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.util.Skills;
import com.gmail.nossr50.util.Users;

public class TreeAssistBlockListener implements Listener  
{
	public TreeAssist plugin;
	
	public int derp = 0;
	
	public List<Integer> toolgood = Arrays.asList(271, 275, 258, 286,279);
	public List<Integer> toolbad = Arrays.asList(256,257,267,268,269,270,272,273,274,276,277,278,283,284,285,290,291,292,293,294);
	
	public TreeAssistBlockListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		if(plugin.config.getBoolean("Leaf Decay.Fast Leaf Decay") && plugin.Enabled)
		{
			Block block = event.getBlock();
			World world = block.getWorld();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(world.getName()))
				{
					return;
				}
			}
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();
			
			leafBreak(world.getBlockAt(x-1, y+1, z+1));
			leafBreak(world.getBlockAt(x, y+1, z+1));
			leafBreak(world.getBlockAt(x+1, y+1, z+1));
			leafBreak(world.getBlockAt(x+1, y+1, z));
			leafBreak(world.getBlockAt(x+1, y+1, z-1));
			leafBreak(world.getBlockAt(x, y+1, z-1));
			leafBreak(world.getBlockAt(x-1, y+1, z-1));
			leafBreak(world.getBlockAt(x-1, y+1, z));
			leafBreak(world.getBlockAt(x, y+1, z));
			
			leafBreak(world.getBlockAt(x-1, y, z+1));
			leafBreak(world.getBlockAt(x, y, z+1));
			leafBreak(world.getBlockAt(x+1, y, z+1));
			leafBreak(world.getBlockAt(x+1, y, z));
			leafBreak(world.getBlockAt(x+1, y, z-1));
			leafBreak(world.getBlockAt(x, y, z-1));
			leafBreak(world.getBlockAt(x-1, y, z-1));
			leafBreak(world.getBlockAt(x-1, y, z));

			leafBreak(world.getBlockAt(x-1, y-1, z+1));
			leafBreak(world.getBlockAt(x, y-1, z+1));
			leafBreak(world.getBlockAt(x+1, y-1, z+1));
			leafBreak(world.getBlockAt(x+1, y-1, z));
			leafBreak(world.getBlockAt(x+1, y-1, z-1));
			leafBreak(world.getBlockAt(x, y-1, z-1));
			leafBreak(world.getBlockAt(x-1, y-1, z-1));
			leafBreak(world.getBlockAt(x-1, y-1, z));
			leafBreak(world.getBlockAt(x, y-1, z));
		}
		
		
		/*
		if(plugin.config.getBoolean("Leaf Decay.Enable Custom Drops"))
		{
			List<?> listOfDrops = plugin.config.getList("Leaf Decay.Drops");
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
	
	private void leafBreak(Block blockAt) 
	{
		if(blockAt.getTypeId() == 18)
		{
			blockAt.breakNaturally();
			World world = blockAt.getWorld();
			int x = blockAt.getX();
			int y = blockAt.getY();
			int z = blockAt.getZ();
			for(int x2 = -8; x2 < 9; x2++)
			{
				for(int z2 = -8; z2 < 9; z2++)
				{
					FloatingLeaf(world.getBlockAt(x+x2, y+2, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y+1, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y-1, z+z2));
					FloatingLeaf(world.getBlockAt(x+x2, y-2, z+z2));
				}
			}
			
		}
	}

	private void FloatingLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() != 18)
		{
			return;
		}
		World world = blockAt.getWorld();
		int x = blockAt.getX();
		int y = blockAt.getY();
		int z = blockAt.getZ();
		
		int fail = 0;
		
		fail+=Air(world.getBlockAt(x-1, y+1, z+1));
		fail+=Air(world.getBlockAt(x, y+1, z+1));
		fail+=Air(world.getBlockAt(x+1, y+1, z+1));
		fail+=Air(world.getBlockAt(x+1, y+1, z));
		fail+=Air(world.getBlockAt(x+1, y+1, z-1));
		fail+=Air(world.getBlockAt(x, y+1, z-1));
		fail+=Air(world.getBlockAt(x-1, y+1, z-1));
		fail+=Air(world.getBlockAt(x-1, y+1, z));
		fail+=Air(world.getBlockAt(x, y+1, z));
		
		fail+=Air(world.getBlockAt(x-1, y, z+1));
		fail+=Air(world.getBlockAt(x, y, z+1));
		fail+=Air(world.getBlockAt(x+1, y, z+1));
		fail+=Air(world.getBlockAt(x+1, y, z));
		fail+=Air(world.getBlockAt(x+1, y, z-1));
		fail+=Air(world.getBlockAt(x, y, z-1));
		fail+=Air(world.getBlockAt(x-1, y, z-1));
		fail+=Air(world.getBlockAt(x-1, y, z));

		fail+=Air(world.getBlockAt(x-1, y-1, z+1));
		fail+=Air(world.getBlockAt(x, y-1, z+1));
		fail+=Air(world.getBlockAt(x+1, y-1, z+1));
		fail+=Air(world.getBlockAt(x+1, y-1, z));
		fail+=Air(world.getBlockAt(x+1, y-1, z-1));
		fail+=Air(world.getBlockAt(x, y-1, z-1));
		fail+=Air(world.getBlockAt(x-1, y-1, z-1));
		fail+=Air(world.getBlockAt(x-1, y-1, z));
		fail+=Air(world.getBlockAt(x, y-1, z));
		
		if(fail < 5)
		{
			blockAt.breakNaturally();
		}
	}

	private int Air(Block blockAt) 
	{
		if(blockAt.getTypeId() == 0 || blockAt.getTypeId() == 106)
		{
			return 0;
		}
		else if(blockAt.getTypeId() == 17)
		{
			return 5;
		}
		else
		{
			return 1;
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(plugin.config.getBoolean("Main.Ignore User Placed Blocks") && event.getBlock().getTypeId() == 17)
		{
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(event.getBlock().getWorld().getName()))
				{
					return;
				}
			}
			Block block = event.getBlock();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.data.getList("Blocks");
			list.add("" + block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName());
			plugin.data.set("Blocks", list);
			plugin.saveData();
		}
	}
	
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if(plugin.config.getBoolean("Sapling Replant.Replant When Tree Burns Down") && plugin.Enabled)
		{
			Block block = event.getBlock();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(block.getWorld().getName()))
				{
					return;
				}
			}
			if(block.getType() == Material.LOG) 
			{
				Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
				Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
				if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
				{
					if(oneabove.getType() == Material.AIR || oneabove.getType() == Material.LOG)
					{
						Runnable b = new TreeAssistReplant(plugin, block, block.getData());
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
					}
				}	
			}
		}
	}
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event)
	{
		if(plugin.config.getBoolean("Sapling Replant.Replant When Tree Burns Down") && plugin.Enabled)
		{
			Block block = event.getBlock();
			if(plugin.config.getBoolean("Worlds.Enable Per World"))
			{
				if(!plugin.config.getList("Worlds.Enabled Worlds").contains(block.getWorld().getName()))
				{
					return;
				}
			}
			if(block.getType() == Material.LOG) 
			{
				Block onebelow = event.getBlock().getRelative(BlockFace.DOWN, 1);
				Block oneabove = event.getBlock().getRelative(BlockFace.UP, 1);
				if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS)
				{
					if(oneabove.getType() == Material.LOG)
					{
						Runnable b = new TreeAssistReplant(plugin, block, block.getData());
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
					}
				}	
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{		
		Block block = event.getBlock();
		Block bottom = block;
		Block top = block;
		Player player = event.getPlayer();
		Block[] blocksToRemove  = new Block[400];
		World world = player.getWorld();
		int typeid = block.getTypeId();
		byte data = block.getData();
		Block[] jungle = new Block[4];
		int j = 1;
		boolean success = true;
		
		if(plugin.config.getBoolean("Worlds.Enable Per World"))
		{
			if(!plugin.config.getList("Worlds.Enabled Worlds").contains(world.getName()))
			{
				return;
			}
		}
		
		if(plugin.config.getBoolean("Main.Ignore User Placed Blocks"))
		{
			String check = "" + block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName();
			List<String> list = new ArrayList<String>();
			list = (List<String>) plugin.data.getList("Blocks");
			
			if(list.contains(check))
			{
				plugin.data.getList("Blocks").remove(check);
				plugin.saveData();
				return;
			}
		}
		
		if(typeid != 17)
		{
			if(typeid == 6)
			{
				if(plugin.blockList.contains(block.getLocation()))
				{
					player.sendMessage(ChatColor.GREEN + "This sapling is protected!");
					event.setCancelled(true);
				}
			}
			else if(typeid == 2 || typeid == 3 || typeid == 82)
			{
				if(plugin.blockList.contains(block.getRelative(BlockFace.UP, 1).getLocation()))
				{
					player.sendMessage(ChatColor.GREEN + "This sapling is protected!");
					event.setCancelled(true);
				}
			}
			else if(typeid != 6 && plugin.blockList.contains(block.getLocation()))
			{
				plugin.blockList.remove(block.getLocation());
			}
			
			return;
		}
		
		if(plugin.getWorldGuard() != null)
		{
			if(!plugin.getWorldGuard().canBuild(player, block))
			{
				return;
			}
		}
		
		if(this.mcMMOTreeFeller(player))
		{
			return;
		}
		
		bottom = getBottom(block);
		top = getTop(block);
		if(bottom == null)
		{
			return;
		}
		if(plugin.config.getBoolean("Main.Automatic Tree Destruction"))
		{
			if(top == null)
			{
				return;
			}
			if(top.getY() - bottom.getY() < 3)
			{
				return;
			}
		}
		if(data == 3)
		{
			jungle[0] = bottom;
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ());
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ());
				j++;
			}
			if(world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()-1);
				j++;
			}
			if(world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX(), bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()-1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()+1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()+1, bottom.getY(), bottom.getZ()+1);
				j++;
			}
			if(world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()-1).getTypeId() == 17 && j < 4)
			{
				jungle[j] = world.getBlockAt(bottom.getX()-1, bottom.getY(), bottom.getZ()-1);
				j++;
			}
		}
		
		if(!event.isCancelled() && plugin.config.getBoolean("Main.Automatic Tree Destruction") && ((player.hasPermission("treeassist.autoremove") || (!plugin.config.getBoolean("Main.Use Permissions")))))
		{
			if(plugin.config.getBoolean("Tools.Tree Destruction Require Tools"))
			{
				ItemStack inHand = player.getItemInHand();
				List<?> fromConfig = plugin.config.getList("Tools.Tools List");
				if(!fromConfig.contains(inHand.getType().toString()))
				{
					return;
				}
			}
				
			String[] directions = {"NORTH", "SOUTH", "EAST", "WEST", "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST"};
			List<Integer> validTypes = Arrays.asList(0, 2, 3, 6, 8, 9, 18, 37, 38, 39, 40, 31, 32, 83, 106, 111, 78, 12, 50); //if it's not one of these blocks, it's safe to assume its a house/building
			for(int x = 0; x < directions.length; x++)
			{
				if(!validTypes.contains(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId()))
				{
					if(!(block.getRelative(BlockFace.valueOf(directions[x])).getTypeId() == 17 && block.getData() == 3))
					{
						return; 
					}
				}
			}
				
			if(!plugin.playerList.contains(player.getName()))
			{
				byte blockdata = block.getData();
				if((blockdata == 1 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Spruce")) || (blockdata == 2 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Birch"))) //simpler calculation for birch and pine trees :D
				{
					blocksToRemove[0] = bottom;
					Block NextBlockUp = world.getBlockAt(bottom.getX(), bottom.getY() + 1, bottom.getZ());
					for(int q = 1; NextBlockUp.getTypeId() == 17; q++)
					{
						blocksToRemove[q] = NextBlockUp;
						NextBlockUp = world.getBlockAt(NextBlockUp.getX(), NextBlockUp.getY() + 1, NextBlockUp.getZ());
					}
					
					int total = removeBlocks(blocksToRemove, player);
					success = true;
					if(plugin.config.getBoolean("Main.Apply Full Tool Damage"))
					{
						int type = player.getItemInHand().getTypeId();
						if(type == 258 || type == 271 || type == 275 || type == 279 || type == 286)
						{
							player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+total));
						}
					}
				}
				if((blockdata == 0 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Oak")) || (blockdata == 3 && plugin.config.getBoolean("Automatic Tree Destruction.Tree Types.Jungle")) || blockdata > 3) //ugly branch messes
				{
					if(plugin.config.getBoolean("Main.Apply Full Tool Damage"))
					{
						if(player.getItemInHand() != null)
						{
							if(this.toolgood.contains(player.getItemInHand().getTypeId()))
							{
								player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() - 1));
								checkBlock(bottom, top, player.getItemInHand(), player);
								success = true;
							}
							else if(this.toolbad.contains(player.getItemInHand().getTypeId()))
							{
								player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() - 2));
								checkBlock(bottom, top, player.getItemInHand(), player);
								success = true;
							}
							else
							{
								checkBlock(bottom, top, null, player);
								success = true;
							}
						}
					}
					else
					{
						checkBlock(bottom, top, null, player);
						success = true;
					}
					
					if(blockdata == 3)
					{
						int x = bottom.getX();
						int y = bottom.getY();
						int z = bottom.getZ();
						
						logBreak(world.getBlockAt(x-1, y-1, z+1));
						logBreak(world.getBlockAt(x, y-1, z+1));
						logBreak(world.getBlockAt(x+1, y-1, z+1));
						logBreak(world.getBlockAt(x+1, y-1, z));
						logBreak(world.getBlockAt(x+1, y-1, z-1));
						logBreak(world.getBlockAt(x, y-1, z-1));
						logBreak(world.getBlockAt(x-1, y-1, z-1));
						logBreak(world.getBlockAt(x-1, y-1, z));
						logBreak(world.getBlockAt(x, y-1, z));
					}
				}
			}
		}
		
		if(plugin.config.getBoolean("Main.Sapling Replant") && !event.isCancelled()) 
		{
			if((plugin.config.getBoolean("Main.Use Permissions") && player.hasPermission("treeassist.replant")) ||  !(plugin.config.getBoolean("Main.Use Permissions")))
			{
				if(plugin.config.getBoolean("Tools.Sapling Replant Require Tools"))
				{
					ItemStack inHand = player.getItemInHand();
					List<?> fromConfig = plugin.config.getList("Tools.Tools List");
					if(!fromConfig.contains(inHand.getType().toString()))
					{
						return;
					}
				}
				if(plugin.config.getBoolean("Main.Automatic Tree Destruction"))
				{
					if(block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.DIRT || block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.GRASS || block.getWorld().getBlockAt(bottom.getX(), bottom.getY() - 1, bottom.getZ()).getType() == Material.CLAY)
					{
						Runnable b = new TreeAssistReplant(plugin, bottom, data);
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
						
						if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
						{
							plugin.blockList.add(bottom.getLocation());
							Runnable X = new TreeAssistProtect(plugin, bottom.getLocation());
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
						}
						if(data == 3 && j > 1)
						{
							for(int rts = 1; rts < 4; rts++)
							{
								if(jungle[rts] != null)
								{
									Runnable t = new TreeAssistReplant(plugin, jungle[rts], data);
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20);
								
									if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
									{
										plugin.blockList.add(jungle[rts].getLocation());
										Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
								}
							}
						}
					}
				}
				else
				{
					Block onebelow1 = event.getBlock().getRelative(BlockFace.DOWN, 1);
					Block oneabove1 = event.getBlock().getRelative(BlockFace.UP, 1);
					if(onebelow1.getType() == Material.DIRT || onebelow1.getType() == Material.GRASS || onebelow1.getType() == Material.CLAY)
					{
						if(!plugin.getConfig().getBoolean("Sapling Replant.Bottom Block has to be Broken First"))
						{
							Runnable b = new TreeAssistReplant(plugin, block, data);
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
							
							if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
							{
								plugin.blockList.add(block.getLocation());
								Runnable X = new TreeAssistProtect(plugin, block.getLocation());
								
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
							}
							if(block.getData() == 3 && j == 4)
							{
								for(int rts = 1; rts < 4; rts++)
								{
									if(jungle[rts] != null && jungle[rts].getTypeId() == 0)
									{
										Runnable t = new TreeAssistReplant(plugin, jungle[rts], data);
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20);
									
										if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
										{
											plugin.blockList.add(jungle[rts].getLocation());
											Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
											plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
										}
									}
								}
							}
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
									Runnable b = new TreeAssistReplant(plugin, block, data);
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, b, 20);
									if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
									{
										plugin.blockList.add(block.getLocation());
										Runnable X = new TreeAssistProtect(plugin, block.getLocation());
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
									}
									if(block.getData() == 3 && j == 4)
									{
										for(int rts = 1; rts < 4; rts++)
										{
											if(jungle[rts] != null && jungle[rts].getTypeId() == 0)
											{
												Runnable t = new TreeAssistReplant(plugin, jungle[rts], data);
												plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, t, 20);
											
												if(plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0)
												{
													plugin.blockList.add(jungle[rts].getLocation());
													Runnable X2 = new TreeAssistProtect(plugin, jungle[rts].getLocation());
													plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, X2, 20*plugin.config.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
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
		if(success)
		{
			event.setCancelled(true);
		}
	}

	private void logBreak(Block blockAt) 
	{
		if(blockAt.getTypeId() == 17)
		{
			blockAt.breakNaturally();
		}	
	}

	private void checkBlock(Block block, Block Top, ItemStack tool, Player p) 
	{
		if(block.getTypeId() != 17)
		{
			if(block.getTypeId() == 18 && plugin.config.getBoolean("Leaf Decay.Fast Leaf Decay"))
			{
				block.breakNaturally();
				return;
			}
			else
			{
				return;
			}
		}
		
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		World world = block.getWorld();
		
		if(world.getBlockAt(x, y+1, z).getTypeId() == 17 || world.getBlockAt(x, y-1, z).getTypeId() == 17) //might be a trunk
		{
			if(block.getX() != Top.getX() && block.getZ() != Top.getZ())
			{
				if(block.getData() < 3)
				{
					int failCount = 0;
					for(int cont = -4; cont < 5; cont++)
					{
						if(world.getBlockAt(x, y+cont, z).getTypeId() == 17)
						{
							failCount++;
						}
					}
					if(failCount > 3)
					{
						return;
					}
				}
				else 
				{
					boolean diff = true;
					for(int Cx = -1; Cx < 2; Cx++)
					{
						for(int Cz = -1; Cz < 2; Cz++)
						{
							if(block.getX()-Cx == Top.getX() && block.getZ()-Cz == Top.getZ())
							{
								diff = false;
								Cx = 2;
								Cz = 2;
							}
						}
					}
					if(diff)
					{
						int failCount = 0;
						for(int cont = -4; cont < 5; cont++)
						{
							if(world.getBlockAt(x, y+cont, z).getTypeId() == 17)
							{
								failCount++;
							}
						}
						if(failCount > 3)
						{
							return;
						}
					}
				}
			}
		}
		
		breakBlock(block, tool, p);
			
		if(block.getData() == 3)
		{
			checkBlock(world.getBlockAt(x-2, y+1, z-2), Top, tool, p);
			checkBlock(world.getBlockAt(x-1, y+1, z-2), Top, tool, p);
			checkBlock(world.getBlockAt(x, y+1, z-2), Top, tool, p);
			checkBlock(world.getBlockAt(x+1, y+1, z-2), Top, tool, p);
			checkBlock(world.getBlockAt(x+2, y+1, z-2), Top, tool, p);
			checkBlock(world.getBlockAt(x+2, y+1, z-1), Top, tool, p);
			checkBlock(world.getBlockAt(x+2, y+1, z), Top, tool, p);
			checkBlock(world.getBlockAt(x+2, y+1, z+1), Top, tool, p);
			checkBlock(world.getBlockAt(x+2, y+1, z+2), Top, tool, p);
			checkBlock(world.getBlockAt(x+1, y+1, z+2), Top, tool, p);
			checkBlock(world.getBlockAt(x, y+1, z+2), Top, tool, p);
			checkBlock(world.getBlockAt(x-1, y+1, z+2), Top, tool, p);
			checkBlock(world.getBlockAt(x-2, y+1, z+2), Top, tool, p);
			checkBlock(world.getBlockAt(x-2, y+1, z+1), Top, tool, p);
			checkBlock(world.getBlockAt(x-2, y+1, z), Top, tool, p);
			checkBlock(world.getBlockAt(x-2, y+1, z-1), Top, tool, p);
		}
		checkBlock(world.getBlockAt(x-1, y, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x, y, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y, z), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x, y, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x-1, y, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x-1, y, z), Top, tool, p);
		
		checkBlock(world.getBlockAt(x-1, y+1, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x, y+1, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y+1, z+1), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y+1, z), Top, tool, p);
		checkBlock(world.getBlockAt(x+1, y+1, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x, y+1, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x-1, y+1, z-1), Top, tool, p);
		checkBlock(world.getBlockAt(x-1, y+1, z), Top, tool, p);
		checkBlock(world.getBlockAt(x, y+1, z), Top, tool, p);
	}

	private void breakBlock(Block block, ItemStack tool, Player play) 
	{
		if(plugin.mcMMO)
		{
			this.mcMMOFake(play, block);
		}
		block.breakNaturally();
		//ItemStack drop = new ItemStack(block.getType(), 1, block.getData());
		//block.getWorld().dropItem(block.getLocation(), drop);
		
		//block.setTypeId(0);
				
		if (tool != null) 
		{
			if(this.toolgood.contains(play.getItemInHand().getTypeId()))
			{
				play.getItemInHand().setDurability((short) (play.getItemInHand().getDurability() + 1));
			}
			else if(this.toolbad.contains(play.getItemInHand().getTypeId()))
			{
				play.getItemInHand().setDurability((short) (play.getItemInHand().getDurability() + 2));
			}
		}
	}

	private Block getTop(Block block) 
	{
		int  x = block.getX();
		int  y = block.getY();
		int  z = block.getZ();
		Block above = null;
		Block top = null;
		while(top == null)
		{
			y++;
			above = block.getWorld().getBlockAt(x, y, z);
			
			if(above.getTypeId() == 18)
			{
				top = block.getWorld().getBlockAt(x, y - 1, z);
			}
			else if(above.getTypeId() != 17)
			{
				return null;
			}
		}
		if(leafCheck(top))
		{
			return top;
		}
		else
		{
			return null;
		}
	}

	private boolean leafCheck(Block top) 
	{
		if(top.getData() > 2)
		{
			return true;
		}
		int x = top.getX();
		int y = top.getY();
		int z = top.getZ();
		World world = top.getWorld();
		int total = 0;
		
		total += isLeaf(world.getBlockAt(x-1, y, z+1));
		total += isLeaf(world.getBlockAt(x, y, z+1));
		total += isLeaf(world.getBlockAt(x+1, y, z+1));
		total += isLeaf(world.getBlockAt(x+1, y, z));
		total += isLeaf(world.getBlockAt(x+1, y, z-1));
		total += isLeaf(world.getBlockAt(x, y, z-1));
		total += isLeaf(world.getBlockAt(x-1, y, z-1));
		total += isLeaf(world.getBlockAt(x-1, y, z));

		total += isLeaf(world.getBlockAt(x-1, y-1, z+1));
		total += isLeaf(world.getBlockAt(x, y-1, z+1));
		total += isLeaf(world.getBlockAt(x+1, y-1, z+1));
		total += isLeaf(world.getBlockAt(x+1, y-1, z));
		total += isLeaf(world.getBlockAt(x+1, y-1, z-1));
		total += isLeaf(world.getBlockAt(x, y-1, z-1));
		total += isLeaf(world.getBlockAt(x-1, y-1, z-1));
		total += isLeaf(world.getBlockAt(x-1, y-1, z));
		total += isLeaf(world.getBlockAt(x, y-1, z));
		
		if(total > 3)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private int isLeaf(Block blockAt) 
	{
		if(blockAt.getTypeId() == 18)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	private Block getBottom(Block block) 
	{
		boolean found = false;
		int counter = 1;
		do
		{
			if(block.getWorld().getBlockAt(block.getX(), block.getY() - counter, block.getZ()).getTypeId() == 17)
			{
				counter++;
			}
			else
			{
				block = block.getWorld().getBlockAt(block.getX(), block.getY() - (counter - 1), block.getZ());
				found = true;
			}
		}
		while(!found);
		
		Block onebelow = block.getRelative(BlockFace.DOWN, 1);
		
		if(onebelow.getType() == Material.DIRT || onebelow.getType() == Material.GRASS || onebelow.getType() == Material.CLAY)
		{
			return block;
		}
		else
		{
			return null;
		}
	}
	
	public boolean mcMMOTreeFeller(Player player) 
	{
        boolean isMcMMOEnabled = plugin.getServer().getPluginManager().isPluginEnabled("mcMMO");
        
        if(!isMcMMOEnabled) 
        {
                return false;
        }
        
        PlayerProfile pp = Users.getProfile(player);
        if(pp.getAbilityMode(AbilityType.TREE_FELLER)) 
        {
                return true;
        } 
        else 
        {
                return false;
        }
	}

	public void mcMMOFake(Player player, Block block) 
	{
        PlayerProfile pp = Users.getProfile(player);
        Skills.monitorSkill(player, pp, 0, SkillType.WOODCUTTING);
        Plugin mcmmo = plugin.getServer().getPluginManager().getPlugin("mcMMO");
        if(block.getData() == 0) 
        {
            pp.addXP(player, SkillType.WOODCUTTING, mcmmo.getConfig().getInt("Experience.Woodcutting.Oak"));
        }
        else if(block.getData() == 1) 
        {
        	pp.addXP(player, SkillType.WOODCUTTING, mcmmo.getConfig().getInt("Experience.Woodcutting.Spruce"));
        }
        else if(block.getData() == 2) 
        {
        	pp.addXP(player, SkillType.WOODCUTTING, mcmmo.getConfig().getInt("Experience.Woodcutting.Birch"));
        }
        else if(block.getData() == 3) 
        {
        	pp.addXP(player, SkillType.WOODCUTTING, mcmmo.getConfig().getInt("Experience.Woodcutting.Jungle"));
        }
        Skills.XpCheckAll(player);
	}

	private int removeBlocks(Block[] blocksToRemove, Player player) 
	{
		int count = 0;
		for(int x = 0; x < blocksToRemove.length; x++)
		{
			if(blocksToRemove[x] != null)
			{
				if(plugin.mcMMO)
				{
					mcMMOFake(player, blocksToRemove[x]);
				}
				blocksToRemove[x].breakNaturally();
				count++;
			}
		}
		return count;
	}
}