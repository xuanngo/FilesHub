package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.Utils;

public class Document
{
  private final String tablename  = "Document";
  
  private Conn conn = Conn.getInstance();
  
  public PreparedStatement insert = null;
  public PreparedStatement select = null;
  /*
  public PreparedStatement delete = null;
  public PreparedStatement update = null;  
  */
  
 
  public boolean addFile(File file)
  {
    final String canonical_path = Utils.getCanonicalPath(file); 
    if(this.isSameFile(canonical_path))
    {
      // File ignore because it is the exact same file that had already been processed.
      System.out.println("Exact file: "+canonical_path);
    }
    else
    { // Check hash
      
      String hash = Utils.getHash(file);
      long uid = this.findHash(hash);
      
      if(uid==0)
      {// Hash is not found.
        this.insert(file, hash);
      }
      else
      {
        System.out.println(String.format("[%s][%s]: Existing hash found.", uid, canonical_path));
      }
      
      
    }
    
    return true;
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
  
  private long findHash(final String hash)
  {
    return this.findString("hash", hash);
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
  
  private final int insert(final File file, final String hash)
  {

    final String query = "INSERT INTO "+this.tablename+  "(canonical_path, filename, hash) VALUES(?, ?, ?)";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      final String canonical_path = Utils.getCanonicalPath(file);
      final String filename = file.getName();
      int i=1;
      this.insert.setString(i++, canonical_path);
      this.insert.setString(i++, filename);
      this.insert.setString(i++, hash);
      
      // Insert row.
      rowAffected = this.insert.executeUpdate();
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
  
    return rowAffected;
  }
  

  
  /**
   * Don't put too much constraint on the column. Do validations on the application side.
   *   For example, it is tempted to set "hash" column to be UNIQUE or NOT NULL. Don't.
   *   Hashing takes a lot of time. What if you want to calculate the hash value later on.
   *   
   * @return Create table query.
   */
  private String createTableQuery()
  {
    return  "CREATE TABLE "+tablename+" ("
                + "uid            INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "canonical_path TEXT NOT NULL, "
                + "filename       TEXT NOT NULL, "
                + "hash           TEXT "              
                + ")";
     
  }
  
  
}
