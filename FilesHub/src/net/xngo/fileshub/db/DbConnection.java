package net.xngo.fileshub.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implement database connection and generic functions.
 * @author Xuan Ngo
 *
 */
public class DbConnection
{
  private final String dbname     = "FilesHub";
  private final String filePath   = System.getProperty(this.dbname+".home")+File.separator+dbname+".db"; // File.separator might be double depending on how dbname.home system property is supplied.
  
  Connection  connection  = null;
  Statement   statement   = null;
  
  public DbConnection()
  {
      String sqlUrl = "jdbc:sqlite:/"+this.filePath.replace('\\', '/'); // Not efficient. Use File.toURI().toURL().toString();
      this.connect(sqlUrl);
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
  
  /**
   * Connect to database.
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