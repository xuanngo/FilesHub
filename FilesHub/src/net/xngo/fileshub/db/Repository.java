package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.db.Duplicate;
import net.xngo.fileshub.db.PairFile;
import net.xngo.fileshub.Utils;

/**
 * Implement functionalities related to documents(files) in the database.
 * @author Xuan Ngo
 *
 */
public class Repository
{
  private final String tablename  = "Repository";
  
  private Conn conn = Conn.getInstance();
  
  private PreparedStatement insert = null;
  private PreparedStatement select = null;
  
  /**
   * Add file if it doesn't exist.
   * @param file
   * @return Document UID. Otherwise, -1 = EXACT_SAME_FILE,  0 = DUPLICATE_HASH.
   */
  public PairFile addFile(File file)
  {
    PairFile pairFile = new PairFile();
    pairFile.toAddFile = file;
    
    if(this.isSameFile(file))
    {// Do nothing.
      pairFile.uid = PairFile.EXACT_SAME_FILE;
      pairFile.dbFile = file;
    }
    else
    { // Not the exact same file.
      
      // Check hash.
      String hash = Utils.getHash(file);
      int uid = this.findHash(hash);
      
      if(uid==0)
      {// Hash is not found.
        pairFile.uid = this.insert(file, hash); // Return generatedKeys
        pairFile.dbFile = null;
      }
      else
      { // Same hash but add the record to Duplicate table to keep as history.
        
        // Add duplicate file in database if it doesn't exist.
        Duplicate duplicate = new Duplicate();
        duplicate.addFile(uid, hash, file);
        
        pairFile.uid = PairFile.DUPLICATE_HASH;
        pairFile.dbFile = new File(this.getCanonicalPath(uid));        

      }
    }
    
    return pairFile;
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
  
  
  private boolean isSameFile(File file)
  {
    if(this.getCount(file)>0)
      return true;
    else
      return false;
  }
  
  private long getCount(final File file)
  {
    final String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?", this.tablename, "canonical_path", "last_modified");
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      int i=1;
      this.select.setString(i++, Utils.getCanonicalPath(file));
      this.select.setLong(i++, file.lastModified());
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        return resultSet.getLong(1);
      }
      else
        return 0;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return 0;
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
  
  private int findHash(final String hash)
  {
    return this.findString("hash", hash);
  }
  
  /**
   * 
   * @param columnName
   * @param value
   * @return uid number. If not found, return 0. It is assumed AUTO_INCREMENT start value is 1.
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
  
  private String getCanonicalPath(int uid)
  {
    String canonical_path = null;
    final String query = String.format("SELECT canonical_path FROM %s WHERE uid = ?", this.tablename);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setInt(1, uid);
      
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

    final String query = "INSERT INTO "+this.tablename+  "(canonical_path, filename, last_modified, hash) VALUES(?, ?, ?, ?)";
    
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
                + "last_modified  INTEGER NOT NULL, " // Optimization: Rerun same directories but files have changed since last run.                
                + "hash           TEXT "              
                + ")";
     
  }
  
  
}
