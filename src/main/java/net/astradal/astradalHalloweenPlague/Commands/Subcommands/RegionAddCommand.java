package net.astradal.astradalHalloweenPlague.Commands.Subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.Commands.StaffSelection;
import net.astradal.astradalHalloweenPlague.Plague.HospitalRegion;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public final class RegionAddCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("add")
            .requires(PlaguePermissions.requires("region"))
            .then(Commands.argument("name", string())
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        String regionName = StringArgumentType.getString(ctx, "name").toLowerCase();

        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, Component.text("Only players can define regions.", NamedTextColor.RED));
            return 0;
        }

        StaffSelection staffSelection = plugin.getStaffSelection();
        Location[] selection = staffSelection.getCompleteSelection(player);

        if (selection == null) {
            MessageUtil.sendMessage(sender, Component.text("You must select two positions first using ", NamedTextColor.RED)
                .append(Component.text("/plague region setpos <1|2>", NamedTextColor.YELLOW)));
            return 0;
        }

        Location pos1 = selection[0];
        Location pos2 = selection[1];

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            MessageUtil.sendMessage(sender, Component.text("Positions must be in the same world!", NamedTextColor.RED));
            return 0;
        }

        // --- Core Logic: Create and Save Region ---
        HospitalRegion newRegion = new HospitalRegion(
            regionName,
            pos1.getWorld().getName(),
            pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
            pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
        );

        plugin.getRegionUtil().addRegion(newRegion);

        MessageUtil.sendMessage(sender,
            Component.text("Hospital region '", NamedTextColor.GREEN)
                .append(Component.text(regionName, NamedTextColor.YELLOW))
                .append(Component.text("' created and saved!", NamedTextColor.GREEN))
        );

        return Command.SINGLE_SUCCESS;
    }
}