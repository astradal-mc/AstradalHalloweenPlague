package net.astradal.astradalHalloweenPlague.Commands.Subcommands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Root node for all /plague region commands.
 */
public final class RegionCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("region")
            .requires(PlaguePermissions.requires("region"))
            .executes(ctx -> {
                // Fix: Replace Commands.component with MessageUtil.sendMessage
                MessageUtil.sendMessage(ctx.getSource().getSender(),
                    Component.text("Usage: /plague region <add|remove|list|setpos>", NamedTextColor.RED));
                return 0;
            })
            // Attach nested subcommands
            .then(RegionSetPosCommand.build(plugin))
            .then(RegionAddCommand.build(plugin))
            .then(RegionRemoveCommand.build(plugin))
            .then(RegionListCommand.build(plugin));
    }
}