package me.itsatacoshop247.TreeAssist.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import me.itsatacoshop247.TreeAssist.TreeAssist;

public class Debugger {
	public static boolean override = false;
	
	private static Logger logger = null;
	private static List<Logger> loggers = new ArrayList<Logger>();
	
	private final TreeAssist plugin;
	private final int debugID;

	private static Set<Integer> check = new HashSet<Integer>();

	public Debugger(TreeAssist plugin, int id) {
		debugID = id;
		this.plugin = plugin;
		
		if (logger == null) {
	        logger = Logger.getAnonymousLogger();
	        logger.setLevel(Level.ALL);
	        logger.setUseParentHandlers(false);
	        
	        for (Handler handler : logger.getHandlers()) {
	            logger.removeHandler(handler);
	        }
	
	        try {
	            final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

	            final File debugFolder = new File(plugin.getDataFolder(), "debug");
	            debugFolder.mkdirs();
	            final File logFile = new File(debugFolder, dateformat.format(new Date()) + ".log");
	            
	            final FileHandler handler = new FileHandler(logFile.getAbsolutePath());
	            
	            handler.setFormatter(LogFileFormatter.newInstance());
	            
	            logger.addHandler(handler);

	    		loggers.add(logger);
	        } catch (IOException ex) {
	        	plugin.getLogger().log(Level.SEVERE, null, ex);
	        } catch (SecurityException ex) {
	        	plugin.getLogger().log(Level.SEVERE, null, ex);
	        }
        }
	}
	private boolean debugs() {
		return override || check.contains(debugID);
	}
	
    static class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        private LogFileFormatter() {
            super();
            this.date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable exception = record.getThrown();

            builder.append(this.date.format(Long.valueOf(record.getMillis())));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (exception != null) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }
    
	public void i(final String string) {
		if (!debugs()) {
			return;
		}
		logger.info(System.currentTimeMillis()%1000 + " " + string);
		System.out.print(string);
	}
	
	public static void load(final TreeAssist instance, final CommandSender sender) {
		check.clear();
		override = false;
		final String debugs = instance.getConfig().getString("Debug");
		if (debugs.equals("none")) {
			instance.getLogger().info("debugging: off");
		} else {
			if (debugs.equals("all") || debugs.equals("full") || debugs.equals("on")) {
				override = true;
				sender.sendMessage("debugging EVERYTHING");
			} else {
				final String[] sIds = debugs.split(",");
				sender.sendMessage("debugging: " + debugs);
				for (String s : sIds) {
					try {
						check.add(Integer.valueOf(s));
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}
	}
	
	public static void destroy() {
		
		for (Logger log : loggers) {
			Handler[] handlers = log.getHandlers().clone();
			for (Handler hand : handlers) {
				log.removeHandler(hand);
			}
		}
		loggers.clear();
	}
	public static String parse(Location location) {
		StringBuffer sb = new StringBuffer(location.getWorld().getName());
		sb.append(':');
		sb.append(location.getBlockX());
		sb.append('/');
		sb.append(location.getBlockY());
		sb.append('/');
		sb.append(location.getBlockZ());
		return sb.toString();
	}
}
