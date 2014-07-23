package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.Utils;

public class Duplicate
{
  private final String tablename  = "Duplicate";
  
  private Conn conn = Conn.getInstance();
  
  public PreparedStatement insert = null;
  public PreparedStatement select = null;
 
  public int addFile(final long duid, File file)
  {
    int generatedKey = 0;
    final String canonical_path = Utils.getCanonicalPath(file);
    
    if(!this.isSameFile(canonical_path))
    {// File was never processed before.
      generatedKey = this.insert(duid, file);
    }
    
    return generatedKey;
  }
  
  public void createTable()
  {
    String query = this.createTableQuery();
    this.conn.executeUpdate(query);
  }
  
  public void deleteTable()
  {
    // Delete table.
    String query="DROP TABLE IF EXISTS " + this.tablename;
    this.conn.executeUpdate(query);    
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  
  private boolean isSameFile(String canonicalPath)
  {
    return this.isStringExists("canonical_path", canonicalPath);
  }
  
  private boolean isStringExists(String columnName, String value)
  {
    final String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", this.tablename, columnName);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, value);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        int count = resultSet.getInt(1);
        if(count>0)
          return true;
        else
          return false;        
      }
      else
        return false;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return false;
  }
  
  
  /**
   * 
   * @param columnName
   * @param value
   * @return uid number. If not found, it will return 0. It is assumed AUTO_INCREMENT start value is 1.
   */
  private long findString(String columnName, String value)
  {
    final String query = String.format("SELECT uid FROM %s WHERE %s = ?", this.tablename, columnName);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, value);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      int uid = 0;  
      if(resultSet.next())
      {
        uid = resultSet.getInt(1);
      }
      
      return uid;
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return 0;
  }
  
  private final int insert(final long duid, final File file)
  {

    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename) VALUES(?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      final String canonical_path = Utils.getCanonicalPath(file);
      final String filename = file.getName();
      int i=1;
      this.insert.setLong  (i++, duid);
      this.insert.setString(i++, canonical_path);
      this.insert.setString(i++, filename);

      
      // Insert row.
      this.insert.executeUpdate();
      ResultSet resultSet =  this.insert.getGeneratedKeys();
      if(resultSet.next())
      {
        generatedKey = resultSet.getInt(1);
 
      }
      
    }
    catch(SQLException e)
    {
      if(e.getMessage().indexOf("not unique")!=-1)
      {
        System.err.println(String.format("WARNING: [%s] already exists in database!", file.getName()));
      }
      else
      {
        e.printStackTrace();
      }
    }
  
    return generatedKey;
  }
  

  
  /**
   *   
   * @return Create table query.
   */
  private String createTableQuery()
  {
    return  "CREATE TABLE "+tablename+" ("
                + "uid            INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "duid           INTEGER NOT NULL, "       // Document UID
                + "canonical_path TEXT NOT NULL, "
                + "filename       TEXT NOT NULL"
                + ")";
  }
  
  
}
