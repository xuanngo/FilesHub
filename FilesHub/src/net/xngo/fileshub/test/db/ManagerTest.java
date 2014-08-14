package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.ResultDocSet;
import net.xngo.fileshub.Utils;

// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;


// TestNG
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;




// Java Library
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
public class ManagerTest
{
  private Manager manager = new Manager();
  
  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    this.manager.createDbStructure();
  }
  
  @Test(description="Add new unique file.")
  public void addUniqueFile()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("AddUniqueFile");
    this.manager.addFile(uniqueFile);

    
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    
    assertNotNull(shelfDoc, String.format("Expected [%s] to be added in Shelf table but it is not.\n"
                                                + "%s"
                                                ,uniqueFile.getName(),
                                                Data.getFileInfo(uniqueFile, "File to add")
                                          ));
    
    // Clean up.
    uniqueFile.delete();
  }
  
  @Test(description="Add exact same file.")
  public void addExactSameFile()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addExactSameFile");
    this.manager.addFile(uniqueFile); // Add file 1st time.
    
    // Expected
    Shelf shelf = new Shelf();
    final int expected_totalDocsShelf = shelf.getTotalDocs();
    Trash trash = new Trash();
    final int expected_totalDocsTrash = trash.getTotalDocs();
    
    // Add the same unique file again.
    this.manager.addFile(uniqueFile); // Add the exact same file the 2nd time.

    final int actual_totalDocsShelf = shelf.getTotalDocs();
    final int actual_totalDocsTrash = trash.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, String.format("The expected number of documents in Shelf is %d but it is %d. Expect no change.", expected_totalDocsShelf, actual_totalDocsShelf));
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, String.format("The expected number of documents in Shelf is %d but it is %d. Expect no change.", expected_totalDocsTrash, actual_totalDocsTrash));
    
    // Clean up.
    uniqueFile.delete();
  }
  
  @Test(description="Add file with same hash but different file name/path.")
  public void addFileWithSameHash()
  {
    // Add unique file.
    File uniqueFile = Data.createTempFile("addFileWithSameHash");
    Document shelfDoc = this.manager.addFile(uniqueFile).document;
    
    // Copy unique file and then add to database.
    File duplicateFile = Data.createTempFile("addFileWithSameHash_duplicate_hash");
    Data.copyFile(uniqueFile, duplicateFile);
    
    // Add duplicate file to database.
    this.manager.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    // Validate
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    assertNotNull(trashDoc, String.format("[%s] is not added in Trash table. It should.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      ,duplicateFile.getName(),
                                                      Data.getFileInfo(duplicateFile, "File to add"),
                                                      shelfDoc.getInfo("Shelf")
                                                 ));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
  }
  
  @Test(description="Add the same file that has changed since FilesHub last ran.")
  public void addFileChangedSinceLastRun()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("AddFileChangedSinceLastRun");
    long expected_trash_last_modified = uniqueFile.lastModified();
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document oldShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile));     
    
    // Update the unique file.
    Data.writeStringToFile(uniqueFile, "new content");
    long expected_repo_last_modified = uniqueFile.lastModified();
    
    // Add the exact same file again with new last modified time.
    this.manager.addFile(uniqueFile);
    
    // Validations: Check that Shelf document info is moved to Trash table and the new document is updated in Shelf table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc.last_modified, expected_trash_last_modified,
                                  String.format("Last modified time from Trash should be the same as the old file.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , oldShelfDoc.getInfo("Old file"),
                                                      trashDoc.getInfo("Trash")
                                                ));      
    
    Document newShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile)); 
    assertEquals(newShelfDoc.last_modified, expected_repo_last_modified,
                                  String.format("Last modified time from Shelf should be the same as the file to add.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , Data.getFileInfo(uniqueFile, "File to add"),
                                                      newShelfDoc.getInfo("Shelf")
                                                ));      
    
    // Clean up.
    uniqueFile.delete();    
    
  }
  
  @Test(description="Add duplicated file that has changed since last run.")
  public void addFileTrashFileChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("addFileTrashFileChanged");
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileTrashFileChanged_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    this.manager.addFile(duplicateFile);


    // Update the duplicated file.
    Data.writeStringToFile(duplicateFile, "new content");
    long expected_last_modified = duplicateFile.lastModified();    
    
    // Add the exact same duplicated file again with new last modified time.
    this.manager.addFile(duplicateFile);
    
    // Validate
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByHash(Utils.getHash(duplicateFile));
    assertEquals(trashDoc.last_modified, expected_last_modified,
                            String.format("Last modified time from Trash should be the same as the file to add.\n"
                                                + "%s"
                                                + "\n"
                                                + "%s"
                                                , Data.getFileInfo(duplicateFile, "File to add"),
                                                trashDoc.getInfo("Trash")
                                          ));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();      
    
  }
  
  @Test(description="Add exactly the same deleted file. Status check only.")
  public void addFileTrashSameFile()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("addFileDeletedChangedFile");
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileDeletedChangedFile_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    this.manager.addFile(duplicateFile);
    
    
    // Add the exact same file again with new last modified time.
    ResultDocSet resultDocSet = this.manager.addFile(duplicateFile);

    // Simple status check:
    assertEquals(resultDocSet.status, ResultDocSet.EXACT_SAME_TRASH_FILE,
        String.format("[%s] already exists in database. Status should be %d.\n"
                            + "File to add:\n"
                            + "\tlast_modified = %d\n"
                            + "\tcanonical_path = %s\n"
                            
                            + "\n"
                            + "Trash:\n"
                            + "\tuid = %d\n"
                            + "\tlast_modified = %d\n"
                            + "\tcanonical_path = %s\n"
                            + "\thash = %s\n"
                            , resultDocSet.file.getName(), resultDocSet.status, 
                                resultDocSet.file.lastModified(), Utils.getCanonicalPath(resultDocSet.file),
                                resultDocSet.document.uid, resultDocSet.document.last_modified, resultDocSet.document.canonical_path, resultDocSet.document.hash));
    
    // Clean up after validations. Otherwise, resultDocSet.file will be empty because it is deleted.
    uniqueFile.delete();
    duplicateFile.delete();    
    
  }
  
  
  @Test(description="Add same filename but different content.")
  public void addFileSameFilenameDiffContent()
  {
    // Add a temporary file in database.
    File tmpFile = Data.createTempFile("addFileSameFilenameDiffContent");
    File tmpDirectory = new File(System.getProperty("java.io.tmpdir")+System.nanoTime());
    tmpDirectory.mkdir();
    Document ShelfDoc = this.manager.addFile(tmpFile).document;
    
    // Copied temporary file to another directory and add content to the copied file so it will have different content.
    File copiedFile = Data.copyFileToDirectory(tmpFile, tmpDirectory);
    Data.writeStringToFile(copiedFile, System.nanoTime()+"");
    this.manager.addFile(copiedFile);
    
    // Validations
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(copiedFile));
    
    assertNotNull(trashDoc, String.format("Expected a row is added in Trash table but it is not.\n"
                                            + "File to add:\n"
                                            + "\tlast_modified = %d\n"
                                            + "\tcanonical_path = %s\n"
                                            + "\thash = %s\n"
                                            + "\tfilename = %s\n"
                                            
                                            + "\n"
                                            + "Shelf:\n"
                                            + "\tuid = %d\n"
                                            + "\tlast_modified = %d\n"
                                            + "\tcanonical_path = %s\n"
                                            + "\thash = %s\n"
                                            + "\tfilename = %s\n"
                                            ,   copiedFile.lastModified(), Utils.getCanonicalPath(copiedFile), Utils.getHash(copiedFile), copiedFile.getName(),
                                            ShelfDoc.uid, ShelfDoc.last_modified, ShelfDoc.canonical_path, ShelfDoc.hash, ShelfDoc.filename));

  }  
  
  @Test(description="Update file that has changed since added in database.")
  public void updateFileChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("updateFileChanged");
    long expected_trash_last_modified = uniqueFile.lastModified();
    ResultDocSet resultDocSet = this.manager.addFile(uniqueFile);
    
    // Update the unique file.
    try { FileUtils.touch(uniqueFile); } catch(IOException e){ e.printStackTrace(); }
    long expected_shelf_last_modified = uniqueFile.lastModified();
    
    // Update database
    this.manager.update();
    
    // Testing: Check old last modified time is moved to Trash table and new last modified time is in Shelf table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc.last_modified, expected_trash_last_modified, "Check last modified time in Trash table.");
    
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByUid(resultDocSet.document.uid);
    assertEquals(shelfDoc.last_modified, expected_shelf_last_modified, "Check last modified time in Shelf table.");
    
    // Clean up after validations. Otherwise, resultDocSet.file will be empty because it is deleted.
    uniqueFile.delete();      
  }
  
  @Test(description="Update missing files. Do simple count check. Note: For each run, it will take longer time to run because the database grow.")
  public void updateMissingFiles()
  {
    // Add unique files in Shelf.
    int MAX = new Shelf().getTotalDocs()+3; // Bigger than total files in Shelf so that it is not fooled by the remnant deleted files of the other tests.
    for(int i=0; i<MAX; i++)
    {
      File uniqueFile = Data.createTempFile("updateMissingFiles_"+i);
      this.manager.addFile(uniqueFile);
      uniqueFile.delete();
    }

    // Get a list of missing files through update().
    List<Document> docList = this.manager.update();
    
    assertTrue((docList.size()>=MAX), String.format("Missing files=%d, Deleted files=%d, Missing Files >= Deleted files", docList.size(), MAX));
  }
  

  
}
