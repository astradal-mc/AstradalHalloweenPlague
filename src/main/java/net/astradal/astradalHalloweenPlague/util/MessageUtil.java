package net.astradal.astradalHalloweenPlague.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Utility class for sending formatted messages to players using Kyori Adventure Components.
 */
public class MessageUtil {

    // Define a consistent prefix component
    private static final Component PREFIX = Component.text()
        .append(Component.text("[PLAGUE] ", NamedTextColor.RED, TextDecoration.BOLD))
        .build();

    /**
     * Helper to create a formatted component with the prefix.
     * We'll stick to simple component building for clarity instead of MiniMessage for now.
     */
    private static Component format(Component message) {
        return PREFIX.append(message);
    }

    /**
     * Sends the message displayed upon initial infection.
     */
    public static void sendInfectionMessage(Player player) {
        Component message = Component.text()
            .append(Component.text("You suddenly feel a chill... ", NamedTextColor.GRAY))
            .append(Component.text("You have contracted the PLAGUE! ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
            .append(Component.text("Find a cure quickly.", NamedTextColor.GRAY))
            .build();

        player.sendMessage(format(message));
    }

    /**
     * Sends the message displayed upon reaching the final, most severe stage.
     */
    public static void sendFinalStageMessage(Player player) {
        Component message = Component.text()
            .append(Component.text("Your plague has progressed to the ", NamedTextColor.RED))
            .append(Component.text("FINAL STAGE! ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
            .append(Component.text("The symptoms are unbearable.", NamedTextColor.RED))
            .build();

        player.sendMessage(format(message));

        // Send a dramatic title
        Title title = Title.title(
            Component.text("CRITICAL", NamedTextColor.DARK_RED),
            Component.text("Final Stage Symptoms Active", NamedTextColor.RED),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );
        player.showTitle(title);
    }

    /**
     * Sends the message displayed when a player is cured.
     */
    public static void sendCureMessage(Player player) {
        Component message = Component.text()
            .append(Component.text("You feel a wave of relief wash over you. ", NamedTextColor.GREEN))
            .append(Component.text("You are cured! ", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            .append(Component.text("Stay safe.", NamedTextColor.GREEN))
            .build();

        player.sendMessage(format(message));
    }


    /**
     * Helper to create a formatted component with the prefix.
     */
    private static Component prefixMessage(Component message) { // Renamed from 'format' to clarify it adds the prefix
        return PREFIX.append(message);
    }

    /**
     * Sends a generic, formatted message (e.g., for commands).
     */
    public static void sendMessage(Player player, Component message) {
        player.sendMessage(prefixMessage(message));
    }

    /**
     * Sends a generic, formatted message (e.g., for commands) to any CommandSender.
     */
    public static void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(prefixMessage(message));
    }
}