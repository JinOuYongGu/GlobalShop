package me.jinou.globalshop.utils;

import lombok.Getter;
import me.jinou.globalshop.GlobalShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * @author 69142
 */
public class EcoVault {
    @Getter
    private static Economy economy = null;

    public static boolean setupEconomy() {
        if (GlobalShop.get().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = GlobalShop.get().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static double getMoney(UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            return economy.getBalance(player);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            return economy.getBalance(offlinePlayer);
        }
    }

    public static void removeMoney(UUID playerUuid, double amount) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            economy.withdrawPlayer(player, amount);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            economy.withdrawPlayer(offlinePlayer, amount);
        }
    }

    public static void addMoney(UUID playerUuid, double amount) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            economy.depositPlayer(player, amount);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            economy.depositPlayer(offlinePlayer, amount);
        }
    }
}
