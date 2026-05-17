# CoarizWildTP - Random Teleportation Plugin

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

**CoarizWildTP** is a powerful and customizable Minecraft plugin that allows players to teleport to random safe locations across multiple dimensions. Perfect for exploration, survival gameplay, and adventure maps!

---

## 🌟 Features

### Core Features
- ✅ **Random Teleportation** - Teleport to random safe locations in any configured dimension
- ✅ **Multi-Dimension Support** - Overworld, Nether, and The End support out of the box
- ✅ **Safe Location Detection** - Automatically finds safe spawn points (no lava, water, or dangerous blocks)
- ✅ **Economy Integration** - Charge players for teleportation using Vault-compatible economy plugins
- ✅ **Cooldown System** - Prevent spam with configurable cooldowns
- ✅ **Teleport Delay** - Add anticipation with configurable delay before teleportation
- ✅ **Movement Prevention** - Option to cancel teleport if player moves during delay
- ✅ **Anti-Dupe Protection** - Inventory locked during GUI interaction to prevent exploits
- ✅ **Customizable GUI** - Beautiful chest GUI for dimension selection
- ✅ **Configurable Worlds** - Fully customizable world names for each dimension
- ✅ **Logging System** - Optional logging for debugging and monitoring
- ✅ **Color Support** - Full support for legacy color codes (&) and hex colors (<#RRGGBB>)

### GUI Features
- 📦 **Single Chest Interface** - Clean 27-slot inventory GUI
- 🎨 **Dimension Selection** - Visual representation with iconic blocks:
  - 🌿 **Grass Block** - Overworld teleportation
  - 🔥 **Netherrack** - Nether teleportation  
  - ✨ **End Stone** - The End teleportation
- 🛡️ **Exploit Prevention** - Inventory locked during interaction
- ⚙️ **Fully Customizable** - Names, lore, titles, and filler materials

---

## 📋 Requirements

- **Minecraft Server**: 1.21.11 (or compatible 1.21.x versions)
- **Java**: 17 or higher
- **Dependencies** (Optional):
  - [Vault](https://www.spigotmc.org/resources/vault.34315/) - Required for economy features
  - Any Vault-compatible economy plugin (e.g., EssentialsX Economy, CMI, etc.)

---

## 📥 Installation

1. **Download** the latest `CoarizWildTP-1.0-SNAPSHOT.jar` from the releases page or build it yourself
2. **Place** the JAR file in your server's `plugins/` directory
3. **Start** or **restart** your server
4. **Configure** the plugin by editing `plugins/CoarizWildTP/config.yml`
5. **Reload** the configuration with `/coarizwildtp reload` (optional, config loads on start)

---

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/wild` | Teleport to a random location in the overworld | `coarizwildtp.use` |
| `/rtp` | Alias for `/wild` | `coarizwildtp.use` |
| `/coarizwildtp gui` | Open the dimension selection GUI | `coarizwildtp.use` |
| `/coarizwildtp reload` | Reload plugin configuration | `coarizwildtp.reload` |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `coarizwildtp.use` | Allows players to use teleport commands and GUI | OP |
| `coarizwildtp.reload` | Allows admins to reload configuration | OP |

---

## ⚙️ Configuration

After first run, edit `plugins/CoarizWildTP/config.yml`:

### Basic Settings

```yaml
# World name for overworld teleportation
world: world

# Teleportation cost (requires Vault + economy plugin)
cost: 0

# Maximum X/Z coordinates for teleportation
max_x: 5000
max_z: 5000
min_x: -5000
min_z: -5000

# Spawn protection radius (distance from spawn where teleportation is disabled)
spawn_radius: 500

# Center point coordinates for random teleportation
center_x: 0
center_z: 0

# Radius around center point for teleportation
radius: 1000

# Maximum attempts to find a safe location (lower for budget servers: 100-250)
max_attempts: 500

# Enable/disable logging for debugging
logging_enabled: false

# Delay before teleportation (in seconds)
teleport_delay: 5

# Cooldown between teleports (in seconds)
cooldown: 5

# Allow movement during teleport delay (if false, teleport cancels on movement)
allow_movement_during_delay: false
```

### Dimension Configuration

Configure which worlds to use for each dimension:

```yaml
dimensions:
  overworld:
    enabled: true
    world: world  # Change to your overworld name
  nether:
    enabled: true
    world: world_nether  # Change to your nether name
  end:
    enabled: true
    world: world_the_end  # Change to your end name
```

### GUI Configuration

Customize the dimension selection GUI:

```yaml
gui:
  title: "&6Select Dimension"  # GUI title (supports color codes)
  filler:
    material: GRAY_STAINED_GLASS_PANE  # Filler item for empty slots
  overworld:
    name: "&aOverworld"
    lore:
      - "&7Teleport to a random location"
      - "&7in the Overworld"
  nether:
    name: "&cNether"
    lore:
      - "&7Teleport to a random location"
      - "&7in the Nether"
  end:
    name: "&dThe End"
    lore:
      - "&7Teleport to a random location"
      - "&7in The End"
```

### Block Lists

Configure which blocks are safe/unsafe for teleportation:

```yaml
# Blocks where teleportation is NOT allowed
blacklisted_blocks:
  - WATER
  - LAVA
  - FIRE
  - CACTUS
  - SWEET_BERRY_BUSH
  - BAMBOO
  - BAMBOO_SAPLING

# Safe surface blocks for teleportation (overworld)
allowed_surface_materials:
  - GRASS_BLOCK
  - DIRT
  - COARSE_DIRT
  - PATH
  - STONE
  - COBBLESTONE
  # ... (and many more)
```

> **Note**: Nether and End-specific blocks are automatically added based on dimension.

---

## 💰 Economy Integration

CoarizWildTP supports economy charges through Vault:

1. Install **Vault** plugin
2. Install a compatible economy plugin (EssentialsX, CMI, etc.)
3. Set `cost` in config.yml to desired amount
4. Players will be charged when teleporting

If Vault or an economy plugin is not found, economy features are automatically disabled.

---

## 🛡️ Security Features

### Anti-Dupe Protection
- Inventory clicks are cancelled during GUI interaction
- Player inventory is locked while GUI is open
- Proper cleanup on inventory close, player quit, or teleport cancellation

### Exploit Prevention
- Movement detection during teleport delay
- Automatic teleport cancellation if player moves (when enabled)
- Cooldown enforcement to prevent spam
- Safe location validation prevents spawning in dangerous areas

---

## 📊 How It Works

### Teleportation Algorithm

1. **Generate Random Coordinates** within configured radius
2. **Check World Border** - Ensure location is inside world border
3. **Check Spawn Radius** - Ensure location is far enough from spawn
4. **Find Surface Height** - Get highest block Y at coordinates
5. **Validate Safety** - Check if location is on allowed surface material
6. **Check Blacklist** - Ensure no dangerous blocks nearby
7. **Repeat** up to `max_attempts` times if unsafe
8. **Teleport** player after delay (if configured)

### Dimension-Specific Safety

The plugin automatically adjusts safe materials based on dimension:
- **Overworld**: Grass, dirt, stone, wood, leaves, etc.
- **Nether**: Netherrack, soul sand, soul soil, basalt, blackstone
- **The End**: End stone

---

## 🔧 Developer API

### Accessing Plugin Instance

```java
CoarizWildTP plugin = (CoarizWildTP) Bukkit.getPluginManager().getPlugin("CoarizWildTP");
```

### Checking Cooldown Status

```java
UUID playerId = player.getUniqueId();
if (plugin.cooldownMap.containsKey(playerId)) {
    // Player is on cooldown
}
```

### Opening GUI Programmatically

```java
plugin.getWildTPGUI().openGUI(player);
```

---

## 🐛 Troubleshooting

### Common Issues

**"Vault or economy plugin not found"**
- Install Vault and a compatible economy plugin
- Restart server after installation

**"Could not find a safe location"**
- Increase `max_attempts` in config
- Expand `radius` or adjust `center_x`/`center_z`
- Check if world is properly loaded

**"Invalid world selected"**
- Verify world names in `dimensions` section match actual world names
- Ensure world is loaded in `server.properties` or multiverse

**GUI not opening**
- Check permission `coarizwildtp.use`
- Verify command is `/coarizwildtp gui`

**Teleportation not working**
- Check console for errors
- Verify `logging_enabled: true` for debug info
- Ensure world border isn't too restrictive

---

## 📝 Building from Source

### Prerequisites
- Java 17+
- Maven 3.6+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/CoarizWildTP.git
cd CoarizWildTP

# Build with Maven
mvn clean package

# Find JAR in target/ directory
ls target/CoarizWildTP-1.0-SNAPSHOT.jar
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 Support

- **Issues**: Report bugs and feature requests on the [GitHub Issues](https://github.com/yourusername/CoarizWildTP/issues) page
- **Discord**: [Join our Discord server](#) (coming soon)
- **Wiki**: [Read the full documentation](#) (coming soon)

---

## 🙏 Credits

- **Developer**: Coariz
- **Contributors**: [List contributors here]
- **Special Thanks**: Vault team, SpigotMC community

---

## 📈 Changelog

### Version 1.0
- ✅ Initial release
- ✅ Multi-dimension support (Overworld, Nether, End)
- ✅ GUI-based dimension selection
- ✅ Economy integration via Vault
- ✅ Configurable cooldowns and delays
- ✅ Safe location detection algorithm
- ✅ Anti-dupe and exploit protection
- ✅ Full configuration customization
- ✅ Color code support (legacy & hex)
- ✅ Minecraft 1.21.11 support

---

<div align="center">

**Made with ❤️ by Coariz**

If you enjoy this plugin, please consider giving it a ⭐ star on GitHub!

</div>
