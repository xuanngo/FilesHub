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
    
    String canonicalFilePath = Utils.getCanonicalPath(file);
    Document docFromDb = this.shelf.findDocByCanonicalPath(canonicalFilePath);
    if(docFromDb!=null)
    {// File path found in Shelf table.
      
      
      if(docFromDb.last_modified == file.lastModified())
      {// Nothing to do. Exact same file.
        
        // Update status.
        resultDocSet.status = ResultDocSet.EXACT_SAME_FILE;
        resultDocSet.file     = file;
        resultDocSet.document = docFromDb;
        
      }
      else
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

    }
    else
    { // File path not found in Shelf table.
      
      Trash trash = new Trash();
      Document trashDoc = trash.findDocByCanonicalPath(canonicalFilePath);
      if(trashDoc!=null)
      {// File path found in Trash table.
        if(trashDoc.last_modified == file.lastModified())
        {
          // Update status.
          resultDocSet.status = ResultDocSet.EXACT_SAME_TRASH_FILE; // Specifically, same path of deleted file.
          resultDocSet.file     = file;
          resultDocSet.document = trashDoc; // Use trashDoc because it conflict with 'file'.           
        }
        else
        {
          // Update status.
          resultDocSet.status = ResultDocSet.SAME_TRASH_PATH_DIFF_HASH; // Potential deleted duplicate: Same path as deleted files but different content.
          resultDocSet.file     = file;
          resultDocSet.document = trashDoc; // Use trashDoc because it conflict with 'file'.             
        }
          
      }
      else
      {// File path NOT found in Trash table.
        // Check hash.
        String hash = Utils.getHash(file);
        Document doc = this.shelf.findDocByHash(hash);
        
        if(doc!=null)
        {// Found hash in Shelf. Therefore, add it to Trash table to keep it as history.
          
          // Add duplicate file in Trash table if it doesn't exist.
          Document trashNewDoc = new Document(file);
          trashNewDoc.uid  = doc.uid;
          trashNewDoc.hash = doc.hash;
          trash.addFile(trashNewDoc);
      
          // Update status.
          resultDocSet.status   = ResultDocSet.DIFF_PATH_SAME_HASH; // Duplicate file.
          resultDocSet.file     = file;
          resultDocSet.document = doc;
  
        }
        else
        {// Hash is not found, new unique file.
          
          doc = new Document(file);
          doc.hash = hash;
          doc.uid = this.shelf.addDoc(doc); // Return generatedKeys
          
          // Update status.
          resultDocSet.status = ResultDocSet.DIFF_PATH_DIFF_HASH; // New unique file.
          resultDocSet.file     = file;
          resultDocSet.document = doc;
        }
      }
    }
    
    return resultDocSet;    
  }
  

}
