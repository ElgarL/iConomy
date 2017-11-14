package com.iConomy.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;


public class VaultConnector implements Economy {
	
	private iConomy plugin;
	private String name;
	
	public VaultConnector(iConomy plugin) {
		this.plugin = plugin;
		//Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(), plugin);
	}

	@Override
	public String getName() {
		name = "iConomy " + plugin.getDescription().getVersion().toString();
		return name;
	}
	
	@Override
	public boolean createPlayerAccount(String playerName) {

		if (hasAccount(playerName)) {
            return false;
        }
        plugin.getAccount(playerName);
        return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0) {

		if (hasAccount(arg0.toString())) {
            return false;
        }
        plugin.getAccount(arg0.getName().toString());
        return true;
	}

	@Override
	public boolean createPlayerAccount(String playerName, String arg1) {

		if (hasAccount(playerName)) {
            return false;
        }
        plugin.getAccount(playerName);
        return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {

		if (hasAccount(arg0.getPlayer().getName())) {
            return false;
        }
        plugin.getAccount(arg0.getPlayer().getName());
        return true;
	}

	@Override
	public String currencyNamePlural() {

		try {
            return Constants.Major.get(1);
        } catch (Exception e) {
            return "";
        }
	}

	@Override
	public String currencyNameSingular() {

		try {
            return Constants.Major.get(0);
        } catch (Exception e) {
            return "";
        }
	}
	
	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        balance = getBalance(playerName);
        type = EconomyResponse.ResponseType.SUCCESS;

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(arg0.getName());
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        balance = getBalance(arg0.getName());
        type = EconomyResponse.ResponseType.SUCCESS;

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        balance = getBalance(playerName);
        type = EconomyResponse.ResponseType.SUCCESS;

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(arg0.getName());
        Holdings holdings = account.getHoldings();
        holdings.add(amount);
        balance = getBalance(arg0.getPlayer().getName());
        type = EconomyResponse.ResponseType.SUCCESS;

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public String format(double amount) {

		 return plugin.format(amount);
	}

	@Override
	public int fractionalDigits() {

		return 0;
	}

	@Override
	public double getBalance(String playerName) {

		return plugin.getAccount(playerName).getHoldings().balance();
	}

	@Override
	public double getBalance(OfflinePlayer offlinePlayer) {		
		return plugin.getAccount(offlinePlayer.getName()).getHoldings().balance();
	}

	@Override
	public double getBalance(String playerName, String arg1) {

		return plugin.getAccount(playerName).getHoldings().balance();
	}

	@Override
	public double getBalance(OfflinePlayer offlinePlayer, String world) {

		return plugin.getAccount(offlinePlayer.getPlayer().getName()).getHoldings().balance();
	}

	@Override
	public List<String> getBanks() {

		return new ArrayList<String>();
	}

	@Override
	public boolean has(String playerName, double amount) {
		
        return getBalance(playerName) >= amount;
        
    }

	@Override
	public boolean has(OfflinePlayer arg0, double amount) {
		
        return getBalance(arg0.getPlayer().getName()) >= amount;
        
    }

	@Override
	public boolean has(String playerName, String world, double amount) {

		return getBalance(playerName) >= amount;
	}

	@Override
	public boolean has(OfflinePlayer arg0, String world, double amount) {

		return getBalance(arg0.getPlayer().getName()) >= amount;
	}

	@Override
	public boolean hasAccount(String playerName) {

		return plugin.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0) {

		return plugin.hasAccount(arg0.getName().toString());
	}

	@Override
	public boolean hasAccount(String playerName, String arg1) {

		return plugin.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0, String arg1) {

		return plugin.hasAccount(arg0.getPlayer().getName());
	}

	@Override
	public boolean hasBankSupport() {

		return false;
	}

	@Override
	public boolean isEnabled() {

		return plugin != null;
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;
            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(arg0.getName());
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(arg0.getName());
            type = EconomyResponse.ResponseType.SUCCESS;
            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(arg0.getName());
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(playerName);
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;
            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Account account = plugin.getAccount(arg0.getPlayer().getName());
        Holdings holdings = account.getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(arg0.getPlayer().getName());
            type = EconomyResponse.ResponseType.SUCCESS;
            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(arg0.getPlayer().getName());
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
    public EconomyResponse bankBalance(String arg0) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");		
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
    }

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
	}
	
	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}
	
	@Override
	public EconomyResponse deleteBank(String arg0) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support bank accounts!");
	}
}
