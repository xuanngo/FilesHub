package net.xngo.fileshub;

import java.sql.SQLException;

import net.xngo.fileshub.cmd.Cmd;
import net.xngo.fileshub.report.Chronometer;
import net.xngo.utils.java.db.Connection;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{
  public static Chronometer chrono = new Chronometer();
  public static Connection connection = new Connection();
  
  public static void main(String[] args)
  {
    connection.connect(Config.JDBC_CLASSLOADER, Config.DB_URL);
    try
    {
      connection.setAutoCommit(true);
    }
    catch(SQLException ex) { ex.printStackTrace(); }
    
    Main.chrono.start();
    Cmd cmd = new Cmd(args);
    Main.connection.close();
    
  }
 
}
