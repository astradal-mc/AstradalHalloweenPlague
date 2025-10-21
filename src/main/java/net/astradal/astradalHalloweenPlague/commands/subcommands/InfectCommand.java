package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public final class InfectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("infect")
            .requires(PlaguePermissions.requires("infect"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();

        // --- DEFINITIVE FIX: Retrieve argument as PlayerTarget and get players from it ---
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);

        // Get the single target (since we used the default player argument which expects one)
        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();


        // ... (Core Logic remains the same) ...
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