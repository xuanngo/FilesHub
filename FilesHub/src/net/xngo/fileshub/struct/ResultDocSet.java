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
  public final static int EXACT_SAME_FILE       = i++;  // Exact same file.
  public final static int SAME_PATH_DIFF_HASH   = i++;  // File has changed.
  public final static int DIFF_PATH_DIFF_HASH   = i++;  // New unique file.
  public final static int DIFF_PATH_SAME_HASH   = i++;  // Duplicate file.
  public final static int EXACT_SAME_TRASH_FILE = i++;  // Exact same file that you deleted.
  public final static int SAME_TRASH_PATH_DIFF_HASH = i++;  // Exact same file.
  
  public int      status   = -1;
  public File     file     = null; // Original file to add.
  public Document document = null; // The 'document' that conflicts with 'file'. The document that influences the status.

}
