package net.xngo.fileshub.db;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.db.Shelf;

/**
 * Manage documents.
 * @author Xuan Ngo
 *
 */
public class Manager
{
  private Shelf shelf = new Shelf();
  private Trash trash = new Trash();
  
  public void createDbStructure()
  {
    // Create database structure if sqlite database file doesn't exist.
    File DbFile = new File(Conn.DB_FILE_PATH);
    if(!DbFile.exists())
    {// Database file doesn't exist.
      this.shelf.createTable();
      this.trash.createTable();
    }
    else if(DbFile.length()<1)
    {// Database file already exist but it is empty.
      
      this.shelf.createTable();
      this.trash.createTable();      
    }
    else
    {
      // Do nothing. Database file already exists.
    }
  
  }
  

  /**
   * Add a file in database. A file is identified by its hash.
   * For optimization, check the path first and then the hash.
   * We can't use filename to identify a file because filename can
   *  be generic, e.g. ../something/Track 1.
   * <pre>
   * {@code
   * If path found in Shelf
   *   -Exact file path. Do nothing
   * else
   *   If path found in Trash
   *     -Return Duplicate
   *   else
   *         If hash found in Shelf
   *           -Add to Trash (New Path)
   *           -Return Duplicate
   *         else
   *           If hash found in Trash
   *              If file was moved
   *                  -Update Shelf.
   *                  -Move info from Shelf to Trash.
   *                  -Return Nothing.
   *              else
   *                  -Add to Trash (New Path)
   *                  -Return Duplicate
   *           else
   *             -Add to Shelf
   *             -Return New File
   * }
   * </pre>               
   * @param file
   * @return  Existing and conflicting document. Otherwise, null.
   */
  public Document addFile(File file)
  {
    Document doc = new Document(file);
    
    Document shelfDoc = shelf.findDocByCanonicalPath(doc.canonical_path);
    if(shelfDoc == null)
    {// Path not found in Shelf.
      
      Document trashDoc = trash.findDocByCanonicalPath(doc.canonical_path);
      if(trashDoc != null)
      {// Path found in Trash.
        return shelf.findDocByUid(trashDoc.uid);
      }
      else
      {
          doc.hash = Utils.getHash(file);
          shelfDoc = shelf.findDocByHash(doc.hash);
          if(shelfDoc != null)
          {// Hash found in Shelf.
            
            if(new File(shelfDoc.canonical_path).exists())
            {// File still exists
              doc.uid = shelfDoc.uid;
              trash.addDoc(doc);
              return shelfDoc;
            }
            else
            {// File doesn't exist anymore.
              
              // Move non-existing file to Trash.
              trash.addDoc(shelfDoc);
              
              // Update current document in Shelf.
              doc.uid = shelfDoc.uid;
              shelf.saveDoc(doc);
              
              return null;
            }
          }
          else
          {
            trashDoc = trash.findDocByHash(doc.hash);
            if(trashDoc != null)
            {// Hash found in Trash.
              doc.uid = trashDoc.uid;
              trash.addDoc(doc);              
              return shelf.findDocByUid(trashDoc.uid);
            }
            else
            {
              shelf.addDoc(doc);
              return null; // New file.
            }
          }
      }
    }
    // Exact same file in Shelf. Therefore, do nothing.
    return null;
  }
  
  /**
   * If files have changed, then update them.
   * @return List of missing files.
   */
  public List<Document> update()
  {
    List<Document> docList = this.shelf.getAllDoc();
    
    List<Document> missingFileList = new ArrayList<Document>();
    for(Document doc: docList)
    {
      File file = new File(doc.canonical_path);
      if(file.exists())
      {
        if(file.lastModified()!=doc.last_modified)
        {// File has changed.
          // Copy old file info in Trash table.
          this.trash.addDoc(doc);
          
          // Update changed file to Shelf table.
          String hash = Utils.getHash(file);
          Document newDoc = new Document(file);
          newDoc.uid = doc.uid;
          newDoc.hash = hash;
          this.shelf.saveDoc(newDoc);
        }
      }
      else
        missingFileList.add(doc);
    }
    return missingFileList;
  }
  
  /**
   * 
   * @param duplicate
   * @param of
   * @return False if nothing is committed in the database. Otherwise, true.
   */
  public boolean markDuplicate(File duplicate, File of)
  {
    if(!this.validateMarkDuplicate(duplicate, of))
      return false;
    else
    {
      String duplicateCanonicalPath = Utils.getCanonicalPath(duplicate);
      String ofCanonicalPath        = Utils.getCanonicalPath(of);
      
      if(duplicateCanonicalPath.compareTo(ofCanonicalPath)==0)
      {
        System.out.println("Error: Both files are exactly the same.");
        return false;
      }
      else
      {
        Document shelfDoc = this.shelf.findDocByCanonicalPath(ofCanonicalPath);
        if(shelfDoc!=null)
        {// Found 'of' in Shelf table.
          String duplicateHash = Utils.getHash(duplicate);
          Document trashDoc = this.trash.findDocByHash(duplicateHash);
          if(trashDoc==null)
          {// Duplicate is not in Trash table.
            Document newTrashDoc = new Document(duplicate);
            newTrashDoc.uid = shelfDoc.uid;
            newTrashDoc.hash = duplicateHash;
            trash.addDoc(newTrashDoc);
            return true;
          }
          return false;
        }
        else
        {
          System.out.println(String.format("Error: [%s] doesn't exist in Shelf table.", ofCanonicalPath));
          return false;
        }
      }
      
    }
    
    
    
  }
  
  private boolean validateMarkDuplicate(File duplicate, File of)
  {
    if(!duplicate.exists())
    {
      System.out.println(String.format("Error: [%s] doesn't exist.", duplicate.getAbsolutePath()));
      return false;
    }
    
    if(!of.exists())
    {
      System.out.println(String.format("Error: [%s] doesn't exist.", of.getAbsolutePath()));
      return false;
    }
    
    
    if(!duplicate.isFile())
    {
      System.out.println(String.format("Error: [%s] is not a file.", duplicate.getAbsolutePath()));
      return false;
    }
    
    if(!of.isFile())
    {
      System.out.println(String.format("Error: [%s] is not a file.", of.getAbsolutePath()));
      return false;
    }
    
    return true;
    
  }
  
}
