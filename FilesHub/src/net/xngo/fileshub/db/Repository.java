package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.ResultDocSet;

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
  private PreparedStatement delete = null;
  private PreparedStatement update = null;
  
  /**
   * Add file if it doesn't exist.
   * @param file
   * @return Document UID. Otherwise, -1 = EXACT_SAME_FILE,  0 = DUPLICATE_HASH.
   */
  public ResultDocSet addFile(File file)
  {
    ResultDocSet resultDocSet = new ResultDocSet();
    
    Document docFromDb = this.findDocByCanonicalPath(Utils.getCanonicalPath(file));
    if(docFromDb!=null)
    {// File path found in Shelf table.
      
      
      if(docFromDb.last_modified != file.lastModified())
      {// File has changed. 
        // Move docFromDb to Trash table.
        Trash trash = new Trash();
        trash.addFile(docFromDb);
        
        // Update changed file to Shelf table.
        String hash = Utils.getHash(file);
        Document newDoc = new Document(file);
        newDoc.uid = docFromDb.uid;
        newDoc.hash = hash;
        this.updateDoc(newDoc);
       
        // Note: It is possible that the file is overwritten with an older version.
        //        Therefore, files in Shelf table can be older than Trash table.
        
        // Update status.
        resultDocSet.status = ResultDocSet.SAME_PATH_DIFF_HASH;
        resultDocSet.file     = file;
        resultDocSet.shelfDoc = newDoc;
        resultDocSet.trashDoc = docFromDb;        
      }
      else
      { // Nothing to do. Exact same file.
        
        // Update status.
        resultDocSet.status = ResultDocSet.EXACT_SAME_FILE;
        resultDocSet.file     = file;
        resultDocSet.shelfDoc = docFromDb;
        resultDocSet.trashDoc = null;
      }

    }
    else
    { // File path not found in Shelf table.
      
      // Check hash.
      String hash = Utils.getHash(file);
      Document doc = this.findDocByHash(hash);
      
      if(doc==null)
      {// Hash is not found.
        doc = new Document(file);
        doc.hash = hash;
        doc.uid = this.insertDoc(doc); // Return generatedKeys
        
        // Update status.
        resultDocSet.status = ResultDocSet.DIFF_PATH_DIFF_HASH; // New unique file.
        resultDocSet.file     = file;
        resultDocSet.shelfDoc = doc;
        resultDocSet.trashDoc = null;        
      }
      else
      { // Found hash but different path. Therefore, add it to Trash table to keep it as history.
        
        // Add duplicate file in Trash table if it doesn't exist.
        Document trashDoc = new Document(file);
        trashDoc.uid = doc.uid;
        trashDoc.hash = doc.hash;
        Trash trash = new Trash();
        trash.addFile(trashDoc);
    
        // Update status.
        resultDocSet.status = ResultDocSet.DIFF_PATH_SAME_HASH; // Duplicate file.
        resultDocSet.file     = file;
        resultDocSet.shelfDoc = doc;
        resultDocSet.trashDoc = trashDoc;        

      }
    }
    
    return resultDocSet;
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
  
  /**
   * @deprecated This is only used by unit test. Remove this if used in application.
   * @param uid
   * @return
   */
  public Document findDocByUid(final int uid)
  {
    return this.findDocBy("uid", uid+"");
  } 
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  private Document findDocByCanonicalPath(final String canonicalPath)
  {
    return this.findDocBy("canonical_path", canonicalPath);
  }
  
  private Document findDocByHash(final String hash)
  {
    return this.findDocBy("hash", hash);
  }
  private Document findDocBy(String column, String value)
  {
    Document doc = null;
    
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment "
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
        doc.uid             = resultSet.getInt(j++);
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
  
  private int deleteDoc(long uid)
  {
    final String query = "DELETE FROM "+this.tablename+" WHERE uid=?";
    int rowsAffected = 0;
    try
    {
      this.delete = this.conn.connection.prepareStatement(query);
      
      this.delete.setLong(1, uid);
      
      rowsAffected = this.delete.executeUpdate();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    return rowsAffected;    
  }
  
  private final int insertDoc(final Document doc)
  {
    doc.sanityCheck();

    final String query = "INSERT INTO "+this.tablename+  "(canonical_path, filename, last_modified, hash, comment) VALUES(?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      this.insert.setString (i++, doc.canonical_path);
      this.insert.setString (i++, doc.filename);
      this.insert.setLong   (i++, doc.last_modified);
      this.insert.setString (i++, doc.hash);
      this.insert.setString (i++, doc.comment);
      
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
  
  private final int updateDoc(Document doc)
  {
    doc.sanityCheck();
    
    final String query = "UPDATE "+this.tablename+  " SET canonical_path = ?, filename = ?, last_modified = ?, hash = ?, comment = ? WHERE uid = ?";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      this.update = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      this.update.setString(i++, doc.canonical_path  );
      this.update.setString(i++, doc.filename        );
      this.update.setLong  (i++, doc.last_modified   );
      this.update.setString(i++, doc.hash            );
      this.update.setString(i++, doc.comment         );      
      this.update.setInt   (i++, doc.uid             );
      
      // update row.
      rowAffected = this.update.executeUpdate();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
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
                + "last_modified  INTEGER NOT NULL, " // Optimization: Rerun same directories but files have changed since last run.                
                + "hash           TEXT, "              
                + "comment        TEXT "
                + ")";
     
  }
  
  
}
