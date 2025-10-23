#  Astradal Halloween Plague Plugin

**A PaperMC plugin for a progressive, contagious plague designed for a Halloween event.**

This plugin introduces a multi-stage disease that spreads via hostile mobs, contact, and proximity, requiring staff-designated "Hospital Zones" for curing.

## ✨ Features

  * **Progressive Infection:** The plague advances through three configurable stages, increasing debuffs over time.
  * **Initial Infection Vectors:** Players can be infected via a **configurable chance when hit by a Zombie**.
  * **Contagion Spread:** Infected players spread the plague through physical contact (hitting) or close proximity to other players (Stage 2+).
  * **Configurable Effects:** All debuffs (Slowness, Weakness, Blindness, etc.), infection chances, and progression timers are tunable in `config.yml`.
  * **Hospital Zones:** Staff can designate cure zones where infected players must remain within the bounds for a configurable duration to be cured, visible via a BossBar timer.
  * **Database Persistence:** Infection and Hospital Zone data are stored persistently using SQLite.
  * **Modern API:** Built using the latest PaperMC API, Kyori Adventure components for messaging, and Brigadier for commands.

## 🛠️ Installation & Setup

### Requirements

  * **Server Type:** PaperMC (Recommended 1.21+)
  * **Java Version:** Java 17+

### Steps

1.  Download the latest `AstradalHalloweenPlague.jar` from the release page.
2.  Place the JAR file into your server's `plugins/` directory.
3.  Start the server once to generate the plugin folder and default configuration (`config.yml`).
4.  Stop the server and edit `config.yml` to tune plague effects, timers, and the new Zombie infection chance.
5.  Restart the server.

-----

## ⚙️ Configuration (`config.yml`)

The primary configuration controls the plague's spread, progression, and cure time.

```yaml
# Astradal Halloween Plague Configuration

progression_settings:

  # Time in seconds required to progress from the current stage to the next.
  # 0 means no further automatic progression.
  infection_radius: 5.0 # Proximity infection radius in blocks

  # Time (in seconds) required for a player to be cured in a hospital zone.
  # If 45 seconds seems too fast, you can increase this value here.
  cure_time_seconds: 45

  # Setting for Zombie Infection Chance
  zombie_infection_chance_percent: 10 # 10% chance to infect on hit

  # Duration (in seconds) a player is immune after being cured.
  immunity_duration_seconds: 300 # 5 minutes of immunity after cure

  stages:

    # STAGE ONE: Initial Incubation / Minor Symptoms
    STAGE_ONE:
      time_to_next_stage_seconds: 180 # 10 minutes
      effects:
        # Format: EFFECT_TYPE, AMPLIFIER, DURATION_SECONDS
        - "HUNGER, 0, 6" # Hunger I for 6 seconds (reapplied every second by task)
        - "NAUSEA, 0, 3" # Nausea I for 3 seconds

    # STAGE TWO: Visible Symptoms / Highly Contagious
    STAGE_TWO:
      time_to_next_stage_seconds: 300 # 15 minutes
      effects:
        - "WEAKNESS, 0, 6" # Weakness I
        - "SLOWNESS, 0, 6" # Slowness I
        - "NAUSEA, 1, 5" # Nausea II

    # STAGE FINAL: Critical / Maximum Debuffs
    STAGE_FINAL:
      time_to_next_stage_seconds: 0 # No further progression
      effects:
        - "WEAKNESS, 1, 6" # Weakness II
        - "SLOWNESS, 1, 6" # Slowness II
        - "BLINDNESS, 0, 3" # Blindness I
        - "POISON, 0, 1" # Minor, occasional poison damage
```

-----

## 💻 Staff Commands

All administrative commands use the root alias `/plague` or `/p`. All commands below require the permission `astradalplague.admin.<subcommand>`.

| Command | Usage | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/plague help` | | `astradalplague.admin.help` | Displays the help menu. |
| **`/plague toggle`** | | `astradalplague.admin.toggle` | **Globally enables/disables** all plugin systems, including progression and spread. |
| **`/plague check <player>`** | `/p check Pookachu` | `astradalplague.admin.check` | Displays detailed status: infection stage, time infected, and **remaining immunity time**. |
| `/plague infect <player>` | `/p infect Pookachu` | `astradalplague.admin.infect` | Manually infects a target player (starts at Stage 1). |
| `/plague cure <player>` | `/p cure Pookachu` | `astradalplague.admin.cure` | Manually removes the plague and grants temporary immunity. |
| `/plague stage <player> <stage>` | `/p stage Pookachu 3` | `astradalplague.admin.stage` | Forcefully sets the infected player's plague stage (1, 2, or 3). |
| `/plague reload` | | `astradalplague.admin.reload` | Reloads `config.yml` (effects, timers) and hospital regions. |

### Hospital Region Management

The region management uses a simple selection system where you must set two corner positions (`setpos 1` and `setpos 2`).

| Command | Usage | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/plague region setpos <1|2>` | `/p region setpos 1` | `astradalplague.admin.region` | Sets the first or second corner of the region at your current location. |
| `/plague region add <name>` | `/p region add main_hospital` | `astradalplague.admin.region` | Saves the region defined by `setpos 1` and `setpos 2` to the database. |
| `/plague region remove <name>` | `/p region remove main_hospital` | `astradalplague.admin.region` | Deletes a hospital region from the database. |
| `/plague region list` | | `astradalplague.admin.region` | Lists all currently configured hospital regions. |

-----

## 🦠 Plague Stages & Behavior

| Stage | Duration | Initial Infection Vector | Spread Vector | Debuffs |
| :--- | :--- | :--- | :--- | :--- |
| **Stage 1** | Configurable (e.g., 3m) | **Zombie Hit (10%)** | **No** | Minor debuffs (Hunger, Nausea I) |
| **Stage 2** | Configurable (e.g., 5m) | N/A | **Yes** (Proximity & Contact) | Moderate debuffs (Slowness I, Weakness I, Nausea II) |
| **Stage Final** | Permanent | N/A | **Yes** (Proximity & Contact) | Severe debuffs (Slowness II, Weakness II, Blindness) |

### Curing Process

1.  An infected player must enter a staff-designated **Hospital Zone**.
2.  Upon entering, a BossBar appears showing the **"CURE IN PROGRESS"** timer.
3.  The player is **free to move** within the bounds of the hospital.
4.  If the player **leaves the hospital zone** at any point, the cure is immediately canceled.
5.  If the player stays for the full configurable duration, they are cured, the debuffs are removed, and the infection record is deleted.
