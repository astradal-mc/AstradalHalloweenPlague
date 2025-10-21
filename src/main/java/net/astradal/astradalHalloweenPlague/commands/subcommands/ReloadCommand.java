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

public final class ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("reload")
            .requires(PlaguePermissions.requires("reload"))
            .executes(ctx -> execute(ctx, plugin));
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();

        // 1. Reload the core configuration
        plugin.reloadPlagueConfig();

        // 2. Reload hospital regions from the database (in case they were manually edited)
        plugin.getRegionUtil().loadRegionsFromDatabase();

        MessageUtil.sendMessage(source.getSender(),
            Component.text("Astradal Halloween Plague reloaded successfully! Configurations and regions updated.", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}