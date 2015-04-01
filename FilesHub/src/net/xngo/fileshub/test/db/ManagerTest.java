package net.xngo.fileshub.test.db;

// FilesHub classes.
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

// Java Library
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.fileshub.test.helpers.ShelfExt;
import net.xngo.fileshub.test.helpers.TrashExt;
// net.xngo.utils.java
import net.xngo.utils.java.math.Random;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
// FilesHub


/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
public class ManagerTest
{
  private static final boolean DEBUG = true;
  
  private final int randomInt = java.lang.Math.abs(Random.Int())+1;
  private AtomicInteger atomicInt = new AtomicInteger(randomInt); // Must set initial value to more than 0. Duid can't be 0.
  
  private Manager manager = new Manager();

  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    this.manager.createDbStructure();
    
    // DEBUG: Commit every single transaction in database.
    if(ManagerTest.DEBUG)
    {
      try { Main.connection.setAutoCommit(true); }
      catch(SQLException ex) { ex.printStackTrace(); }
    }      
  }
  
 
  @Test(description="Update file that has changed since added in database. "
      + "Note: This is exactly the same as addFileShelfFileChanged(), "
      + "except that it uses Manager.update() instead of Manager.addFile().")
  public void updateFileChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("updateFileChanged");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document oldShelfDoc = shelf.getDocByHash(Utils.getHash(uniqueFile));       
    
    // Update the unique file.
    Data.writeStringToFile(uniqueFile, "new content");
    uniqueFile.setLastModified(System.currentTimeMillis()+1000); // Guarantee content update causes an update of File.lastmodified().
                                                                 //   All platforms support file-modification times to the nearest second

    // Update database
    this.manager.update();
    
    // Validations: Check that Shelf document info is moved to Trash table 
    //                and the new document is updated in Shelf table.
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));

    assertNotNull(trashDoc, String.format("trashDoc can't be null. %s is not found in Trash table.", 
                                              Utils.getCanonicalPath(uniqueFile)));
    assertEquals(trashDoc, oldShelfDoc,
                                  String.format("Document information should be moved from Shelf to Trash.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      , oldShelfDoc.getInfo("Old file"),
                                                      trashDoc.getInfo("Trash")
                                                ));      
    
    Document newShelfDoc = shelf.getDocByHash(Utils.getHash(uniqueFile)); 
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
      expectedMissingDocList.add(shelf.getDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile)));
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
  
  @Test(description="update file turned directory. "
      + "(1) Add 'animal' file."
      + "(2) Rename 'animal' file to 'animal_cat.txt' ."
      + "(3) Create directory called 'animal'."
      + "(4) Move 'animal_cat.txt' file to 'animal' directory."
      + "(5) End result: animal/animal_cat.txt"
      )  
  public void updateFileTurnedDirectory()
  {
    //*** Prepare data: Create a unique file. 
    File uniqueFile = Data.createTempFile("updateFileTurnedDirectory");
    this.manager.addFile(uniqueFile);
    
    //*** Main test: Rename the file. Create a directory using the exact same file path. Add the renamed file to the new directory. 
    File tmpUniqueFile = Data.createTempFile("addFileTurnedDirectory_tmp");
    File newFile = new File(uniqueFile.getAbsolutePath()+File.separator+System.currentTimeMillis()+".tmp");
    try
    {
      // Temporarily rename the file.
      Path newPath = Files.move(uniqueFile.toPath(), tmpUniqueFile.toPath(), REPLACE_EXISTING);
      
      // Create a directory with the exact same path.
      Files.createDirectory(uniqueFile.toPath());
      
      // Move the tmp file to the newly created directory.
      newPath = Files.move(newPath, newFile.toPath(), REPLACE_EXISTING);
    }
    catch(IOException ex){ ex.printStackTrace(); }    
    
    // Add the new path to database.
//    this.manager.addFile(newFile);
    
    //*** Validation: animal should be move to Trash. animal/animal_cat.txt should be in Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(newFile.getAbsolutePath());
//    assertNotNull(shelfDoc, String.format("[%s] should be in Shelf because [%s] is now a directory.", newFile.getAbsolutePath(), uniqueFile.getAbsolutePath()));
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
//    assertNotNull(trashDoc, String.format("[%s] should be in Trash because it is now a directory.", uniqueFile.getAbsolutePath()));

    //*** Clean up.
    try
    {
      FileUtils.deleteDirectory(uniqueFile);
    }
    catch(IOException ex){ ex.printStackTrace(); }
  }  
  
  @Test(description="File A is a duplicate of File B and File B exists in database.")
  public void markDuplicateFileBExistInDb()
  {
    // Add File B in database.
    File fileB = Data.createTempFile("markDuplicateFileBExistInDb_fileB");
    this.manager.addFile(fileB);
    
    // Create File A with different content(hash).
    File fileA = Data.createTempFile("markDuplicateFileBExistInDb_fileA");
    Data.copyFile(fileB, fileA);    
    Data.writeStringToFile(fileA, "new content");
    
    // Mark File A is a duplicate of File B.
    this.manager.markDuplicate(fileA, fileB);
    
    // Validate: File A is linked as duplicate to File B.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(fileB.getAbsolutePath());
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(fileA.getAbsolutePath());
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Shelf.uid should be equal to Trash.uid.\n"
                                                                  + "%s"
                                                                  + "\n"
                                                                  + "%s", fileA.getName(), fileB.getName(),
                                                                    shelfDoc.getInfo("Shelf"), trashDoc.getInfo("Trash")));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    
  }
  
  @Test(description="File A is a duplicate of File B but File B doesn't exist in database.")
  public void markDuplicateFileBNotExistInDb()
  {
    // Create File B.
    File fileB = Data.createTempFile("markDuplicateFileBNotExistInDb_fileB");
    
    // Create File A with different content(hash).
    File fileA = Data.createTempFile("markDuplicateFileBNotExistInDb_fileA");
    Data.copyFile(fileB, fileA);    
    Data.writeStringToFile(fileA, "new content");
    
    // Mark File A is a duplicate of File B.
    this.manager.markDuplicate(fileA, fileB);
    
    // Validate:
    //  1-File B is created in Shelf.
    //  2-File A is linked as duplicate of File B.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(fileB.getAbsolutePath());
    assertNotNull(shelfDoc, String.format("[%s] should be created in Shelf table.\n"
                                              + "%s", fileB.getName(), Data.getFileInfo(fileB, "File B to add in Shelf")));
    
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(fileA.getAbsolutePath());
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Shelf.uid should be equal to Trash.uid.\n"
                                                                  + "%s"
                                                                  + "\n"
                                                                  + "%s", fileA.getName(), fileB.getName(),
                                                                    shelfDoc.getInfo("Shelf"), trashDoc.getInfo("Trash")));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
  }
  
  @Test(description="File A is a duplicate of File B but files don't exist. Therefore, no commit.")
  public void markDuplicateFilesNotExist()
  {
    File duplicate = new File("./markDuplicateFilesNotExist_file_A_NotExist");
    File of        = new File("./markDuplicateFilesNotExist_file_B_NotExist");
    // Mark File A is a duplicate of File B.
    boolean commit = this.manager.markDuplicate(duplicate, of);
    
    assertFalse(commit, String.format("Should return false because %s and %s don't exist. Therefore, no commit.", duplicate.getName(), of.getName())); 
  }
  
  @Test(description="Mark File A to be a duplicate of File B but File A itself has a lot of duplicate entries and File B doesn't exist in database.")
  public void markDuplicateTrailingDuplicatesFileBNotInDb()
  {
    // **** Prepare data ****
    // Create duplicates for File A and add them in the database.
    ArrayList<File> fileAs = new ArrayList<File>();
    fileAs.add(Data.createTempFile("markDuplicateTrailingDuplicatesFileBNotInDb_fileA"));
    this.manager.addFile(fileAs.get(0));
    final int NUM_OF_DUPLICATES = 7;
    for(int i=0;i<NUM_OF_DUPLICATES;i++)
    {
      File tmpFileA = Data.createTempFile("markDuplicateTrailingDuplicatesFileBNotInDb_fileA_"+i);
      Data.copyFile(fileAs.get(0), tmpFileA);
      this.manager.addFile(tmpFileA);
      fileAs.add(tmpFileA);

    }
    
    // Create File B.
    File fileB = Data.createTempFile("markDuplicateTrailingDuplicatesFileBNotInDb_fileB");
    
    // **** Main test ****
    // Mark File A is a duplicate of File B.
    this.manager.markDuplicate(fileAs.get(0), fileB);
    
    // Validate:
    //  Duplicates of File A and itself are linked to File B as duplicates.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(fileB));
    Trash trash = new Trash();
    for(File fA: fileAs)
    {
      Document trashDoc = trash.getDocByFilename(fA.getName());
      assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Trash.uid should be equal to Shelf.uid.\n"
                                                                    + "%s"
                                                                    + "\n"
                                                                    + "%s", 
                                                                    fA.getName(), fileB.getName(),
                                                                    shelfDoc.getInfo("Shelf"),
                                                                    trashDoc.getInfo("Trash")));
      
    }

    // Clean up.
    fileB.delete();
    for(File f: fileAs)
    {
      f.delete();
    }
    
  }


  @Test(description="Mark File A to be a duplicate of File B but File A itself has a lot of duplicate entries and File B exists in database.")
  public void markDuplicateTrailingDuplicatesFileBInDb()
  {
    // **** Prepare data ****
    // Create duplicates for File A and add them in the database.
    ArrayList<File> fileAs = new ArrayList<File>();
    fileAs.add(Data.createTempFile("markDuplicateTrailingDuplicatesFileBInDb_fileA"));
    this.manager.addFile(fileAs.get(0));
    final int NUM_OF_DUPLICATES = 7;
    for(int i=0;i<NUM_OF_DUPLICATES;i++)
    {
      File tmpFileA = Data.createTempFile("markDuplicateTrailingDuplicatesFileBInDb_fileA_"+i);
      Data.copyFile(fileAs.get(0), tmpFileA);
      this.manager.addFile(tmpFileA);
      fileAs.add(tmpFileA);
    }
    
    // Add File B in database.
    File fileB = Data.createTempFile("markDuplicateTrailingDuplicatesFileBInDb_fileB");
    this.manager.addFile(fileB);
    
    // **** Main test ****
    // Mark File A is a duplicate of File B.
    this.manager.markDuplicate(fileAs.get(0), fileB);
 
    // Validate:
    //  Duplicates of File A and itself are linked to File B as duplicates.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(fileB));
    Trash trash = new Trash();
    for(File fA: fileAs)
    {
      Document trashDoc = trash.getDocByFilename(fA.getName());
      assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Trash.uid should be equal to Shelf.uid.\n"
                                                                    + "%s"
                                                                    + "\n"
                                                                    + "%s", 
                                                                    fA.getName(), fileB.getName(),
                                                                    shelfDoc.getInfo("Shelf"),
                                                                    trashDoc.getInfo("Trash")));
      
    }
      

    // Clean up.
    fileB.delete();
    for(File f: fileAs)
    {
      f.delete();
    }
    
  }
  
  @Test(description="File B is a duplicate of File C but now you want to mark File A to be a duplicate of File B.")
  public void markDuplicateOfDuplicate()
  {
    //*** Prepare data ****
    // Create File C.
    File fileC = Data.createTempFile("markDuplicateOfDuplicate_fileC");
    this.manager.addFile(fileC);
    
    // Create File B with different content(hash).
    File fileB = Data.createTempFile("markDuplicateOfDuplicate_fileB");
    Data.copyFile(fileC, fileB);    
    Data.writeStringToFile(fileB, "new content fileB");
    
    // Mark File B is a duplicate of File C.
    this.manager.markDuplicate(fileB, fileC);
    
    //*** Main test ****
    // Create File A with different content(hash).
    File fileA = Data.createTempFile("markDuplicateOfDuplicate_fileA");
    Data.copyFile(fileB, fileA);    
    Data.writeStringToFile(fileB, "new content fileA");
    
    // Mark File A is a duplicate of File B.
    this.manager.markDuplicate(fileA, fileB);
    
    // Validate: File A should be in Trash and has same UID of File C.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByFilename(fileC.getName());
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByFilename(fileA.getName());
    
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Trash.uid should be equal to Shelf.uid.\n"
                                                                    + "%s"
                                                                    + "\n"
                                                                    + "%s", 
                                                                    fileA.getName(), fileC.getName(),
                                                                    shelfDoc.getInfo("Shelf"),
                                                                    trashDoc.getInfo("Trash")));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    fileC.delete();
  }
  
  
  @Test(description="File A is a duplicate of File B in the database but now you want to mark File B to be the duplicate of File A")
  public void markDuplicateMainBecomeDuplicate()
  {
    
    //*** Prepare data: Make File A to be a duplicate of File B ****
    // Create File B and add to database.
    File fileB = Data.createTempFile("markDuplicateMainBecomeDuplicate_fileB");
    this.manager.addFile(fileB);
    
    // Copy File B to File A and add to database.
    File fileA = Data.createTempFile("markDuplicateMainBecomeDuplicate_fileA");
    Data.copyFile(fileB, fileA);
    this.manager.addFile(fileA);

    //*** Main test ****
    // Mark File B is a duplicate of File A.
    this.manager.markDuplicate(fileB, fileA);
    
    // Validate: File B should be in Trash and has same UID of File A.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByFilename(fileA.getName());
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByFilename(fileB.getName());
    
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Trash.uid should be equal to Shelf.uid.\n"
                                                                    + "%s"
                                                                    + "\n"
                                                                    + "%s", 
                                                                    fileB.getName(), fileA.getName(),
                                                                    shelfDoc.getInfo("Shelf"),
                                                                    trashDoc.getInfo("Trash")));
    
    // Validate: Check number of rows in Shelf and Trash table.
    final int expectedShelfRows = 1;
    final int expectedTrashRows = 1;
    int actualShelfRows = shelf.searchDocsByFilepath(Utils.getCanonicalPath(fileA)).size();
    int actualTrashRows = trash.searchDocsByFilepath(Utils.getCanonicalPath(fileB)).size();
    
    assertEquals(actualShelfRows, expectedShelfRows, "There should be only 1 row in Shelf table.");
    assertEquals(actualTrashRows, expectedTrashRows, "There should be only 1 row in Trash table.");
    
    // Clean up.
    fileA.delete();
    fileB.delete();
  }
  

  
  
}
