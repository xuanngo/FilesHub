package net.xngo.fileshub.db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.xngo.fileshub.Config;

public class Connection
{
  public java.sql.Connection  connection        = null;
  public PreparedStatement    preparedStatement = null;
  
  
  public Connection()
  {
    String jdbcClassLoader = "org.sqlite.JDBC";
    String dbUrl = "jdbc:sqlite:/"+Config.DB_FILE_PATH.replace('\\', '/'); // Not efficient. Use File.toURI().toURL().toString();
    
    this.connect(jdbcClassLoader, dbUrl);
  }
  
  
  /****************************************************************************
   * 
   *                             GENERIC FUNCTIONS
   * 
   ****************************************************************************/
  
  /**
   * @param jdbcClassLoader e.g. "org.sqlite.JDBC"
   * @param url e.g. "jdbc:sqlite:database_file_path"
   */
  public void connect(String jdbcClassLoader, String dbUrl)
  {
    try
    {
      // Load the JDBC driver using the current class loader
      Class.forName(jdbcClassLoader);
      
      // Create a database connection
      this.connection = DriverManager.getConnection(dbUrl);
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
  
  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    this.preparedStatement = this.connection.prepareStatement(sql);
    return this.preparedStatement;
  }
  
  public ResultSet executeQuery() throws SQLException
  {
    return this.preparedStatement.executeQuery();    
  }
  
  public int executeUpdate()
  {
    try
    {
      return this.preparedStatement.executeUpdate();
    }
    catch(SQLException ex)
    {
      ex.printStackTrace();
    }
    return 0;
  }
  
  public ResultSet getGeneratedKeys() throws SQLException
  {
    return this.preparedStatement.getGeneratedKeys();
  }
  
  public void setString(int parameterIndex, String x) throws SQLException
  {
    this.preparedStatement.setString(parameterIndex, x);
  }
  
  public void setInt(int parameterIndex, int x) throws SQLException
  {
    this.preparedStatement.setInt(parameterIndex, x);
  }
  
  public void setLong(int parameterIndex, long x) throws SQLException
  {
    this.preparedStatement.setLong(parameterIndex, x);
  }
}
