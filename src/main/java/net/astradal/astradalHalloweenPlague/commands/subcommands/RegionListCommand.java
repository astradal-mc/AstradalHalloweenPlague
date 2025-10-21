package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.plague.HospitalRegion;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Collection;

public final class RegionListCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("list")
            .requires(PlaguePermissions.requires("region"))
            .executes(ctx -> execute(ctx, plugin));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();
        Collection<HospitalRegion> regions = plugin.getRegionUtil().getRegions();

        if (regions.isEmpty()) {
            MessageUtil.sendMessage(source.getSender(), Component.text("No hospital regions defined.", NamedTextColor.YELLOW));
            return 0;
        }

        MessageUtil.sendMessage(source.getSender(),
            Component.text("--- Hospital Regions (" + regions.size() + ") ---", NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
        );

        regions.forEach(region -> {
            Component message = Component.text()
                .append(Component.text("Name: ", NamedTextColor.AQUA))
                .append(Component.text(region.getName(), NamedTextColor.WHITE))
                .append(Component.text(" | World: ", NamedTextColor.AQUA))
                .append(Component.text(region.getWorldName(), NamedTextColor.WHITE))
                .append(Component.text(" | Coords: ", NamedTextColor.AQUA))
                .append(Component.text(
                    region.getMinX() + "," + region.getMinY() + "," + region.getCorrectedMinZ() +
                        " to " +
                        region.getMaxX() + "," + region.getMaxY() + "," + region.getCorrectedMaxZ(),
                    NamedTextColor.GRAY
                ))
                .build();
            source.getSender().sendMessage(message);
        });

        return Command.SINGLE_SUCCESS;
    }
}