package net.xngo.fileshub.db;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.report.Report;
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
   *      If original file still exists
   *        -Return Duplicate
   *      else
   *        -Move original file info from Shelf to Trash.
   *        -Add file in Shelf. 
   *        -Return New File
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
        
        // Switch if original file doesn't exist.
        Document originalDoc = shelf.findDocByUid(trashDoc.uid);
        if(new File(originalDoc.canonical_path).exists())
          return shelf.findDocByUid(trashDoc.uid);
        else
        {
          // Switch Shelf<->Trash.
          shelf.saveDoc(trashDoc);
          trash.removeDoc(trashDoc);
          trash.addDoc(originalDoc);
          return null;
        }
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
    
    Report report = new Report();
    report.displayTotalFiles(docList.size());
    
    // Variables for print progress.
    int whenToDisplay = 100;
    int i=1;
    int totalFiles = docList.size();
    
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
      
      // Print progress
      i++;
      if( (i%whenToDisplay)==0)
      {
        report.progressPrint(String.format("[%d/%d]", i, totalFiles));
      }      
    }
    report.progressPrint(String.format("[%d/%d]", totalFiles, totalFiles)); // Print last result.

    return missingFileList;
  }
  
  /**
   * Mark 2 files as duplicate regardless of their content.
   * <pre>
   * {@code
   * If file B found in Shelf
   *    If file A found in Shelf
   *        -Move file A from Shelf to Trash
   *        -Link all duplicates of A to B.
   *        -Return OK
   *    else
   *        -Add file A in Trash & it should be linked to file B
   *        -Return OK
   * else
   *    -Add file B in Shelf
   *    If file A found in Shelf
   *        -Move file A from Shelf to Trash
   *        -Link all duplicates of A to B.
   *        -Return OK
   *    else
   *        -Add file A to Trash & it should be linked to file B
   *        -Return OK
   * }
   * 
   * </pre>
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
        Document shelfDocOf = this.shelf.findDocByCanonicalPath(ofCanonicalPath);
        if(shelfDocOf!=null)
        {// File B found in Shelf
          Document shelfDocDuplicate = this.shelf.findDocByCanonicalPath(duplicateCanonicalPath);
          if(shelfDocDuplicate!=null)
          {// File A found in Shelf
            
            // Execution order is important.
            //    Move File A from Shelf to Trash.            
            this.shelf.removeDoc(shelfDocDuplicate.uid);
            this.trash.markDuplicate(shelfDocDuplicate.uid, shelfDocOf.uid);
            shelfDocDuplicate.uid = shelfDocOf.uid;
            this.trash.addDoc(shelfDocDuplicate);
            
            return true;
          }
          else
          {
            Document newTrashDoc = new Document(duplicate);
            newTrashDoc.uid = shelfDocOf.uid; // linked to file B
            newTrashDoc.hash = Utils.getHash(duplicate);
            this.trash.addDoc(newTrashDoc);
            return true;
          }

        }
        else
        {
          Document newShelfDoc = this.addFile(of);
          
          // Get uid.
          int uid = 0;
          if(newShelfDoc==null)
            uid = this.shelf.findDocByCanonicalPath(ofCanonicalPath).uid;
          
          Document shelfDocDuplicate = this.shelf.findDocByCanonicalPath(duplicateCanonicalPath);
          if(shelfDocDuplicate!=null)
          {// File A found in Shelf
            
            // Execution order is important.
            //    Move File A from Shelf to Trash.
            this.shelf.removeDoc(shelfDocDuplicate.uid);
            this.trash.markDuplicate(shelfDocDuplicate.uid, uid);
            shelfDocDuplicate.uid = uid;  // linked to file B
            this.trash.addDoc(shelfDocDuplicate);
            return true;
          }
          else
          {
            Document newTrashDoc = new Document(duplicate);
            newTrashDoc.uid = uid;
            newTrashDoc.hash = Utils.getHash(duplicate);
            this.trash.addDoc(newTrashDoc);
            return true;            
          }
        }

        
        
      }
      
    }
    
    
    
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/  
  
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
