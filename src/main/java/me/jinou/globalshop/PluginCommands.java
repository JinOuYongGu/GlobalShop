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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 69142
 */
public class PluginCommands implements CommandExecutor, TabCompleter {
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

        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(MsgUtil.get("error-sell"));
            return false;
        }

        // TODO check price
        if (price <= 0) {
            player.sendMessage(MsgUtil.get("error-sell"));
            return false;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MsgUtil.get("error-item"));
            return false;
        }

        IDataManager dataManager = GlobalShop.get().getDataManager();
        double finalPrice = price;
        new BukkitRunnable() {

            @Override
            public void run() {
                int uid = dataManager.generateUid();
                new BukkitRunnable() {

                    @Override
                    public void run() {
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
