package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
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
import java.util.ArrayList;

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

    // Validation:
    //  Check if file path exists in Shelf.
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
    
    // Expected values:
    //   Regardless of how many times you add the exact same file, the number of documents in Shelf and Trash.
    Shelf shelf = new Shelf();
    final int expected_totalDocsShelf = shelf.getTotalDocs();
    Trash trash = new Trash();
    final int expected_totalDocsTrash = trash.getTotalDocs();
    
    // Add the same unique file again.
    this.manager.addFile(uniqueFile); // Add the exact same file the 2nd time.

    // Actual values.
    final int actual_totalDocsShelf = shelf.getTotalDocs();
    final int actual_totalDocsTrash = trash.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsShelf, actual_totalDocsShelf));
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsTrash, actual_totalDocsTrash));
    
    // Clean up.
    uniqueFile.delete();
  }
  
  @Test(description="Add file with same hash but different file name/path.")
  public void addFileWithSameHash()
  {
    // Add unique file.
    File uniqueFile = Data.createTempFile("addFileWithSameHash");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByFilename(uniqueFile.getName());
    
    // Copy unique file and then add to database.
    File duplicateFile = Data.createTempFile("addFileWithSameHash_duplicate_hash");
    Data.copyFile(uniqueFile, duplicateFile);
    
    // Add duplicate file to database.
    this.manager.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    // Validate:
    //  Check new hash is added in Trash.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByHash(Utils.getHash(duplicateFile));
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
  
  @Test(description="Add the same file in Shelf that has changed since FilesHub last ran.")
  public void addFileShelfFileChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("addFileShelfFileChanged");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document oldShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile));     
    
    // Update the unique file.
    Data.writeStringToFile(uniqueFile, "new content");
    
    // Add the exact same file again with new last modified time.
    this.manager.addFile(uniqueFile);
    
    // Validations: Check that Shelf document info is moved to Trash table and the new document is updated in Shelf table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc, oldShelfDoc,
                                  String.format("Document information should be moved from Shelf to Trash.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , oldShelfDoc.getInfo("Old file"),
                                                      trashDoc.getInfo("Trash")
                                                ));      
    
    Document newShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile)); 
    assertEquals(newShelfDoc.last_modified, uniqueFile.lastModified(),
                                  String.format("Last modified time in Shelf table should be the same as the file to add.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , Data.getFileInfo(uniqueFile, "File to add"),
                                                      newShelfDoc.getInfo("Shelf")
                                                ));      
    
    // Clean up.
    uniqueFile.delete();    
    
  }
  
  @Test(description="Add same file in Trash that has changed since last run.")
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
    Data.writeStringToFile(duplicateFile, " new content");
    long expected_last_modified = duplicateFile.lastModified();    
    
    // Add the exact same duplicated file again with new last modified time.
    this.manager.addFile(duplicateFile);
    
    // Validate
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByHash(Utils.getHash(duplicateFile));
    assertEquals(trashDoc.last_modified, expected_last_modified,
                            String.format("Last modified time in Trash table should be the same as the file to add.\n"
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
  
  @Test(description="Add exactly the same Trash file.")
  public void addFileTrashSameFile()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("addFileTrashSameFile");
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileTrashSameFile_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    this.manager.addFile(duplicateFile);
    
    // Expected values.
    Shelf shelf = new Shelf();
    final int expected_totalDocsShelf = shelf.getTotalDocs();
    Trash trash = new Trash();
    final int expected_totalDocsTrash = trash.getTotalDocs();    
    
    // Add the exact same file again.
    this.manager.addFile(duplicateFile);

    // Actual values.
    final int actual_totalDocsShelf = shelf.getTotalDocs();
    final int actual_totalDocsTrash = trash.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsShelf, actual_totalDocsShelf));
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsTrash, actual_totalDocsTrash));
        
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();    
    
  }
  
  
  @Test(description="Add same filename but different content.")
  public void addFileSameNameDiffContent()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileSameNameDiffContent");
    this.manager.addFile(uniqueFile);
    
    // Copied temporary file to another directory and add content to the copied file so it will have different content.
    File tmpDirectory = new File(System.getProperty("java.io.tmpdir")+System.nanoTime());
    tmpDirectory.mkdir();
    File copiedFile = Data.copyFileToDirectory(uniqueFile, tmpDirectory);
    Data.writeStringToFile(copiedFile, " new content");
    this.manager.addFile(copiedFile);
    
    // Validations
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(copiedFile));
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile));
    assertNotNull(trashDoc, String.format("Expected a row is added in Trash table but it is not.\n"
                                            + "%s"
                                            + "\n"
                                            + "%s"
                                            , Data.getFileInfo(copiedFile, "File to add"),
                                            shelfDoc.getInfo("Shelf")));
    
    // Clean up.
    uniqueFile.delete();
    copiedFile.delete();
    tmpDirectory.delete();

  }  
  
  @Test(description="Add same file from multiple paths.")
  public void addFileSameFileWithDiffPaths()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileSameFileWithDiffPaths");
    this.manager.addFile(uniqueFile);
    
    // Expected values:
    //  Total # of documents should stay the same after the initial add of new file.
    Shelf shelf = new Shelf();
    final int expectedTotalDocsShelf = shelf.getTotalDocs();
    Trash trash = new Trash();
    final int expectedTotalDocsTrash = trash.getTotalDocs()+1;
    
    // Add same file from multiple paths
    for(int i=0; i<5; i++)
    {
      File tmpDirectory = new File(System.getProperty("java.io.tmpdir")+System.nanoTime()+i);
      tmpDirectory.mkdir();
      File copiedFile = Data.copyFileToDirectory(uniqueFile, tmpDirectory);
      this.manager.addFile(copiedFile);
      copiedFile.delete();
      tmpDirectory.delete();
    }
    
    // Actual values.
    final int actualTotalDocsShelf = shelf.getTotalDocs();
    final int actualTotalDocsTrash = trash.getTotalDocs();
    
    // Validate that no unexpected row are added.
    assertEquals(actualTotalDocsShelf, expectedTotalDocsShelf, String.format("More rows are added in Shelf table than expected."));
    assertEquals(actualTotalDocsTrash, expectedTotalDocsTrash, String.format("More rows are added in Trash table than expected."));
    
    // Clean up.
    uniqueFile.delete();

  }   
  
  
  @Test(description="Update file that has changed since added in database. Note: This is exactly the same as addFileShelfFileChanged(), except that it uses Manager.update() instead of Manager.addFile().")
  public void updateFileChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("updateFileChanged");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document oldShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile));       
    
    // Update the unique file.
    Data.writeStringToFile(uniqueFile, "new content");
    
    // Update database
    this.manager.update();
    
    // Validations: Check that Shelf document info is moved to Trash table and the new document is updated in Shelf table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc, oldShelfDoc,
                                  String.format("Document information should be moved from Shelf to Trash.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , oldShelfDoc.getInfo("Old file"),
                                                      trashDoc.getInfo("Trash")
                                                ));      
    
    Document newShelfDoc = shelf.findDocByHash(Utils.getHash(uniqueFile)); 
    assertEquals(newShelfDoc.last_modified, uniqueFile.lastModified(),
                                  String.format("Last modified time in Shelf table should be the same as the file to add.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , Data.getFileInfo(uniqueFile, "File to add"),
                                                      newShelfDoc.getInfo("Shelf")
                                                ));       
    
    // Clean up.
    uniqueFile.delete();      
  }
  
  @Test(description="Check whether or not Update() returns the correct number of missing files.")
  public void updateMissingFilesCount()
  {
    // Add unique files in Shelf.
    List<Document> expectedMissingDocList = new ArrayList<Document>();
    Shelf shelf = new Shelf();
    int MAX = 17;
    for(int i=0; i<MAX; i++)
    {
      File uniqueFile = Data.createTempFile("updateMissingFilesCount_"+i);
      this.manager.addFile(uniqueFile);
      expectedMissingDocList.add(shelf.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile)));
      uniqueFile.delete();
    }

    // Get a list of missing files through update().
    List<Document> actualMissingDocList = this.manager.update();
    
    for(Document doc: expectedMissingDocList)
    {
      if(actualMissingDocList.contains(doc))
        assertTrue(true);
      else
        assertTrue(false, String.format("The following document should be missing:\n"
                                          + "%s", doc.getInfo("Missing file info")
                                       ));
    }
  }
  

  
}
