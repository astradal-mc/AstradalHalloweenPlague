package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ToggleCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("toggle")
            .requires(PlaguePermissions.requires("toggle"))
            .executes(ctx -> execute(ctx, plugin));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();
        boolean currentState = plugin.isPluginEnabled();
        boolean newState = !currentState;

        plugin.setPluginEnabled(newState);

        Component status = newState
            ? Component.text("ENABLED", NamedTextColor.GREEN)
            : Component.text("DISABLED", NamedTextColor.RED);

        MessageUtil.sendMessage(source.getSender(),
            Component.text("Plague system is now ").append(status).append(Component.text(".")));

        return Command.SINGLE_SUCCESS;
    }
}