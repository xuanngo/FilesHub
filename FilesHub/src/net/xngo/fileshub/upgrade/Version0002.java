package net.xngo.fileshub.upgrade;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.math.Math;


/**
 * Assuming size column is created in Shelf and Trash table.
 * Make sure that it doesn't use a huge amount of heap memory: 
 *    sqlite3 FilesHub.db "select length(canonical_path), * from Shelf order by length(canonical_path) desc limit 1;" | wc -c
 *    sqlite3 FilesHub.db "select length(canonical_path), * from Trash order by length(canonical_path) desc limit 1;" | wc -c
 * 
 *    Assuming 100 MB of heap memory and average Document size = 400 bytes(largest = 493 bytes).
 *    Number of possible in heap=100*1024*1024/400 = 262,144
 *    
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
    System.out.println();
    this.updateTrashFileSize();
    
    Main.connection.close();
  }
  
  private void updateShelfFileSize()
  {
    List<Document> shelfDocs = this.shelf.getDocsWithMissingFileSize();
    final int total = shelfDocs.size();
    final int updateFrequency = Utils.getUpdateFrequency(total);
    int i = 0;
    for(Document shelfDoc: shelfDocs)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        
        try
        {
          // Fix bug: For unknown reason, some Documents don't have hash.
          //    Here is to ensure that Document will get a hash if it does physically exist in the filesystem.
          if(shelfDoc.hash==null)
          {
            shelfDoc.hash = Utils.getHash(file);
            shelfDoc.update(file);
            this.shelf.saveDoc(shelfDoc);
          }
          else
          {
            if(shelfDoc.hash.isEmpty())
            {
              shelfDoc.hash = Utils.getHash(file);
              shelfDoc.update(file);
              this.shelf.saveDoc(shelfDoc);
            }
            else
            {
              this.shelf.saveSize(shelfDoc.uid, size);
            }
          }
          
          this.trash.saveSize(shelfDoc.hash, size);  // Update file size in Trash where hash is the same as Shelf. 

          
          // Display progress.
          i++;
          if( (i%updateFrequency)==0 )
          {
            Main.connection.commit();            
            Main.console.printProgress(String.format("Migrating Shelf table: %s [%d/%d] %s", Math.getReadablePercentage(i, total), 
                                                                              i,
                                                                              total,
                                                                              Utils.getRAMUsage()));
          }          
        }
        catch(Exception ex)
        {
          System.out.println(String.format("Rollback up to the last %d potential commits. Issue is in %s.", updateFrequency, shelfDoc.canonical_path));
          System.out.println(shelfDoc.getInfo("Entry info in Shelf:"));
          ex.printStackTrace();          
          try
          {
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) { sqlEx.printStackTrace(); }
        }
      }
    }
    try{ Main.connection.commit(); } catch(SQLException ex) { ex.printStackTrace(); }
    Main.console.printProgress(String.format("Migrating Shelf table: %s [%d/%d] %s", 
                                                                            "100.00%", 
                                                                            total, 
                                                                            total,
                                                                            Utils.getRAMUsage()));      
  }
  
  private void updateTrashFileSize()
  {
    List<Document> trashDocs = this.trash.getDocsWithMissingFileSize();
    final int total = trashDocs.size();
    final int updateFrequency = Utils.getUpdateFrequency(total);    
    int i = 0;    
    for(Document trashDoc: trashDocs)
    {
      File file = new File(trashDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        try
        {
      
          this.trash.saveSize(trashDoc.hash, size);
          
          // Display progress.
          i++;
          if( (i%updateFrequency)==0 )
          {
            Main.connection.commit();
            Main.console.printProgress(String.format("Migrating Trash table: %s [%d/%d] %s", Math.getReadablePercentage(i, total), 
                                                                              i, 
                                                                              total,
                                                                              Utils.getRAMUsage()));
          }
        }
        catch(SQLException ex)
        { 
          System.out.println(String.format("Rollback %s", trashDoc.canonical_path));
          System.out.println(trashDoc.getInfo("Entry info in Trash:"));
          ex.printStackTrace();          
          try
          {
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) { sqlEx.printStackTrace(); }
        }
        
      }
    }
    try{ Main.connection.commit(); } catch(SQLException ex) { ex.printStackTrace(); }
    Main.console.printProgress(String.format("Migrating Trash table: %s [%d/%d] %s", 
                                                                              "100.00%", 
                                                                              total, 
                                                                              total,
                                                                              Utils.getRAMUsage()));      
  }
  
}
