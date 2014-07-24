package net.xngo.fileshub.db;

import java.io.File;

/**
 * Data structure to hold information of 2 files.
 * @author Xuan Ngo
 *
 */
public class PairFile
{
  // Other value of toAddFile's uid
  public static int EXACT_SAME_FILE = -1;
  public static int DUPLICATE_HASH  = 0;
  
  public int  uid;              // Generated Document UID.
  public File toAddFile = null;
  public File dbFile    = null; // File registered in database.
  
}
