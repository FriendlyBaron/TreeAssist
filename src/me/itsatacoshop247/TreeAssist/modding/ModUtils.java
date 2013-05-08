package me.itsatacoshop247.TreeAssist.modding;

import java.util.List;

import org.bukkit.block.Block;

public final class ModUtils {
	private ModUtils() {
	}

	public static List<?> customTreeBlocks = null;
	public static List<?> customLogs = null;
	public static List<?> customSaplings = null;

	public static boolean isCustomLog(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (customLogs.contains(blockAt.getTypeId())) {
				return true;
			}
			return customLogs.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return customLogs.contains(blockAt.getTypeId());
	}

	public static boolean isCustomTreeBlock(Block blockAt) {
		if (blockAt.getData() > 0) {
			if (customTreeBlocks.contains(blockAt.getTypeId())) {
				return true;
			}
			return customTreeBlocks.contains(blockAt.getTypeId()+":"+blockAt.getData());
		}
		return customTreeBlocks.contains(blockAt.getTypeId());
	}
}
