package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.db.Duplicate;
import net.xngo.fileshub.Utils;

/**
 * Implementions functionalities related to documents(files) in the database.
 * @author Xuan Ngo
 *
 */
public class Document
{
  private final String tablename  = "Document";
  
  private Conn conn = Conn.getInstance();
  
  private PreparedStatement insert = null;
  private PreparedStatement select = null;
  
  public int addFile(File file)
  {
    int generatedKey = 0;
    final String canonical_path = Utils.getCanonicalPath(file); 
    if(!this.isSameFile(canonical_path))
    { // Not the exact same file.
      
      // Check hash.
      String hash = Utils.getHash(file);
      long uid = this.findHash(hash);
      
      if(uid==0)
      {// Hash is not found.
        generatedKey = this.insert(file, hash);
      }
      else
      {
        // Same hash but add the record to Duplicate table to keep as history.
        Duplicate dup = new Duplicate();
        dup.addFile(uid, hash, file);
        
        // Output duplicate file.
        System.out.println(String.format("[%s], <%s>", canonical_path, this.getCanonicalPath(uid)));
      }
      
      
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
  
  private String getCanonicalPath(long uid)
  {
    String canonical_path = null;
    final String query = String.format("SELECT canonical_path FROM %s WHERE uid = ?", this.tablename);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setLong(1, uid);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        canonical_path = resultSet.getString(1);
      }
      
      return canonical_path;
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return canonical_path;
  }
  
  private final int insert(final File file, final String hash)
  {

    final String query = "INSERT INTO "+this.tablename+  "(canonical_path, filename, hash) VALUES(?, ?, ?)";
    
    int generatedKey = 0;
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
