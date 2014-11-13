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
import net.xngo.utils.java.math.Math;

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
  
  private int totalDocs = 0;
  private int docsProcessed = 0;
  private final int whenToDisplay = 101;
  private Report report = new Report();
  
  public void run()
  {
    Main.connection = new Connection();
    
    List<Document> shelfDocs = this.shelf.getDocs();
    List<Document> trashDocs = this.trash.getDocsWithMissingFileSize();
    this.totalDocs = shelfDocs.size() + trashDocs.size();
    
    this.updateShelfFileSize(shelfDocs);
    this.updateTrashFileSize(trashDocs);
    
    this.report.console.printProgress(String.format("%s [%d/%d] %s", 
                                            "100.00%", 
                                            this.totalDocs, 
                                            this.totalDocs,
                                            report.getRAMUsage()));    
    
    Main.connection.close();
  }
  
  private void updateShelfFileSize(List<Document> shelfDocs)
  {
    for(Document shelfDoc: shelfDocs)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        
        try
        {
if(size==0)
  System.out.println(file.getAbsolutePath());

          this.shelf.saveSize(shelfDoc.uid, size);
          this.trash.saveSize(shelfDoc.hash, size); // Update file size in Trash where hash is the same as Shelf.
          Main.connection.commit();
          
          // Display progress.
          this.docsProcessed++;
          if( (this.docsProcessed%this.whenToDisplay)==0 )
          {
            this.report.console.printProgress(String.format("%s [%d/%d] %s", Math.getReadablePercentage(this.docsProcessed, this.totalDocs), 
                                                                              this.docsProcessed, 
                                                                              this.totalDocs,
                                                                              report.getRAMUsage()));
          }          
        }
        catch(Exception ex)
        {
          try
          {
            System.out.println(String.format("Rollback %s", shelfDoc.canonical_path));
            System.out.println(shelfDoc.getInfo("Entry info in Shelf:"));
            ex.printStackTrace();
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) { sqlEx.printStackTrace(); }
        }
      }
    }    
  }
  
  private void updateTrashFileSize(List<Document> trashDocs)
  {
    for(Document trashDoc: trashDocs)
    {
      File file = new File(trashDoc.canonical_path);
      if(file.exists())
      {
        long size = file.length();
        try
        {
          this.trash.saveSize(trashDoc.hash, size);
          
          Main.connection.commit();
          
          // Display progress.
          this.docsProcessed++;
          if( (this.docsProcessed%this.whenToDisplay)==0 )
          {
            this.report.console.printProgress(String.format("%s [%d/%d] %s", Math.getReadablePercentage(this.docsProcessed, this.totalDocs), 
                                                                              this.docsProcessed, 
                                                                              this.totalDocs,
                                                                              report.getRAMUsage()));
          }          
        }
        catch(SQLException ex)
        { 
          try
          {
            System.out.println(String.format("Rollback %s", trashDoc.canonical_path));
            System.out.println(trashDoc.getInfo("Entry info in Trash:"));
            ex.printStackTrace();
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) { sqlEx.printStackTrace(); }
        }
        
      }
    }
  }
  
}
