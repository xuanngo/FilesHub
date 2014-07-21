package net.xngo.fileshub.db;

import java.io.File;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.db.Document;

/**
 * Implement database structure.
 * @author Xuan Ngo
 *
 */
public class Database
{
  public void create()
  {
    // Create database structure if sqlite database file doesn't exist.
    File DbFile = new File(Conn.DB_FILE_PATH);
    if(!DbFile.exists())
    {
      this.createDbStructure();
    }
  }
  
  private void createDbStructure()
  {
    Document doc = new Document();
    doc.createTable();
  }
}
