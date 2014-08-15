package net.xngo.fileshub.db;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

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
  
  /**
   * The logic of this method:
   * 
   *   If same path in Shelf
   *     If same modified time
   *       Do nothing: It is the exact same file.
   *     else // Content has changed since it was inputted
   *       Copy document info from Shelf to Trash table.
   *       Update last modified time and hash in Shelf table.
   *   else
   *     If same path in Trash table
   *       If same modified time
   *         Return Duplicate(same as deleted file)
   *       else
   *         Return Potential Duplicate: need user invervention
   *     else
   *       If hash exists in Shelf table
   *         Save document in Trash // No need to check for duplicates as it has been checked.
   *         Return Duplicate.
   *       else
   *         Save document in Shelf table
   *         Return new unique file status.
   *         
   * @param file
   * @return
   */
  public ResultDocSet addFile(File file)
  {
    ResultDocSet resultDocSet = new ResultDocSet();
    resultDocSet.file         = file;
    
    String canonicalFilePath = Utils.getCanonicalPath(file);
    Document docFromDb = this.shelf.findDocByCanonicalPath(canonicalFilePath);
    if(docFromDb!=null)
    {// File path found in Shelf table.
      
      
      if(docFromDb.last_modified == file.lastModified())
      {// Nothing to do. Exact same file.
        
        // Update status.
        resultDocSet.status = ResultDocSet.EXACT_SAME_FILE;
        resultDocSet.document = docFromDb;
        
      }
      else
      {// File has changed. 

        // Move docFromDb to Trash table.
        this.trash.addDoc(docFromDb);
        
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
        resultDocSet.document = docFromDb; // Use docFromDb instead of newDoc because it conflicts with 'file'.        

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
          resultDocSet.document = trashDoc; // Use trashDoc because it conflict with 'file'.           
        }
        else
        {
          // Add new entry in Trash table because file has new hash.
          Document newTrashDoc = new Document(file);
          newTrashDoc.uid = trashDoc.uid;
          newTrashDoc.hash = Utils.getHash(file);
          trash.addDoc(newTrashDoc);
          
          // Update status.
          resultDocSet.status = ResultDocSet.SAME_TRASH_PATH_DIFF_HASH; // Potential deleted duplicate: Same path as deleted files but different content.
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
          trash.addDoc(trashNewDoc);
      
          // Update status.
          resultDocSet.status   = ResultDocSet.DIFF_PATH_SAME_HASH; // Duplicate file.
          resultDocSet.document = doc;
  
        }
        else
        {// Hash is not found, new unique file.
          
          doc = new Document(file);
          doc.hash = hash;
          doc.uid = this.shelf.addDoc(doc); // Return generatedKeys
          
          // Update status.
          resultDocSet.status = ResultDocSet.DIFF_PATH_DIFF_HASH; // New unique file.
          resultDocSet.document = doc;
        }
      }
    }
    
    return resultDocSet;    
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
  
}
