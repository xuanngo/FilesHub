package net.xngo.fileshub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

import net.xngo.fileshub.Config;

/**
 * Implement database connection and generic query functions.
 * Conn name is used to differentiate with java.sql.Connection.
 * @author Xuan Ngo
 *
 */
public class Conn
{

  public static final String DB_FILE_PATH = Config.DB_FILE_PATH; 
  
  
  private static Conn instance = null;
  
  public Connection  connection  = null;
  public Statement   statement   = null;
  
  protected Conn(){}
  
  public static Conn getInstance()
  {
    if(instance == null)
    {
      instance = new Conn();
//      instance.checkWritePermission();
      instance.connectToSqlite();
    }
    return instance;    
  }
  
  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or 
   *    an SQL statement that returns nothing, such as an SQL DDL statement.
   * @param query
   */
  public void executeUpdate(final String query)
  {
    try
    {
      this.statement.executeUpdate(query);
      this.statement.close();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }     
    
  }  
  
  /**
   * Close database connection.
   */
  public void close()
  {
    try
    {
        if(this.connection != null)
            this.connection.close();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }     
  }
  

  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  private void checkWritePermission()
  {
    File dbFile = new File(Config.DB_FILE_PATH);
    if(!dbFile.canWrite())
    {
      System.out.println(String.format("Error: You don't have permission to write to '%s'.", Config.DB_FILE_PATH));
      System.exit(-1);
    }
  }
  
  private void connectToSqlite()
  {
    // Construct JDBC connection string.
    String sqlUrl = "jdbc:sqlite:/"+DB_FILE_PATH.replace('\\', '/'); // Not efficient. Use File.toURI().toURL().toString();
    this.connect(sqlUrl);
  }
  
  /**
   * Connect to database engine
   */
  private void connect(final String sqlUrl)
  {
    try
    {
      // Load the sqlite-JDBC driver using the current class loader
      Class.forName("org.sqlite.JDBC");
      
      // Create a database connection
      this.connection = DriverManager.getConnection(sqlUrl);
      this.statement  = connection.createStatement();
      this.statement.setQueryTimeout(30);  // Set timeout to 30 sec. Default is 3000 seconds.
      
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    catch(ClassNotFoundException e)
    {
      e.printStackTrace();
    }
  }
    
 
    
}