package me.itsatacoshop247.TreeAssist.trees;

import org.bukkit.block.Block;

import java.util.List;

public interface ISpecialTree {

    void checkBlock(List<Block> list, Block block,
           Block top, boolean deep, byte origData);
}
