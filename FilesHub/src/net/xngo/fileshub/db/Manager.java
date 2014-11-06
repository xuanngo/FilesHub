package net.xngo.fileshub.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.report.Difference;
import net.xngo.fileshub.report.Report;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.PairFile;

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
    File DbFile = new File(Config.DB_FILE_PATH);
    if(!DbFile.exists())
    {// Database file doesn't exist.
      this.shelf.createTable();
      this.trash.createTable();
    }
    else
    {
      if(DbFile.length()<1)
      {// Database file already exist but it is empty.
        this.shelf.createTable();
        this.trash.createTable();
      }
    }
  }
  

  /**
   * Add a file in database. A file is identified by its hash.
   * For optimization, check the path first and then the hash.
   * We can't use filename to identify a file because filenames can
   *  be generic and have different content, e.g. ../something/Track 1.
   * <pre>
   * {@code
   * The pseudo-code below serves as a general idea of what addFiles() does.
   *  It has since modified to add more edge cases.
   * ===============================================================
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
    
    Document shelfDoc = this.shelf.getDocByCanonicalPath(doc.canonical_path);
    if(shelfDoc == null)
    {// Path not found in Shelf.
      
      Document trashDoc = this.trash.getDocByCanonicalPath(doc.canonical_path);
      if(trashDoc != null)
      {// Path found in Trash.
        
        // Check if original file doesn't exist.
        Document originalDoc = this.shelf.getDocByUid(trashDoc.uid);
        if(new File(originalDoc.canonical_path).exists())
          return this.shelf.getDocByUid(trashDoc.uid);
        else
        {// Shelf file doesn't exist.
          // Move original file info from Shelf to Trash.
          this.shelf.saveDoc(trashDoc); // trashDoc is used instead of 'doc' because they both
                                        //    have exact same path. It will save hash time.
          this.trash.removeDoc(trashDoc); // Remove from Trash because it is moved to Shelf.
          this.trash.addDoc(originalDoc); // Move original doc from Shelf to Trash because it doesn't exist anymore.
          return null;
        }
      }
      else
      {
          doc.hash = Utils.getHash(file);
          if(doc.hash==null)
          {
            throw new RuntimeException(String.format("FileNotFoundException: %s", file.getAbsolutePath()));
          }
          else
          {
            shelfDoc = this.shelf.getDocByHash(doc.hash);
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

              List<Document> trashDocsList = this.trash.getDocsByHash(doc.hash);
              if(trashDocsList.size()>0)
              {// Hash found in Trash.
                shelfDoc = this.shelf.getDocByUid(trashDocsList.get(0).uid);
                if(shelfDoc==null)
                {// No document exists in Shelf.
                  
                  final int fromUid = this.shelf.addDoc(doc); // Simply add to Shelf with auto-generated Uid.
                  // Link the newly created document in Shelf back to the existing duplicated in Trash table.
                  this.shelf.changeUid(fromUid, trashDocsList.get(0).uid); 
                  
                  return null; // Because added file become the main file in Shelf table.
                }
                else
                {// Hash found in Shelf
                  
                  if(new File(shelfDoc.canonical_path).exists())
                  {
                    doc.uid = shelfDoc.uid;
                    trash.addDoc(doc);
                    return shelfDoc;                    
                  }
                  else
                  {// File from Shelf doesn't exist in filesystem.
                    
                    // Overwrite Shelf document with 'to add file'.
                    doc.uid = shelfDoc.uid;
                    this.shelf.saveDoc(doc);
                    
                    // Move Shelf document to trash.
                    this.trash.addDoc(shelfDoc);        // Add non-existing file to Trash.
                    
                    return null;// No duplicate. 'to add file' becomes main file.                    
                  }
                }
              }
              else
              {
                this.shelf.addDoc(doc);
                return null; // New file.
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
    List<Document> docList = this.shelf.getDocs();
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
   * FROM  to   TO
   * Shelf to Shelf:
   *    -Fshelf.uid moves to Trash with To.uid.
   *    -Fduplicates(trash.duid) move to Trash with To.uid.
   *    
   * Trash to Shelf:
   *    -Ftrash.canonical_paths move to Trash with To.uid.
   *    
   * Shelf to Trash:
   *    -Fshelf.uid moves to Trash with To.uid
   *    -Fduplicates(trash.duid) move to Trash with To.uid.
   *    
   * Trash to Trash:
   *    -Ftrash.canonical_paths move to Trash with To.uid.
   * @param fileFrom
   * @param fileTo
   */
  public boolean markDuplicate(File fileFrom, File fileTo)
  {
    String fileFromPath = Utils.getCanonicalPath(fileFrom);
    String fileToPath   = Utils.getCanonicalPath(fileTo);
    if(fileFromPath.compareTo(fileToPath)==0)
    {
      System.out.println("Error: Both files are exactly the same.");
      return false;
    }    
    
    Document shelfDocFrom = this.shelf.getDocByCanonicalPath(fileFromPath);
    if(shelfDocFrom!=null)
    {// FROM is in Shelf.
      
      Document shelfDocTo = this.shelf.getDocByCanonicalPath(fileToPath);
      if(shelfDocTo!=null)
      {// TO is in Shelf.
        
        if(new File(shelfDocTo.canonical_path).exists())
        {// TO file does exist.
          this.moveShelfDocToTrash(shelfDocFrom, shelfDocTo.uid);
          this.trash.changeDuid(shelfDocFrom.uid, shelfDocTo.uid);          
        }
        else
        {// TO file doesn't exist.
          if(new File(shelfDocFrom.canonical_path).exists())
          {// FROM file does exist.
            this.moveShelfDocToTrash(shelfDocTo, shelfDocTo.uid);
            this.shelf.changeUid(shelfDocFrom.uid, shelfDocTo.uid);
          }
          else
          {// FROM and TO files don't exist.
            this.moveShelfDocToTrash(shelfDocFrom, shelfDocTo.uid);
            this.trash.changeDuid(shelfDocFrom.uid, shelfDocTo.uid);                 
          }
        }

      }
      else
      {// TO is NOT in Shelf.
        
        Document trashDocTo = this.trash.getDocByCanonicalPath(fileToPath);
        if(trashDocTo!=null)
        {// TO is in Trash.
          

          // Are we manipulating entries from the same document?
          if(trashDocTo.uid==shelfDocFrom.uid)
          {// YES
            
            // Move trashDoc to Shelf table only if trashDoc file exists. 
            if(new File(trashDocTo.canonical_path).exists())
            {
              this.moveShelfDocToTrash(shelfDocFrom, trashDocTo.uid);
              this.moveTrashDocToShelf(trashDocTo, trashDocTo.uid);
            }// Else do nothing current situation is correct.
          }
          else
          {// NO
            
            Document shelfDoc = this.shelf.getDocByUid(trashDocTo.uid);
            if(new File(shelfDoc.canonical_path).exists())
            {// ShelfDoc file of TO does exist.
              this.moveShelfDocToTrash(shelfDocFrom, trashDocTo.uid);
              this.trash.changeDuid(shelfDocFrom.uid, trashDocTo.uid); // Change FROM duplicates to new uid.
            }
            else
            {// ShelfDoc file of TO doesn't exist.
              if(new File(shelfDocFrom.canonical_path).exists())
              {// FROM file exists.
                this.moveShelfDocToTrash(shelfDoc, trashDocTo.uid);
                this.shelf.changeUid(shelfDocFrom.uid, trashDocTo.uid);
              }
              else
              {// FROM and TO files don't exist.
                this.moveShelfDocToTrash(shelfDocFrom, trashDocTo.uid);
                this.trash.changeDuid(shelfDocFrom.uid, trashDocTo.uid); // Change FROM duplicates to new uid.                
              }
            }
          }            

        }
        else
        {// TO is NOT in Shelf nor Trash
          
          if(fileTo.exists())
          {// TO file exists.
            this.addFile(fileTo);
            this.markDuplicate(fileFrom, fileTo);
          }
          else
          {// TO file doesn't physically exist and it is not in the database.
            System.out.println(String.format("ERROR: %s doesn't exist.", fileTo.getAbsolutePath()));
            return false;
          }
        }
      }
    }
    else
    {// FROM is NOT in Shelf.
      
      Document trashDocFrom = this.trash.getDocByCanonicalPath(fileFromPath);
      if(trashDocFrom!=null)
      {// FROM is in Trash.
        
        Document shelfDocTo = this.shelf.getDocByCanonicalPath(fileToPath);
        if(shelfDocTo!=null)
        {// TO is in Shelf.
          if(new File(shelfDocTo.canonical_path).exists())
          {// TO file exists
            this.trash.changeDuid(fileFromPath, shelfDocTo.uid);
          }
          else
          {// TO file doesn't exist.
            if(new File(fileFromPath).exists())
            {// FROM file exists.
              
              // Order of methods below are important. Otherwise, there will be 2 entries with the same
              //  Uid in Shelf table.
              //  You have to moveShelfDocToTrash()
              //    then moveTrashDocToShelf().
              this.moveShelfDocToTrash(shelfDocTo, shelfDocTo.uid);
              this.moveTrashDocToShelf(trashDocFrom, shelfDocTo.uid);
            }
            else
            {// FROM and TO files don't exist.
              this.trash.changeDuid(fileFromPath, shelfDocTo.uid);
            }
          }
        }
        else
        {// TO is NOT in Shelf.
          
          Document trashDocTo = this.trash.getDocByCanonicalPath(fileToPath);
          if(trashDocTo!=null)
          {// TO is in Trash
            
            Document shelfDocMainTo = this.shelf.getDocByUid(trashDocTo.uid);
            if(new File(shelfDocMainTo.canonical_path).exists())
            {// Main file of TO exists.
              this.trash.changeDuid(fileFromPath, trashDocTo.uid);
            }
            else
            {// Main file of TO doesn't exist.
              if(new File(trashDocFrom.canonical_path).exists())
              {// FROM file physically exists.
                // Make FROM file the main file of TO.
                this.moveShelfDocToTrash(shelfDocMainTo, trashDocTo.uid);
                this.moveTrashDocToShelf(trashDocFrom, trashDocTo.uid);
              }
              else
              {// FROM & TO physically don't exist.
                this.trash.changeDuid(fileFromPath, trashDocTo.uid);
              }
            }

          }
          else
          {// TO is not in Shelf nor in Trash.
            
            this.addFile(fileTo);
            this.markDuplicate(fileFrom, fileTo);
          }
        }
      }
      else
      {// FROM is not in Shelf nor in Trash.
        if(fileFrom.exists())
        {
          // Add FROM file only if it exists.
          this.addFile(fileFrom);
          this.markDuplicate(fileFrom, fileTo);
        }
        else
        {// FROM file doesn't exist.
          System.out.println(String.format("ERROR: %s doesn't exist.", fileFrom.getAbsolutePath()));
          return false;
        }
      }
    }
    
    return true;
  }
  
  /**
   * Move shelf document to Trash with to uid.
   * @param shelfDocFrom
   * @param toUid
   */
  private void moveShelfDocToTrash(final Document shelfDocFrom, int toUid)
  {
    // Remove shelf doc from Shelf.
    this.shelf.removeDoc(shelfDocFrom.uid);
    
    // Move shelf doc info to Trash.
    Document trashDoc = new Document(shelfDocFrom);
    trashDoc.uid = toUid;
    this.trash.addDoc(trashDoc);
  }
  
  private void moveTrashDocToShelf(final Document trashDocFrom, int toUid)
  {
    // Add document in Shelf table.
    Document shelfDoc = new Document(trashDocFrom);
    final int newUid = this.shelf.addDoc(shelfDoc);
    
    // Change the added document with the specified uid(i.e. toUid)
    this.shelf.changeUid(newUid, toUid);
    
    // Remove the document entry from Trash.
    this.trash.removeDoc(trashDocFrom);
  }
  
  public void searchByUid(int uid)
  {
    this.display(uid);
  }
  
  public void searchByHash(String hash)
  {
    // Search in Shelf table and display results.
    List<Document> shelfDocsList = this.shelf.getDocsByHash(hash);
    if(shelfDocsList.size()==0)
    {
      System.out.println("Shelf:");
      System.out.println(String.format("  Hash '%s' is not found in Shelf table.\n", hash));
    }
    else
    {
      this.displayDocument("Shelf:", true, shelfDocsList);
      System.out.println(String.format("%d found in Shelf table.\n", shelfDocsList.size()));
    } 
    
    // Search in Trash table and display results.
    List<Document> trashDocsList = this.trash.getDocsByHash(hash);
    if(trashDocsList.size()==0)
    {
      System.out.println("Trash:");
      System.out.println(String.format("  Hash '%s' is not found in Trash table.", hash));
    }
    else
    {
      this.displayDocument("Trash:", true, trashDocsList);
      System.out.println(String.format("%d found in Trash table.", trashDocsList.size()));
    }    
  }

  
  public void searchByFilename(String filename)
  {
    // Search in Shelf table and display results.
    List<Document> shelfDocsList = this.shelf.searchDocsByFilename(filename);
    if(shelfDocsList.size()==0)
    {
      System.out.println("Shelf:");
      System.out.println(String.format("  Filename '%s' is not found in Shelf table.\n", filename));
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
      System.out.println("Trash:");
      System.out.println(String.format("  Filename '%s' is not found in Trash table.", filename));
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
      System.out.println("Shelf:");
      System.out.println(String.format("  Filepath '%s' is not found in Shelf table.\n", filepath));
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
      System.out.println("Trash:");
      System.out.println(String.format("  Filepath '%s' is not found in Trash table.", filepath));
    }
    else
    {
      this.displayDocument("Trash:", true, trashDocsList);
      System.out.println(String.format("%d found in Trash table.", trashDocsList.size()));
    }
    
  }
  
  public ArrayList<PairFile> searchSimilarFilename(int fuzzyRate)
  {
    List<Document> docsList = this.cleanFilenames(this.shelf.getDocs());
    ArrayList<PairFile> pairFileList = new ArrayList<PairFile>();
    if(docsList.size()>1)
    {
      for(int i=0; i<docsList.size()-1; i++)
      {
        for(int j=i+1; j<docsList.size(); j++)
        {
          Difference diff = new Difference(docsList.get(i).filename, docsList.get(j).filename);
          if(diff.getSimilarRate()>fuzzyRate)
          {
            System.out.println(String.format("[%d] %s ?= %s ", diff.getSimilarRate(), docsList.get(i).filename, docsList.get(j).filename));
            PairFile pairFile = new PairFile();
            pairFile.similarRate = diff.getSimilarRate();
            pairFile.fileA = docsList.get(i).canonical_path;
            pairFile.fileB = docsList.get(j).canonical_path;
            pairFileList.add(pairFile);
          }
        }
      }
    }
    return pairFileList;
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  private List<Document> cleanFilenames(List<Document> docsList)
  {
    ArrayList<String> commonTerms = this.getCommonTerms();
    ArrayList<Document> resultDocs = new ArrayList<Document>();
    for(Document doc: docsList)
    {
      StringBuilder filename = new StringBuilder(doc.filename.toLowerCase());
      for(String term: commonTerms)
      {
        int start = filename.indexOf(term);
        if(start!=-1)
        {
          int end = start + term.length();
          filename.replace(start, end, "");
        }
      }
      doc.filename = filename.toString();
      resultDocs.add(doc);
    }
    
    return resultDocs;
  }
  private ArrayList<String> getCommonTerms()
  {
    ArrayList<String> strList = new ArrayList<String>();
    try 
    {
      BufferedReader in = new BufferedReader(new FileReader(Config.WORD_LIST));
      String line;
      while ((line = in.readLine()) != null)
      {
        strList.add(line.toLowerCase());
      }
      in.close();
  
    }
    catch (FileNotFoundException ex)
    {
      System.out.println(String.format("Warning: %s is missing!", Config.WORD_LIST));
    }
    catch (IOException ex) 
    {
      ex.printStackTrace();
    }
    
    return strList;
  }
  
  /**
   * Display all entries of a document.
   * @param uid
   */
  private void display(int uid)
  {
    /**
     * It is assumed that a document must always have a row in Shelf table.
     */
    Document shelfDoc = this.shelf.getDocByUid(uid);
    if(shelfDoc==null)
    {
      System.out.println(String.format("'%d' is not found!", uid));
    }
    else
    {
      // Display Shelf document.
      ArrayList<Document> shelfDocsList = new ArrayList<Document>();
      shelfDocsList.add(shelfDoc);
      this.displayDocument("Shelf:", true, shelfDocsList);
      
      List<Document> trashDocsList = this.trash.getDocsByUid(uid);
      this.displayDocument("Trash:", false, trashDocsList);
    }
  }
  
  private void displayDocument(String title, boolean header, List<Document> docsList)
  {
    int uidLength      = this.maxUidLength(docsList);
    int hashLength     = this.maxHashLength(docsList);
    int filenameLength = this.maxFilenameLength(docsList);
    
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
  private int maxUidLength(List<Document> docsList)
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
  private int maxHashLength(List<Document> docsList)
  {
    int maxLength="<HASH>".length(); // default value.
    for(Document doc: docsList)
    {
      if(doc.hash.length()>maxLength)
        maxLength = doc.hash.length();
    }
    return maxLength;
  }
  
  private int maxFilenameLength(List<Document> docsList)
  {
    int maxLength="<FILENAME>".length(); // default value.
    for(Document doc: docsList)
    {
      if(doc.filename.length()>maxLength)
        maxLength = doc.filename.length();
    }
    return maxLength;
  }
  
  /**
   * Note: File A is a duplicate of File B.
   * @param fileA
   * @param fileB
   * @return
   */
  private boolean validateMarkDuplicate(File fileA, File fileB)
  {
    if(!fileA.exists())
    {
      System.out.println(String.format("Error: [%s] doesn't exist.", fileA.getAbsolutePath()));
      return false;
    }
    
    if(!fileB.exists())
    {
      System.out.println(String.format("Error: [%s] doesn't exist.\n"
          + "Do the followings:\n"
          + "  FilesHub -a %s\n"
          + "  FilesHub -d %s %s", 
          fileB.getAbsolutePath(), 
          fileA.getAbsolutePath(), 
          fileB.getAbsolutePath(), fileA.getAbsolutePath() ));
      return false;
    }
    
    
    if(!fileA.isFile())
    {
      System.out.println(String.format("Error: [%s] is not a file.", fileA.getAbsolutePath()));
      return false;
    }
    
    if(!fileB.isFile())
    {
      System.out.println(String.format("Error: [%s] is not a file.", fileB.getAbsolutePath()));
      return false;
    }
    
    return true;
    
  }
  
}
