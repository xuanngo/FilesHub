package net.xngo.fileshub.test.helpers;

import net.xngo.fileshub.struct.Document;

public class DocumentExt extends Document
{
  public DocumentExt(Document doc)
  {
    super.uid            = doc.uid;
    super.canonical_path = doc.canonical_path;
    super.filename       = doc.filename;
    super.last_modified  = doc.last_modified;
    super.size           = doc.size;
    super.hash           = doc.hash;
    super.comment        = doc.comment;    
  }
  
  /**
   * Are document information found in str?
   * Note: Don't be strict. Check bare minimum pieces of info to confirm that document is found. 
   * @param str
   * @return
   */
  public boolean foundIn(String str)
  {
    StringBuilder errorMsg = new StringBuilder(str.length());
    if(str.indexOf(this.uid+"")==-1)        { errorMsg.append(String.format("\t uid=%d\n", this.uid)); };
    if(str.indexOf(this.canonical_path)==-1){ errorMsg.append(String.format("\t canonical_path=%s\n", this.canonical_path)); };
    if(str.indexOf(this.filename)==-1)      { errorMsg.append(String.format("\t filename=%s\n", this.filename)); };
//    if(str.indexOf(this.last_modified+"")==-1){ errorMsg.append(String.format("\t last_modified=%d\n", this.last_modified)); };
    if(str.indexOf(this.size+"")==-1)       { errorMsg.append(String.format("\t size=%d\n", this.size)); };
    if(str.indexOf(this.hash)==-1)          { errorMsg.append(String.format("\t hash=%s\n", this.hash)); };
    if(str.indexOf(this.comment)==-1)       { errorMsg.append(String.format("\t comment=%s\n", this.comment)); };
    
    if(errorMsg.length()==0)
      return true;
    else
    {
      System.err.println("The following information are not found:"+errorMsg.length());
      errorMsg.append("in\n\n");
      errorMsg.append(str);
      System.err.println(errorMsg.toString());
      return false;
    }
  }
}
