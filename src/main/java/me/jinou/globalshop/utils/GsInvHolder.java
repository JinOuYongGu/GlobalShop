package me.jinou.globalshop.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 69142
 */
@Setter
@Getter
public class GsInvHolder implements InventoryHolder {
    private int currentPage = 0;
    private String inventoryName;
    private List<ShopItem> shopItems = new ArrayList<>();
    private String type = "all";
    private String filter = "timeAscend";

    @Override
    public @NonNull Inventory getInventory() {
        return Bukkit.createInventory(this, 54, inventoryName);
    }
}
