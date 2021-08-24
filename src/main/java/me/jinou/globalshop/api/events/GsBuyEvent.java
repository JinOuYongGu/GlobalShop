package me.jinou.globalshop.api.events;

import lombok.Getter;
import lombok.NonNull;
import me.jinou.globalshop.utils.ShopItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * @author 69142
 * This event is fired when a player buys something from the shop.
 */
public class GsBuyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final ShopItem shopItem;
    @Getter
    private final UUID buyerUuid;
    private boolean cancelled = false;

    public GsBuyEvent(UUID buyerUuid, ShopItem shopItem) {
        this.buyerUuid = buyerUuid;
        this.shopItem = shopItem;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}