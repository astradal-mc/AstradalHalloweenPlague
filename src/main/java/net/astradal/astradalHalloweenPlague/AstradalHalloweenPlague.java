package net.astradal.astradalHalloweenPlague;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalHalloweenPlague.commands.PlagueCommand;
import net.astradal.astradalHalloweenPlague.commands.StaffSelection;
import net.astradal.astradalHalloweenPlague.database.DatabaseManager;
import net.astradal.astradalHalloweenPlague.database.ImmunityRepository;
import net.astradal.astradalHalloweenPlague.database.InfectionRepository;
import net.astradal.astradalHalloweenPlague.database.RegionRepository;
import net.astradal.astradalHalloweenPlague.listeners.HospitalListener;
import net.astradal.astradalHalloweenPlague.listeners.InfectionListener;
import net.astradal.astradalHalloweenPlague.plague.PlagueConfig;
import net.astradal.astradalHalloweenPlague.plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.plague.PlagueProgressionTask;
import net.astradal.astradalHalloweenPlague.plague.PlagueStage;
import net.astradal.astradalHalloweenPlague.util.RegionUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class AstradalHalloweenPlague extends JavaPlugin {

    private DatabaseManager databaseManager;
    private InfectionRepository infectionRepository;
    private RegionRepository regionRepository;
    private ImmunityRepository immunityRepository;
    private PlagueManager plagueManager;
    private RegionUtil regionUtil;
    private StaffSelection staffSelection;
    private PlagueConfig plagueConfig;
    private volatile boolean enabledState = true;

    @Override
    public void onEnable() {
        // We no longer need saveDefaultConfig() for region data, but keep it if other config is added.
        // For now, we'll keep it just in case.
        saveDefaultConfig();

        this.plagueConfig = new PlagueConfig(this);
        PlagueStage.initialize(this.plagueConfig);

        // --- 1-3. Database Setup ---

        this.databaseManager = new DatabaseManager(this);

        try {
            databaseManager.connect();
            // This runs the updated schema with the 'regions' table
            databaseManager.runSchemaFromResource("/schema.sql");
        } catch (Exception e) {
            getLogger().severe("FATAL: Failed to initialize database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- 4. Initialize Repositories and Managers ---
        this.infectionRepository = new InfectionRepository(this.databaseManager);
        this.regionRepository = new RegionRepository(this.databaseManager);
        this.immunityRepository = new ImmunityRepository(this.databaseManager);

        this.plagueManager = new PlagueManager(this, this.infectionRepository, immunityRepository);
        this.regionUtil = new RegionUtil(this, this.regionRepository);

        this.staffSelection = new StaffSelection();

        // --- 5. Register Repeating Task ---
        long period = 20L;
        new PlagueProgressionTask(this, plagueManager).runTaskTimer(this, period, period);

        // --- 6. Register Listeners ---
        getServer().getPluginManager().registerEvents(new InfectionListener(this, plagueManager), this);
        getServer().getPluginManager().registerEvents(new HospitalListener(this, plagueManager, regionUtil), this);

        getLogger().info("Astradal Halloween Plague is now enabled! Regions loaded from DB.");

        // --- 7. Register Commands ---
        registerCommands();

    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Astradal Halloween Plague has been disabled.");
    }

    /**
     * Registers the /plague command and its alias /p.
     */
    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var registrar = event.registrar();
            final var dispatcher = registrar.getDispatcher();

            // 1. Get the builder for the main command (/plague).
            LiteralArgumentBuilder<CommandSourceStack> plagueBuilder = PlagueCommand.create(this, dispatcher);

            // 2. Build the main command node.
            LiteralCommandNode<CommandSourceStack> plagueNode = plagueBuilder.build();

            // 3. Register the main command node.
            registrar.register(plagueNode, "Manage Plague Status and Hospital Regions");

            // 4. Create the alias node (/p) that redirects to the main node, and register it.
            LiteralCommandNode<CommandSourceStack> aliasNode = Commands.literal("p")
                .redirect(plagueNode)
                .build();
            registrar.register(aliasNode, "Alias for /plague");

            getLogger().info("Registered /plague and /p commands using Paper Brigadier.");
        });
    }

    public void reloadPlagueConfig() {
        // 1. Reload config file data
        this.plagueConfig = new PlagueConfig(this);
        // 2. Re-inject the new config into the static enum
        PlagueStage.initialize(this.plagueConfig);
        // Note: The PlagueProgressionTask will use the newly injected config
        // for effects and timings in its next run via PlagueStage.getEffects()
    }

    /**
     * Toggles the plugin's operational state.
     */
    public void setPluginEnabled(boolean enabled) {
        this.enabledState = enabled;

        if (!enabled) {
            // Optional: Perform global cleanup when disabled
            getLogger().warning("Plague disabled by command. Note: Active infections remain in the database.");
        } else {
            getLogger().info("Plague enabled by command. Infection systems are now active.");
        }
    }

    // --- Getters for Managers ---

    public InfectionRepository getInfectionRepository() {
        return infectionRepository;
    }

    public RegionRepository getRegionRepository() {
        return regionRepository;
    }

    public  ImmunityRepository getImmunityRepository() { return immunityRepository; }

    public PlagueManager getPlagueManager() {
        return plagueManager;
    }

    public RegionUtil getRegionUtil() {
        return regionUtil;
    }

    public StaffSelection getStaffSelection() {
        return staffSelection;
    }

    public PlagueConfig getPlagueConfig() {
        return plagueConfig;
    }

    public boolean isPluginEnabled() { return enabledState; }


}