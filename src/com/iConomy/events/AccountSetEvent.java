 package com.iConomy.events;
 
 import org.bukkit.event.Event;
 
public class AccountSetEvent extends Event {
	/**
	 * 
	 */
	private final String account;
	private double balance;

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
}