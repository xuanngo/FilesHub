package net.xngo.fileshub.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.struct.Document;

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
   * @deprecated This is bad. Use addDoc() instead.
   * Add file if it doesn't exist.
   * @param duid Document UID.
   * @param hash
   * @param file
   * @return Duplicate UID added. Otherwise, 0.
   */
  public int addFile(final Document doc)
  {
    int generatedKey = 0;
    
    if(!this.isSameFile(doc.canonical_path))
    {// File was never processed before.
      generatedKey = this.insertDoc(doc);
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
  
  /**
   * @deprecated This is only used by unit test. Remove this if used in application.
   * @param canonicalPath
   * @return
   */
  public Document findDocByCanonicalPath(final String canonicalPath)
  {
    return this.findDocBy("canonical_path", canonicalPath);
  }
  
  /**
   * @deprecated This is only used by unit test. Remove this if used in application.
   * @param hash
   * @return
   */
  public Document findDocByHash(String hash)
  {
    return this.findDocBy("hash", hash);
  }
  
  /**
   * @deprecated Currently used in unit test. Otherwise, remove deprecated.
   * @return
   */
  public int getTotalDocs()
  {
    final String query = String.format("SELECT COUNT(*) FROM %s", this.tablename);
    
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      ResultSet resultSet =  this.select.executeQuery();
      if(resultSet.next())
      {
        return resultSet.getInt(1);
      }
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return 0;
  }  
  
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
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
      
      if(returnValue == null)
      {
        String e = String.format("Result not found: SELECT %s FROM %s WHERE %s = %s", returnColumn, this.tablename, findColumn, findValue);
        throw new RuntimeException(e);
      }
      return returnValue;
    }
    catch(SQLException e)
    {
      e.printStackTrace();
      return returnValue;
    }
  }
  
  private Document findDocBy(String column, String value)
  {
    Document doc = null;
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s "
                                        + "WHERE %s = ?", this.tablename, column);
    
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      int i=1;
      this.select.setString(i++, value);
      
      ResultSet resultSet =  this.select.executeQuery();
      if(resultSet.next())
      {
        doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        return doc;
      }
      else
        return doc;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return doc;
  }
  
  private final int insertDoc(final Document doc)
  {
    doc.checkUid();
    doc.sanityCheck();
    
    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename, last_modified, hash, comment) VALUES(?, ?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      int i=1; // Order must match with query.
      this.insert.setInt   (i++, doc.uid);
      this.insert.setString(i++, doc.canonical_path);
      this.insert.setString(i++, doc.filename);
      this.insert.setLong  (i++, doc.last_modified);
      this.insert.setString(i++, doc.hash);
      this.insert.setString(i++, doc.comment);

      
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
        System.err.println(String.format("WARNING: [%s] already exists in database!", doc.filename));
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
