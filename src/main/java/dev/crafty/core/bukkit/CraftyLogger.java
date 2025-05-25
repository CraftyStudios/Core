package dev.crafty.core.bukkit;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Enhanced logger
 * @since 1.0.0
 */
public final class CraftyLogger {
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GRAY   = "\u001B[90m";

    private static final String TICK   = "✔";
    private static final String CROSS  = "✖";
    private static final String ARROW  = "➜";
    private static final String WARN   = "⚠";

    private final Logger loggerDelegae;

    public CraftyLogger(JavaPlugin plugin) {
        this.loggerDelegae = plugin.getLogger();
    }

    public void info(String msg, Object... args)   { log(Level.INFO,  GREEN, TICK,  msg, args); }
    public void warn(String msg, Object... args)   { log(Level.WARNING, YELLOW, WARN,  msg, args); }
    public void error(String msg, Object... args)  { log(Level.SEVERE, RED,  CROSS, msg, args); }
    public void debug(String msg, Object... args)  { log(Level.FINE,  BLUE,  ARROW, msg, args); }
    public void trace(String msg, Object... args)  { log(Level.FINER, CYAN,  "·",  msg, args); }

    private void log(Level lvl,
                            String color,
                            String symbol,
                            String msg,
                            Object... args) {
        String formatted = args.length == 0 ? msg : String.format(msg, args);
        String finalLine = String.format("%s%s %s%s %s", color, symbol, RESET, formatted, RESET);

        loggerDelegae.log(lvl, finalLine);
    }
}
