package me.itsatacoshop247.TreeAssist;

import me.itsatacoshop247.TreeAssist.blocklists.*;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.metrics.MetricsLite;
import me.itsatacoshop247.TreeAssist.timers.CooldownCounter;
import me.itsatacoshop247.TreeAssist.trees.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;


//Running changelog

//- Tree destruction will only destory the main blocks Type ID. E.G. if a Oak Tree branch hits a jungle tree branch, the Oak Tree will not destroy the jungle Branch and vice-versa
//- Fixed Blocks dissapearing when used to destroy a tree.
//- Config autoupdates

public class TreeAssist extends JavaPlugin implements Listener {
    public List<Location> saplingLocationList = new ArrayList<Location>();
    private final Map<String, List<String>> disabledMap = new HashMap<String, List<String>>();
    private Map<String, CooldownCounter> coolDowns = new HashMap<String, CooldownCounter>();

    public boolean Enabled = true;
    public boolean mcMMO = false;

    File configFile;
    FileConfiguration config;

    public BlockList blockList;
    TreeAssistBlockListener listener;

    public int getCoolDown(Player player) {
        if (hasCoolDown(player)) {
            return coolDowns.get(player.getName()).getSeconds();
        }
        return 0;
    }

    public TreeAssistBlockListener getListener() {
        return listener;
    }

    public boolean hasCoolDown(Player player) {
        return coolDowns.containsKey(player.getName());
    }

    public boolean isActive(World world) {
        return (!config.getBoolean("Worlds.Enable Per World")) ||
                config.getList("Worlds.Enabled Worlds").contains(
                        world.getName());
    }

    public boolean isDisabled(String world, String player) {
        if (disabledMap.containsKey("global")) {
            if (disabledMap.get("global").contains(player)) {
                return true;
            }
        }
        if (disabledMap.containsKey(world)) {
            return disabledMap.get(world).contains(player);
        }
        return false;
    }

    public boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isForceAutoDestroy() {
        return getConfig().getBoolean("Main.Automatic Tree Destruction")
                && getConfig().getBoolean("Automatic Tree Destruction.Forced Removal");
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void loadYamls() {
        try {
            this.config.load(this.configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("TreeAssist")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("Reload")) {
                    if (!sender.hasPermission("treeassist.reload")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_RELOAD));
                        return true;
                    }
                    blockList.save();
                    reloadConfig();
                    this.loadYamls();
                    reloadLists();
                    sender.sendMessage(Language.parse(MSG.SUCCESSFUL_RELOAD));
                    return true;
                } else if (args[0].equalsIgnoreCase("Toggle")) {

                    if (sender.hasPermission("treeassist.toggle.other") && args.length > 1) {

                        if (args.length > 2) {
                            if (Bukkit.getWorld(args[2]) == null) {
                                sender.sendMessage(Language.parse(MSG.ERROR_NOTFOUND_WORLD, args[1]));
                                return true;
                            }

                            if (toggleWorld(args[2], args[1])) {
                                sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_ON, args[1], args[2]));
                            } else {
                                sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_OTHER_WORLD_OFF, args[1], args[2]));
                            }
                        }

                        if (toggleGlobal(args[1])) {
                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_OTHER_ON, args[1]));
                        } else {
                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_OTHER_OFF, args[1]));
                        }
                        return true;
                    }

                    if (!sender.hasPermission("treeassist.toggle")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_TOGGLE));
                        return true;
                    }

                    if (args.length > 1) {
                        if (Bukkit.getWorld(args[1]) == null) {
                            sender.sendMessage(Language.parse(MSG.ERROR_NOTFOUND_WORLD, args[1]));
                            return true;
                        }

                        if (toggleWorld(args[1], sender.getName())) {
                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_ON, args[1]));
                        } else {
                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_YOU_WORLD_OFF, args[1]));
                        }
                    }

                    if (toggleGlobal(sender.getName())) {
                        sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_YOU_ON));
                    } else {
                        sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_YOU_OFF));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("Global")) {
                    if (!sender.hasPermission("treeassist.toggle.global")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_TOGGLE_GLOBAL));
                        return true;
                    }
                    if (!this.Enabled) {
                        this.Enabled = true;
                        sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_GLOBAL_ON));
                    } else {
                        this.Enabled = false;
                        sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOGGLE_GLOBAL_OFF));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("Debug")) {
                    if (args.length < 2) {
                        getConfig().set("Debug", "none");
                        Debugger.load(this, sender);
                    } else {
                        getConfig().set("Debug", args[1]);
                        Debugger.load(this, sender);
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("ProtectTool")) {
                    if (!sender.hasPermission("treeassist.tool")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_TOGGLE_TOOL));
                        return true;

                    }
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        boolean found = false;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null) {
                                if (item.hasItemMeta()) {
                                    if (listener.isProtectTool(item)) {
                                        player.getInventory().removeItem(item);
                                        sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOOL_OFF));
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {
                            player.getInventory().addItem(listener.getProtectionTool());
                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_TOOL_ON));
                        }
                        return true;
                    }
                    sender.sendMessage(Language.parse(MSG.ERROR_ONLY_PLAYERS));
                    return true;
                } else if (args[0].equalsIgnoreCase("noreplace")) {
                    int seconds = getConfig().getInt("Sapling Replant.Command Time Delay (Seconds)", 30);
                    listener.noReplace(sender.getName(), seconds);
                    sender.sendMessage(Language.parse(MSG.SUCCESSFUL_NOREPLACE, String.valueOf(seconds)));
                    return true;
                } else if (args[0].equalsIgnoreCase("purge")) {
                    if (!sender.hasPermission("treeassist.purge")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_PURGE));
                        return true;

                    }
                    if (blockList instanceof FlatFileBlockList) {
                        FlatFileBlockList bl = (FlatFileBlockList) blockList;
                        try {
                            int days = Integer.parseInt(args[1]);
                            int done = bl.purge(days);

                            sender.sendMessage(Language.parse(MSG.SUCCESSFUL_PURGE_DAYS, String.valueOf(done), args[1]));
                        } catch (NumberFormatException e) {
                            if (args[1].equalsIgnoreCase("global")) {
                                int done = bl.purge(sender);
                                sender.sendMessage(Language.parse(MSG.SUCCESSFUL_PURGE_GLOBAL, String.valueOf(done)));
                            } else {
                                int done = bl.purge(args[1]);
                                sender.sendMessage(Language.parse(MSG.SUCCESSFUL_PURGE_WORLD, String.valueOf(done), args[1]));
                            }
                        }
                    } else {
                        sender.sendMessage(Language.parse(MSG.ERROR_ONLY_TREEASSIST_BLOCKLIST));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("addtool")) {
                    if (!sender.hasPermission("treeassist.addtool")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_ADDTOOL));
                        return true;
                    }
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Utils.addRequiredTool(player);
                        return true;
                    }
                    sender.sendMessage(Language.parse(MSG.ERROR_ONLY_PLAYERS));
                    return true;
                } else if (args[0].equalsIgnoreCase("removetool")) {
                    if (!sender.hasPermission("treeassist.removetool")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_REMOVETOOL));
                        return true;

                    }
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Utils.removeRequiredTool(player);
                        return true;
                    }
                    sender.sendMessage(Language.parse(MSG.ERROR_ONLY_PLAYERS));
                    return true;
                } else if (args[0].equalsIgnoreCase("addcustom")) {
                    if (!sender.hasPermission("treeassist.addcustom")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_ADDCUSTOM));
                        return true;
                    }
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Utils.addCustomGroup(player);
                        return true;
                    }
                    sender.sendMessage(Language.parse(MSG.ERROR_ONLY_PLAYERS));
                    return true;
                } else if (args[0].equalsIgnoreCase("removecustom")) {
                    if (!sender.hasPermission("treeassist.removecustom")) {
                        sender.sendMessage(Language.parse(MSG.ERROR_PERMISSION_REMOVECUSTOM));
                        return true;

                    }
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Utils.removeCustomGroup(player);
                        return true;
                    }
                    sender.sendMessage(Language.parse(MSG.ERROR_ONLY_PLAYERS));
                    return true;
                }
            }
        }
        return false;
    }

    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        Debugger.destroy();
    }

    public void onEnable() {
        checkMcMMO();

        Utils.plugin = this;

        this.configFile = new File(getDataFolder(), "config.yml");
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.config = new YamlConfiguration();

        this.listener = new TreeAssistBlockListener(this);

        loadYamls();
        config.options().copyDefaults(true);
        //check for defaults to set newly

        this.updateConfig();

        getServer().getPluginManager().registerEvents(listener, this);
        if (config.getBoolean("Main.Auto Plant Dropped Saplings")) {
            getServer().getPluginManager().registerEvents(new TreeAssistSpawnListener(this), this);
        }
        reloadLists();

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        BaseTree.debug = new Debugger(this, 1);
        CustomTree.debugger = new Debugger(this, 2);
        InvalidTree.debugger = new Debugger(this, 3);
        MushroomTree.debugger = new Debugger(this, 4);
        VanillaTree.debugger = new Debugger(this, 5);
        VanillaOneSevenTree.debugger = new Debugger(this, 6);
        Debugger.load(this, Bukkit.getConsoleSender());


        initiateList("Modding.Custom Logs", Utils.validTypes);
        initiateList("Modding.Custom Tree Blocks", Utils.validTypes);

        if (!getConfig().getBoolean("Main.Ignore User Placed Blocks")) {
            String pluginName = getConfig().getString(
                    "Placed Blocks.Handler Plugin Name", "TreeAssist");
            if ("TreeAssist".equalsIgnoreCase(pluginName)) {
                blockList = new FlatFileBlockList();
            } else if ("Prism".equalsIgnoreCase(pluginName)) {
                blockList = new Prism2BlockList();
            } else if ("LogBlock".equalsIgnoreCase(pluginName)) {
                blockList = new LogBlockBlockList();
            } else if ("CoreProtect".equalsIgnoreCase(pluginName)) {
                blockList = new CoreProtectBlockList();
            } else {
                blockList = new EmptyBlockList();
            }
        } else {
            blockList = new EmptyBlockList();
        }
        blockList.initiate();

        Language.init(this, config.getString("Main.Language", "en"));
    }

    public void removeCountDown(String playerName) {
        try {
            coolDowns.get(playerName).cancel();
        } catch (Exception e) {

        }
        coolDowns.remove(playerName);
    }

    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCoolDown(Player player, BaseTree tree) {
        int coolDown = getConfig().getInt("Automatic Tree Destruction.Cooldown (seconds)", 0);
        if (coolDown == 0 || tree == null || !tree.isValid()) {
            return;
        } else if (coolDown < 0) {
            coolDown = tree.calculateCooldown(player.getItemInHand());
            player.sendMessage(Language.parse(
                    MSG.INFO_COOLDOWN_WAIT, String.valueOf(coolDown)));
        }
        CooldownCounter cc = new CooldownCounter(player, coolDown);
        cc.runTaskTimer(this, 20L, 20L);
        coolDowns.put(player.getName(), cc);
    }

    /**
     * @return true if the result is "player may use plugin"
     */
    boolean toggleGlobal(String player) {
        return toggleWorld("global", player);
    }

    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("mcMMO")) {
            mcMMO = true;
            getLogger().info("Loaded mcMMO, better late than never!");
        }
    }

    private void checkMcMMO() {
        if (getConfig().getBoolean("Main.Use mcMMO if Available")) {
            this.mcMMO = getServer().getPluginManager().isPluginEnabled("mcMMO");
            this.getLogger().info("Loaded mcMMO: " + mcMMO);
            if (!mcMMO) {
                getServer().getPluginManager().registerEvents(this, this);
            }
        } else {
            this.mcMMO = false;
        }
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void firstRun() throws Exception {
        if (!this.configFile.exists()) {
            this.configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), this.configFile);
        }
    }

    private void initiateList(String string, List<Integer> validTypes) {
        for (Object obj : config.getList(string)) {
            if (obj instanceof Integer) {
                validTypes.add((Integer) obj);
                continue;
            }
            if (obj.equals("LIST ITEMS GO HERE")) {
                List<Object> list = new ArrayList<Object>();
                list.add(-1);
                config.set(string, list);
                saveConfig();
                break;
            }
            validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
        }
    }

    private HashMap<String, String> loadConfigurables(HashMap<String, String> items) {
        //Pre-5.0
        items.put("Main.Automatic Tree Destruction", "true");
        items.put("Main.Use Permissions", "false");
        items.put("Main.Sapling Replant", "true");
        items.put("Main.Apply Full Tool Damage", "true");
        items.put("Main.Ignore User Placed Blocks", "false");
        items.put("Main.Use mcMMO if Available", "true");
        items.put("Automatic Tree Destruction.Tree Types.Birch", "true");
        items.put("Automatic Tree Destruction.Tree Types.Jungle", "true");
        items.put("Automatic Tree Destruction.Tree Types.Oak", "true");
        items.put("Automatic Tree Destruction.Tree Types.Spruce", "true");
        items.put("Leaf Decay.Fast Leaf Decay", "true");
        items.put("Sapling Replant.Bottom Block has to be Broken First", "true");
        items.put("Sapling Replant.Time to Protect Sapling (Seconds)", "0");
        items.put("Sapling Replant.Replant When Tree Burns Down", "true");
        items.put("Sapling Replant.Block all breaking of Saplings", "false");
        items.put("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)", "1");
        items.put("Tools.Sapling Replant Require Tools", "true");
        items.put("Tools.Tree Destruction Require Tools", "true");
        items.put("Tools.Tools List", "LIST");
        items.put("Worlds.Enable Per World", "false");
        items.put("Worlds.Enabled Worlds", "LIST");
        items.put("Config Help", "dev.bukkit.org/server-mods/tree-assist/pages/config-walkthrough/");

        //5.0 additions
        items.put("Sapling Replant.Tree Types to Replant.Birch", "true");
        items.put("Sapling Replant.Tree Types to Replant.Jungle", "true");
        items.put("Sapling Replant.Tree Types to Replant.Oak", "true");
        items.put("Sapling Replant.Tree Types to Replant.Spruce", "true");

        //5.2 additions
        items.put("Main.Destroy Only Blocks Above", "false");

        //5.3 additions
        items.put("Modding.Custom Logs", "LIST");
        items.put("Modding.Custom Tree Blocks", "LIST");
        items.put("Modding.Custom Saplings", "LIST");

        //5.4 additions
        items.put("Automatic Tree Destruction.Tree Types.BigJungle", "true");
        items.put("Sapling Replant.Tree Types to Replant.BigJungle", "true");

        //5.5 additions
        items.put("Automatic Tree Destruction.Delay (ticks)", "0");
        items.put("Automatic Tree Destruction.Forced Removal", "false");
        items.put("Automatic Tree Destruction.Initial Delay (seconds)", "10");

        //5.6 additions
        items.put("Main.Auto Plant Dropped Saplings", "false");
        items.put("Auto Plant Dropped Saplings.Chance (percent)", "10");
        items.put("Auto Plant Dropped Saplings.Delay (seconds)", "5");

        //5.7 additions
        items.put("Automatic Tree Destruction.Tree Types.Brown Shroom", "true");
        items.put("Automatic Tree Destruction.Tree Types.Red Shroom", "true");

        //5.7.1 additions
        items.put("Tools.Drop Chance.DIAMOND_AXE", "100");
        items.put("Tools.Drop Chance.WOOD_AXE", "100");
        items.put("Tools.Drop Chance.GOLD_AXE", "100");
        items.put("Tools.Drop Chance.IRON_AXE", "100");
        items.put("Tools.Drop Chance.STONE_AXE", "100");

        //5.7.2 additions
        items.put("Automatic Tree Destruction.Cooldown (seconds)", "0");

        //5.7.3 additions
        items.put("Custom Drops.APPLE", "0.1");
        items.put("Custom Drops.GOLDEN_APPLE", "0.0");

        //5.8 additions
        items.put("Placed Blocks.Handler Plugin Name", "TreeAssist");
        items.put("Automatic Tree Destruction.Tree Types.Acacia", "true");
        items.put("Automatic Tree Destruction.Tree Types.Dark Oak", "true");
        items.put("Sapling Replant.Tree Types to Replant.Acacia", "true");
        items.put("Sapling Replant.Tree Types to Replant.Dark Oak", "true");
        items.put("Automatic Tree Destruction.Tree Types.BigSpruce", "true");
        items.put("Sapling Replant.Tree Types to Replant.BigSpruce", "true");

        items.put("Sapling Replant.Enforce", "true");
        items.put("Automatic Tree Destruction.Remove Leaves", "true");
        items.put("Main.Toggle Default", "true");
        items.put("Sapling Replant.Command Time Delay (Seconds)", "30");

        items.put("Automatic Tree Destruction.When Sneaking", "true");
        items.put("Automatic Tree Destruction.Required Lore", "");
        items.put("Main.Initial Delay", "false");

        items.put("Sapling Replant.Time to Block Sapling Growth (Seconds)", "0");
        items.put("Main.Language", "en");
        return items;
    }

    public void reloadLists() {
        CustomTree.customTreeBlocks = config.getList("Modding.Custom Tree Blocks");
        CustomTree.customLogs = config.getList("Modding.Custom Logs");
        CustomTree.customSaplings = config.getList("Modding.Custom Saplings");
    }

    /**
     * @return true if the result is "player may use plugin"
     */
    private boolean toggleWorld(String world, String player) {
        if (disabledMap.containsKey(world)) {
            if (disabledMap.get(world).contains(player)) {
                disabledMap.get(world).remove(player);
                return true;
            } else {
                disabledMap.get(world).add(player);
            }
        } else {
            disabledMap.put(world, new ArrayList<String>());
            disabledMap.get(world).add(player);
        }
        return false;
    }

    private void updateConfig() {
        HashMap<String, String> items = new HashMap<String, String>();

        items = loadConfigurables(items);

        int num = 0;
        for (Map.Entry<String, String> item : items.entrySet()) {
            if (this.config.get(item.getKey()) == null) {
                getLogger().info(item.getKey());
                if (item.getValue().equalsIgnoreCase("LIST")) {
                    List<String> list = Arrays.asList("LIST ITEMS GO HERE");
                    this.config.addDefault(item.getKey(), list);
                } else if (item.getValue().equalsIgnoreCase("true")) {
                    this.config.addDefault(item.getKey(), true);
                } else if (item.getValue().equalsIgnoreCase("false")) {
                    this.config.addDefault(item.getKey(), false);
                } else if (isInteger(item.getValue())) {
                    this.config.addDefault(item.getKey(), Integer.parseInt(item.getValue()));
                } else if (isDouble(item.getValue())) {
                    this.config.addDefault(item.getKey(), Double.parseDouble(item.getValue()));
                } else {
                    this.config.addDefault(item.getKey(), item.getValue());
                }
                num++;
            }
        }
        if (num > 0) {
            getLogger().info(num + " missing items added to config file.");
        }
        this.saveConfig();
    }
}
