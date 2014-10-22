package net.xngo.fileshub.db;


import net.xngo.fileshub.Config;

/**
 * All database connection settings initialized.
 * @author Xuan Ngo
 *
 */
public class Connection extends net.xngo.utils.java.db.Connection
{
  public Connection()
  {
    super.connect(Config.JDBC_CLASSLOADER, Config.DB_URL);
  }
}
