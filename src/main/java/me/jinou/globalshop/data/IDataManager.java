package me.jinou.globalshop.data;

import me.jinou.globalshop.utils.ShopItem;

import java.util.List;
import java.util.UUID;

/**
 * @author 69142
 */
public interface IDataManager {

    /**
     * Add an item to shop
     *
     * @param item the trade item
     * @return true if add item successfully
     */
    void addShopItem(ShopItem item);

    void removeShopItem(int uid);

    void close();

    void update();

    ShopItem getShopItem(int uid);

    List<ShopItem> getShopItems(String type, String filter, int startIdx, int length);

    List<ShopItem> getPlayerAllShopItems(UUID playerUuid, int startIdx, int length);

    int generateUid();
}
