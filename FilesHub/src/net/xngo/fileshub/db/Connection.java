package net.xngo.fileshub.db;

import java.sql.DriverManager;
import java.sql.ResultSet;
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
  
  public void setAutoCommit(boolean autoCommit) throws SQLException
  {
    this.connection.setAutoCommit(autoCommit);
  }
  public void commit() throws SQLException
  {
    this.connection.commit();
  }
  
  public void rollback() throws SQLException
  {
    this.connection.rollback();
  }
  
  
  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    if(Debug.activate())
    {
      System.out.print("\n"+sql+": ");
    }
    
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
    if(Debug.activate())
    {
      System.out.print(x+", ");
    }
    
    this.preparedStatement.setString(parameterIndex, x);
  }
  
  public void setInt(int parameterIndex, int x) throws SQLException
  {
    if(Debug.activate())
    {
      System.out.print(x+", ");
    }    
    this.preparedStatement.setInt(parameterIndex, x);
  }
  
  public void setLong(int parameterIndex, long x) throws SQLException
  {
    if(Debug.activate())
    {
      System.out.print(x+", ");
    }    
    this.preparedStatement.setLong(parameterIndex, x);
  }
  
  public void closePStatement()
  {
    try
    {
      if(this.preparedStatement != null)
      {
        this.preparedStatement.close();
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
  
  public void close()
  {
    // Close prepared statement.
    this.closePStatement();
    
    // Close connection.
    try
    {
      if(this.connection != null)
      {
        this.connection.close();
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
}
