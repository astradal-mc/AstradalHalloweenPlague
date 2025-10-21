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

public final class InfectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("infect")
            .requires(PlaguePermissions.requires("infect"))
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
        boolean infected = plugin.getPlagueManager().infectPlayer(target);

        if (infected) {
            MessageUtil.sendMessage(source.getSender(),
                Component.text(target.getName() + " has been manually infected.", NamedTextColor.GREEN));
        } else {
            MessageUtil.sendMessage(source.getSender(),
                Component.text(target.getName() + " is already infected.", NamedTextColor.YELLOW));
        }

        return Command.SINGLE_SUCCESS;
    }
}