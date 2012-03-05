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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class iConomy extends JavaPlugin
{
  public static Banks Banks = null;
  public static Accounts Accounts = null;

  private static Server Server = null;
  private static Database Database = null;
  private static Transactions Transactions = null;
  //private static PermissionHandler Permissions = null;
  private static Players playerListener = null;
  private static Timer Interest_Timer = null;

  public void onEnable()
  {
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
      System.out.println("[iConomy] Failed to retrieve configuration from directory.");
      System.out.println("[iConomy] Please back up your current settings and let iConomy recreate it.");
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

    try
    {
      Database = new Database();
      Database.setupAccountTable();

      if (Constants.Banking) {
        Database.setupBankTable();
        Database.setupBankRelationTable();
      }
    } catch (Exception e) {
      System.out.println("[iConomy] Database initialization failed: " + e);
      Server.getPluginManager().disablePlugin(this);
      return;
    }

    try
    {
      Transactions = new Transactions();
      Database.setupTransactionTable();
    } catch (Exception e) {
      System.out.println("[iConomy] Could not load transaction logger: " + e);
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
      System.out.println("[iConomy] Failed to start interest system: " + e);
      Server.getPluginManager().disablePlugin(this);
      return;
    }

    playerListener = new Players(getDataFolder().getPath());

    getServer().getPluginManager().registerEvents(playerListener, this);

    System.out.println("[iConomy] v" + pdfFile.getVersion() + " (" + "Eruanna" + ") loaded.");
    System.out.println("[iConomy] Developed by: " + pdfFile.getAuthors());
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

    return playerListener.onPlayerCommand(sender, split);
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

              System.out.println(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

              int i = 1;
              SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : GENERIC;

              for (String Query : SQL) {
                stmt = conn.createStatement();
                stmt.execute(Query);

                System.out.println("   Executing SQL Query #" + i + " of " + SQL.size());
                i++;
              }

              file.write(Double.valueOf(version));

              System.out.println(" + Database Update Complete.");
            } catch (SQLException ex) {
              System.out.println("[iConomy] Error updating database: " + ex.getMessage());
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
        System.out.println("[iConomy] Error on version check: ");
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
            System.out.println(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

            int i = 1;
            SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : SQLite;

            for (String Query : SQL) {
              ps = conn.prepareStatement(Query);
              ps.executeQuery(Query);

              System.out.println("   Executing SQL Query #" + i + " of " + SQL.length);
              i++;
            }

            System.out.println(" + Database Update Complete.");
          }

          file.write(Double.valueOf(version));
        } catch (SQLException ex) {
          System.out.println("[iConomy] Error updating database: " + ex.getMessage());
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
      InputStream input = getClass().getResourceAsStream("/default/" + name);
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

          System.out.println("[iConomy] Default setup file written: " + name);
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
  	return false;
  }

  public static Server getBukkitServer()
  {
    return Server;
  }

}