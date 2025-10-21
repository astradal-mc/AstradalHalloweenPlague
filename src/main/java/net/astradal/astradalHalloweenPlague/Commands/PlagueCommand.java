package net.astradal.astradalHalloweenPlague.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Commands.Subcommands.*;

public final class PlagueCommand {

    /**
     * Creates and builds the root '/plague' command node with all its subcommands.
     *
     * @param plugin     The main plugin instance.
     * @param dispatcher The command dispatcher (used by HelpCommand).
     * @return The fully constructed command node, ready for registration.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> create(
        AstradalHalloweenPlague plugin,
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        var rootNode = Commands.literal("plague")
            .executes(ctx -> HelpCommand.execute(ctx, dispatcher, plugin)); // Default action

        // Register all subcommands
        rootNode.then(HelpCommand.build(dispatcher, plugin));
        rootNode.then(InfectCommand.build(plugin));
        rootNode.then(CureCommand.build(plugin));
        rootNode.then(StageCommand.build(plugin));

        // Region Management Subcommands
        rootNode.then(RegionCommand.build(plugin));

        return rootNode;
    }
}