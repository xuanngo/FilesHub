package net.xngo.fileshub.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.db.DbUtils;

/**
 * Implement functionalities related to duplicate documents(files) in database.
 * @author Xuan Ngo
 *
 */
public class Trash
{
  final static Logger log = LoggerFactory.getLogger(Trash.class);
  
  protected final String tablename  = "Trash";
 
  /**
   * @param canonicalPath
   * @return {@link Document}
   */
  public Document getDocByCanonicalPath(final String canonicalPath)
  {
    return this.getDocBy("canonical_path", canonicalPath);
  }
  
  /**
   * @param hash
   * @return {@link Document}
   */
  public Document getDocByHash(String hash)
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
  public Document getDocByFilename(String filename)
  {
    return this.getDocBy("filename", filename);
  }
  
  public List<Document> getDocsByUid(int uid)
  {
    return this.getDocsBy("duid", uid+"");
  }  
  
  public List<Document> getDocsWithMissingFileSize()
  {
    return this.getDocsBy("size", "<", "1");
  }
  
  
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
  }
  
  public int removeDoc(Document doc)
  {
    int rowsAffected = this.deleteDoc(doc);
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No document is removed: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;
  }
  
  public List<Document> searchDocsByFilename(String filename)
  {
    return this.searchLikeDocsBy("filename", filename);
  }
  
  public List<Document> searchDocsByFilepath(String filepath)
  {
    return this.searchLikeDocsBy("canonical_path", filepath);
  }  
  
  public int changeDuid(int fromDuid, int toDuid)
  {
    return this.updateInteger("duid", fromDuid, "duid", toDuid);
  }
  
  public int changeDuid(String fromCanonicalPath, int toDuid)
  {
    final int rowsAffected = this.update("canonical_path", fromCanonicalPath, "duid", toDuid);
    
    if (rowsAffected==0)
      throw new RuntimeException(String.format("No duid has been changed: %s", Main.connection.getQueryString()));
    else
      return rowsAffected;    
  }
  
  /**
   * Save size.
   * Use in upgrade version 2.
   * @param hash
   * @param size
   * @return
   */
  public int saveSize(String hash, long size)
  {
    if(hash==null)
      throw new RuntimeException("Hash can't be null.");
    if(hash.isEmpty())
      throw new RuntimeException("Hash can't be empty.");    
    
    if(size<0)
      throw new RuntimeException(String.format("Size can't be negative: hash=%s, size=%d", hash, size));
    
    return this.update("hash", hash, "size", size);
  }
  
  /**
   * Orphan = duid in Trash but not Shelf.
   * @return List of Trash documents that don't have a matching DUID from Shelf table.
   */
  public List<Document> getOrphans()
  {
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, size, hash, comment "
                                      + "FROM %s "
                                      + "LEFT JOIN Shelf ON Trash.duid=Shelf.uid WHERE Shelf.uid IS NULL", this.tablename);
    
    // Get the documents.
    ArrayList<Document> docsList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
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
   * Orphan = duid in Trash but not Shelf.
   * @return Total number of Trash documents that don't have a matching DUID from Shelf table.
   */
  public int getTotalOrphans()
  {
    final String query = String.format("SELECT COUNT(*) "
                                      + "FROM Trash "
                                      + "LEFT JOIN Shelf ON Trash.duid=Shelf.uid WHERE Shelf.uid IS NULL");
    
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
  
  /**
   * Remove orphan documents in Trash: duid in Trash but not Shelf
   * @return Total number of orphan documents in Trash table deleted.
   */
  public int removeOrphans()
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
  
  public int removeDocByDuid(int duid)
  {
    // Add conditions that make Document unique.
    final String query = String.format("DELETE FROM %s WHERE duid=?", this.tablename);
    int rowsAffected = 0;
    try
    {
      Main.connection.prepareStatement(query);
      
      int i=1; // Order must match with query.
      Main.connection.setInt   (i++, duid);
      
      rowsAffected = Main.connection.executeUpdate();

      Main.connection.closePreparedStatement();     
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    return rowsAffected;    
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/

  /**
   * Update key* with replace*
   * @param keyColumn
   * @param keyValue
   * @param replaceColumn
   * @param replaceValue
   * @return
   */
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
  
  private int deleteDoc(Document doc)
  {
    // Add conditions that make Document unique.
    final String query = "DELETE FROM "+this.tablename+" WHERE duid=? AND hash=? and canonical_path=?";
    int rowsAffected = 0;
    try
    {
      Main.connection.prepareStatement(query);
      
      int i=1; // Order must match with query.
      Main.connection.setInt   (i++, doc.uid);
      Main.connection.setString(i++, doc.hash);
      Main.connection.setString(i++, doc.canonical_path);
      
      rowsAffected = Main.connection.executeUpdate();

      Main.connection.closePreparedStatement();     
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    return rowsAffected;    
  }
  
  private List<Document> searchLikeDocsBy(String column, String value)
  {
    // Input validations.
    if(column==null){ throw new RuntimeException("column can't be null."); }
    if(column.compareTo("uid")==0 || column.compareTo("duid")==0 || column.compareTo("last_modified")==0)
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
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, size, hash, comment"
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
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
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
      StringBuffer msg = new StringBuffer(String.format("'%s' returns %d entries with the same hash. Expect 0 or 1 entry. Entries found are:\n", Main.connection.getQueryString(), docs.size()));
      for(Document doc: docs)
        msg.append(doc.toString("\t")).append("\n");
      
      RuntimeException rException = new RuntimeException(msg.toString());
      log.error("This method can't return 2 or more documents with the same hash.", rException);
      throw rException;
      
    }
  }
  
  private List<Document> getDocsBy(String column, String operator, Object value)
  {
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, size, hash, comment"
                                      + " FROM %s"
                                      + " WHERE %s %s ?", this.tablename, column, operator);
    
    // Get the documents.
    ArrayList<Document> docsList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      Main.connection.setObject(1, value);
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
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
  
  private List<Document> getDocsBy(String column, String value)
  {
    // Construct sql query.
    String where = "";
    if(column!=null)
    {
      if(!column.isEmpty())
        where = String.format("WHERE %s = ?", column);
    }    
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, size, hash, comment "
                                      + " FROM %s"
                                      + " %s", this.tablename, where);
    
    // Get the documents.
    ArrayList<Document> docsList = new ArrayList<Document>();
    try
    {
      Main.connection.prepareStatement(query);
      Main.connection.setString(1, value);
      ResultSet resultSet =  Main.connection.executeQuery();

      while(resultSet.next())
      {
        Document doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
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
   * Insert a document.
   * @param doc
   * @return Generated key. Otherwise, 0 for failure.
   */
  private final int insertDoc(final Document doc)
  {
    doc.checkUid();
    doc.sanityCheck();
    
    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename, last_modified, size, hash, comment) VALUES(?, ?, ?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1; // Order must match with query.
      Main.connection.setInt   (i++, doc.uid);
      Main.connection.setString(i++, doc.canonical_path);
      Main.connection.setString(i++, doc.filename);
      Main.connection.setLong  (i++, doc.last_modified);
      Main.connection.setLong  (i++, doc.size);
      Main.connection.setString(i++, doc.hash);
      Main.connection.setString(i++, doc.comment);

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

  
}
