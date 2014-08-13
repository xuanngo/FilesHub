package net.xngo.fileshub.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.struct.Document;

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
    List<Document> docList = this.findDocBy("uid", uid+"");
    if(docList.size()==0)
      return null;
    else
      return docList.get(0);
  } 
  
  public Document findDocByCanonicalPath(final String canonicalPath)
  {
    List<Document> docList = this.findDocBy("canonical_path", canonicalPath);
    if(docList.size()==0)
      return null;
    else
      return docList.get(0);    
  }
  
  public Document findDocByHash(final String hash)
  {
    List<Document> docList = this.findDocBy("hash", hash);
    if(docList.size()==0)
      return null;
    else
      return docList.get(0);       
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
  
  
  public List<Document> getAllDoc()
  {
    return this.findDocBy(null, null);
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
  
  private List<Document> findDocBy(String column, String value)
  {
    // Construct sql query.
    String where = "";
    if(column!=null)
    {
      if(!column.isEmpty())
        where = String.format("WHERE %s = ?", column);
    }
    
    final String query = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s "
                                        + "%s", this.tablename, where);
    
    
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
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return docList;
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
