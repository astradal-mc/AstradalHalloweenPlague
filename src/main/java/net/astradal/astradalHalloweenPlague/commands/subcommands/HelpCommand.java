package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HelpCommand {

    // Descriptions for all subcommands
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>() {{
        put("help", "Shows this help message.");
        put("infect", "Manually infects a target player.");
        put("cure", "Manually cures an infected player.");
        put("stage", "Manually sets the plague stage for a player.");
        put("region", "Manages hospital regions (add, remove, list).");
    }};

    public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher, AstradalHalloweenPlague plugin) {
        return Commands.literal("help")
            .requires(PlaguePermissions.requires("help"))
            .executes(ctx -> execute(ctx, dispatcher, plugin));
    }

    public static int execute(CommandContext<CommandSourceStack> ctx, CommandDispatcher<CommandSourceStack> dispatcher, AstradalHalloweenPlague plugin) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage(
            Component.text("--- Astradal Halloween Plague Help ---", NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
        );

        CommandNode<CommandSourceStack> rootNode = dispatcher.getRoot().getChild("plague");

        if (rootNode == null) {
            MessageUtil.sendMessage(sender, Component.text("Error: Command structure not found.", NamedTextColor.RED));
            return 0;
        }

        // Iterate through all registered subcommands of /plague
        for (CommandNode<CommandSourceStack> node : rootNode.getChildren()) {
            if (!(node instanceof LiteralCommandNode<?> literalNode)) continue;

            String subcommand = literalNode.getLiteral();

            // Special handling for nested commands like 'region'
            String permissionKey = subcommand.startsWith("region") ? "region" : subcommand;

            // Only show commands the player has permission to use
            if (!PlaguePermissions.has(sender, permissionKey)) continue;

            String description = DESCRIPTIONS.getOrDefault(subcommand, "No description available.");
            String usage = "/plague " + subcommand;

            sender.sendMessage(
                Component.text(usage, NamedTextColor.YELLOW)
                    .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(description, NamedTextColor.GRAY))
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}