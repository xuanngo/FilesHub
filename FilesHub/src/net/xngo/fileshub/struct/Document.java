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
    try
    {
      if(this.hash == null)
        throw new NullPointerException(this.getErrorMsg());
      
      if(this.hash.isEmpty())
        throw new RuntimeException(this.getErrorMsg());
  
      if(this.canonical_path.isEmpty())
        throw new RuntimeException(this.getErrorMsg());
  
      if(this.filename.isEmpty())
        throw new RuntimeException(this.getErrorMsg());
      
      if(this.last_modified<1)
        System.out.println(String.format("Warning: [%s] is older than January 1, 1970. Last modified = %d.", this.canonical_path, this.last_modified));
    }
    catch(Exception ex)
    {
      System.out.println(String.format("Failed on [%s].", this.canonical_path));
      ex.printStackTrace();
    }

  }
  
  public void checkUid()
  {
    String uidErrorMsg = String.format("[uid = %d] // Can't be zero.\nMore Info:\n%s", this.uid, this.getErrorMsg());
    if(this.uid<1)
      throw new RuntimeException(uidErrorMsg);
  }
  /**
   * Return information of the document with user define title.
   * @param title
   * @return
   */
  public String getInfo(String title)
  {
    return String.format( "%s:\n"
        + "\tuid            = %d\n"
        + "\tlast_modified  = %d\n"
        + "\thash           = %s\n"
        + "\tfilename       = %s\n"
        + "\tcanonical_path = %s\n"
        + "\tcomment        = %s\n"
          , title, 
          this.uid, this.last_modified, this.hash, this.filename, this.canonical_path, this.comment);    
  }
  
  /**
   * Define equality of state.
   */
   @Override public boolean equals(Object obj) 
   {
     if (this == obj) return true;
     if (!(obj instanceof Document)) return false;

     Document doc = (Document)obj;
     return
       ( this.uid           == doc.uid                 ) &&
       ( this.last_modified == doc.last_modified       ) &&
       ( this.hash          .equals(doc.hash          )) &&
       ( this.filename      .equals(doc.filename      )) &&
       ( this.canonical_path.equals(doc.canonical_path)) &&
       ( this.comment       .equals(doc.comment       ))
       ;
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
                        + "last_modified  = %d // Warning will display if less than January 1, 1970. \n"
                        + "hash           = %s // Can't be null nor empty. \n"
                        + "comment        = %s"
                          , this.uid, this.canonical_path, this.filename, this.last_modified, this.hash, this.comment);
  }
}
