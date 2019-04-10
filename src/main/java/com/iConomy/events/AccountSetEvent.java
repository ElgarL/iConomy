package com.iConomy.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountSetEvent extends Event {
    private final String account;
    private double balance;
    private static final HandlerList handlers = new HandlerList();

    public AccountSetEvent(String account, double balance) {
        super();
        this.account = account;
        this.balance = balance;
    }

    public String getAccountName() {
        return this.account;
    }

    public double getBalance() {
        return this.balance;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
