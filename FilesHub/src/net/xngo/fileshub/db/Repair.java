package net.xngo.fileshub.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repair database:
 *    - Check hash is correct: 32 chars long and hexadecimal.
 * @author root
 *
 */
public class Repair
{
  final static Logger log = LoggerFactory.getLogger(Repair.class);
  
  public void commit(boolean commit)
  {
    if(commit)
    {
      System.out.println("Commit");
    }
    else
    {
      System.out.println("Simulation");
    }
  }
}
