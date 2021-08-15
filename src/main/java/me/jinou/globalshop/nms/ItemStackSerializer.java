package me.jinou.globalshop.nms;

import org.bukkit.inventory.ItemStack;

public interface ItemStackSerializer {

    /**
     * Converts the ItemStack to a base64 string
     *
     * @param is input ItemStack
     * @return a base64 string contains item info
     */
    String toBase64(ItemStack is);

    /**
     * Converts the base64 string to a ItemStack
     *
     * @param base64 String in base64 that contains item info
     * @return ItemStack converted from string
     */
    ItemStack fromBase64(String base64);
}
