package me.jinou.globalshop.listener;

import me.jinou.globalshop.GlobalShop;
import me.jinou.globalshop.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author 69142
 */
public class GuiListener implements Listener {

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

        String guiName = ((GsInvHolder) holder).getInventoryName();
        if (guiName.equals(MsgUtil.get("gui-shop-title", false))) {
            shopClick(event);
            return;
        }
        if (guiName.equals(MsgUtil.get("gui-info-title", false))) {
            infoClick(event);
            return;
        }
    }

    private void infoClick(InventoryClickEvent event) {
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
                            player.getInventory().setItem(firstEmpty, dbShopItem.getItemStack());
                            player.sendMessage(MsgUtil.get("buy"));
                            Gui.openShopGui(player, page, type, filter);
                        }
                    }.runTask(GlobalShop.get());
                }
            }.runTaskAsynchronously(GlobalShop.get());
        }
    }

    private void shopClick(InventoryClickEvent event) {
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
    }
}
