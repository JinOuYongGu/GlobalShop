package me.jinou.globalshop.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author 69142
 */
@AllArgsConstructor
@Getter
public class ShopItem {
    private final int uid;
    private final String ownerName;
    private final UUID ownerId;
    private final ItemStack itemStack;
    private final Double price;
    private final String type;
    private final Long createTime;
}
