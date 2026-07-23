package com.atlasplugins.atlasenchants.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Suppresses the vanilla "Named entity ... died: ..." console spam.
 * <p>
 * Several of this plugin's enchantments (e.g. Blessing of Knowledge) temporarily
 * set a custom name on mobs to display a health bar. Whenever a named entity dies,
 * vanilla Minecraft logs a line like:
 * <pre>Named entity WitherSkeleton['...'] died: ... was slain by _Ghillie</pre>
 * which floods the server console. This Log4j filter drops those lines before they
 * are printed, without touching any other logging.
 */
public class NamedEntityLogFilter extends AbstractFilter {

    private static NamedEntityLogFilter installed;

    private static LoggerConfig rootConfig() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        return ctx.getConfiguration().getRootLogger();
    }

    /** Attach the filter to the root logger. Safe to call once on plugin enable. */
    public static void install() {
        if (installed != null) return;
        installed = new NamedEntityLogFilter();
        rootConfig().addFilter(installed);
        ((LoggerContext) LogManager.getContext(false)).updateLoggers();
    }

    /** Detach the filter on plugin disable so reloads don't stack duplicates. */
    public static void uninstall() {
        if (installed == null) return;
        rootConfig().removeFilter(installed);
        ((LoggerContext) LogManager.getContext(false)).updateLoggers();
        installed = null;
    }

    private boolean shouldSuppress(String message) {
        return message != null
                && message.startsWith("Named entity ")
                && message.contains(" died:");
    }

    private Result evaluate(String message) {
        return shouldSuppress(message) ? Result.DENY : Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null || event.getMessage() == null) return Result.NEUTRAL;
        return evaluate(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : evaluate(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : evaluate(msg.toString());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        // The vanilla call is LOGGER.info("Named entity {} died: {}", entity, deathMessage),
        // so the raw pattern already carries the tell-tale "Named entity " / " died:" markers.
        return evaluate(msg);
    }
}
