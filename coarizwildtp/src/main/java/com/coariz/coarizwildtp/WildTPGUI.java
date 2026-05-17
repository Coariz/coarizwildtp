package com.coariz.coarizwildtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WildTPGUI implements Listener {

    private final CoarizWildTP plugin;
    private final ConcurrentHashMap<UUID, Inventory> openInventories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, BukkitRunnable> teleportTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Location> initialLocationMap = new ConcurrentHashMap<>();

    public WildTPGUI(CoarizWildTP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        FileConfiguration config = plugin.getConfig();
        String guiTitle = plugin.colorize(config.getString("gui.title", "&6Select Dimension"));

        // Create a single chest inventory (9 slots per row, 3 rows = 27 slots)
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        // Get configured worlds
        String overworldName = config.getString("dimensions.overworld.world", "world");
        String netherName = config.getString("dimensions.nether.world", "world_nether");
        String endName = config.getString("dimensions.end.world", "world_the_end");

        boolean overworldEnabled = config.getBoolean("dimensions.overworld.enabled", true);
        boolean netherEnabled = config.getBoolean("dimensions.nether.enabled", true);
        boolean endEnabled = config.getBoolean("dimensions.end.enabled", true);

        // Create items for each dimension
        ItemStack overworldItem = new ItemStack(Material.GRASS_BLOCK, 1);
        ItemStack netherItem = new ItemStack(Material.NETHERRACK, 1);
        ItemStack endItem = new ItemStack(Material.END_STONE, 1);

        // Set item names and lore
        org.bukkit.inventory.meta.ItemMeta meta;

        if (overworldEnabled) {
            meta = overworldItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.colorize(config.getString("gui.overworld.name", "&aOverworld")));
                meta.setLore(plugin.colorizeList(config.getStringList("gui.overworld.lore")));
                overworldItem.setItemMeta(meta);
            }
            gui.setItem(10, overworldItem); // Middle-left slot
        }

        if (netherEnabled) {
            meta = netherItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.colorize(config.getString("gui.nether.name", "&cNether")));
                meta.setLore(plugin.colorizeList(config.getStringList("gui.nether.lore")));
                netherItem.setItemMeta(meta);
            }
            gui.setItem(13, netherItem); // Center slot
        }

        if (endEnabled) {
            meta = endItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(plugin.colorize(config.getString("gui.end.name", "&dThe End")));
                meta.setLore(plugin.colorizeList(config.getStringList("gui.end.lore")));
                endItem.setItemMeta(meta);
            }
            gui.setItem(16, endItem); // Middle-right slot
        }

        // Fill remaining slots with glass panes for decoration
        Material glassMaterial = Material.matchMaterial(config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE")) != null 
            ? Material.matchMaterial(config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE")) 
            : Material.GRAY_STAINED_GLASS_PANE;
        ItemStack filler = new ItemStack(glassMaterial, 1);
        org.bukkit.inventory.meta.ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        // Open the inventory and track it
        player.openInventory(gui);
        openInventories.put(player.getUniqueId(), gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();

        // Check if this is our GUI
        if (!openInventories.containsKey(playerId)) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(openInventories.get(playerId))) return;

        // Cancel the event to prevent item manipulation (anti-dupe)
        event.setCancelled(true);

        int slot = event.getSlot();
        FileConfiguration config = plugin.getConfig();

        String overworldName = config.getString("dimensions.overworld.world", "world");
        String netherName = config.getString("dimensions.nether.world", "world_nether");
        String endName = config.getString("dimensions.end.world", "world_the_end");

        boolean overworldEnabled = config.getBoolean("dimensions.overworld.enabled", true);
        boolean netherEnabled = config.getBoolean("dimensions.nether.enabled", true);
        boolean endEnabled = config.getBoolean("dimensions.end.enabled", true);

        World targetWorld = null;

        // Determine which dimension was clicked
        if (overworldEnabled && slot == 10) {
            targetWorld = Bukkit.getWorld(overworldName);
        } else if (netherEnabled && slot == 13) {
            targetWorld = Bukkit.getWorld(netherName);
        } else if (endEnabled && slot == 16) {
            targetWorld = Bukkit.getWorld(endName);
        }

        if (targetWorld == null) {
            player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("invalid_world", "&cInvalid world selected.")));
            player.closeInventory();
            return;
        }

        // Close the GUI
        player.closeInventory();

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (plugin.cooldownMap.containsKey(playerId)) {
            long lastUsed = plugin.cooldownMap.get(playerId);
            long cooldownTime = config.getLong("cooldown", 5) * 1000L;
            if (currentTime - lastUsed < cooldownTime) {
                long remainingTime = (lastUsed + cooldownTime - currentTime) / 1000L;
                player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("cooldown_message", "&cYou must wait %seconds% more seconds before using this command again.").replace("%seconds%", String.valueOf(remainingTime))));
                return;
            }
        }

        // Check if economy is enabled
        if (plugin.isEconomyEnabled()) {
            double cost = config.getDouble("cost", 0);
            if (cost > 0) {
                if (plugin.getEconomy().getBalance(player) < cost) {
                    player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("not_enough_money", "&cYou don't have enough money to use this command.")));
                    return;
                }
                plugin.getEconomy().withdrawPlayer(player, cost);
            }
        }

        // Find safe location in the target world
        Location safeLocation = findSafeLocation(targetWorld);
        if (safeLocation == null) {
            player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("no_safe_location", "&cCould not find a safe location.")));
            return;
        }

        int teleportDelay = config.getInt("teleport_delay", 5);
        boolean allowMovementDuringDelay = config.getBoolean("allow_movement_during_delay", false);

        player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("teleport_delay_message", "&eTeleporting in %seconds% seconds...").replace("%seconds%", String.valueOf(teleportDelay))));

        // Store the player's initial location
        initialLocationMap.put(playerId, player.getLocation());

        // Create a BukkitRunnable task for teleportation
        BukkitRunnable teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    initialLocationMap.remove(playerId);
                    plugin.cooldownMap.remove(playerId);
                    teleportTasks.remove(playerId);
                    return;
                }

                if (!allowMovementDuringDelay) {
                    Location initialLocation = initialLocationMap.get(playerId);
                    if (initialLocation != null && player.getWorld().equals(initialLocation.getWorld())) {
                        if (player.getLocation().distanceSquared(initialLocation) > 0.1) {
                            player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("moved_during_delay", "&cYou moved during the teleportation delay. Teleportation cancelled.")));
                            initialLocationMap.remove(playerId);
                            plugin.cooldownMap.remove(playerId);
                            teleportTasks.remove(playerId);
                            return;
                        }
                    }
                }

                player.teleport(safeLocation);
                player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("teleport_success", "&aTeleported to a random location!")));
                initialLocationMap.remove(playerId);
                plugin.cooldownMap.put(playerId, currentTime);
                teleportTasks.remove(playerId);
            }
        };

        // Schedule the teleportation with a delay
        teleportTask.runTaskLater(plugin, teleportDelay * 20L);

        // Store the teleport task in the map
        teleportTasks.put(playerId, teleportTask);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove from tracking
        openInventories.remove(playerId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        boolean allowMovementDuringDelay = config.getBoolean("allow_movement_during_delay", false);

        if (!allowMovementDuringDelay && initialLocationMap.containsKey(playerId)) {
            Location initialLocation = initialLocationMap.get(playerId);
            if (initialLocation != null && player.getWorld().equals(initialLocation.getWorld())) {
                if (player.getLocation().distanceSquared(initialLocation) > 0.1) {
                    player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("moved_during_delay", "&cYou moved during the teleportation delay. Teleportation cancelled.")));
                    initialLocationMap.remove(playerId);
                    plugin.cooldownMap.remove(playerId);

                    // Cancel the teleport task
                    BukkitRunnable teleportTask = teleportTasks.remove(playerId);
                    if (teleportTask != null) {
                        teleportTask.cancel();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Clean up all tracking maps
        openInventories.remove(playerId);
        initialLocationMap.remove(playerId);
        plugin.cooldownMap.remove(playerId);

        // Cancel the teleport task
        BukkitRunnable teleportTask = teleportTasks.remove(playerId);
        if (teleportTask != null) {
            teleportTask.cancel();
        }
    }

    private Location findSafeLocation(World world) {
        Set<Material> blacklist = new HashSet<>();
        for (String materialName : plugin.getConfig().getStringList("blacklisted_blocks")) {
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                blacklist.add(material);
            }
        }

        Set<Material> allowedSurfaceMaterials = new HashSet<>();
        for (String materialName : plugin.getConfig().getStringList("allowed_surface_materials")) {
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                allowedSurfaceMaterials.add(material);
            }
        }

        // Add dimension-specific materials
        if (world.getEnvironment() == World.Environment.NETHER) {
            allowedSurfaceMaterials.add(Material.NETHERRACK);
            allowedSurfaceMaterials.add(Material.SOUL_SAND);
            allowedSurfaceMaterials.add(Material.SOUL_SOIL);
            allowedSurfaceMaterials.add(Material.BASALT);
            allowedSurfaceMaterials.add(Material.BLACKSTONE);
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            allowedSurfaceMaterials.add(Material.END_STONE);
        }

        WorldBorder worldBorder = world.getWorldBorder();
        Location spawnLocation = world.getSpawnLocation();
        double spawnRadius = plugin.getConfig().getDouble("spawn_radius", 1000);

        int centerX = plugin.getConfig().getInt("center_x", 0);
        int centerZ = plugin.getConfig().getInt("center_z", 0);
        int radius = plugin.getConfig().getInt("radius", 2000);

        int maxAttempts = plugin.getConfig().getInt("max_attempts", 500);
        boolean loggingEnabled = plugin.getConfig().getBoolean("logging_enabled", true);

        for (int i = 0; i < maxAttempts; i++) {
            int x = centerX + (int) (Math.random() * (2 * radius + 1)) - radius;
            int z = centerZ + (int) (Math.random() * (2 * radius + 1)) - radius;

            // Check if the location is within the world border
            if (!worldBorder.isInside(new Location(world, x, 0, z))) {
                if (loggingEnabled) {
                    plugin.getLogger().info("Attempt " + (i + 1) + ": Location (" + x + ", 0, " + z + ") is outside the world border.");
                }
                continue;
            }

            // Check if the location is too close to the spawn point
            Location loc = new Location(world, x, 0, z);
            if (loc.distance(spawnLocation) < spawnRadius) {
                if (loggingEnabled) {
                    plugin.getLogger().info("Attempt " + (i + 1) + ": Location (" + x + ", 0, " + z + ") is too close to the spawn point.");
                }
                continue;
            }

            int y = world.getHighestBlockYAt(x, z);
            Location safeLoc = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (isSafeSurfaceLocation(safeLoc, blacklist, allowedSurfaceMaterials)) {
                if (loggingEnabled) {
                    plugin.getLogger().info("Attempt " + (i + 1) + ": Found safe surface location: " + safeLoc);
                }
                return safeLoc;
            } else {
                if (loggingEnabled) {
                    Material below = world.getBlockAt(x, y - 1, z).getType();
                    plugin.getLogger().info("Attempt " + (i + 1) + ": Location (" + x + ", " + y + ", " + z + ") is not a safe surface location.");
                    plugin.getLogger().info("Block below: " + below);
                }
            }
        }

        if (loggingEnabled) {
            plugin.getLogger().info("Failed to find a safe surface location after " + maxAttempts + " attempts.");
        }
        return null;
    }

    private boolean isSafeSurfaceLocation(Location loc, Set<Material> blacklist, Set<Material> allowedSurfaceMaterials) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        World world = loc.getWorld();

        Material below = world.getBlockAt(x, y - 1, z).getType();

        if (below.isSolid() && !blacklist.contains(below) && allowedSurfaceMaterials.contains(below)) {
            return true;
        }
        return false;
    }

    public void cleanup() {
        HandlerList.unregisterAll(this);
    }
}
