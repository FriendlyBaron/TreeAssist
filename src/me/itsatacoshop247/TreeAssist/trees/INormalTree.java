package me.itsatacoshop247.TreeAssist.trees;

import org.bukkit.block.Block;

import java.util.List;

public interface INormalTree {
    void checkBlock(List<Block> list, Block block,
                    Block top, boolean deep);
}
