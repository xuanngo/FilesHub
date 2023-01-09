package net.xngo.fileshub.struct;

import java.io.File;

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
  public long   size            = 0;
  public String hash            = "";
  public String comment         = "";
  
  public Document()
  {}
  
  public Document(final File file)
  {
    this.update(file);
  }
  
  public Document(Document doc)
  {
    this.uid            = doc.uid;
    this.canonical_path = doc.canonical_path;
    this.filename       = doc.filename;
    this.last_modified  = doc.last_modified;
    this.size           = doc.size;
    this.hash           = doc.hash;
    this.comment        = doc.comment;
  }
  
  /**
   * Update all file information.
   * @param file
   */
  public void update(final File file)
  {
    this.canonical_path = Utils.getCanonicalPath(file);
    this.filename       = file.getName();
    this.last_modified  = file.lastModified();
    this.size           = file.length();    
  }
  
  /**
   * Throw RuntimeException if Document data is not consistent and expected.
   */
  public void sanityCheck()
  {
    
    if(this.hash == null)
      throw new NullPointerException(this.getErrorMsg("Error: Hash can't be null."));
    
        if(this.hash.isEmpty())
          throw new RuntimeException(this.getErrorMsg("Error: Hash can't be empty."));
    
    if(this.canonical_path == null)
      throw new NullPointerException(this.getErrorMsg("Error: Canonical path can't be null."));
    
        if(this.canonical_path.isEmpty())
          throw new RuntimeException(this.getErrorMsg("Error: Canonical path can't be empty."));
    
    if(this.filename == null)
      throw new NullPointerException(this.getErrorMsg("Error: Filename can't be null."));
        
        if(this.filename.isEmpty())
          throw new RuntimeException(this.getErrorMsg("Error: Filename can't be empty."));
    
    if(this.last_modified<1)
      System.out.println(String.format("Warning: [%s] is older or equal to January 1, 1970. Last modified = %d.", this.canonical_path, this.last_modified));
    
    if(this.size<0)
      throw new RuntimeException(this.getErrorMsg(String.format("Error: Size=%d can't be negative.", this.size)));
  }
  
  public void checkUid()
  {
    if(this.uid<1)
    {
      String uidErrorMsg = String.format("Error: uid = %d can't be zero or less.", this.uid);      
      throw new RuntimeException(this.getErrorMsg(uidErrorMsg));
    }
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
        + "\tsize           = %d\n"
        + "\thash           = %s\n"
        + "\tfilename       = %s\n"
        + "\tcanonical_path = %s\n"
        + "\tcomment        = %s\n"
          , title 
          , this.uid, this.last_modified
          , this.size, this.nullOrEmpty(this.hash)
          , this.nullOrEmpty(this.filename), this.nullOrEmpty(this.canonical_path)
          , this.nullOrEmpty(this.comment));    
  }
  
  /**
   * Define equality of document.
   */
  @Override 
  public boolean equals(Object obj) 
  {
    if (this == obj) return true;
    if (!(obj instanceof Document)) return false;

    Document doc = (Document)obj;
    return
        ( this.uid           == doc.uid                 ) &&
        ( this.last_modified == doc.last_modified       ) &&
        ( this.size          == doc.size                ) &&
        ( this.hash          .equals(doc.hash          )) &&
        ( this.filename      .equals(doc.filename      )) &&
        ( this.canonical_path.equals(doc.canonical_path)) &&
        ( this.comment       .equals(doc.comment       ))
        ;
  }
  
  /**
   * 
   * @param prefix
   * @return Formatted string of the document information.
   */
  public String toString(String prefix)
  {
    return String.format( "%s%s:\n"
        + "%1$s  uid            = %d\n"
        + "%1$s  last_modified  = %d\n"
        + "%1$s  size           = %d\n"
        + "%1$s  hash           = %s\n"
        + "%1$s  comment        = %s"
          , prefix
          , this.nullOrEmpty(this.canonical_path) 
          , this.uid, this.last_modified
          , this.size, this.nullOrEmpty(this.hash)
          , this.nullOrEmpty(this.comment));       
  }
  
  /**
   * 
   * @return Information of document in 1 string line.
   */
  public String toStringLine()
  {
    return String.format( "%s ["
        + "uid = %d | "
        + "last_modified = %d | "
        + "size = %d | "
        + "hash = %s | "
        + "comment = %s]"
          , this.nullOrEmpty(this.canonical_path) 
          , this.uid, this.last_modified
          , this.size, this.nullOrEmpty(this.hash)
          , this.nullOrEmpty(this.comment));       
  }  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  private final String getErrorMsg(String errorMsg)
  {
    return String.format( "%s\n"
                        + "\tuid            = %d // Can be zero if document is not created in the database. \n"
                        + "\tcanonical_path = %s // Can't be null nor empty. \n"
                        + "\tfilename       = %s // Can't be null nor empty. \n"
                        + "\tlast_modified  = %d // Warning will display if less than January 1, 1970. \n"
                        + "\tsize           = %d // Can't be negative. \n"
                        + "\thash           = %s // Can't be null nor empty. \n"
                        + "\tcomment        = %s"
                          , errorMsg
                          , this.uid, this.nullOrEmpty(this.canonical_path)
                          , this.nullOrEmpty(this.filename), this.last_modified
                          , this.size, this.nullOrEmpty(this.hash)
                          , this.nullOrEmpty(this.comment));
  }
  
  private String nullOrEmpty(String str)
  {
    String s = str;
    if(str==null)
      s = "<null>";
    else
    {
      if(str.isEmpty())
        s = "<empty>";
    }
  
    return s;
  }
}
