package me.jinou.globalshop;

import lombok.Getter;
import me.jinou.globalshop.data.DatabaseManager;
import me.jinou.globalshop.data.IDataManager;
import me.jinou.globalshop.listener.GuiListener;
import me.jinou.globalshop.nms.ItemStackSerializer;
import me.jinou.globalshop.nms.Serializer116R3;
import me.jinou.globalshop.utils.EcoVault;
import me.jinou.globalshop.utils.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * @author 69142
 */
public final class GlobalShop extends JavaPlugin {
    @Getter
    private static FileConfiguration fileConfig = null;
    private static GlobalShop instance;
    private static ItemStackSerializer itemStackSerializer = null;
    private static IDataManager dataManager = null;

    public static GlobalShop get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        fileConfig = getConfig();
        MsgUtil.updateMsg();

        if (!EcoVault.setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        itemStackSerializer = new Serializer116R3();

        if (fileConfig.getBoolean("mysql.enable")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    dataManager = new DatabaseManager();
                }
            }.runTaskAsynchronously(this);
            getLogger().info("Using MySQL");
        }

        PluginCommands pc = new PluginCommands();
        Objects.requireNonNull(Bukkit.getPluginCommand("globalshop")).setExecutor(pc);
        Objects.requireNonNull(Bukkit.getPluginCommand("globalshop")).setTabCompleter(pc);

        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.close();
        }
    }

    public void onReload() {
        reloadConfig();
        fileConfig = getConfig();
        MsgUtil.updateMsg();
        dataManager.update();
    }

    public ItemStackSerializer getItemStackSerializer() {
        return itemStackSerializer;
    }

    public IDataManager getDataManager() {
        return dataManager;
    }
}
