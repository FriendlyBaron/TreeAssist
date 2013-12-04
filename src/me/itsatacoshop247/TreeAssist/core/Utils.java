package me.itsatacoshop247.TreeAssist.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.itsatacoshop247.TreeAssist.TreeAssist;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;

public final class Utils {
	private Utils() {
	}

	public static List<Integer> toolgood = Arrays.asList(271, 275, 258, 286,
			279);
	public static List<Integer> toolbad = Arrays.asList(256, 257, 267, 268,
			269, 270, 272, 273, 274, 276, 277, 278, 283, 284, 285, 290, 291,
			292, 293, 294);

	public static List<Integer> validTypes = new ArrayList<Integer>(Arrays.asList(0,
			2, 3, 6, 8, 9, 18, 37, 38, 39, 40, 31, 32, 83, 106, 111,
			78, 12, 50, 66, 99, 100, 161)); // if it's not one of these blocks, it's
								// safe to assume its a house/building
	
	
	/**
	 * Check if the player has a needed tool
	 * 
	 * @param inHand
	 *            the held item
	 * @return if the player has a needed tool
	 */
	public static boolean isRequiredTool(final ItemStack inHand) {
		List<?> fromConfig = Utils.plugin.getConfig().getList("Tools.Tools List");
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

	public static boolean isVanillaTool(final ItemStack itemStack) {
		return (toolbad.contains(itemStack.getTypeId()) || toolgood
				.contains(itemStack.getTypeId()));
	}

	/**
	 * Add mcMMO exp for destroying a block
	 * 
	 * @param player
	 *            the player to give exp
	 * @param block
	 *            the block being destroyed
	 */
	public static void mcMMOaddExp(Player player, Block block) {
		Plugin mcmmo = Utils.plugin.getServer().getPluginManager().getPlugin("mcMMO");

		if (player == null) {
			return;
		}
		
		if (player.isOnline()) {
		
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
		} else {
			
			if (block.getData() == 0) {
				ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
						.getInt("Experience.Woodcutting.Oak"));
			} else if (block.getData() == 1) {
				ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
						.getInt("Experience.Woodcutting.Spruce"));
			} else if (block.getData() == 2) {
				ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
						.getInt("Experience.Woodcutting.Birch"));
			} else if (block.getData() == 3) {
				ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
						.getInt("Experience.Woodcutting.Jungle"));
			}
		}
	}

	public final static BlockFace[] NEIGHBORFACES = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,
	BlockFace.NORTH_EAST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.SOUTH_WEST};
	
	
	/**
	 * check if a player is using the tree feller ability atm
	 * 
	 * @param player
	 *            the player to check
	 * @return if a player is using tree feller
	 */
	public static boolean mcMMOTreeFeller(Player player) {
		boolean isMcMMOEnabled = Utils.plugin.getServer().getPluginManager()
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
	public static boolean replantType(byte data) {
		if (data == 0) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Oak");
		}
		if (data == 1) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Spruce");
		}
		if (data == 2) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Birch");
		}
		if (data == 3) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Jungle");
		}
		if (data == 4) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Acacia");
		}
		if (data == 5) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Dark Oak");
		}
		if (data == 99) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Brown Shroom");
		}
		if (data == 100) {
			return Utils.plugin.getConfig()
						.getBoolean("Sapling Replant.Tree Types to Replant.Red Shroom");
		}
		return false;
	}


	public static void initiateList(String string, List<Integer> validTypes) {
		for (Object obj : Utils.plugin.getConfig().getList(string)) {
			if (obj instanceof Integer) {
				validTypes.add((Integer) obj);
				continue;
			}
			if (obj.equals("LIST ITEMS GO HERE")) {
				List<Object> list = new ArrayList<Object>();
				list.add(-1);
				Utils.plugin.getConfig().set(string, list);
				Utils.plugin.saveConfig();
				break;
			}
			validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
		}
	}

	public static TreeAssist plugin;
}
