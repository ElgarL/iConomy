package com.iConomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountRemoveEvent extends Event {
    private final String account;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public AccountRemoveEvent(String account) {
    	super(!Bukkit.getServer().isPrimaryThread());
        this.account = account;
    }

    public String getAccountName() {
        return this.account;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
