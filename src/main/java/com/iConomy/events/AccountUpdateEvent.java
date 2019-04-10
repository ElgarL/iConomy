package com.iConomy.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountUpdateEvent extends Event {
    private final String account;
    private double balance;
    private double previous;
    private double amount;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public AccountUpdateEvent(String account, double previous, double balance, double amount) {
        super();
        this.account = account;
        this.previous = previous;
        this.balance = balance;
        this.amount = amount;
    }

    public String getAccountName() {
        return this.account;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.balance = this.previous + amount;
    }

    public double getPrevious() {
        return this.previous;
    }

    public double getBalance() {
        return this.balance;
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
