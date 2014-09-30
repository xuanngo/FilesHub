package net.xngo.fileshub.db;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.report.Report;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.db.Shelf;
import net.xngo.utils.java.io.FileUtils;

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
   * We can't use filename to identify a file because filenames can
   *  be generic and have different content, e.g. ../something/Track 1.
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
   * @return  Existing and conflicting document. Otherwise, null = new unique file. 
   *              NULL is used to find duplicates.
   */
  public Document addFile(File file)
  {
    Document doc = new Document(file);
    
    Document shelfDoc = this.shelf.findDocByCanonicalPath(doc.canonical_path);
    if(shelfDoc == null)
    {// Path not found in Shelf.
      
      Document trashDoc = this.trash.findDocByCanonicalPath(doc.canonical_path);
      if(trashDoc != null)
      {// Path found in Trash.
        
        // Check if original file doesn't exist.
        Document originalDoc = this.shelf.findDocByUid(trashDoc.uid);
        if(new File(originalDoc.canonical_path).exists())
          return this.shelf.findDocByUid(trashDoc.uid);
        else
        {// Shelf file doesn't exist.
          // Move original file info from Shelf to Trash.
          this.shelf.saveDoc(trashDoc);      // trashDoc is used instead of 'doc' because they both
                                        //    have exact same path. It will save hash time.
          this.trash.removeDoc(trashDoc);
          this.trash.addDoc(originalDoc);
          return null;
        }
      }
      else
      {
          doc.hash = Utils.getHash(file);
          shelfDoc = this.shelf.findDocByHash(doc.hash);
          if(shelfDoc != null)
          {// Hash found in Shelf.
            
            if(new File(shelfDoc.canonical_path).exists())
            {// Shelf file still exists
              doc.uid = shelfDoc.uid;
              this.trash.addDoc(doc);
              return shelfDoc;
            }
            else
            {// Shelf file doesn't exist anymore.
              
              // Move non-existing file to Trash.
              this.trash.addDoc(shelfDoc);
              
              // Update current document in Shelf.
              doc.uid = shelfDoc.uid;
              this.shelf.saveDoc(doc);
              
              return null;
            }
          }
          else
          {// Hash not found in Shelf.
            trashDoc = trash.findDocByHash(doc.hash);
            if(trashDoc != null)
            {// Hash found in Trash.
              doc.uid = trashDoc.uid;
              trash.addDoc(doc);              
              return this.shelf.findDocByUid(trashDoc.uid);
            }
            else
            {
              this.shelf.addDoc(doc);
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
    Report.FILES_TO_PROCESS = docList.size();
    report.displayTotalFilesToProcess();    
    
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
        report.console.printProgress(String.format("[%d/%d]", i, totalFiles));
      }      
    }
    report.console.printProgress(String.format("[%d/%d]", totalFiles, totalFiles)); // Print last result.

    return missingFileList;
  }
  

  /**
   * Mark fileA is a duplicate of fileB
   * <pre>
   * {@code
   * 
   * If fileB path found in Shelf
   *    -Link fileA's to fileB
   * else
   *    If fileB path found in Trash
   *        -Handle circular duplication case: A was duplicate of B. Now B is duplicate of A.
   *        -Link fileA's to fileB
   *    else
   *        If fileB hash found in Shelf
   *            -Link fileA's to fileB
   *        else
   *            If fileB hash found in Trash
   *                -Link fileA's to fileB
   *            else
   *                -Add fileB in database. // At this stage, fileB is definitely not in the database.
   *                -Link fileA's to fileB.
   * 
   * }
   * </pre>
   * @param fileA
   * @param fileB
   * @return
   */
  public boolean markDuplicate(File fileA, File fileB)
  {
    // Validations
    if(!this.validateMarkDuplicate(fileA, fileB))
      return false;
    
    String fileAPath = Utils.getCanonicalPath(fileA);
    String fileBPath = Utils.getCanonicalPath(fileB);
    if(fileAPath.compareTo(fileBPath)==0)
    {
      System.out.println("Error: Both files are exactly the same.");
      return false;
    }
    
    // *** Main logic start here *****
    Document shelfDocB = this.shelf.findDocByCanonicalPath(fileBPath);
    if(shelfDocB!=null)
    {// fileB path found in Shelf
      this.linkUIDs(fileAPath, shelfDocB.uid);
    }
    else
    {// fileB path NOT found in Shelf
      
      Document trashDocB = this.trash.findDocByCanonicalPath(fileBPath);
      if(trashDocB!=null)
      {// fileB path found in Trash
        
        // Circular duplication case: A was duplicate of B. Now B is duplicate of A.
        int uidB = trashDocB.uid;
        Document shelfDocMainB = this.shelf.findDocByUid(trashDocB.uid);
        if(shelfDocMainB!=null)
        {
          if(shelfDocMainB.canonical_path.compareTo(fileAPath)==0)
          {
            this.shelf.addDoc(trashDocB); // New uid will be created.
            uidB = this.shelf.findDocByCanonicalPath(fileBPath).uid;
            this.trash.removeDoc(trashDocB);
          }
        }
        
        this.linkUIDs(fileAPath, uidB);
      }
      else
      {// fileB path NOT found in Trash
        
        String fileBHash = Utils.getHash(fileB);
        shelfDocB = this.shelf.findDocByHash(fileBHash);
        if(shelfDocB!=null)
        {// fileB hash found in Shelf
          this.linkUIDs(fileAPath, shelfDocB.uid);
        }
        else
        {// fileB hash NOT found in Shelf
          
          trashDocB = this.trash.findDocByHash(fileBHash);
          if(trashDocB!=null)
          {// fileB hash found in Trash
            this.linkUIDs(fileAPath, trashDocB.uid);
          }
          else
          {// fileB hash NOT found in Trash
            
            // Add fileB directly to database. Should I use Manager.addFile()?
            Document newDoc = new Document(fileB);
            newDoc.hash = fileBHash;
            this.shelf.addDoc(newDoc);
            
            shelfDocB = this.shelf.findDocByCanonicalPath(fileBPath);
            this.linkUIDs(fileAPath, shelfDocB.uid);
            
          }
        }
      }
    }
    
    return true;
    
  }
  
  private void linkUIDs(String fromPathA, int uidB)
  {
    // Get uid of file A.
    Document shelfDocA = this.shelf.findDocByCanonicalPath(fromPathA);
    Document trashDocA = this.trash.findDocByCanonicalPath(fromPathA);
    
    // Move file A and its duplicates to B.
    if(shelfDocA!=null)
    {// FileA is found in Shelf
      
      // Move all duplicates of A's to B.
      this.trash.markDuplicate(shelfDocA.uid, uidB);
      
      // Move A to B.
      this.shelf.removeDoc(shelfDocA.uid);
      shelfDocA.uid = uidB;
      this.trash.addDoc(shelfDocA);
    }
    
    // Move all duplicates of A's to B.    
    if(trashDocA!=null)
    {
      this.trash.markDuplicate(trashDocA.uid, uidB);
    }
    
    if(shelfDocA==null && trashDocA==null)
    {// File A doesn't exist.
      File fileA = new File(fromPathA);
      Document trashDoc = new Document(fileA);
      trashDoc.hash = Utils.getHash(fileA);
      trashDoc.uid = uidB;
      this.trash.addDoc(trashDoc);
    }
  }
  
  public void searchByUid(int uid)
  {
    this.display(uid);
  }
  
  public void searchByHash(String hash)
  {
    Document shelfDoc = this.shelf.findDocByHash(hash);
    if(shelfDoc==null)
    {
      Document trashDoc = this.trash.findDocByHash(hash);
      if(trashDoc==null)
      {
        System.out.println(String.format("%s is not found.", hash));
      }
      else
        this.display(trashDoc.uid);
    }
    else
      this.display(shelfDoc.uid);
  }

  public void searchByFilename(String filename)
  {
    // Search in Shelf table and display results.
    List<Document> shelfDocsList = this.shelf.searchDocsByFilename(filename);
    if(shelfDocsList.size()==0)
    {
      System.out.println(String.format("Filename '%s' is not found in Shelf table.", filename));
    }
    else
    {
      this.displayDocument("Shelf:", true, shelfDocsList);
      System.out.println(String.format("%d found in Shelf table.\n", shelfDocsList.size()));
    }
    
    // Search in Trash table and display results.
    List<Document> trashDocsList = this.trash.searchDocsByFilename(filename);
    if(trashDocsList.size()==0)
    {
      System.out.println(String.format("Filename '%s' is not found in Trash table.", filename));
    }
    else
    {
      this.displayDocument("Trash:", true, trashDocsList);
      System.out.println(String.format("%d found in Trash table.", trashDocsList.size()));
    }
    
  }
  
  public void searchByFilepath(String filepath)
  {
    // Search in Shelf table and display results.
    List<Document> shelfDocsList = this.shelf.searchDocsByFilepath(filepath);
    if(shelfDocsList.size()==0)
    {
      System.out.println(String.format("Filepath '%s' is not found in Shelf table.", filepath));
    }
    else
    {
      this.displayDocument("Shelf:", true, shelfDocsList);
      System.out.println(String.format("%d found in Shelf table.\n", shelfDocsList.size()));
    }
    
    // Search in Trash table and display results.
    List<Document> trashDocsList = this.trash.searchDocsByFilepath(filepath);
    if(trashDocsList.size()==0)
    {
      System.out.println(String.format("Filepath '%s' is not found in Trash table.", filepath));
    }
    else
    {
      this.displayDocument("Trash:", true, trashDocsList);
      System.out.println(String.format("%d found in Trash table.", trashDocsList.size()));
    }
    
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/  
  
  /**
   * select duid, count(hash) from Trash group by hash having count(hash)>10 ;
   * @param uid
   */
  private void display(int uid)
  {
    Document shelfDoc = this.shelf.findDocByUid(uid);
    if(shelfDoc==null)
    {
      System.out.println(String.format("%d is not found!", uid));
    }
    else
    {
      // Display Shelf document.
      ArrayList<Document> shelfDocsList = new ArrayList<Document>();
      shelfDocsList.add(shelfDoc);
      this.displayDocument("Shelf:", true, shelfDocsList);
      
      List<Document> trashDocsList = this.trash.findDocsByUid(uid);
      this.displayDocument("Trash:", false, trashDocsList);
    }
  }
  
  private void displayDocument(String title, boolean header, List<Document> docsList)
  {
    int uidLength      = this.getMaxLengthOfUid(docsList);
    int hashLength     = this.getMaxLengthOfHash(docsList);
    int filenameLength = this.getMaxLengthOfFilename(docsList);
    
    System.out.println(title);
    if(header)
    {
      System.out.println(String.format( "  %"+uidLength     +"s | "
                                      + "%"+hashLength      +"s | "
                                      + "%-"+filenameLength +"s | "
                                      + "%s", 
                                      "<UID>", "<HASH>", "<FILENAME>", "<CANONICAL_PATH>"));
    }
    for(Document doc: docsList)
    {
      System.out.println(String.format( "  %"+uidLength     +"d | "
                                      + "%"+hashLength      +"s | "
                                      + "%-"+filenameLength +"s | "
                                      + "%s", 
                                      doc.uid, doc.hash, doc.filename, doc.canonical_path));
    }
  }
  private int getMaxLengthOfUid(List<Document> docsList)
  {
    int maxLength="<UID>".length(); // default value.
    
    for(Document doc: docsList)
    {
      String uidStr = doc.uid+"";
      if(uidStr.length()>maxLength)
        maxLength = uidStr.length();
    }
    return maxLength;
  }
  private int getMaxLengthOfHash(List<Document> docsList)
  {
    int maxLength="<HASH>".length(); // default value.
    for(Document doc: docsList)
    {
      if(doc.hash.length()>maxLength)
        maxLength = doc.hash.length();
    }
    return maxLength;
  }
  
  private int getMaxLengthOfFilename(List<Document> docsList)
  {
    int maxLength="<FILENAME>".length(); // default value.
    for(Document doc: docsList)
    {
      if(doc.filename.length()>maxLength)
        maxLength = doc.filename.length();
    }
    return maxLength;
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
