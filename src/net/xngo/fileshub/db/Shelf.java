package net.xngo.fileshub.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xngo.fileshub.Config;
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
  final static Logger log = LoggerFactory.getLogger(Shelf.class);
  
  protected final String tablename  = "Shelf";
  
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
  
  /**
   * Save size.
   * Use in upgrade version 2.
   * @param uid
   * @param size
   * @return
   */
  public int saveSize(int uid, long size)
  {
    if(size<0)
      throw new RuntimeException(String.format("Size can't be negative: uid=%d, size=%d", uid, size));
    
    final int rowsAffected = this.update("uid", uid, "size", size);
    
    if (rowsAffected==0)
      throw new RuntimeException(String.format("Size is not updated: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;        
  }
  
  public int changeUid(int fromUid, int toUid)
  {
    final int rowsAffected = this.update("uid", fromUid, "uid", toUid);
    
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No uid has been changed: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;
  }
  
  /**
   * Return all documents from Shelf table.
   * Warning: Big table = big memory usage.
   * @return all documents from Shelf table.
   */
  public List<Document> getDocs()
  {
    return this.getDocsBy(null, null);
  }

  public List<Document> searchDocsByFilename(String filename)
  {
    return this.searchLikeDocsBy("filename", filename);
  }
  
  public List<Document> searchDocsByFilepath(String filepath)
  {
    return this.searchLikeDocsBy("canonical_path", filepath);
  }
  
  public List<Document> getDocsWithMissingFileSize()
  {
    return this.getDocsBy("size", "<", "1");
  }  
 
  public int removeDuplicateHash()
  {
    final String query = "DELETE FROM Trash WHERE duid IN "
                          + "("
                          + "SELECT Trash.duid FROM Trash LEFT JOIN Shelf ON Trash.duid=Shelf.uid WHERE Shelf.uid IS NULL"
                          + ")";
    int rowsAffected = 0;
    try
    {
      Main.connection.prepareStatement(query);
      
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
   * Remove all documents in Shelf and in Trash tables having matching duid.
   * @param duid
   * @return
   */
  public int removeAllDoc(int duid)
  {
    Trash trash = new Trash();
    int rowsAffectedShelf = this.deleteDoc(duid);
    int rowsAffectedTrash = trash.removeDocByDuid(duid);
    if (rowsAffectedShelf==0)
      throw new RuntimeException(String.format("No document is removed: %s", Main.connection.getQueryString()));
    else
      return rowsAffectedShelf;    
  }
  
  /**
   * @return Total number of duplicate hash in Shelf table.
   */
  public int getTotalDuplicateHash()
  {
    // COUNT(*) will not give the total number of rows when using with GROUP BY.
    final String query = String.format("SELECT hash FROM Shelf GROUP BY hash HAVING COUNT(*) > 1");
    int found = 0;    
    try
    {
      Main.connection.prepareStatement(query);
      
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        found++;
      }      
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return found;
  }
  
  public List<String> getDuplicateHashes()
  {
    final String query = String.format("SELECT hash FROM Shelf GROUP BY hash HAVING COUNT(*) > 1");
    
    // Get the documents.
    List<String> hashList = new ArrayList<String>();
    try
    {
      Main.connection.prepareStatement(query);
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        hashList.add(resultSet.getString(1));
      }
      DbUtils.close(resultSet);
      Main.connection.closePreparedStatement();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return hashList;
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
  
  private int update(String replaceColumn, String replaceValue, String keyColumn, String keyValue)
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

  private int update(String keyColumn, Object keyValue, String replaceColumn, Object replaceValue)
  {
    final String query = String.format("UPDATE %s SET %s=? WHERE %s=?", this.tablename, replaceColumn, keyColumn);
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setObject(i++, replaceValue );
      Main.connection.setObject(i++, keyValue     );
      
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
  
  private int update(String keyColumn, int keyValue, String replaceColumn, int replaceValue)
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
      StringBuffer msg = new StringBuffer(String.format("'%s' returns %d entries with same the hash. Expect 0 or 1 entry. Entries found are:\n", Main.connection.getQueryString(), docs.size()));
      for(Document doc: docs)
        msg.append(doc.toString("\t")).append("\n");

      RuntimeException rException = new RuntimeException(msg.toString());
      log.error("This method can't return 2 or more documents with the same hash.", rException);
      throw rException;
      
    }
  }
  
  private List<Document> getDocsBy(String column, String operator, Object value)
  {
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, size, hash, comment"
                                        + " FROM %s"
                                        + " WHERE %s %s ?", this.tablename, column, operator);
    
    // Get the documents.
    List<Document> docList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      Main.connection.setObject(1, value);
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
    
  
  private List<Document> getDocsBy(String column, String value)
  {
    // Construct sql query.
    String where = "";
    if(column!=null)
    {
      if(!column.isEmpty())
        where = String.format("WHERE %s = ?", column);
    }
    
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, size, hash, comment"
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
                                        + " WHERE %s %s ?", this.tablename, column, likeOrEqual);
    
    
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
 
}
