package net.xngo.fileshub.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.db.DbUtils;

/**
 * Class that manipulate documents in Shelf table.
 * @author Xuan Ngo
 *
 */
public class Shelf
{
  private final String tablename  = "Shelf";
  
  
  public void createTable()
  {
    // Create table.
    String query = this.createTableQuery();
    try
    {
      Main.connection.prepareStatement(query);
      Main.connection.executeUpdate();
    }
    catch(SQLException ex) { ex.printStackTrace(); }
    
    // Create indices.
    this.createIndices();    
  }
  
  public void deleteTable()
  {
    // Delete table.
    String query="DROP TABLE IF EXISTS " + this.tablename;
    try
    {
      Main.connection.prepareStatement(query);
      Main.connection.executeUpdate();
    }
    catch(SQLException ex) { ex.printStackTrace(); }   
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

  public List<Document> getDocsByHash(String hash)
  {
    return this.getDocsBy("hash", hash);
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
    int rowsAffected = this.deleteDoc(duid);
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No document is removed: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;    
  }
  
  /**
   * Add a document in Shelf table.
   * @param doc
   * @return generated key.
   */
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
  }
  
  public int saveDoc(Document doc)
  {
    final int rowsAffected = this.updateDoc(doc);
    
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No document is saved: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;    
  }
  
  public int changeUid(int fromUid, int toUid)
  {
    final int rowsAffected = this.updateInteger("uid", fromUid, "uid", toUid);
    
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No uid has been changed: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;     
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
      Main.connection.prepareStatement(query);
      
      ResultSet resultSet =  Main.connection.executeQuery();
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
      Main.connection.prepareStatement(query);
      
      Main.connection.setInt(1, duid);
      
      rowsAffected = Main.connection.executeUpdate();
      Main.connection.closePreparedStatement();    
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    return rowsAffected;    
  }
  
  /**
   * Insert document in Shelf table.
   * Note: Never add uid. Let's the database engine generated a new ID.
   *        If you need to change uid, use {@link changeUid}.
   * @param doc
   * @return generated key
   */
  private final int insertDoc(final Document doc)
  {
    doc.sanityCheck();

    final String query = "INSERT INTO "+this.tablename+  "(canonical_path, filename, last_modified, size, hash, comment) VALUES(?, ?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setString (i++, doc.canonical_path);
      Main.connection.setString (i++, doc.filename);
      Main.connection.setLong   (i++, doc.last_modified);
      Main.connection.setLong   (i++, doc.size);
      Main.connection.setString (i++, doc.hash);
      Main.connection.setString (i++, doc.comment);
      
      // Insert row.
      Main.connection.executeUpdate();
      ResultSet resultSet =  Main.connection.getGeneratedKeys();
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
      Main.connection.closePreparedStatement();
    }  
    return generatedKey;
  }
  
  private final int updateDoc(Document doc)
  {
    doc.sanityCheck();
    doc.checkUid();
    
    final String query = "UPDATE "+this.tablename+  " SET canonical_path = ?, filename = ?, last_modified = ?, size = ?, hash = ?, comment = ? WHERE uid = ?";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setString(i++, doc.canonical_path  );
      Main.connection.setString(i++, doc.filename        );
      Main.connection.setLong  (i++, doc.last_modified   );
      Main.connection.setLong  (i++, doc.size            );
      Main.connection.setString(i++, doc.hash            );
      Main.connection.setString(i++, doc.comment         );      
      Main.connection.setInt   (i++, doc.uid             );
      
      // update row.
      rowAffected = Main.connection.executeUpdate();
         
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      Main.connection.closePreparedStatement();
    }
    
    return rowAffected;
  }
  
  private int updateString(String replaceColumn, String replaceValue, String keyColumn, String keyValue)
  {
    final String query = String.format("UPDATE %s SET %s=? WHERE %s=?", this.tablename, replaceColumn, keyColumn);
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setString(i++, replaceValue );
      Main.connection.setString(i++, keyValue     );
      
      // update row.
      rowAffected = Main.connection.executeUpdate();
         
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      Main.connection.closePreparedStatement();
    }
    
    return rowAffected;    
  }

  private int updateInteger(String keyColumn, int keyValue, String replaceColumn, int replaceValue)
  {
    final String query = String.format("UPDATE %s SET %s=? WHERE %s=?", this.tablename, replaceColumn, keyColumn);
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setInt(i++, replaceValue );
      Main.connection.setInt(i++, keyValue     );
      
      // update row.
      rowAffected = Main.connection.executeUpdate();

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    finally
    {
      Main.connection.closePreparedStatement();
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
      String msg = String.format("'%s' returns %d entries. Expect 0 or 1 entry.", Main.connection.getQueryString(), docs.size());
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
    
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, size, hash, comment "
                                        + " FROM %s"
                                        + " %s", this.tablename, where);
    
    // Get the documents.
    List<Document> docList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      
      if(!where.isEmpty())
      {
        int i=1;
        Main.connection.setString(i++, value);
      }
    
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++);
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.size            = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docList.add(doc);
      }
      DbUtils.close(resultSet);
      Main.connection.closePreparedStatement();
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
    
    // Optimization: If no wildcard, then use equal(=). Otherwise, user LIKE.
    String likeOrEqual = "=";
    if(value.indexOf('*')!=-1)
    {// Wildcard found.
      likeOrEqual = "like";
    }
    
    // Convert wildcard(*) to %.
    String likeValue = value.replaceAll("[\\*\\*]+", "*"); // Clean duplicate adjacent wildcard.
    likeValue = likeValue.replace('*', '%');
    
    // Construct the query.
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, size, hash, comment"
                                        + " FROM %s"
                                        + " WHERE %s %s ?", this.tablename, column, likeOrEqual, likeValue);
    
    
    List<Document> docsList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      
      Main.connection.setString(1, likeValue);

      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++);
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.size            = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docsList.add(doc);
      }
      DbUtils.close(resultSet);
      Main.connection.closePreparedStatement();      
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
                + "size           INTEGER NOT NULL, " // Document size in bytes.                
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
      try
      {
        Main.connection.prepareStatement(query);
        Main.connection.executeUpdate();
      }
      catch(SQLException ex) { ex.printStackTrace(); }
    }
  }
  
}
