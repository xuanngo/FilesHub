package net.xngo.fileshub.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
  private final String tablename  = "Trash";
 
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
  
  /**
   * @deprecated Currently used in unit test. Otherwise, remove deprecated.
   * @return
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
  
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
  }
  
  public int removeDoc(Document doc)
  {
    return this.deleteDoc(doc);
  }
  

  
  
  public int markDuplicate(int duplicate, int of)
  {
    final String query = "UPDATE "+this.tablename+  " SET duid = ? WHERE duid = ?";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      Main.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      Main.connection.setInt(i++, of);
      Main.connection.setInt(i++, duplicate);
      
      // update row.
      rowAffected = Main.connection.executeUpdate();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
  
    return rowAffected;    
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
 
  private int deleteDoc(Document doc)
  {
    // Add conditions that make Document unique.
    final String query = "DELETE FROM "+this.tablename+" WHERE uid=? AND hash=? and canonical_path=?";
    int rowsAffected = 0;
    try
    {
      Main.connection.prepareStatement(query);
      
      int i=1; // Order must match with query.
      Main.connection.setInt   (i++, doc.uid);
      Main.connection.setString(i++, doc.hash);
      Main.connection.setString(i++, doc.canonical_path);
      
      rowsAffected = Main.connection.executeUpdate();

//      DbUtils.close(this.delete);        
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
    
    // Convert wildcard(*) to %.
    String likeValue = value.replace('*', '%');
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s "
                                        + " WHERE %s like ?", this.tablename, column, likeValue);
    
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
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docsList.add(doc);
      }
      DbUtils.close(resultSet);
//      DbUtils.close(this.select);        

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
      String msg = String.format("'SELECT ... FROM %s WHERE %s = %s' returns %d entries. Expect 0 or 1 entry.", this.tablename, column, value, docs.size());
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
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, hash, comment "
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
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        docsList.add(doc);
        
      }
      DbUtils.close(resultSet);
//      DbUtils.close(this.select);         
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
    
    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename, last_modified, hash, comment) VALUES(?, ?, ?, ?, ?, ?)";
    
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
//      DbUtils.close(this.insert);
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
      /* Temporarily comment this due to Conn.java refactoring.
      try
      {
        if(this.insert!=null)
          this.insert.close();
      }
      catch(SQLException ex) 
      {
        RuntimeException rException = new RuntimeException();
        rException.setStackTrace(ex.getStackTrace());
        throw rException;
      }
      */
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
  
  private void createIndices()
  {
    String[] indices={"CREATE INDEX trash_hash ON "+this.tablename+" (hash);",
                      "CREATE INDEX trash_canonical_path ON "+this.tablename+" (canonical_path);"};
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
