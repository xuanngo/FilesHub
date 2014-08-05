package net.xngo.fileshub.struct;

import java.io.File;
import java.lang.RuntimeException;

import net.xngo.fileshub.Utils;

/**
 * Document is a container holding specific informations of a file.
 * @author Xuan Ngo
 *
 */
public class Document
{
  public int    uid             = 0; // For now, uid is an INTEGER. Don't use LONG.
  public String canonical_path  = "";
  public String filename        = "";
  public long   last_modified   = 0;
  public String hash            = "";
  public String comment         = "";
  
  public Document()
  {}
  
  public Document(final File file)
  {
    this.canonical_path = Utils.getCanonicalPath(file);
    this.filename = file.getName();
    this.last_modified = file.lastModified();
  }
  
  /**
   * Throw RuntimeException if Document data is not consistent and expected.
   */
  public void sanityCheck()
  {
    if(this.hash.isEmpty())
      throw new RuntimeException(this.getErrorMsg());

    if(this.canonical_path.isEmpty())
      throw new RuntimeException(this.getErrorMsg());

    if(this.filename.isEmpty())
      throw new RuntimeException(this.getErrorMsg());
    
    if(this.last_modified<1)
      throw new RuntimeException(this.getErrorMsg());    

  }
  
  public void checkUid()
  {
    String uidErrorMsg = String.format("[uid = %d] // Can't be zero.", this.uid);
    if(this.uid<1)
      throw new RuntimeException(uidErrorMsg);
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  private final String getErrorMsg()
  {
    return String.format( "\n"
                        + "uid            = %d // Can be zero if document is not created in the database. \n"
                        + "canonical_path = %s // Can't be empty. \n"
                        + "filename       = %s // Can't be empty. \n"
                        + "last_modified  = %d // Has to > 0. \n"
                        + "hash           = %s // Can't be empty. \n"
                        + "comment        = %s"
                          , this.uid, this.canonical_path, this.filename, this.last_modified, this.hash, this.comment);
  }
}
