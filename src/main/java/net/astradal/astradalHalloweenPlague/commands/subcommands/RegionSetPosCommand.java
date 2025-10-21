package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.commands.StaffSelection;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RegionSetPosCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("setpos")
            .requires(PlaguePermissions.requires("region"))
            .then(Commands.literal("1")
                .executes(ctx -> execute(ctx, plugin, "pos1"))
            )
            .then(Commands.literal("2")
                .executes(ctx -> execute(ctx, plugin, "pos2"))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin, String posName) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, Component.text("Only players can set region positions.", NamedTextColor.RED));
            return 0;
        }

        Location location = player.getLocation().getBlock().getLocation(); // Use block location for bounding box accuracy
        StaffSelection staffSelection = plugin.getStaffSelection();

        staffSelection.setSelection(player, posName, location);

        MessageUtil.sendMessage(sender,
            Component.text(posName + " set to: ", NamedTextColor.GREEN)
                .append(Component.text(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(), NamedTextColor.YELLOW))
        );

        if (staffSelection.hasCompleteSelection(player)) {
            MessageUtil.sendMessage(sender, Component.text("Both positions are set! Use ", NamedTextColor.AQUA)
                .append(Component.text("/plague region add <name>", NamedTextColor.YELLOW))
                .append(Component.text(" to create the region.", NamedTextColor.AQUA))
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}