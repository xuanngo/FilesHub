package net.xngo.fileshub.db;


import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.struct.Document;

/**
 * Repair database:
 *    - Delete duplicate hash in Shelf table.
 *    - Delete orphans document in Trash table. Orphans = documents that don't have a matching DUID from Shelf table.
 *    - Check hash is correct: 32 chars long and hexadecimal.
 * @author Xuan Ngo
 *
 */
public class Repair
{
  final static Logger log = LoggerFactory.getLogger(Repair.class);
  
  private Trash trash = new Trash();
  private Shelf shelf = new Shelf();
  
  public void commit(boolean commit)
  {
    if(commit)
    {
      String orphanMsg = String.format("%d orphan documents deleted from Trash table.", this.trash.removeOrphans());
      String duplicateHashMsg = String.format("%d duplicate hashes deleted.", this.removeDuplicateHashes());
      
      System.out.println("Commit");
      System.out.println(orphanMsg);
      System.out.println(duplicateHashMsg);
    }
    else
    {
      String orphanMsg        = String.format("%d orphan documents to be deleted from Trash table.", this.trash.getTotalOrphans());
      String duplicateHashMsg = String.format("%d duplicate hashes to be deleted from Shelf table.", this.shelf.getTotalDuplicateHash());
      
      System.out.println("Simulation");
      System.out.println(orphanMsg);
      System.out.println(duplicateHashMsg);
    }
  }
  
  public int removeDuplicateHashes()
  {
    int duplicateHashRemoved = 0;
    List<String> duplicateHashList = this.shelf.getDuplicateHashes();
    for(String duplicateHash: duplicateHashList)
    {
      List<Document> duplicateDocList = this.shelf.getDocsByHash(duplicateHash);
      for(int i=1; i<duplicateDocList.size(); i++)
      {
        // Remove duplicate hash.
        this.shelf.removeAllDoc(duplicateDocList.get(i).uid);
        duplicateHashRemoved++;
        
        // Log removed documents.
        String duplicateRemovedMsg = String.format("Removed from Shelf table %s", duplicateDocList.get(i).toStringLine());
        log.warn(duplicateRemovedMsg);
      }
    }
    
    if(duplicateHashRemoved>0)
    {
      try
      {
        Main.connection.commit();
      }
      catch(SQLException ex)
      {
        log.error("Can't commit.", ex);
        ex.printStackTrace();
      }
    }
    
    
    return duplicateHashRemoved;
  }
  
  
}
