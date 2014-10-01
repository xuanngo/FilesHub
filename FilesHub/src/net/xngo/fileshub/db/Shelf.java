package net.xngo.fileshub.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.report.Chronometer;
import net.xngo.utils.java.db.DbUtils;

/**
 * Class that manipulate documents.
 * @author Xuan Ngo
 *
 */
public class Shelf
{
  private final String tablename  = "Shelf";
  
  private Conn conn = Conn.getInstance();
  
  private PreparedStatement insert = null;
  private PreparedStatement select = null;
  private PreparedStatement delete = null;
  private PreparedStatement update = null;
  
  public void createTable()
  {
    // Create table.
    String query = this.createTableQuery();
    this.conn.executeUpdate(query);
    
    // Create indices.
    this.createIndices();    
  }
  
  public void deleteTable()
  {
    // Delete table.
    String query="DROP TABLE IF EXISTS " + this.tablename;
    this.conn.executeUpdate(query);    
  }
  
  /**
   * @param uid
   * @return {@link Document}
   */
  public Document getDocByUid(final int uid)
  {
    return this.getDocBy("uid", uid+"");
  } 
  
  /**
   * 
   * @param canonicalPath
   * @return {@link Document}
   */
  public Document getDocByCanonicalPath(final String canonicalPath)
  {
    return this.getDocBy("canonical_path", canonicalPath);
  }
  
  /**
   * 
   * @param hash
   * @return {@link Document}
   */
  public Document getDocByHash(final String hash)
  {
    return this.getDocBy("hash", hash);
  }

  /**
   * 
   * @param filename
   * @return {@link Document}
   */
  public Document getDocByFilename(final String filename)
  {
    return this.getDocBy("filename", filename);
  }
  
  public int removeDoc(int duid)
  {
    return this.deleteDoc(duid);
  }
  
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
  }
  
  public int saveDoc(Document doc)
  {
    return this.updateDoc(doc);
  }
  
  /**
   * Return all documents from Shelf table.
   * @return all documents from Shelf table.
   */
  public List<Document> getDocs()
  {
    return this.getDocsBy(null, null);
  }

  
  /**
   * @deprecated Currently used in unit test. Otherwise, remove deprecated.
   * @return {@link Document}
   */
  public int getTotalDocs()
  {
    final String query = "SELECT COUNT(*) FROM " + this.tablename;
    
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
  
  public List<Document> searchDocsByFilename(String filename)
  {
    return this.searchLikeDocsBy("filename", filename);
  }
  
  public List<Document> searchDocsByFilepath(String filepath)
  {
    return this.searchLikeDocsBy("canonical_path", filepath);
  }  
  
 
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/

  private int deleteDoc(int duid)
  {
    final String query = "DELETE FROM "+this.tablename+" WHERE uid=?";
    int rowsAffected = 0;
    try
    {
      this.delete = this.conn.connection.prepareStatement(query);
      
      this.delete.setInt(1, duid);
      
      rowsAffected = this.delete.executeUpdate();
      DbUtils.close(this.delete);      
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
      
      DbUtils.close(resultSet);
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
    finally
    {
      DbUtils.close(this.insert);
    }  
    return generatedKey;
  }
  
  private final int updateDoc(Document doc)
  {
    doc.sanityCheck();
    doc.checkUid();
    
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

      DbUtils.close(this.update);         
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        if(this.update!=null)
          this.update.close();
      }
      catch(SQLException ex) 
      {
        RuntimeException rException = new RuntimeException();
        rException.setStackTrace(ex.getStackTrace());
        throw rException;
      }
    }
    
    return rowAffected;
  }

  /**
   * 
   * @param column
   * @param value
   * @return {@link Document}
   */
  private Document getDocBy(String column, String value)
  {
    List<Document> docs = this.getDocsBy(column, value);
    if(docs.size()==0)
    {
      return null;
    }
    else if(docs.size()==1)
    {
      return docs.get(0);
    }
    else
    {
      String msg = String.format("'SELECT ... FROM %s WHERE %s = %s' returns %d entries. Expect 0 or 1.", this.tablename, column, value, docs.size());      
      throw new RuntimeException(msg);
    }
  }
  
  private List<Document> getDocsBy(String column, String value)
  {
    // Construct sql query.
    String where = "";
    if(column!=null)
    {
      if(!column.isEmpty())
        where = String.format("WHERE %s = ?", column);
    }
    
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s"
                                        + " %s", this.tablename, where);
    
    // Get the documents.
    List<Document> docList = new ArrayList<Document>();
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      if(!where.isEmpty())
      {
        int i=1;
        this.select.setString(i++, value);
      }
    
      ResultSet resultSet =  this.select.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++);
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docList.add(doc);
      }
      DbUtils.close(resultSet);
      DbUtils.close(this.select);      
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return docList;
  }  
  
  private List<Document> searchLikeDocsBy(String column, String value)
  {
    // Input validations.
    if(column==null){ throw new RuntimeException("column can't be null."); }
    if(column.compareTo("uid")==0 || column.compareTo("last_modified")==0)
    { 
      throw new RuntimeException(column+" is an integer field. It is not allowed to be used in LIKE statement."); 
    }
    
    // Convert wildcard(*) to %.
    String likeValue = value.replace('*', '%');
    
    // Construct the query.
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s"
                                        + " WHERE %s like ?", this.tablename, column, likeValue);
    
    
    List<Document> docsList = new ArrayList<Document>();
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, likeValue);

      ResultSet resultSet =  this.select.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++);
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docsList.add(doc);
      }
      DbUtils.close(resultSet);
      DbUtils.close(this.select);      
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return docsList;
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
  
  private void createIndices()
  {
    String[] indices={"CREATE INDEX shelf_hash ON "+this.tablename+" (hash);",
                      "CREATE INDEX shelf_canonical_path ON "+this.tablename+" (canonical_path);"};
    for(String query: indices)
    {
      this.conn.executeUpdate(query);
    }
  }
  
}
