package net.xngo.fileshub.struct;

import java.lang.Comparable;

import net.xngo.fileshub.struct.Document;

/**
 * Used by {@link Report} class.
 * @author Xuan Ngo
 *
 */
public class Duplicate implements Comparable<Duplicate>
{
  public Document toAddDoc = new Document();
  public Document shelfDoc = new Document();
  
  public Duplicate(Document toAddDoc, Document shelfDoc)
  {
    this.toAddDoc = toAddDoc;
    this.shelfDoc = shelfDoc;
  }
  
  // Natural order: Sort by toAddDoc.canonical_path.
  public int compareTo(Duplicate otherDup)
  {
    return this.toAddDoc.canonical_path.compareTo(otherDup.toAddDoc.canonical_path);
        
  }
}
