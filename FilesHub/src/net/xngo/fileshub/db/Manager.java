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
   * Add a file in database. A file is identified by its name or hash.
   * <pre>
   * {@code
   * -Check path
   * -Check name
   * -Check hash
   * If path found in Shelf
   *   -Exact file path. Do nothing
   * else
   *   If path found in Trash
   *     -Return Duplicate
   *   else
   *     If name found in Shelf
   *       -Add to Trash if different hash
   *       -Return Duplicate
   *     else
   *       If name found in Trash
   *         -Add to Trash if different hash
   *         -Return Duplicate
   *       else
   *         If hash found in Shelf
   *           -Add to Trash if different hash
   *           -Return Duplicate
   *         else
   *           If hash found in Trash
   *             -Return Duplicate
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
    {
      Document trashDoc = trash.findDocByCanonicalPath(doc.canonical_path);
      if(trashDoc != null)
      {
        return shelf.findDocByUid(trashDoc.uid);
      }
      else
      {
        shelfDoc = shelf.findDocByFilename(doc.filename);
        if(shelfDoc != null)
        {
          doc.uid = shelfDoc.uid;
          doc.hash = Utils.getHash(file);
          trash.addDocIfDiffHash(doc);
          
          return shelfDoc;
        }
        else
        {
          trashDoc = trash.findDocByFilename(doc.filename);
          if(trashDoc != null)
          {
            doc.uid = trashDoc.uid;
            doc.hash = Utils.getHash(file);
            trash.addDocIfDiffHash(doc);
            
            return shelf.findDocByUid(trashDoc.uid);            
          }
          else
          {
            doc.hash = Utils.getHash(file);
            shelfDoc = shelf.findDocByHash(doc.hash);
            if(shelfDoc != null)
            {
              doc.uid = shelfDoc.uid;
              trash.addDocIfDiffHash(doc);
              return shelfDoc;
            }
            else
            {
              trashDoc = trash.findDocByHash(doc.hash);
              if(trashDoc != null)
                return shelf.findDocByUid(trashDoc.uid);
              else
              {
                shelf.addDoc(doc);
                return null; // New file.
              }
            }
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
  
}
