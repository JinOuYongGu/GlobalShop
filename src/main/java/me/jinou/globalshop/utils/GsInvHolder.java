package me.jinou.globalshop.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 69142
 */
public class GsInvHolder implements InventoryHolder {
    @Setter
    @Getter
    private int currentPage = 0;

    @Getter
    @Setter
    private String inventoryName;

    @Getter
    @Setter
    private List<ShopItem> shopItems = new ArrayList<>();

    @Getter
    @Setter
    private String type = "all";

    @Getter
    @Setter
    private String filter = "timeAscend";

    @Override
    public Inventory getInventory() {
        return null;
    }
}
