package net.astradal.astradalHalloweenPlague.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.plague.HospitalRegion;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public final class RegionRemoveCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("remove")
            .requires(PlaguePermissions.requires("region"))
            .then(Commands.argument("name", string())
                // Add suggestions for existing region names
                .suggests((ctx, builder) -> suggestRegionNames(ctx, builder, plugin))
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    // Brigadier Suggestions provider
    private static CompletableFuture<Suggestions> suggestRegionNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, AstradalHalloweenPlague plugin) {
        String currentInput = builder.getRemaining().toLowerCase();

        plugin.getRegionUtil().getRegions().stream()
            .map(HospitalRegion::getName)
            .filter(name -> name.startsWith(currentInput))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();
        String regionName = StringArgumentType.getString(ctx, "name").toLowerCase();

        // Check if region exists before removing
        boolean exists = plugin.getRegionUtil().getRegions().stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(regionName));

        if (!exists) {
            MessageUtil.sendMessage(source.getSender(), Component.text("Region '", NamedTextColor.RED)
                .append(Component.text(regionName, NamedTextColor.YELLOW))
                .append(Component.text("' not found.", NamedTextColor.RED))
            );
            return 0;
        }

        // --- Core Logic: Remove Region ---
        plugin.getRegionUtil().removeRegion(regionName);

        MessageUtil.sendMessage(source.getSender(),
            Component.text("Hospital region '", NamedTextColor.GREEN)
                .append(Component.text(regionName, NamedTextColor.YELLOW))
                .append(Component.text("' deleted.", NamedTextColor.GREEN))
        );

        return Command.SINGLE_SUCCESS;
    }
}