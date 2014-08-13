package net.xngo.fileshub.struct;

import java.io.File;

/**
 * ResultDocSet is a container holding information for reporting purposes.
 * @author Xuan Ngo
 *
 */
public class ResultDocSet
{
  // Possible statuses.
  private static int i=0;
  public static final int EXACT_SAME_FILE       = i++;  // Exact same file.
  public static final int SAME_PATH_DIFF_HASH   = i++;  // File has changed.
  public static final int DIFF_PATH_DIFF_HASH   = i++;  // New unique file.
  public static final int DIFF_PATH_SAME_HASH   = i++;  // Duplicate file.
  public static final int EXACT_SAME_TRASH_FILE = i++;  // Exact same file that you deleted.
  public static final int SAME_TRASH_PATH_DIFF_HASH = i++;  // Exact same file.
  
  public int      status   = -1;
  public File     file     = null; // Original file to add.
  public Document document = null; // The 'document' that conflicts with 'file'. The document that influences the status.

  public String getStatusMsg()
  {
    if(this.status==EXACT_SAME_FILE)
      return "Exact same file";
    else if (this.status==SAME_PATH_DIFF_HASH)
      return "File has changed";
    else if (this.status==DIFF_PATH_DIFF_HASH)
      return "New unique file";
    else if (this.status==DIFF_PATH_SAME_HASH)
      return "Duplicate file";
    else if (this.status==EXACT_SAME_TRASH_FILE)
      return "Exact same duplicated file";
    else if (this.status==SAME_TRASH_PATH_DIFF_HASH)
      return "Exact same duplicated file has changed";
    else
      return "Unknown status";
  }
}
