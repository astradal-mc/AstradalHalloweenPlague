package net.astradal.astradalHalloweenPlague.Commands.Subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType; // Correct Mojang Brigadier import
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Commands.PlaguePermissions;
import net.astradal.astradalHalloweenPlague.Plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.Plague.PlagueStage;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.player;

public final class StageCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalHalloweenPlague plugin) {
        return Commands.literal("stage")
            .requires(PlaguePermissions.requires("stage"))
            .then(Commands.argument("target", player())
                .then(Commands.argument("stage", integer(1, PlagueStage.STAGE_FINAL.getStageLevel()))
                    .executes(ctx -> execute(ctx, plugin))
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalHalloweenPlague plugin) {
        CommandSourceStack source = ctx.getSource();

        // --- FIX: Retrieve the target player correctly from the context ---
        Player target = ctx.getArgument("target", Player.class);

        int newStage = IntegerArgumentType.getInteger(ctx, "stage");
        PlagueManager plagueManager = plugin.getPlagueManager();

        if (target == null) {
            MessageUtil.sendMessage(source.getSender(), Component.text("Player not found or is not online.", NamedTextColor.RED));
            return 0;
        }

        // --- Core Logic ---
        if (!plagueManager.isPlayerInfected(target.getUniqueId())) {
            MessageUtil.sendMessage(source.getSender(),
                Component.text(target.getName() + " is not infected. Use /plague infect first.", NamedTextColor.YELLOW));
            return 0;
        }

        // Get the InfectionData from the manager's cache and update it
        plagueManager.getActiveInfections().computeIfPresent(target.getUniqueId(), (uuid, data) -> {
            data.setStage(newStage);

            // Update database
            plugin.getInfectionRepository().updateInfectionStage(uuid, newStage);

            // Send final stage notification if needed
            if (newStage == PlagueStage.STAGE_FINAL.getStageLevel()) {
                MessageUtil.sendFinalStageMessage(target);
            }

            return data;
        });

        MessageUtil.sendMessage(source.getSender(),
            Component.text(target.getName() + "'s plague stage set to " + newStage + ".", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}