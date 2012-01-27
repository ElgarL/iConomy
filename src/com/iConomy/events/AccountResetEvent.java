package com.iConomy.events;
 
import org.bukkit.event.Event;
 
public class AccountResetEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String account;
	private boolean cancelled = false;

	public AccountResetEvent(String account) {
		super("ACCOUNT_RESET");
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
