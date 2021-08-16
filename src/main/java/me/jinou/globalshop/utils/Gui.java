package me.jinou.globalshop.utils;

import me.jinou.globalshop.GlobalShop;
import me.jinou.globalshop.data.IDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 69142
 */
public class Gui {
    private static final GlobalShop PLUGIN = GlobalShop.get();
    private static final IDataManager DATA_MANAGER = PLUGIN.getDataManager();

    public static void openShopGui(Player player, int page, String type, String filter) {
        GsInvHolder holder = new GsInvHolder();
        holder.setCurrentPage(page);
        String guiName = MsgUtil.get("gui-shop-title", false);
        holder.setInventoryName(guiName);
        holder.setType(type);
        holder.setFilter(filter);
        Inventory inventory = Bukkit.createInventory(holder, 9 * 6, guiName);

        ItemStack selfItemIcon = makeIcon(Material.ENDER_CHEST,
                "gui-self-item-icon-title",
                "gui-self-item-icon-lore");

        ItemStack prevPageIcon = makeIcon(Material.PINK_DYE,
                "gui-prev-page-icon-title",
                "gui-prev-page-icon-lore");

        ItemStack nextPageIcon = makeIcon(Material.LIME_DYE,
                "gui-next-page-icon-title",
                "gui-next-page-icon-lore");

        String filterIconLoreKey = "";
        if ("timeAscend".equalsIgnoreCase(filter)) {
            filterIconLoreKey = "gui-filter-icon-ta-lore";
        }
        if ("timeDescend".equalsIgnoreCase(filter)) {
            filterIconLoreKey = "gui-filter-icon-td-lore";
        }
        if ("priceAscend".equalsIgnoreCase(filter)) {
            filterIconLoreKey = "gui-filter-icon-pa-lore";
        }
        if ("priceDescend".equalsIgnoreCase(filter)) {
            filterIconLoreKey = "gui-filter-icon-pd-lore";
        }

        ItemStack filterIcon = makeIcon(Material.ENDER_PEARL,
                "gui-filter-icon-title",
                filterIconLoreKey);

        inventory.setItem(46, selfItemIcon);
        inventory.setItem(48, prevPageIcon);
        inventory.setItem(50, nextPageIcon);
        inventory.setItem(52, filterIcon);

        new BukkitRunnable() {
            @Override
            public void run() {
                List<ShopItem> shopItems = DATA_MANAGER.getShopItems(type, filter, page * 45, 5 * 9);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int idx = 0; idx < shopItems.size(); idx++) {
                            ShopItem shopItem = shopItems.get(idx);

                            // Create shop item icon
                            inventory.setItem(idx, createShopItemIcon(shopItem));
                        }
                        GsInvHolder shopHolder = (GsInvHolder) (inventory.getHolder());
                        if (shopHolder == null) {
                            return;
                        }
                        shopHolder.setShopItems(shopItems);

                        player.openInventory(inventory);
                    }
                }.runTask(PLUGIN);
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    private static ItemStack createShopItemIcon(ShopItem shopItem) {
        ItemStack icon = shopItem.getItemStack();
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }

        String itemType;
        if ("sell".equalsIgnoreCase(shopItem.getType())) {
            itemType = MsgUtil.get("gui-shop-item-type-sell", false);
        } else {
            itemType = MsgUtil.get("gui-shop-item-type-buy", false);
        }

        long validTime = GlobalShop.getFileConfig().getLong("shop.valid-time-in-mins") * 60 * 1000 -
                (System.currentTimeMillis() - shopItem.getCreateTime());
        long validDay = validTime / (24 * 60 * 60 * 1000);
        validTime = validDay > 0 ? validTime % (validDay * 24 * 60 * 60 * 1000) : validTime;
        long validHour = validTime / (60 * 60 * 1000);
        validTime = validHour > 0 ? validTime % (validHour * 60 * 60 * 1000) : validTime;
        long validMinute = validTime / (60 * 1000);
        validTime = validMinute > 0 ? validTime % (validMinute * 60 * 1000) : validTime;

        String dayLore = "";
        if (validDay > 0) {
            dayLore = validDay + MsgUtil.get("day", false);
        }
        String hourLore = "";
        if (validHour > 0) {
            hourLore = validHour + MsgUtil.get("hour", false);
        }
        String minLore = "";
        if (validMinute > 0) {
            minLore = validMinute + MsgUtil.get("minute", false);
        }

        List<String> iconLore;
        if (meta.getLore() != null) {
            iconLore = meta.getLore();
        } else {
            iconLore = new ArrayList<>();
        }
        for (String lore : MsgUtil.getList("gui-shop-item-lore")) {
            lore = lore.replace("{OWNER}", shopItem.getOwnerName());
            lore = lore.replace("{TYPE}", itemType);
            lore = lore.replace("{PRICE}", String.valueOf(shopItem.getPrice()));
            lore = lore.replace("{DAY}", dayLore);
            lore = lore.replace("{HOUR}", hourLore);
            lore = lore.replace("{MINUTE}", minLore);
            iconLore.add(lore);
        }
        if (validTime < 0) {
            iconLore.addAll(MsgUtil.getList("gui-item-expired-lore"));
        }

        meta.setLore(iconLore);
        icon.setItemMeta(meta);
        return icon;
    }

    public static void openInfoGui(Player player, ShopItem shopItem, int page, String type, String filter) {
        GsInvHolder holder = new GsInvHolder();
        String guiName = MsgUtil.get("gui-info-title", false);
        holder.setInventoryName(guiName);
        List<ShopItem> shopItems = new ArrayList<>();
        shopItems.add(shopItem);
        holder.setShopItems(shopItems);
        holder.setCurrentPage(page);
        holder.setType(type);
        holder.setFilter(filter);
        Inventory inventory = Bukkit.createInventory(holder, 9, guiName);

        inventory.setItem(4, shopItem.getItemStack());

        ItemStack acceptIcon = makeIcon(Material.GREEN_STAINED_GLASS_PANE,
                "gui-accept-icon-title",
                "gui-accept-icon-lore");

        ItemStack cancelIcon = makeIcon(Material.RED_STAINED_GLASS_PANE,
                "gui-cancel-icon-title",
                "gui-cancel-icon-lore");

        inventory.setItem(0, acceptIcon);
        inventory.setItem(1, acceptIcon);
        inventory.setItem(2, acceptIcon);
        inventory.setItem(3, acceptIcon);
        inventory.setItem(4, shopItem.getItemStack());
        inventory.setItem(5, cancelIcon);
        inventory.setItem(6, cancelIcon);
        inventory.setItem(7, cancelIcon);
        inventory.setItem(8, cancelIcon);

        player.openInventory(inventory);
    }

    public static void openSelfGui(Player player, int page) {
        GsInvHolder guiHolder = new GsInvHolder();
        guiHolder.setCurrentPage(page);
        String guiName = MsgUtil.get("gui-self-item-title", false);
        guiHolder.setInventoryName(guiName);
        Inventory inventory = Bukkit.createInventory(guiHolder, 9 * 6, guiName);

        ItemStack backShopIcon = makeIcon(Material.CHEST,
                "gui-back-shop-icon-title",
                "gui-back-shop-icon-lore");

        ItemStack prevPageIcon = makeIcon(Material.PINK_DYE,
                "gui-prev-page-icon-title",
                "gui-prev-page-icon-lore");

        ItemStack nextPageIcon = makeIcon(Material.LIME_DYE,
                "gui-next-page-icon-title",
                "gui-next-page-icon-lore");

        inventory.setItem(46, backShopIcon);
        inventory.setItem(48, prevPageIcon);
        inventory.setItem(50, nextPageIcon);

        new BukkitRunnable() {
            @Override
            public void run() {
                List<ShopItem> shopItems = DATA_MANAGER.getPlayerAllShopItems(player.getUniqueId(), page * 45, 5 * 9);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int idx = 0; idx < shopItems.size(); idx++) {
                            ShopItem shopItem = shopItems.get(idx);

                            // Create shop item icon
                            inventory.setItem(idx, createShopItemIcon(shopItem));
                        }
                        GsInvHolder shopHolder = (GsInvHolder) (inventory.getHolder());
                        if (shopHolder == null) {
                            return;
                        }
                        shopHolder.setShopItems(shopItems);

                        player.openInventory(inventory);
                    }
                }.runTask(PLUGIN);
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    public static ItemStack makeIcon(Material material, String titleKey, String loreKey) {
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();

        if (meta == null) {
            return icon;
        }

        meta.setDisplayName(MsgUtil.get(titleKey, false));
        List<String> lore = MsgUtil.getList(loreKey);
        if (lore.size() != 0) {
            meta.setLore(lore);
        }
        icon.setItemMeta(meta);

        return icon;
    }

    public static void setIconTitle(Inventory inventory, int slotId, String title) {
        if (inventory == null || title == null || inventory.getSize() - 1 < slotId) {
            return;
        }

        ItemStack icon = inventory.getItem(slotId);
        if (icon == null) {
            return;
        }

        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setDisplayName(title);
        icon.setItemMeta(meta);
    }

    public static void setIconLore(Inventory inventory, int slotId, List<String> lore) {
        if (inventory == null || lore == null || inventory.getSize() - 1 < slotId) {
            return;
        }

        ItemStack icon = inventory.getItem(slotId);
        if (icon == null) {
            return;
        }

        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
    }
}
