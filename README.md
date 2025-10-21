# Astradal Halloween Plague Plugin

**A PaperMC plugin for a progressive, contagious plague designed for a Halloween event.**

This plugin introduces a multi-stage disease that spreads via contact and proximity, requiring staff-designated "Hospital Zones" for curing.

## ✨ Features

  * **Progressive Infection:** The plague advances through three configurable stages, increasing debuffs over time.
  * **Contagion:** Players spread the plague through physical contact (hitting) or close proximity to infected players (Stage 2+).
  * **Configurable Effects:** All debuffs (Slowness, Weakness, Blindness, etc.) and progression timers are configurable in `config.yml`.
  * **Hospital Zones:** Staff can designate cure zones where infected players must stand still for a configurable duration to be cured, visible via a BossBar timer.
  * **Database Persistence:** Infection and Hospital Zone data are stored persistently using SQLite.
  * **Modern API:** Built using the latest PaperMC API, Kyori Adventure components for messaging, and Brigadier for commands.

## 🛠️ Installation & Setup

### Requirements

  * **Server Type:** PaperMC (Recommended 1.18+)
  * **Java Version:** Java 17+

### Steps

1.  Download the latest `AstradalHalloweenPlague.jar` from the release page.
2.  Place the JAR file into your server's `plugins/` directory.
3.  Start the server once to generate the plugin folder and default configuration (`config.yml`).
4.  Stop the server and edit `config.yml` to tune plague effects and timers.
5.  Restart the server.

-----

## ⚙️ Configuration (`config.yml`)

The primary configuration controls the progression and cure time.

```yaml
progression_settings:

  # Proximity infection radius in blocks (used in PlagueProgressionTask)
  infection_radius: 5.0 

  # Time (in seconds) required for a player to be cured in a hospital zone.
  cure_time_seconds: 45 

  stages:
    
    # STAGE ONE: Initial Incubation / Minor Symptoms
    STAGE_ONE:
      time_to_next_stage_seconds: 180 # 3 minutes
      effects:
        # Format: EFFECT_TYPE (modern name), AMPLIFIER (0=I, 1=II), DURATION_SECONDS
        - "HUNGER, 0, 6"
        - "NAUSEA, 0, 3" 
        
    # STAGE TWO: Highly Contagious - Proximity spread becomes active here
    STAGE_TWO:
      time_to_next_stage_seconds: 300 # 5 minutes
      effects:
        - "WEAKNESS, 0, 6"
        - "SLOWNESS, 0, 6"
        - "NAUSEA, 1, 5"
        
    # STAGE FINAL: Critical / Maximum Debuffs
    STAGE_FINAL:
      time_to_next_stage_seconds: 0 # No further progression
      effects:
        - "WEAKNESS, 1, 6"
        - "SLOWNESS, 1, 6"
        - "BLINDNESS, 0, 3"
        - "POISON, 0, 1"
```

-----

## 💻 Staff Commands

All administrative commands use the root alias `/plague` or `/p`. All commands below require the permission `astradalplague.admin.<subcommand>`.

### Plague Management

| Command | Usage | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/plague help` | | `astradalplague.admin.help` | Displays the help menu. |
| `/plague infect <player>` | `/p infect Pookachu` | `astradalplague.admin.infect` | Manually infects a target player (starts at Stage 1). |
| `/plague cure <player>` | `/p cure Pookachu` | `astradalplague.admin.cure` | Manually removes the plague from a player. |
| `/plague stage <player> <stage>` | `/p stage Pookachu 3` | `astradalplague.admin.stage` | Forcefully sets the infected player's plague stage (1, 2, or 3). |
| `/plague reload` | | `astradalplague.admin.reload` | Reloads `config.yml` and re-initializes all stages/timers. |

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

| Stage | Duration | Contagious? | Debuffs |
| :--- | :--- | :--- | :--- |
| **Stage 1** | Configurable (e.g., 3m) | **No** | Minor debuffs (Hunger, Nausea I) |
| **Stage 2** | Configurable (e.g., 5m) | **Yes** (Proximity & Contact) | Moderate debuffs (Slowness I, Weakness I, Nausea II) |
| **Stage Final** | Permanent | **Yes** (Proximity & Contact) | Severe debuffs (Slowness II, Weakness II, Blindness) |

### Curing Process

1.  An infected player must enter a staff-designated **Hospital Zone**.
2.  Upon entering, a BossBar appears showing the **"CURE IN PROGRESS"** timer (e.g., 45 seconds).
3.  The player is **free to move** within the bounds of the hospital.
4.  If the player **leaves the hospital zone** at any point, the cure is immediately canceled.
5.  If the player stays for the full duration, they are cured, the debuffs are removed, and the infection record is deleted.
