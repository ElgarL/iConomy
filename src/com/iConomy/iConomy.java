package com.iConomy;

import com.iConomy.entity.Players;
import com.iConomy.net.Database;
import com.iConomy.system.Account;
import com.iConomy.system.Accounts;
import com.iConomy.system.Bank;
import com.iConomy.system.Banks;
import com.iConomy.system.Interest;
import com.iConomy.system.Transactions;
import com.iConomy.util.Constants;
import com.iConomy.util.Downloader;
import com.iConomy.util.FileManager;
import com.iConomy.util.Misc;
import com.iConomy.util.VaultConnector;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class iConomy extends JavaPlugin
{
  public static Banks Banks = null;
  public static Accounts Accounts = null;
  public static Economy economy = null;

  private static Server Server = null;
  private static Database Database = null;
  private static Transactions Transactions = null;
  //private static PermissionHandler Permissions = null;
  private static Players playerListener = null;
  private static Timer Interest_Timer = null;

  public static iConomy instance = null;
  
  Logger log = getServer().getLogger();
  
  public void onEnable()
  {
    instance = this;
    Locale.setDefault(Locale.US);
    Server = getServer();

    new File("lib" + File.separator).mkdir();
    new File("lib" + File.separator).setWritable(true);
    new File("lib" + File.separator).setExecutable(true);

    getDataFolder().mkdir();
    getDataFolder().setWritable(true);
    getDataFolder().setExecutable(true);

    Constants.Plugin_Directory = getDataFolder().getPath();

    PluginDescriptionFile pdfFile = getDescription();

    FileManager file = new FileManager(getDataFolder().getPath(), "VERSION", false);

    extract("Config.yml");
    extract("Template.yml");
    try
    {
      Constants.load(new File(getDataFolder(), "Config.yml"));
    } catch (Exception e) {
      Server.getPluginManager().disablePlugin(this);
      log.info("[iConomy] Failed to retrieve configuration from directory.");
      log.info("[iConomy] Please back up your current settings and let iConomy recreate it.");
      return;
    }

    if (Misc.is(Constants.DatabaseType, new String[] { "sqlite", "h2", "h2sql", "h2db" })) {
      if (!new File("lib" + File.separator, "h2.jar").exists()) {
        Downloader.install(Constants.H2_Jar_Location, "h2.jar");
      }
    }
    else if (!new File("lib" + File.separator, "mysql-connector-java-bin.jar").exists()) {
      Downloader.install(Constants.MySQL_Jar_Location, "mysql-connector-java-bin.jar");
    }

    if (!registerEconomy()) {
    	onDisable();
    	return;
    }
    
    try
    {
      Database = new Database();
      Database.setupAccountTable();

      if (Constants.Banking) {
        Database.setupBankTable();
        Database.setupBankRelationTable();
      }
    } catch (Exception e) {
      log.severe("[iConomy] Database initialization failed: " + e);
      Server.getPluginManager().disablePlugin(this);
      return;
    }

    try
    {
      Transactions = new Transactions();
      Database.setupTransactionTable();
    } catch (Exception e) {
      log.info("[iConomy] Could not load transaction logger: " + e);
    }

    update(file, Double.valueOf(pdfFile.getVersion()).doubleValue());

    Accounts = new Accounts();

    if (Constants.Banking)
      Banks = new Banks();
    try
    {
      if (Constants.Interest) {
        long time = Constants.InterestSeconds * 1000L;

        Interest_Timer = new Timer();
        Interest_Timer.scheduleAtFixedRate(new Interest(getDataFolder().getPath()), time, time);
      }
    } catch (Exception e) {
      log.severe("[iConomy] Failed to start interest system: " + e);
      Server.getPluginManager().disablePlugin(this);
      return;
    }

    playerListener = new Players(getDataFolder().getPath());

    getServer().getPluginManager().registerEvents(playerListener, this);

    log.info("[iConomy] v" + pdfFile.getVersion() + " (" + Constants.Codename + ") loaded.");
    log.info("[iConomy] Developed by: " + pdfFile.getAuthors());

  }

    private boolean registerEconomy() {
      if (Server.getPluginManager().isPluginEnabled("Vault")) {
          final ServicesManager sm = Server.getServicesManager();
          sm.register(Economy.class, new VaultConnector(this), this, ServicePriority.Highest);          
          log.info("[iConomy] Registered Vault interface.");

          RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
          //Economy econ = null;
          if (rsp != null) {
              economy = rsp.getProvider();
          }
          //log.info("[Vault] [Economy] " + economy.getName() + " found: Waiting.");
          return true;
      } else {
    	  PluginDescriptionFile pdfFile = getDescription();
          log.severe("[iConomy] Vault not found. Please download Vault to use iConomy " + pdfFile.getVersion().toString());
          return false;
      }
  }
  
  public void onDisable()
  {
    try {
      if (Misc.is(Constants.DatabaseType, new String[] { "sqlite", "h2", "h2sql", "h2db" })) {
        Database.connectionPool().dispose();
      }

      System.out.println("[iConomy] Plugin disabled.");
    } catch (Exception e) {
      System.out.println("[iConomy] Plugin disabled.");
    } finally {
      if (Interest_Timer != null) {
        Interest_Timer.cancel();
      }

      Server = null;
      Banks = null;
      Accounts = null;
      Database = null;
      Transactions = null;
      playerListener = null;
      Interest_Timer = null;
    }
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    String[] split = new String[args.length + 1];
    split[0] = cmd.getName().toLowerCase();
    System.arraycopy(args, 0, split, 1, args.length);
    if (!(sender instanceof Player) && split[0].equalsIgnoreCase("icoimport"))
    	return importEssEco();
    else 
    	return playerListener.onPlayerCommand(sender, split);

  }

  private boolean importEssEco() {

	File accountsFolder = null;
	try {
		accountsFolder = new File("plugins/Essentials/userdata/");
	} catch (Exception e) {		
		log.warning("Essentials data not found in plugins/essentials/userdata/");
		return false;
	}

    if (!accountsFolder.isDirectory()) {
        return false;
    }

    File[] accounts = accountsFolder.listFiles(new FilenameFilter() {
        public boolean accept(File file, String name) {
            return name.toLowerCase().endsWith(".yml");
        }
    });
    log.info("Amount of accounts found:" + accounts.length);
    int i = 0;
    String line;
    for (File account : accounts) {
        String uuid = null;
        String name = null;
        double money = 0;
        boolean haveMoney = false;
        if (account.getName().contains("-")) {
            uuid = account.getName().replace(".yml", "");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(account));
            try {
                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("money:")) {
                        String value = line.replace("money: '", "");
                        if (value.contains("money")) {
                            value = line.replace("money: ", "");
                        }
                        money = Double.parseDouble(value.substring(0, value.length() - 1));
                        haveMoney = true;
                    } else if (line.startsWith("lastAccountName:")) {
                        name = line.replace("lastAccountName: ", "").trim().replace("\'", "").replace("\"", "");
                        String actualName;
						if (name.startsWith("town_")) {
                        	actualName = name.substring(5);
                        	String townName = null;
							for (Town towns : TownyUniverse.getDataSource().getTowns()) {
                        		townName = towns.getName();
								if (townName.equalsIgnoreCase(actualName)) {                        		
                        			log.info("[iConomy] Import: Town account found: " + actualName);
                        			name = "town-" + townName;
								}
                        	}                        		
						} else if (name.startsWith("nation_")) {
                        	actualName = name.substring(7);                        	
                        	String nationName = null;
							for (Nation nations : TownyUniverse.getDataSource().getNations()) {
                        		nationName = nations.getName();
								if (nationName.equalsIgnoreCase(actualName)) {                        		
                        			log.info("[iConomy] Import: Nation account found: " + actualName);
                        			name = "nation-" + nationName;
								}
                        	}

						}
                    }
                }
            } catch (NumberFormatException e) {
                log.warning("The account "+uuid+" don't have a valid money value!");
            }
            try {
				if (haveMoney) {
				    if (Accounts.exists(name)) {
				    	if (Accounts.get(name).getHoldings().balance() == money){
				    		continue;
				    	} else 
				    		Accounts.get(name).getHoldings().set(money);                		
				    } else {
				    	Accounts.create(name);
				    	Accounts.get(name).getHoldings().set(money);
				    }
				}
			} catch (Exception e) {
				log.warning("[iConomy] Importer could not parse account for " + account.getName());
			}
            if (i % 10 == 0) {
                log.info(i + " accounts loaded.");
            }
            i++;
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    return true;
}

private void update(FileManager file, double version) {
    if (file.exists()) {
      file.read();
      try
      {
        double current = Double.parseDouble(file.getSource());
        LinkedList<String> MySQL = new LinkedList<String>();
        LinkedList<String> GENERIC = new LinkedList<String>();
        LinkedList<String> SQL = new LinkedList<String>();

        if (current != version) {
          if (current < 4.64D) {
            MySQL.add("ALTER TABLE " + Constants.SQLTable + " ADD hidden boolean DEFAULT '0';");
            GENERIC.add("ALTER TABLE " + Constants.SQLTable + " ADD HIDDEN BOOLEAN DEFAULT '0';");
          }

          if (current < 4.62D) {
            MySQL.add("ALTER IGNORE TABLE " + Constants.SQLTable + " ADD UNIQUE INDEX(username(32));");
            GENERIC.add("ALTER TABLE " + Constants.SQLTable + " ADD UNIQUE(username);");
          }

          if ((!MySQL.isEmpty()) && (!GENERIC.isEmpty())) {
            Connection conn = null;
            ResultSet rs = null;
            Statement stmt = null;
            try
            {
              conn = getiCoDatabase().getConnection();
              stmt = null;

              log.info(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

              int i = 1;
              SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : GENERIC;

              for (String Query : SQL) {
                stmt = conn.createStatement();
                stmt.execute(Query);

                log.info("   Executing SQL Query #" + i + " of " + SQL.size());
                i++;
              }

              file.write(Double.valueOf(version));

              log.info(" + Database Update Complete.");
            } catch (SQLException ex) {
              log.warning("[iConomy] Error updating database: " + ex.getMessage());
            } finally {
              if (stmt != null) try {
                  stmt.close();
                } catch (SQLException ex) {
                } if (rs != null) try {
                  rs.close();
                } catch (SQLException ex) {
                } getiCoDatabase().close(conn);
            }
          }
        } else {
          file.write(Double.valueOf(version));
        }
      } catch (Exception e) {
        log.warning("[iConomy] Error on version check: ");
        e.printStackTrace();
        file.delete();
      }
    } else {
      if (!Constants.DatabaseType.equalsIgnoreCase("flatfile")) {
        String[] SQL = new String[0];

        String[] MySQL = { "DROP TABLE " + Constants.SQLTable + ";", "RENAME TABLE ibalances TO " + Constants.SQLTable + ";", "ALTER TABLE " + Constants.SQLTable + " CHANGE  player  username TEXT NOT NULL, CHANGE balance balance DECIMAL(64, 2) NOT NULL;" };

        String[] SQLite = { "DROP TABLE " + Constants.SQLTable + ";", "CREATE TABLE '" + Constants.SQLTable + "' ('id' INT ( 10 ) PRIMARY KEY , 'username' TEXT , 'balance' DECIMAL ( 64 , 2 ));", "INSERT INTO " + Constants.SQLTable + "(id, username, balance) SELECT id, player, balance FROM ibalances;", "DROP TABLE ibalances;" };

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try
        {
          conn = getiCoDatabase().getConnection();
          DatabaseMetaData dbm = conn.getMetaData();
          rs = dbm.getTables(null, null, "ibalances", null);
          ps = null;

          if (rs.next()) {
            log.info(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

            int i = 1;
            SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : SQLite;

            for (String Query : SQL) {
              ps = conn.prepareStatement(Query);
              ps.executeQuery(Query);

              log.info("   Executing SQL Query #" + i + " of " + SQL.length);
              i++;
            }

            log.info(" + Database Update Complete.");
          }

          file.write(Double.valueOf(version));
        } catch (SQLException ex) {
          log.warning("[iConomy] Error updating database: " + ex.getMessage());
        } finally {
          if (ps != null) try {
              ps.close();
            } catch (SQLException ex) {
            } if (rs != null) try {
              rs.close();
            } catch (SQLException ex) {
            } if (conn != null) {
            getiCoDatabase().close(conn);
          }
        }
      }
      file.create();
      file.write(Double.valueOf(version));
    }
  }

  private void extract(String name) {
    File actual = new File(getDataFolder(), name);
    if (!actual.exists()) {
      InputStream input = getClass().getResourceAsStream("/" + name);
      if (input != null) {
        FileOutputStream output = null;
        try
        {
          output = new FileOutputStream(actual);
          byte[] buf = new byte[8192];
          int length = 0;

          while ((length = input.read(buf)) > 0) {
            output.write(buf, 0, length);
          }

          log.info("[iConomy] Default setup file written: " + name);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            if (input != null)
              input.close();
          } catch (Exception e) {
          }
          try {
            if (output != null)
              output.close();
          }
          catch (Exception e)
          {
          }
        }
      }
    }
  }

  public static String format(String account)
  {
    return getAccount(account).getHoldings().toString();
  }

  public static String format(String bank, String account)
  {
    return new Bank(bank).getAccount(account).getHoldings().toString();
  }

  public static String format(double amount)
  {
    DecimalFormat formatter = new DecimalFormat("#,##0.00");
    String formatted = formatter.format(amount);

    if (formatted.endsWith(".")) {
      formatted = formatted.substring(0, formatted.length() - 1);
    }

    return Misc.formatted(formatted, Constants.Major, Constants.Minor);
  }

  public static Account getAccount(String name)
  {
    return Accounts.get(name);
  }

  public static boolean hasAccount(String name) {
    return Accounts.exists(name);
  }

  public static Bank getBank(String name)
  {
    return Banks.get(name);
  }

  public static Bank getBank(int id)
  {
    return Banks.get(id);
  }

  public static Database getiCoDatabase()
  {
    return Database;
  }

  public static Transactions getTransactions()
  {
    return Transactions;
  }

  public static boolean hasPermissions(CommandSender sender, String node)
  {
    if ((sender instanceof Player)) {
    	return ((Player)sender).hasPermission(node);
    }
  	return true;
  }

  public static Server getBukkitServer()
  {
    return Server;
  }

}