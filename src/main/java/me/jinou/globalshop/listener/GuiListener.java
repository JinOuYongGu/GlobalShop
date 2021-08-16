package me.jinou.globalshop.listener;

import lombok.NonNull;
import me.jinou.globalshop.GlobalShop;
import me.jinou.globalshop.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author 69142
 */
public class GuiListener implements Listener {
    private static HashMap<UUID, Long> playerClickTimes = new HashMap<>();

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof GsInvHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getRawSlot() < 0) {
            return;
        }

        UUID playerUuid = event.getWhoClicked().getUniqueId();
        if (!playerClickTimes.containsKey(playerUuid)) {
            playerClickTimes.put(playerUuid, System.currentTimeMillis());
        }
        if (System.currentTimeMillis() - playerClickTimes.get(playerUuid)
                < GlobalShop.getFileConfig().getLong("shop.gui-min-click-interval")) {
            return;
        }

        String guiName = ((GsInvHolder) holder).getInventoryName();
        if (guiName.equals(MsgUtil.get("gui-shop-title", false))) {
            shopClick(event);
            return;
        }
        if (guiName.equals(MsgUtil.get("gui-info-title", false))) {
            infoClick(event);
            return;
        }
        if (guiName.equals(MsgUtil.get("gui-self-item-title", false))) {
            selfClick(event);
            return;
        }
    }

    private void selfClick(@NonNull InventoryClickEvent event) {
        int slotId = event.getRawSlot();
        if (slotId < 0 || slotId > 6 * 9 - 1) {
            return;
        }

        Inventory guiInv = event.getInventory();
        GsInvHolder guiHolder = (GsInvHolder) guiInv.getHolder();
        if (guiHolder == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int page = guiHolder.getCurrentPage();
        String type = guiHolder.getType();
        String filter = guiHolder.getFilter();

        // Click next page
        if (slotId == 50) {
            if (guiHolder.getShopItems().size() < 45) {
                return;
            }
            Gui.openSelfGui(player, page + 1);
            return;
        }

        // Click prev page
        if (slotId == 48) {
            if (guiHolder.getCurrentPage() == 0) {
                return;
            }
            Gui.openSelfGui(player, page - 1);
        }

        // Click back icon
        if (slotId == 46) {
            Gui.openShopGui(player, page, type, filter);
            return;
        }

        // Click self item icon
        if (slotId <= guiHolder.getShopItems().size() - 1) {
            ShopItem guiShopItem = guiHolder.getShopItems().get(slotId);
            int uid = guiShopItem.getUid();

            new BukkitRunnable() {
                @Override
                public void run() {
                    ShopItem dbShopItem = GlobalShop.get().getDataManager().getShopItem(uid);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }

                            if (dbShopItem == null) {
                                player.sendMessage(MsgUtil.get("error-no-such-item"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-no-such-item", false));
                                return;
                            }

                            int firstEmpty = player.getInventory().firstEmpty();
                            if (firstEmpty == -1) {
                                player.sendMessage(MsgUtil.get("error-full-inv"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-full-inv", false));
                                return;
                            }

                            GlobalShop.get().getDataManager().removeShopItem(uid);

                            player.getInventory().setItem(firstEmpty, dbShopItem.getItemStack());
                            player.sendMessage(MsgUtil.get("back"));
                            Gui.openSelfGui(player, page);
                        }
                    }.runTask(GlobalShop.get());
                }
            }.runTaskAsynchronously(GlobalShop.get());
        }
    }

    private void infoClick(@NonNull InventoryClickEvent event) {
        int slotId = event.getRawSlot();
        if (slotId < 0 || slotId > 8) {
            return;
        }

        Inventory guiInv = event.getInventory();
        GsInvHolder guiHolder = (GsInvHolder) guiInv.getHolder();
        if (guiHolder == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int page = guiHolder.getCurrentPage();
        String type = guiHolder.getType();
        String filter = guiHolder.getFilter();

        // Click cancel icon
        if (slotId >= 5) {
            Gui.openShopGui(player, page, type, filter);
            return;
        }

        // Click accept icon
        if (slotId <= 3) {
            ShopItem shopItem = guiHolder.getShopItems().get(0);
            int uid = shopItem.getUid();

            new BukkitRunnable() {
                @Override
                public void run() {
                    ShopItem dbShopItem = GlobalShop.get().getDataManager().getShopItem(uid);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }

                            if (dbShopItem == null) {
                                player.sendMessage(MsgUtil.get("error-no-such-item"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-no-such-item", false));
                                return;
                            }

                            if (EcoVault.getMoney(player.getUniqueId()) < dbShopItem.getPrice()) {
                                player.sendMessage(MsgUtil.get("error-lack-money"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-lack-money", false));
                                return;
                            }

                            int firstEmpty = player.getInventory().firstEmpty();
                            if (firstEmpty == -1) {
                                player.sendMessage(MsgUtil.get("error-full-inv"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-full-inv", false));
                                return;
                            }

                            if (System.currentTimeMillis() - dbShopItem.getCreateTime()
                                    > GlobalShop.getFileConfig().getLong("shop.valid-time-in-mins") * 60 * 1000) {
                                player.sendMessage(MsgUtil.get("error-no-such-item"));
                                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("error-no-such-item", false));
                                return;
                            }

                            GlobalShop.get().getDataManager().removeShopItem(uid);

                            EcoVault.removeMoney(player.getUniqueId(), dbShopItem.getPrice());
                            EcoVault.addMoney(dbShopItem.getOwnerId(), dbShopItem.getPrice());
                            player.getInventory().setItem(firstEmpty, dbShopItem.getItemStack());
                            player.sendMessage(MsgUtil.get("buy"));
                            Gui.openShopGui(player, page, type, filter);
                        }
                    }.runTask(GlobalShop.get());
                }
            }.runTaskAsynchronously(GlobalShop.get());
        }
    }

    private void shopClick(@NonNull InventoryClickEvent event) {
        int slotId = event.getRawSlot();
        if (slotId < 0 || slotId > 6 * 9 - 1) {
            return;
        }

        Inventory guiInv = event.getInventory();
        GsInvHolder guiHolder = (GsInvHolder) guiInv.getHolder();
        if (guiHolder == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int page = guiHolder.getCurrentPage();
        String type = guiHolder.getType();
        String filter = guiHolder.getFilter();

        // Click a shop item
        if (slotId <= guiHolder.getShopItems().size() - 1) {
            ShopItem shopItem = guiHolder.getShopItems().get(slotId);
            if (shopItem.getOwnerId().equals(player.getUniqueId())) {
                Gui.setIconTitle(guiInv, slotId, MsgUtil.get("gui-self-item", false));
                return;
            }
            Gui.openInfoGui(player, shopItem, page, type, filter);
            return;
        }

        // Click next page
        if (slotId == 50) {
            if (guiHolder.getShopItems().size() < 45) {
                return;
            }
            Gui.openShopGui(player, page + 1, type, filter);
            return;
        }

        // Click prev page
        if (slotId == 48) {
            if (guiHolder.getCurrentPage() == 0) {
                return;
            }
            Gui.openShopGui(player, page - 1, type, filter);
        }

        // Click filter icon
        if (slotId == 52) {
            if ("timeDescend".equalsIgnoreCase(filter)) {
                Gui.openShopGui(player, page, type, "timeAscend");
            } else if ("timeAscend".equalsIgnoreCase(filter)) {
                Gui.openShopGui(player, page, type, "priceDescend");
            } else if ("priceDescend".equalsIgnoreCase(filter)) {
                Gui.openShopGui(player, page, type, "priceAscend");
            } else if ("priceAscend".equalsIgnoreCase(filter)) {
                Gui.openShopGui(player, page, type, "timeDescend");
            }
            return;
        }

        // Click self item icon
        if (slotId == 46) {
            Gui.openSelfGui(player, 0);
        }
    }
}
