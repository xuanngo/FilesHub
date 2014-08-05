package net.xngo.fileshub.db;

import java.io.File;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.ResultDocSet;
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
  
  public ResultDocSet addFile(File file)
  {
    ResultDocSet resultDocSet = new ResultDocSet();
    resultDocSet.file = file;
    
    Document docFromDb = this.shelf.findDocByCanonicalPath(Utils.getCanonicalPath(file));
    if(docFromDb!=null)
    {// File path found in Shelf table.
      
      
      if(docFromDb.last_modified != file.lastModified())
      {// File has changed. 
        // Move docFromDb to Trash table.
        Trash trash = new Trash();
        trash.addFile(docFromDb);
        
        // Update changed file to Shelf table.
        String hash = Utils.getHash(file);
        Document newDoc = new Document(file);
        newDoc.uid = docFromDb.uid;
        newDoc.hash = hash;
        this.shelf.saveDoc(newDoc);
       
        // Note: It is possible that the file is overwritten with an older version.
        //        Therefore, files in Shelf table can be older than Trash table.
        
        // Update status.
        resultDocSet.status = ResultDocSet.SAME_PATH_DIFF_HASH;
        resultDocSet.file     = file;
        resultDocSet.document = docFromDb; // Use docFromDb instead of newDoc because it conflict with 'file'.
        
      }
      else
      { // Nothing to do. Exact same file.
        
        // Update status.
        resultDocSet.status = ResultDocSet.EXACT_SAME_FILE;
        resultDocSet.file     = file;
        resultDocSet.document = docFromDb;

      }

    }
    else
    { // File path not found in Shelf table.
      
      // Check hash.
      String hash = Utils.getHash(file);
      Document doc = this.shelf.findDocByHash(hash);
      
      if(doc==null)
      {// Hash is not found.
        doc = new Document(file);
        doc.hash = hash;
        doc.uid = this.shelf.addDoc(doc); // Return generatedKeys
        
        // Update status.
        resultDocSet.status = ResultDocSet.DIFF_PATH_DIFF_HASH; // New unique file.
        resultDocSet.file     = file;
        resultDocSet.document = doc;

      }
      else
      { // Found hash but different path. Therefore, add it to Trash table to keep it as history.
        
        // Add duplicate file in Trash table if it doesn't exist.
        Document trashDoc = new Document(file);
        trashDoc.uid  = doc.uid;
        trashDoc.hash = doc.hash;
        Trash trash = new Trash();
        trash.addFile(trashDoc);
    
        // Update status.
        resultDocSet.status   = ResultDocSet.DIFF_PATH_SAME_HASH; // Duplicate file.
        resultDocSet.file     = file;
        resultDocSet.document = doc;

      }
    }
    
    return resultDocSet;    
  }
  

}
