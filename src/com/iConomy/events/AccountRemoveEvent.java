package com.iConomy.events;

import org.bukkit.event.Event;

public class AccountRemoveEvent extends Event {
	/**
	 * 
	 */
	private final String account;
	private boolean cancelled = false;

	public AccountRemoveEvent(String account) {
		super();
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
}

