package net.xngo.fileshub.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repair database:
 *    - Delete orphans document in Trash table.
 *    - Check hash is correct: 32 chars long and hexadecimal.
 * @author root
 *
 */
public class Repair
{
  final static Logger log = LoggerFactory.getLogger(Repair.class);
  
  private Trash trash = new Trash();
  
  public void commit(boolean commit)
  {
    if(commit)
    {
      String orphanMsg = String.format("%d orphan documents deleted.", this.trash.removeOrphans());
      
      System.out.println("Commit");
      System.out.println(orphanMsg);
    }
    else
    {
      String orphanMsg = String.format("%d orphan documents to be deleted.", this.trash.getTotalOrphans());
      
      System.out.println("Simulation");
      System.out.println(orphanMsg);
    }
  }
  
  
}
