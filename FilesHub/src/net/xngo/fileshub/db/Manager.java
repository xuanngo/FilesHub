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
   * Goal: Avoid processing hash multiple times:
   * If name found in Shelf
   *    If last modified time found
   *        Return Duplicate
   *    else
   *        If exact file path found in Shelf
   *            -Copy Shelf document to Trash.
   *            -Update Shelf document with current file.
   *            -Return New File(File content has changed.)
   *        else
   *            If last modifed time found in Trash
   *                Return Duplicate
   *            else
   *                -Add file info in Trash(New Hash)
   *                -Return Duplicate
   * else
   *    If name found in Trash
   *        If last modified time found
   *            Return Duplicate
   *        else
   *            -Add file info in Trash(New hash)
   *            -Return Duplicate
   *    else
   *        hash = getHash(file)
   *        If hash found in Shelf
   *            -Add file info in Trash(New name)
   *            -Return Duplicate
   *        else
   *            If hash found in Trash
   *                -Add file info in Trash(New name)
   *                -Return Duplicate
   *            else
   *                -Add file info in Shelf
   *                -Return New File(Completely new file)
   * @param file
   * @return  Existing and conflicting document. Otherwise, null.
   */
  public Document addFile(File file)
  {
    final String filename = file.getName();
    final long lastModified = file.lastModified();
    
    Document shelfDoc = shelf.findDocByFilename(filename);
    if(shelfDoc != null)
    {
      if(shelfDoc.last_modified == lastModified)
        return shelfDoc;
      else
      {
        Document tmpShelfDoc = shelf.findDocByCanonicalPath(Utils.getCanonicalPath(file));
        if(tmpShelfDoc != null)
        {
          trash.addDoc(tmpShelfDoc);
          
          Document newShelfDoc = new Document(file);
          newShelfDoc.uid = tmpShelfDoc.uid;
          newShelfDoc.hash = Utils.getHash(file);
          shelf.saveDoc(newShelfDoc);
          
          return null; // Dilemma null or newShelfDoc. In this case, it's the exact same file and path but content has changed.
        }
        else
        {
          Document trashDoc = trash.findDocByModifiedTimeAndFilename(lastModified, filename);
          if(trashDoc != null)
            return shelfDoc;
          else
          {
            Document newTrashDoc = new Document(file);
            newTrashDoc.uid = shelfDoc.uid;
            newTrashDoc.hash = Utils.getHash(file);
            trash.addDoc(newTrashDoc);
  
            return shelfDoc;
          }
        }
      }
    }
    else
    {
      Document trashDoc = trash.findDocByFilename(filename);
      if(trashDoc != null)
      {
        if(trash.findDocByModifiedTimeAndFilename(lastModified, filename) != null)
        {
          return shelf.findDocByUid(trashDoc.uid);
        }
        else
        {
          Document newTrashDoc = new Document(file);
          newTrashDoc.uid = trashDoc.uid;
          newTrashDoc.hash = Utils.getHash(file);
          trash.addDoc(newTrashDoc);
          return shelf.findDocByUid(trashDoc.uid);
        }
      }
      else
      {
        final String hash = Utils.getHash(file);
        shelfDoc = shelf.findDocByHash(hash);
        if(shelfDoc != null)
        {
          Document newTrashDoc = new Document(file);
          newTrashDoc.uid = shelfDoc.uid;
          newTrashDoc.hash = hash;
          trash.addDoc(newTrashDoc);
          return shelfDoc;
        }
        else
        {
          trashDoc = trash.findDocByHash(hash);
          if(trashDoc != null)
          {
            Document newTrashDoc = new Document(file);
            newTrashDoc.uid = trashDoc.uid;
            newTrashDoc.hash = hash;
            trash.addDoc(newTrashDoc);
            return shelf.findDocByUid(trashDoc.uid);
          }
          else
          {
            Document newShelfDoc = new Document(file);
            newShelfDoc.hash = hash;
            shelf.addDoc(newShelfDoc);
            
            return null;
          }
        }
        
      }
    }
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
