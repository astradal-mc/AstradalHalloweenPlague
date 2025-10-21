package net.astradal.astradalHalloweenPlague.commands;

import java.util.function.Predicate;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.permissions.Permissible;

/**
 * Utility class for defining and checking command permissions.
 */
public final class PlaguePermissions {

    private static final String BASE = "astradal.plague.";
    private static final String ADMIN_BASE = BASE + "admin.";

    /**
     * Gets the full permission string for a subcommand.
     * @param subcommand The subcommand name (e.g., "infect").
     * @return The full permission string (e.g., "astradal.plague.admin.infect").
     */
    public static String get(String subcommand) {
        return ADMIN_BASE + subcommand;
    }

    /**
     * Checks if a sender has permission for a subcommand.
     */
    public static boolean has(Permissible sender, String subcommand) {
        return sender.hasPermission(get(subcommand));
    }

    /**
     * Brigadier Predicate for permission checking.
     */
    public static Predicate<CommandSourceStack> requires(String subcommand) {
        return source -> has(source.getSender(), subcommand);
    }
}