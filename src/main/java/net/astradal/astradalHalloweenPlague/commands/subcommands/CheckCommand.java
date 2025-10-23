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
import net.astradal.astradalHalloweenPlague.plague.InfectionData;
import net.astradal.astradalHalloweenPlague.plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.plague.PlagueStage;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class CheckCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("check")
            .requires(PlaguePermissions.requires("check"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();

        // Retrieve the target player using the confirmed Brigadier pattern
        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
        final Player target = targetResolver.resolve(ctx.getSource()).getFirst();

        if (target == null) {
            MessageUtil.sendMessage(sender, Component.text("Player not found or is not online.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        PlagueManager manager = plugin.getPlagueManager();
        Optional<InfectionData> infectionData = manager.getInfectionData(target.getUniqueId());

        // Use the manager to check immunity, which automatically cleans up expired immunity
        Long immunityExpirationTime = manager.getActiveImmunity().get(target.getUniqueId());

        sender.sendMessage(
            Component.text("--- Plague Check: ", NamedTextColor.GOLD)
                .append(target.name().color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" ---", NamedTextColor.GOLD))
        );

        // --- INFECTION STATUS ---
        if (infectionData.isPresent()) {
            InfectionData data = infectionData.get();
            PlagueStage stage = PlagueStage.getByLevel(data.getStage());

            long timeInfected = System.currentTimeMillis() - data.getInfectedTime();
            String duration = formatDuration(timeInfected);

            sender.sendMessage(Component.text("  Infection Status: ", NamedTextColor.RED)
                .append(Component.text("INFECTED", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)));

            sender.sendMessage(Component.text("  Stage: ", NamedTextColor.GRAY)
                .append(Component.text(stage.name() + " (" + stage.getStageLevel() + ")", NamedTextColor.WHITE)));

            sender.sendMessage(Component.text("  Time Infected: ", NamedTextColor.GRAY)
                .append(Component.text(duration, NamedTextColor.WHITE)));

        } else {
            sender.sendMessage(Component.text("  Infection Status: ", NamedTextColor.GREEN)
                .append(Component.text("HEALTHY", NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD)));
        }

        // --- IMMUNITY STATUS ---
        if (immunityExpirationTime != null) {
            long remaining = immunityExpirationTime - System.currentTimeMillis();
            if (remaining > 0) {
                String duration = formatDuration(remaining);

                sender.sendMessage(Component.text("  Immunity Remaining: ", NamedTextColor.AQUA)
                    .append(Component.text(duration, NamedTextColor.LIGHT_PURPLE)));
            } else {
                // Should have been cleaned up by PlagueManager, but safety check
                sender.sendMessage(Component.text("  Immunity Status: ", NamedTextColor.GRAY)
                    .append(Component.text("EXPIRED (Needs Cleanup)", NamedTextColor.DARK_GRAY)));
                // Trigger cleanup just in case
                manager.getActiveImmunity().remove(target.getUniqueId());
                plugin.getImmunityRepository().removeImmunity(target.getUniqueId());
            }
        } else {
            sender.sendMessage(Component.text("  Immunity Status: ", NamedTextColor.GRAY)
                .append(Component.text("NONE", NamedTextColor.DARK_GRAY)));
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Formats milliseconds into a readable [Xm Ys] string.
     */
    private static String formatDuration(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}