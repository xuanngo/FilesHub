package net.xngo.fileshub.db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


import net.xngo.fileshub.Config;

public class Connection
{
  public java.sql.Connection  connection        = null;
  public PreparedStatement    preparedStatement = null;
  
  // Logging purposed only.
  private boolean           log         = false;
  private String            query       = "";
  private ArrayList<String> values      = new ArrayList<String>();
  private LimitedSizeQueue<String> queries = null;
  
  
  public Connection(boolean log, int queryLogSize)
  {
    this();
    this.log      = log;
    this.queries  = new LimitedSizeQueue<String>(queryLogSize);
  }
  
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
    if(this.log) { this.query = sql; }
    
    this.preparedStatement = this.connection.prepareStatement(sql);
    return this.preparedStatement;
  }
  
  public ResultSet executeQuery() throws SQLException
  {
    if(this.log) { this.queries.add(this.getQueryString()); }
    return this.preparedStatement.executeQuery();    
  }
  
  public int executeUpdate()
  {
    try
    {
      if(this.log) { this.queries.add(this.getQueryString()); }
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
    if(this.log)
    { 
      if(x == null)
        this.values.add("<null>");
      else
      {
        if(x.isEmpty()) 
          this.values.add("<empty>"); 
        else
          this.values.add(x);
      }
    }
    
    this.preparedStatement.setString(parameterIndex, x);
  }
  
  public void setInt(int parameterIndex, int x) throws SQLException
  {
    if(this.log) { this.values.add(x+""); }
    
    this.preparedStatement.setInt(parameterIndex, x);
  }
  
  public void setLong(int parameterIndex, long x) throws SQLException
  {
    if(this.log) { this.values.add(x+""); }
    
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
  
  public String getQueryString()
  {
    StringBuilder valuesStr = new StringBuilder();
    if(values.size()>0)
    {
      for(int i=0; i<values.size()-1; i++)
      {
        valuesStr.append(values.get(i));
        valuesStr.append(", ");
      }
      valuesStr.append(values.get(values.size()-1));
    }
    
    // Clean up
    this.values.clear();
    
    return String.format("%s : %s ", this.query, valuesStr);
    
  }
  
  public void displayLoggedQueries()
  {
    System.out.println(String.format("============ Last %d queries logged start here ============", this.queries.getMaxSize()));
    for(String query: this.queries)
    {
      System.out.println(query);
    }
    System.out.println("============ Logged queries end ============");
  }
  
}
