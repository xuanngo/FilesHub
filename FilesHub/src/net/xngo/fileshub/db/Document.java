package net.xngo.fileshub.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.Utils;

public class Document
{
  private final String tablename  = "Document";
  
  private Conn connection = Conn.getInstance();
  
  public PreparedStatement insert = null;
  /*
  public PreparedStatement select = null;
  public PreparedStatement delete = null;
  public PreparedStatement update = null;  
  */
  
  public Document()
  {
    
  }
  
  public boolean addFile(File file)
  {
    
    this.insert(file);
    
    return true;
  }
  
  private final int insert(final File file)
  {

    final String query = "INSERT INTO "+this.tablename+  "(path, filename, size, hash) VALUES(?, ?, ?, ?)";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      this.insert = this.connection.connection.prepareStatement(query);
      
      // Set the data.
      final String path = Utils.getCanonicalPath(file);
      final String filename = file.getName();
      final long size = file.length();
      final String hash = Utils.getHash(file);
      int i=1;
      this.insert.setString(i++, path);
      this.insert.setString(i++, filename);
      this.insert.setLong  (i++, size);
      this.insert.setString(i++, hash);
      
      // Insert row.
      rowAffected = this.insert.executeUpdate();
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
  
    return rowAffected;
  }
  
  public void createTable()
  {
    String query = this.createTableQuery();
    connection.executeUpdate(query);
  }
  
  public void deleteTable()
  {
    // Delete table.
    String query="DROP TABLE IF EXISTS " + this.tablename;
    connection.executeUpdate(query);    
  }
  
  /**
   * 
   * @return Create table query.
   */
  private String createTableQuery()
  {
    return  "CREATE TABLE "+tablename+" ("
                + "uid      INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "path     TEXT NOT NULL, "
                + "filename TEXT NOT NULL, "
                + "size     INTEGER NOT NULL, "
                + "hash     TEXT "
                + ")";
     
  }
  
  
}
