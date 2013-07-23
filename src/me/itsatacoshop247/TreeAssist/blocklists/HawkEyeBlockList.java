package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.Utils;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import uk.co.oliwali.HawkEye.DataType;
import uk.co.oliwali.HawkEye.HawkEye;
import uk.co.oliwali.HawkEye.SearchParser;
import uk.co.oliwali.HawkEye.callbacks.BaseCallback;
import uk.co.oliwali.HawkEye.database.SearchQuery.SearchDir;
import uk.co.oliwali.HawkEye.database.SearchQuery.SearchError;
import uk.co.oliwali.HawkEye.entry.DataEntry;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

public class HawkEyeBlockList implements BlockList {
	private final boolean active;
	
	public HawkEyeBlockList() {
		active = isHawkEyeRunning();
	}
	private boolean isHawkEyeRunning() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("HawkEye");
		     
		if (plugin == null || !(plugin instanceof HawkEye)) {
		  return false;
		}
		
		return plugin.isEnabled();
	}

	@Override
	public void initiate() {
		if (!active) {
			Utils.plugin.getLogger().warning("HawkEye selected as BlockList, but not enabled!");
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		if (!active) {
			return false;
		}
		class SimpleCallback extends BaseCallback {
	        final CommandSender sender;
	        public SimpleCallback(CommandSender sender) {
	            this.sender = sender;
	        }

	        public void execute() {
	            //sender.sendMessage("Search complete. " + results.size() + " results found");
	        }
	        
	        public void error(SearchError error, String message) {
	        	sender.sendMessage(message);
	        }

	    }
		SimpleCallback callback = new SimpleCallback(Bukkit.getConsoleSender());
		
		SearchParser parser = new SearchParser();
		parser.loc = block.getLocation().toVector();
		parser.actions.add(DataType.BLOCK_BREAK);
		parser.actions.add(DataType.BLOCK_BURN);
		parser.actions.add(DataType.BLOCK_FADE);
		parser.actions.add(DataType.BLOCK_IGNITE);
		parser.actions.add(DataType.BLOCK_PLACE);
		
		HawkEyeAPI.performSearch(callback,
				parser, SearchDir.DESC);
		
		for (DataEntry entry : callback.results) {
			if (entry.getType() == DataType.BLOCK_PLACE) {
				return true;
			}
			return false;
		}
		
		return false;
	}

	@Override
	public void addBlock(Block block) {
		// plugin does that
	}

	@Override
	public void removeBlock(Block block) {
		// plugin does that
	}

	@Override
	public void save() {
		// plugin does that
	}

	@Override
	public void logBreak(Block block, Player player) {
		if (!active) {
			return;
		}
		HawkEyeAPI.addCustomEntry(Utils.plugin, "block-break",
				player == null ? "TreeAssist" : player.getName(),
						block.getLocation(), "");
	}

}
