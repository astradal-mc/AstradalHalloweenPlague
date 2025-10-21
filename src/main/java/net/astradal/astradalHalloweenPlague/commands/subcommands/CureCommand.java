package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver; // <-- REQUIRED IMPORT
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

// We use the full class name for ArgumentTypes here to match your example,
// rather than a static import.

public final class CureCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("cure")
            .requires(PlaguePermissions.requires("cure"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();

        // --- CORRECTED PLAYER RETRIEVAL ---
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

        if (target == null) {
            MessageUtil.sendMessage(source.getSender(), Component.text("Player not found or is not online.", NamedTextColor.RED));
            return 0;
        }

        // --- Core Logic ---
        if (plugin.getPlagueManager().isPlayerInfected(target.getUniqueId())) {
            plugin.getPlagueManager().curePlayer(target.getUniqueId());

            MessageUtil.sendMessage(source.getSender(),
                Component.text(target.getName() + " has been manually cured.", NamedTextColor.GREEN));
        } else {
            MessageUtil.sendMessage(source.getSender(),
                Component.text(target.getName() + " is not currently infected.", NamedTextColor.YELLOW));
        }

        return Command.SINGLE_SUCCESS;
    }
}