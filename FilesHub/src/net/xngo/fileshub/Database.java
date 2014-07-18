package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Database
{
  
  private final String dbname  = "FilesHub";
  private final String filePath = System.getProperty(this.dbname+".home")+File.separator+dbname+".db"; // File.separator might be double depending on how dbname.home system property is supplied.
  private final String tablename  = this.dbname;
  
  Connection  connection  = null;
  Statement   statement   = null;
  
  public Database()
  {
      String sqlUrl = "jdbc:sqlite:/"+this.filePath.replace('\\', '/'); // Not efficient. Use File.toURI().toURL().toString();
      this.connect(sqlUrl);
  }
  
  /**
   * Connect to database.
   */
  private void connect(final String sqlUrl)
  {
    try
    {
      // Check if database file exists.
      File file = new File(this.filePath);
      final boolean emptyFile = file.exists();
      
      // Load the sqlite-JDBC driver using the current class loader
      Class.forName("org.sqlite.JDBC");
      
      // Create a database connection
      this.connection = DriverManager.getConnection(sqlUrl);
      this.statement  = connection.createStatement();
      this.statement.setQueryTimeout(30);  // Set timeout to 30 sec. Default is 3000 seconds.
      
      // Create database if sqlite database file doesn't exist.
      if(!emptyFile)
      {
        this.initialize();
      }
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
    
  /**
   * Create database from scratch if sqlite database file doesn't exist.
   */
  private void initialize()
  {
    
    // Delete table.
    final String query="DROP TABLE IF EXISTS " + this.tablename;
    this.executeUpdate(query);
    
    // Create table.
    this.createTable();
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
   * Create table.
   */
  public void createTable()
  {
    final String query="CREATE TABLE "+this.tablename+" ("
                              + "uid      INTEGER PRIMARY KEY AUTOINCREMENT, "
                              + "path     TEXT NOT NULL, "
                              + "filename TEXT NOT NULL, "
                              + "size     INTEGER NOT NULL, "
                              + "hash     TEXT "
                              + ")";
    this.executeUpdate(query);
  }
    
    
  private void executeUpdate(final String query)
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
    
  
    
}