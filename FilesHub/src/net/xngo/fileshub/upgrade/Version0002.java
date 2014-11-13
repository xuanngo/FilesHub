package net.xngo.fileshub.upgrade;

import java.sql.SQLException;
import java.util.List;
import java.io.File;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.report.Report;

/**
 * Assuming size column is created in Shelf and Trash table.
 * Make sure that it doesn't use a huge amount of heap memory: 
 *    sqlite3 FilesHub.db "select length(canonical_path), * from Shelf order by length(canonical_path) desc limit 1;" | wc -c
 *    sqlite3 FilesHub.db "select length(canonical_path), * from Trash order by length(canonical_path) desc limit 1;" | wc -c
 * 
 *    Assuming 100 MB of heap memory and average Document size = 400 bytes(largest = 493 bytes).
 *    Number of possible in heap=100*1024*1024/400 = 262,144
 * @author Xuan Ngo
 *
 */
public class Version0002
{
  private Shelf shelf = new Shelf();
  private Trash trash = new Trash();
  
  public void run()
  {
    Main.connection = new Connection();
    this.updateShelfFileSize();
    this.updateTrashFileSize();
    Main.connection.close();
  }
  
  private void updateShelfFileSize()
  {
    Report report = new Report();
    report.getRAMUsage();
        
    List<Document> shelfDocs = this.shelf.getDocs();
System.out.println(report.getRAMUsage());
    for(Document shelfDoc: shelfDocs)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        
        try
        {
          this.shelf.saveSize(shelfDoc.uid, size);
          this.trash.saveSize(shelfDoc.hash, size); // Update file size in Trash where hash is the same as Shelf.
          Main.connection.commit();
        }
        catch(Exception ex)
        {
          try
          {
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) { sqlEx.printStackTrace(); }
        }
      }
    }    
  }
  
  private void updateTrashFileSize()
  {
    List<Document> trashDocs = this.trash.getDocsWithMissingFileSize();
    for(Document trashDoc: trashDocs)
    {
      File file = new File(trashDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        this.trash.saveSize(trashDoc.hash, size);
        try
        {
          Main.connection.commit();  
        }
        catch(SQLException ex){ ex.printStackTrace(); }
        
      }
    }
  }
  
}
