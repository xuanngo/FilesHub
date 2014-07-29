package net.xngo.fileshub.db;

import java.io.File;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.db.Repository;
import net.xngo.fileshub.db.Trash;

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
    {// Database file doesn't exist.
      this.createDbStructure();
    }
  }
  
  private void createDbStructure()
  {
    Repository repository = new Repository();
    repository.createTable();
    
    Trash trash = new Trash();
    trash.createTable();
  }
}
