package net.xngo.fileshub.db;

import net.xngo.fileshub.db.Conn;

public class Document
{
  private final String tablename  = "Document";
  
  private Conn connection = Conn.getInstance();
  
  public Document()
  {
    
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
