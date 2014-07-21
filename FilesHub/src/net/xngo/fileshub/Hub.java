package net.xngo.fileshub;

import java.io.File;

import net.xngo.fileshub.db.Database;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Database database = new Database();
  
  public Hub()
  {
    database.create();
  }
  
  /**
   * Add file if it is unique.
   * 
   * If filename is 
   * @param file
   * @return
   */
  public boolean add(File file)
  {
    
    return true;
  }
  
  private boolean isFileExists()
  {
    return true;
  }
  
  private boolean isFilenameExists(final String filename)
  {
    return true;
  }
  private boolean isSizeExists(final int size)
  {
    return true;
  }
  
}
