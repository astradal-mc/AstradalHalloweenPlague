package net.astradal.astradalHalloweenPlague.Commands.Subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.player;

public final class CureCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("cure")
            .requires(PlaguePermissions.requires("cure"))
            .then(Commands.argument("target", player())
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();

        // --- FIX: Retrieve the target player correctly from the context ---
        Player target = ctx.getArgument("target", Player.class);

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