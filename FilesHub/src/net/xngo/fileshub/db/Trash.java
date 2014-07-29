package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.Utils;

/**
 * Implement functionalities related to duplicate documents(files) in database.
 * @author Xuan Ngo
 *
 */
public class Trash
{
  private final String tablename  = "Trash";
  
  private Conn conn = Conn.getInstance();
  
  private PreparedStatement insert = null;
  private PreparedStatement select = null;
 
  /**
   * Add file if it doesn't exist.
   * @param duid Document UID.
   * @param hash
   * @param file
   * @return Duplicate UID added. Otherwise, 0.
   */
  public int addFile(final int duid, final String hash, final File file)
  {
    int generatedKey = 0;
    final String canonical_path = Utils.getCanonicalPath(file);
    
    if(!this.isSameFile(canonical_path))
    {// File was never processed before.
      generatedKey = this.insert(duid, hash, file);
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
  
  public boolean isSameFile(String canonicalPath)
  {
    return this.isStringExists("canonical_path", canonicalPath);
  }  
  
  /**
   * @deprecated This is only used by unit test. Remove this if used in application.
   * @param canonicalPath
   * @return Document UID.
   */
  public int getDuidByCanonicalPath(String canonicalPath)
  {
    return Integer.parseInt(this.getString("duid", "canonical_path", canonicalPath));
  }
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  

  
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
  private int findString(String columnName, String value)
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
  
 
  private String getString(String returnColumn, String findColumn, String findValue)
  {
    String returnValue = null;
    
    final String query = String.format("SELECT %s FROM %s WHERE %s = ?", returnColumn, this.tablename, findColumn);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, findValue);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        returnValue = resultSet.getString(1);
      }
      
      return returnValue;
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return returnValue;
  }
  
  private final int insert(final int duid, final String hash, final File file)
  {

    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename, last_modified, hash) VALUES(?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      final String canonical_path = Utils.getCanonicalPath(file);
      final String filename = file.getName();
      int i=1; // Order must match with query.
      this.insert.setInt   (i++, duid);
      this.insert.setString(i++, canonical_path);
      this.insert.setString(i++, filename);
      this.insert.setLong(i++, file.lastModified());
      this.insert.setString(i++, hash);

      
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
                + "duid           INTEGER NOT NULL, " // Document UID
                + "canonical_path TEXT NOT NULL, "
                + "filename       TEXT NOT NULL, "
                + "last_modified  INTEGER NOT NULL, " // Optimization: Rerun same directories but files have changed since last run.
                + "hash           TEXT, "
                + "comment        TEXT "
                + ")";
  }
  
  
}
