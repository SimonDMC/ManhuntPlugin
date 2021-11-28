package me.simondmc.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Manhunt extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        updateCompass();
    }

    @Override
    public void onDisable() {}

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("runner")) {
            if (!(sender instanceof Player)) return true;
            Player player = (Player) sender;
            player.sendMessage(ChatColor.YELLOW + "You are now a runner.");
            player.addScoreboardTag("runner");
            player.removeScoreboardTag("hunter");
            return true;
        } else if (label.equalsIgnoreCase("hunter")) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.YELLOW + "You are now a hunter.");
            player.addScoreboardTag("hunter");
            player.removeScoreboardTag("runner");
            player.getInventory().setItem(8, new ItemStack(Material.COMPASS));
            return true;
        }
        return false;
    }

    // remove compass from hunter death drops
    @EventHandler
    public void hunterDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!player.getScoreboardTags().contains("hunter")) return;
        event.getDrops().removeIf(i -> i.getType().equals(Material.COMPASS));
    }

    // give compass whenever a hunter respawns
    @EventHandler
    public void hunterRespawn(PlayerRespawnEvent event) {
        Player player = (Player) event.getPlayer();
        if (!player.getScoreboardTags().contains("hunter")) return;
        player.getInventory().setItem(8, new ItemStack(Material.COMPASS));
    }

    // updates the hunters' compass to track the nearest runner every second (20t)
    public void updateCompass() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player hunter : Bukkit.getOnlinePlayers()) {
                    if (!hunter.getScoreboardTags().contains("hunter")) continue;
                    Location l = null;
                    // gets the nearest runner
                    Player nearestRunner = null;
                    for (Player runner : Bukkit.getServer().getOnlinePlayers()) {
                        if (!runner.getScoreboardTags().contains("runner")) continue;
                        if (nearestRunner == null) nearestRunner = runner;
                        else if (runner.getLocation().distance(hunter.getLocation()) < nearestRunner.getLocation().distance(hunter.getLocation())) nearestRunner = runner;
                    }
                    // return if no runner found
                    if (nearestRunner == null) return;
                    l = nearestRunner.getLocation();
                    // set their compass to track them
                    hunter.setCompassTarget(l);
                }
            }
        }, 0, 20);
    }

    @EventHandler
    public void informPlayer(PlayerJoinEvent event) {
        if (event.getPlayer().getScoreboardTags().contains("informed")) return;
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',"&eTo select Manhunt roles, use &a/runner &eand &a/hunter"));
        event.getPlayer().addScoreboardTag("informed");
    }
}