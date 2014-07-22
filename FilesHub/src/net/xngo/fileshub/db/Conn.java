package net.xngo.fileshub.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implement database connection and generic query functions.
 * Conn name is used to differentiate with java.sql.Connection.
 * @author Xuan Ngo
 *
 */
public class Conn
{
  public static final String DB_NAME      = "FilesHub";
  public static final String DB_FILE_PATH = System.getProperty(DB_NAME+".home")+File.separator+DB_NAME+".db"; // File.separator might be double depending on how dbname.home system property is supplied.
  
  
  private static Conn instance = null;
  
  public Connection  connection  = null;
  public Statement   statement   = null;
  
  protected Conn(){}
  
  public static Conn getInstance()
  {
    if(instance == null)
    {
      instance = new Conn();
      instance.connectToSqlite();
    }
    return instance;    
  }
  
  public Connection getConnection()
  {
    return this.connection;
  }
  
  public void executeUpdate(final String query)
  {
    try
    {
      statement.executeUpdate(query);
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