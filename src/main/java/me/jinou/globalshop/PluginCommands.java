package me.jinou.globalshop;

import lombok.NonNull;
import me.jinou.globalshop.data.IDataManager;
import me.jinou.globalshop.utils.Gui;
import me.jinou.globalshop.utils.MsgUtil;
import me.jinou.globalshop.utils.ShopItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 69142
 */
public class PluginCommands implements CommandExecutor, TabCompleter {
    private static final FileConfiguration CONFIG = GlobalShop.getFileConfig();

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MsgUtil.get("error-player-only"));
                return false;
            }

            Player player = (Player) sender;
            Gui.openShopGui(player, 0, "all", "timeDescend");
            sender.sendMessage(MsgUtil.get("open-gui"));
            return true;
        }

        String helpCmd = "help";
        if (args[0].equalsIgnoreCase(helpCmd)) {
            sender.sendMessage(MsgUtil.get("help", false));
            return true;
        }

        String sellCmd = "sell";
        if (args[0].equalsIgnoreCase(sellCmd)) {
            return sellCmd(sender, args);
        }

        String reloadCmd = "reload";
        if (args.length == 1 && args[0].equalsIgnoreCase(reloadCmd)) {
            if (!sender.isOp()) {
                sender.sendMessage(MsgUtil.get("cmd-invalid"));
                return false;
            }
            GlobalShop.get().onReload();
            sender.sendMessage(MsgUtil.get("reload"));
            return true;
        }

        sender.sendMessage(MsgUtil.get("cmd-invalid"));
        return false;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        List<String> tabList = new ArrayList<>();
        if (args.length == 1) {
            tabList.add("sell");
            tabList.add("help");
            if (sender.isOp()) {
                tabList.add("reload");
            }
        }

        String sellCmd = "sell";
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase(sellCmd)) {
                tabList.add(MsgUtil.get("cmd-price", false));
            }
        }

        return tabList;
    }

    private boolean sellCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MsgUtil.get("error-player-only"));
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(MsgUtil.get("error-sell"));
            return false;
        }

        List<String> blackListItems = CONFIG.getStringList("shop.item-black-list");
        String itemMaterial = player.getInventory().getItemInMainHand().getType().toString();
        if (blackListItems.contains(itemMaterial)) {
            player.sendMessage(MsgUtil.get("error-black-list-item"));
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            price = Double.parseDouble(new DecimalFormat("#.00").format(price));
        } catch (NumberFormatException e) {
            player.sendMessage(MsgUtil.get("error-sell"));
            return false;
        }

        if (price <= 0
                || price < CONFIG.getDouble("shop.min-sell-price")
                || price > CONFIG.getDouble("shop.max-sell-price")) {
            player.sendMessage(MsgUtil.get("error-invalid-price"));
            return false;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MsgUtil.get("error-empty-hand"));
            return false;
        }

        IDataManager dataManager = GlobalShop.get().getDataManager();
        double finalPrice = price;

        new BukkitRunnable() {
            @Override
            public void run() {
                List<ShopItem> playerShopItems = dataManager.getPlayerShopItems(player.getUniqueId(), 0, CONFIG.getInt("shop.sell-limit"));
                int uid = dataManager.generateUid();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }

                        if (playerShopItems.size() >= CONFIG.getInt("shop.sell-limit")) {
                            player.sendMessage(MsgUtil.get("error-sell-limit"));
                            return;
                        }

                        ShopItem shopItem = new ShopItem(
                                uid,
                                player.getName(),
                                player.getUniqueId(),
                                itemInHand,
                                finalPrice,
                                "sell",
                                System.currentTimeMillis()
                        );

                        if (uid >= 0) {
                            GlobalShop.get().getDataManager().addShopItem(shopItem);
                            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            player.sendMessage(MsgUtil.get("sell"));
                        }
                    }
                }.runTask(GlobalShop.get());
            }
        }.runTaskAsynchronously(GlobalShop.get());

        return true;
    }
}
