package net.xngo.fileshub.upgrade;

import java.sql.SQLException;
import java.util.List;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.math.Math;


/**
 * Rehash all files. Change from xxhash32 to md5
 *  because xxhash32 have a very high probability of collision(1 in 4 billions)
 *  whereas md5 have 1 in 2^128.
 *    
 * @author Xuan Ngo
 *
 */
public class Version0003
{
  final static Logger log = LoggerFactory.getLogger(Version0003.class);
  
  private Shelf shelf = new Shelf();
  
  public void run()
  {
    Main.connection = new Connection();
    
    this.rehashShelfFiles();
    // Note: No need to rehash files from Trash table.
    //  Waste of space. Let's be forward compatible only.
    //  By definition, files in Trash table are duplicates.
    
    Main.connection.close();
  }
  
  private void rehashShelfFiles()
  {
    List<Document> shelfDocs = this.shelf.getDocs();
    final int total = shelfDocs.size();
    final int updateFrequency = Utils.getUpdateFrequency(total);
    int i = 0;
    for(Document shelfDoc: shelfDocs)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        try
        {
          // Note: Don't keep the old entry by moving it to the Trash table.
          //    Waste of space. Let's be forward compatible only.
          
          shelfDoc.update(file);
          shelfDoc.hash = Utils.getHash(file);
          this.shelf.saveDoc(shelfDoc);

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
          String shelfEntryInfo = shelfDoc.getInfo("Entry info in Shelf:");
          
          // Log
          log.error("Rollback up to the last {} potential commits. Issue is in {}.\n{}", updateFrequency, shelfDoc.canonical_path, shelfEntryInfo, ex);
          
          // Output to user.
          System.out.println(String.format("Rollback up to the last %d potential commits. Issue is in %s.", updateFrequency, shelfDoc.canonical_path));
          System.out.println(shelfEntryInfo);
          ex.printStackTrace();
          try
          {
            Main.connection.rollback();
          }
          catch(SQLException sqlEx) 
          { 
            log.error("Can't rollback up to the last {} potential commits.", updateFrequency, sqlEx);
            sqlEx.printStackTrace(); 
          }
        }
      }
      else
        log.warn("Skip MD5 hashing. File doesn't exist: {}.", shelfDoc.canonical_path);
    }
    try{ Main.connection.commit(); } catch(SQLException ex) { log.error("Can't commit", ex); ex.printStackTrace(); }
    Main.console.printProgress(String.format("Migrating Shelf table: %s [%d/%d] %s", 
                                                                            "100.00%", 
                                                                            total, 
                                                                            total,
                                                                            Utils.getRAMUsage()));      
  }
  
}
