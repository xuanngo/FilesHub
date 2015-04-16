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
import java.util.Calendar;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.Is.is;


/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
public class ManagerAddFileTest
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
    if(ManagerAddFileTest.DEBUG)
    {
      try { Main.connection.setAutoCommit(true); }
      catch(SQLException ex) { ex.printStackTrace(); }
    }      
  }
  
 
  @Test(description="Add new unique file.")
  public void addFileUniqueFile()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileUniqueFile");
    this.manager.addFile(uniqueFile);

    // Validation:
    //  Check if file path exists in Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertNotNull(shelfDoc, String.format("Expected [%s] to be added in Shelf table but it is not.\n"
                                                + "%s"
                                                ,uniqueFile.getName(),
                                                Data.getFileInfo(uniqueFile, "File to add")
                                          ));
    
    // Clean up.
    uniqueFile.delete();
  }
  
  @Test(description="Add filename with special characters.")
  public void addFileSpecialCharacters()
  {
    String diffEncodingString = null;
    try
    {
      diffEncodingString = new String("�".getBytes("UTF-8"), "ISO-8859-1");
    }
    catch(UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }

    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileSpecialCharacters_"+"äöüß_一个人_"+ diffEncodingString);
    this.manager.addFile(uniqueFile);

    // Validation:
    //  Check if file path exists in Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertNotNull(shelfDoc, String.format("Expected [%s] to be added in Shelf table but it is not.\n"
                                                + "%s"
                                                ,uniqueFile.getName(),
                                                Data.getFileInfo(uniqueFile, "File to add")
                                          ));
    
    // Clean up.
    uniqueFile.delete();
  }  
  
  @Test(description="Add exact same file.")
  public void addFileExactSameFile()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileExactSameFile");
    this.manager.addFile(uniqueFile); // Add file 1st time.
    
    // Expected values:
    //   Regardless of how many times you add the exact same file, 
    //      no new row should be added to Shelf and Trash tables.
    ShelfExt shelfExt = new ShelfExt();
    final int expected_totalDocsShelf = shelfExt.getTotalDocs();
    TrashExt trashExt = new TrashExt();
    final int expected_totalDocsTrash = trashExt.getTotalDocs();
    
    // Add the exact same file the 2nd time.
    this.manager.addFile(uniqueFile); 

    // Actual values.
    final int actual_totalDocsShelf = shelfExt.getTotalDocs();
    final int actual_totalDocsTrash = trashExt.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, "No new row should be created in Shelf table.");
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, "No new row should be created in Trash table.");
    
    // Clean up.
    uniqueFile.delete();
  }
  
  @Test(description="Add file with same hash but different file name/path.")
  public void addFileWithSameHash()
  {
    //*** Prepare data: Create unique file.
    File uniqueFile = Data.createTempFile("addFileWithSameHash");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByFilename(uniqueFile.getName());
    
    //*** Main test: Copy unique file and then add to database.
    File duplicateFile = Data.createTempFile("addFileWithSameHash_duplicate_hash");
    Data.copyFile(uniqueFile, duplicateFile);
    
    // Add duplicate file to database.
    this.manager.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    //*** Validations: Check new hash is added in Trash.
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByHash(Utils.getHash(duplicateFile));
    assertNotNull(trashDoc, String.format("[%s] is not added in Trash table. It should.\n"
                                                      + "%s"
                                                      + "\n"
                                                      + "%s"
                                                      ,duplicateFile.getName(),
                                                      Data.getFileInfo(duplicateFile, "File to add"),
                                                      shelfDoc.getInfo("Shelf")
                                                 ));
    
    //*** Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
  }
  
  @Test(description="Add the same file in Shelf that has changed since FilesHub last ran.")
  public void addFileShelfFileContentChanged()
  {
    //*** Prepare data: Create a unique file and add it in database.   
    File uniqueFile = Data.createTempFile("addFileShelfFileContentChanged");
    this.manager.addFile(uniqueFile);
    Shelf shelf = new Shelf();
    Document oldShelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());

    //*** Main test: Add the exact same file again with new content.
    Data.writeStringToFile(uniqueFile, "new content");
    uniqueFile.setLastModified(System.currentTimeMillis()+1000); // Guarantee content update causes an update of File.lastmodified().
                                                                 //   All platforms support file-modification times to the nearest second
    this.manager.addFile(uniqueFile);

    //*** Validations: Since hash has changed, therefore the old entry will be moved from Shelf to Trash table.
    // Check hash match the new value.
    String newHash = Utils.getHash(uniqueFile);
    Document newShelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertEquals(newShelfDoc.hash, newHash, String.format("Content of file(%s) has changed. The new hash[%s] should be in Shelf table.", uniqueFile.getAbsolutePath(), newHash));
    
    // On content changed, old info should not be logged in Trash.
    Trash trash = new Trash(); 
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertThat(trashDoc, is(nullValue()));
    
    //*** Clean up.
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

    // Expected values:
    //   Regardless of how many times you add the exact same file with new content, 
    //      no new row should be added to Shelf and Trash tables.
    ShelfExt shelfExt = new ShelfExt();
    final int expected_totalDocsShelf = shelfExt.getTotalDocs();
    TrashExt trashExt = new TrashExt();
    final int expected_totalDocsTrash = trashExt.getTotalDocs();    
    
  
    // Add the exact same duplicated file again with new content.
    Data.writeStringToFile(duplicateFile, " new content");
    this.manager.addFile(duplicateFile);
    
    // Actual values.
    final int actual_totalDocsShelf = shelfExt.getTotalDocs();
    final int actual_totalDocsTrash = trashExt.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, "No new row should be created in Shelf table.");
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, "No new row should be created in Trash table.");
    
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
    ShelfExt shelfExt = new ShelfExt();
    final int expected_totalDocsShelf = shelfExt.getTotalDocs();
    TrashExt trashExt = new TrashExt();
    final int expected_totalDocsTrash = trashExt.getTotalDocs();    
    
    // Add the exact same file again.
    this.manager.addFile(duplicateFile);

    // Actual values.
    final int actual_totalDocsShelf = shelfExt.getTotalDocs();
    final int actual_totalDocsTrash = trashExt.getTotalDocs();
    
    // Validations
    assertEquals(actual_totalDocsShelf, expected_totalDocsShelf, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsShelf, actual_totalDocsShelf));
    assertEquals(actual_totalDocsTrash, expected_totalDocsTrash, String.format("The expected number of documents in Shelf is %d but it is %d. Expect to be equal.", expected_totalDocsTrash, actual_totalDocsTrash));
        
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();    
    
  }
  
  
  @Test(description="Add same filename but different content and different path.")
  public void addFileSameNameDiffContentNPath()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileSameNameDiffContentNPath");
    this.manager.addFile(uniqueFile);
    
    // Copied temporary file to another directory and add content to the copied file so it will have different content.
    Path tmpDirectoryPath = Data.createTempDir();
    File copiedFile = Data.copyFileToDirectory(uniqueFile, tmpDirectoryPath.toFile());
    Data.writeStringToFile(copiedFile, " new content "+System.currentTimeMillis());
    this.manager.addFile(copiedFile);
    
    // Validations:
    //  -A new row is created in Shelf for copiedFile.
    //  -No row is created in Trash.
    String hash = Utils.getHash(copiedFile);
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByHash(hash);
    assertEquals(shelfDoc.canonical_path, Utils.getCanonicalPath(copiedFile), String.format("Expected a row is added in Shelf table but it is not.\n"
                                            + "%s"
                                            + "\n"
                                            + "%s"
                                            , Data.getFileInfo(copiedFile, "File to add"),
                                            shelfDoc.getInfo("Return document from Shelf")));
    
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByHash(hash);
    assertNull(trashDoc, String.format("No new row should be created in Trash.\n%s", shelfDoc.getInfo("Unexpected Trash document returned.")));
    
    // Clean up.
    uniqueFile.delete();
    copiedFile.delete();
    tmpDirectoryPath.toFile().delete();

  }  
  
  @Test(description="Add same file from multiple paths.")
  public void addFileSameFileWithDiffPaths()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileSameFileWithDiffPaths");
    this.manager.addFile(uniqueFile);
    
    // Expected values:
    //  Total # of documents in Shelf should stay the same after the initial add of new file.
    //  Total # of documents in Trash should be added to the number of different paths.
    ShelfExt shelfExt = new ShelfExt();
    final int expectedTotalDocsShelf = shelfExt.getTotalDocs();
    TrashExt trashExt = new TrashExt();
    final int NUM_OF_DIFF_PATHS = 5;
    final int expectedTotalDocsTrash = trashExt.getTotalDocs()+NUM_OF_DIFF_PATHS;
    
    // Add same file from multiple paths
    for(int i=0; i<NUM_OF_DIFF_PATHS; i++)
    {
      Path tmpDirectoryPath = Data.createTempDir();
      File copiedFile = Data.copyFileToDirectory(uniqueFile, tmpDirectoryPath.toFile());
      this.manager.addFile(copiedFile);
      copiedFile.delete();
      tmpDirectoryPath.toFile().delete();
    }
    
    // Actual values.
    final int actualTotalDocsShelf = shelfExt.getTotalDocs();
    final int actualTotalDocsTrash = trashExt.getTotalDocs();
    
    // Validate that no unexpected row are added.
    assertEquals(actualTotalDocsShelf, expectedTotalDocsShelf, String.format("More rows are added in Shelf table than expected."));
    assertEquals(actualTotalDocsTrash, expectedTotalDocsTrash, String.format("More rows are added in Trash table than expected."));
    
    // Clean up.
    uniqueFile.delete();

  }   
  
  
  @Test(description="Add the file that have been moved.")
  public void addFileShelfFileMoved()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileShelfFileMoved");
    this.manager.addFile(uniqueFile);
    String originalCanonicalPath = Utils.getCanonicalPath(uniqueFile);
    
    // Move file to another directory.
    Path tmpDirectoryPath = Data.createTempDir();
    File fileMoved = Data.moveFileToDirectory(uniqueFile, tmpDirectoryPath.toFile(), false);
    String newCanonicalPath = Utils.getCanonicalPath(fileMoved);
    
    // Add moved file again.
    this.manager.addFile(fileMoved);
    
    // Validations:
    //  New location of the moved file should be update in Shelf table.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(newCanonicalPath);
    assertNotNull(shelfDoc, String.format("[%s] should be in Shelf table.", newCanonicalPath));
    
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(originalCanonicalPath);
    assertNotNull(trashDoc, String.format("[%s] should be in Trash table.", originalCanonicalPath));
    
    // Clean up.
    uniqueFile.delete();
    fileMoved.delete();
    tmpDirectoryPath.toFile().delete();

  }
  
  @Test(description="Add a duplicate file where the original file is deleted.")
  public void addFileShelfFileDeletedAddTrashFile()
  {
    // Add a unique file in database.
    File uniqueFile = Data.createTempFile("addFileShelfFileDeletedAddTrashFile");
    this.manager.addFile(uniqueFile);
    String originalCanonicalPath = Utils.getCanonicalPath(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileShelfFileDeletedAddTrashFile_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    this.manager.addFile(duplicateFile);
    String duplicateCanonicalPath = Utils.getCanonicalPath(duplicateFile);
    
    // Delete original file.
    uniqueFile.delete();
    
    // Add duplicate file again.
    this.manager.addFile(duplicateFile);
    
    // Validations:
    //  The duplicate file info should be move to Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(duplicateCanonicalPath);
    assertNotNull(shelfDoc, String.format("Duplicate file [%s] should be in Shelf table.", duplicateCanonicalPath));
    
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(originalCanonicalPath);
    assertNotNull(trashDoc, String.format("The original file [%s] should be in Trash table.", originalCanonicalPath));
    
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("trashDoc.uid = %d should be equal to shelfDoc.uid = %d", trashDoc.uid, shelfDoc.uid));
    
    // Clean up.
    duplicateFile.delete();

  }
  
  @Test(description="Add a duplicate of duplicate: Add file A but file A is a duplicate of file B and file B is mark a duplicate of file C.")
  public void addFileDuplicateOfDuplicate()
  {
    //*** Prepare data ****
    // Create File C.
    File fileC = Data.createTempFile("addFileDuplicateOfDuplicate_fileC");
    this.manager.addFile(fileC);
    
    // Create File B with different content(hash).
    File fileB = Data.createTempFile("addFileDuplicateOfDuplicate_fileB");
    Data.copyFile(fileC, fileB);    
    Data.writeStringToFile(fileB, "new content fileB");
    
    // Mark File B is a duplicate of File C.
    this.manager.markDuplicate(fileB, fileC);
    
    //*** Main test ****
    // Create File A with the same content as File B.
    File fileA = Data.createTempFile("markDuplicateOfDuplicate_fileA");
    Data.copyFile(fileB, fileA);    

    // Add File A in database
    this.manager.addFile(fileA);
    
    // Validate: File A should be in Trash and has the same UID of File C.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByFilename(fileC.getName());
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByFilename(fileA.getName());
    
    assertEquals(trashDoc.uid, shelfDoc.uid, String.format("[%s] is not linked to/duplicate of [%s]. Trash.uid should be equal to Shelf.uid.\n"
                                                                    + "%s"
                                                                    + "\n"
                                                                    + "%s", 
                                                                    fileA.getName(), fileC.getName(),
                                                                    shelfDoc.getInfo("Shelf: File C"),
                                                                    trashDoc.getInfo("Trash: File A")));        
    
  }
  
  @Test(description="Add a unique file that is deleted.", expectedExceptions={RuntimeException.class})
  public void addFileDelete()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileDelete");
    uniqueFile.delete();
    try
    {
      this.manager.addFile(uniqueFile);
    }
    finally
    {
      // Validate: File is deleted. Therefore, no commit in the database.
      String canonicalPath = Utils.getCanonicalPath(uniqueFile);
      Shelf shelf = new Shelf();
      Document shelfDoc = shelf.getDocByCanonicalPath(canonicalPath);
      Trash trash = new Trash();
      Document trashDoc = trash.getDocByCanonicalPath(canonicalPath);
      assertNull(shelfDoc, String.format("'%s' should not be in Shelf.", canonicalPath));
      assertNull(trashDoc, String.format("'%s' should not be in Trash.", canonicalPath));
      
    }
    
  }
  
  @Test(description="Add a duplicate file that is deleted.", expectedExceptions={RuntimeException.class})
  public void addFileDuplicateDelete()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileDuplicateDelete");
    String uniqueFilePath = Utils.getCanonicalPath(uniqueFile);
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileDuplicateDelete_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    String duplicateFilePath = Utils.getCanonicalPath(duplicateFile);
    duplicateFile.delete();
    
    try
    {
      this.manager.addFile(duplicateFile);
    }
    finally
    {
      // Validate: Duplicate file is deleted. Therefore, no commit in the database.
      Shelf shelf = new Shelf();
      Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFilePath);
      Trash trash = new Trash();
      Document trashDoc = trash.getDocByCanonicalPath(duplicateFilePath);
      assertEquals(shelfDoc.canonical_path, uniqueFilePath);
      assertNull(trashDoc, String.format("'%s' should not be in Trash.", duplicateFilePath));
    }
    
    // Clean up.
    uniqueFile.delete();
    
  }
  
  @Test(description="Add a deleted duplicate file that is already in the database.")
  public void addFileDuplicateDeleteInDB()
  {
    // Add a unique file.
    File uniqueFile = Data.createTempFile("addFileDuplicateDelete");
    String uniqueFilePath = Utils.getCanonicalPath(uniqueFile);
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database. This file is going to Trash table.
    File duplicateFile = Data.createTempFile("addFileDuplicateDelete_duplicate");
    Data.copyFile(uniqueFile, duplicateFile);
    String duplicateFilePath = Utils.getCanonicalPath(duplicateFile);
    this.manager.addFile(duplicateFile);
    
    duplicateFile.delete();    
    try
    {
      this.manager.addFile(duplicateFile);
    }
    finally
    {
      // Validate: Duplicate file is deleted. Therefore, no commit in the database.
      Shelf shelf = new Shelf();
      List<Document> shelfDocs = shelf.searchDocsByFilepath(uniqueFilePath);
      Trash trash = new Trash();
      List<Document> trashDocs = trash.searchDocsByFilepath(duplicateFilePath);
      assertEquals(shelfDocs.size(), 1, String.format("Expected 1 but found %d entries in Shelf table for %s.", shelfDocs.size(), uniqueFilePath));
      assertEquals(trashDocs.size(), 1, String.format("Expected 1 but found %d entries in Trash table for %s.", trashDocs.size(), duplicateFilePath));
    }
    
    // Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="Add file C but File B and C are already duplicate of A and A is deleted.")
  public void addFileTrailingDuplicateWithMainNotExists()
  {
    //*** Prepare data: Make File B, C to be a duplicate of File A ****
    // Create File A and add to database.
    File fileA = Data.createTempFile("addFileTrailingDuplicateWithMainNotExists_fileA");
    this.manager.addFile(fileA);
    
    // Copy File A to File B and add to database.
    File fileB = Data.createTempFile("addFileTrailingDuplicateWithMainNotExists_fileB");
    Data.copyFile(fileA, fileB);
    this.manager.addFile(fileB);
    
    // Copy File A to File C and add to database.
    File fileC = Data.createTempFile("addFileTrailingDuplicateWithMainNotExists_fileC");
    Data.copyFile(fileA, fileC);
    this.manager.addFile(fileC);
    
    //*** Main test ****
    fileA.delete();
    this.manager.addFile(fileC);
    
    //*** Validations: C should be in Shelf. A and B should be in Trash ****
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    
    Document shelfDocA = shelf.getDocByCanonicalPath(fileA.getAbsolutePath());
    Document trashDocA = trash.getDocByCanonicalPath(fileA.getAbsolutePath());
    assertNull(shelfDocA, String.format("%s should not be in Shelf because it is deleted. It should be moved to Trash.", fileA.getAbsolutePath()));
    assertNotNull(trashDocA, String.format("%s should be in Trash.", fileA.getAbsolutePath()));
    
    Document shelfDocB = shelf.getDocByCanonicalPath(fileB.getAbsolutePath());
    Document trashDocB = trash.getDocByCanonicalPath(fileB.getAbsolutePath());
    assertNull(shelfDocB, String.format("%s should not be in Shelf. It is a duplicate.", fileB.getAbsolutePath()));
    assertNotNull(trashDocB, String.format("%s should be in Trash. It is a duplicate.", fileB.getAbsolutePath()));
    
    Document shelfDocC = shelf.getDocByCanonicalPath(fileC.getAbsolutePath());
    Document trashDocC = trash.getDocByCanonicalPath(fileC.getAbsolutePath());
    assertNotNull(shelfDocC, String.format("%s should in Shelf. It was moved from Trash to Shelf.", fileC.getAbsolutePath()));
    assertNull(trashDocC, String.format("%s should not be in Trash. It was moved from Trash to Shelf.", fileC.getAbsolutePath()));
    
    // Clean up.
    fileB.delete();
    fileC.delete();
    
  }
  
  
  @Test(description="Add file D. A, B, C are duplicates but B & C have the same hash but different from A. D has same hash as B & C")
  public void addFileHashOnlyInTrash()
  {
    //*** Prepare data: Make File B, C to be a duplicate of File A ****
    // Create File A and add to database.
    File fileA = Data.createTempFile("addFileHashOnlyInTrash_fileA");
    this.manager.addFile(fileA);
    
    // Create File B and add to database.
    File fileB = Data.createTempFile("addFileHashOnlyInTrash_fileB");
    this.manager.addFile(fileB);
    
    // Copy File B to File C and add to database.
    File fileC = Data.createTempFile("addFileHashOnlyInTrash_fileC");
    Data.copyFile(fileB, fileC);
    this.manager.addFile(fileC);
    
    // Mark B & C to be duplicates of A.
    this.manager.markDuplicate(fileB, fileA); // File A and File B have different hashes.
    
    //*** Main test ****
    // Copy File B to File D and add to database.
    File fileD = Data.createTempFile("addFileHashOnlyInTrash_fileD");
    Data.copyFile(fileB, fileD);
    this.manager.addFile(fileD);

    // Validations: Check D is in Trash and has A's uid.
    Shelf shelf = new Shelf();
    Document shelfDocA = shelf.getDocByFilename(fileA.getName());
    Trash trash = new Trash();
    Document trashDocD = trash.getDocByFilename(fileD.getName());
    assertEquals(shelfDocA.uid, trashDocD.uid);
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    fileC.delete();
    fileD.delete();
    
  }
  
  @Test(description="Add file C. B is marked as duplicate of A but they don't have the same hash but A doesn't exist anymore. C has the same hash as B.")
  public void addFileDupInTrashMainDelete()
  {
    
    //*** Prepare data: Make File B to be duplicate of File A. ****
    // Create File A and add to database.
    File fileA = Data.createTempFile("addFileDupInTrashMainDelete_fileA");
    this.manager.addFile(fileA);
    
    // Create File B and mark it as duplicate of A.
    File fileB = Data.createTempFile("addFileDupInTrashMainDelete_fileB");
    this.manager.markDuplicate(fileB, fileA);
    
    //*** Main test: Delete fileA and add File C. ***
    fileA.delete();
    
    // Copy File B to File C and add to database.
    File fileC = Data.createTempFile("addFileDupInTrashMainDelete_fileC");
    Data.copyFile(fileB, fileC);
    this.manager.addFile(fileC);
    
    // Validations: File C should be in Shelf whereas File A is moved to Trash.
    Shelf shelf = new Shelf();
      Document shelfDocC = shelf.getDocByFilename(fileC.getName());
    Trash trash = new Trash();
      Document trashDocA = trash.getDocByFilename(fileA.getName());
    assertNotNull(shelfDocC, String.format("%s is not found in Shelf table. It should.", fileC.getName()));
    assertNotNull(trashDocA, String.format("%s is not found in Trash table. It should.", fileA.getName()));
    assertEquals(shelfDocC.uid, trashDocA.uid, String.format("%s.uid=%d should be the same as %s.uid=%d", 
                                                                fileC.getName(), shelfDocC.uid, fileA.getName(), trashDocA.uid));
    
    // Clean up.
    fileB.delete();
    fileC.delete();
  }
  
  @Test(groups={ "Extreme" }, description="Add file C having same hash as A & B but A & B are orphan duplicates in Trash table only.")
  public void addFileToOrphansTrash()
  {
    //*** Prepare data: Add duplicate files A & B directly to Trash table. ****
    // Add duplicate files A & B directly to Trash table.
    File fileA = Data.createTempFile("addFileToOrphansTrash_fileA");
    File fileB = Data.createTempFile("addFileToOrphansTrash_fileB");
    Data.copyFile(fileA, fileB);
    
    final int fakeDuid = atomicInt.get();
    Document trashDocA = new Document(fileA);
    Document trashDocB = new Document(fileB);
      trashDocA.uid = fakeDuid;
      trashDocB.uid = fakeDuid;
      trashDocA.hash = Utils.getHash(fileA);
      trashDocB.hash = Utils.getHash(fileB);
    Trash trash = new Trash();
    trash.addDoc(trashDocA);
    trash.addDoc(trashDocB);
    
    //*** Main test ***
    File fileC = Data.createTempFile("addFileToOrphansTrash_fileC");
    Data.copyFile(fileA, fileC);
    this.manager.addFile(fileC);

    // Validations: Check file C is in Shelf table and shouldn't be in Trash.
    Shelf shelf = new Shelf();
    Document shelfDocC = shelf.getDocByFilename(fileC.getName());
    Document trashDocC = trash.getDocByFilename(fileC.getName());
    assertNotNull(shelfDocC, String.format("%s not found in Shelf table. It should be added in Shelf table.", fileC.getAbsolutePath()));
    assertEquals(shelfDocC.uid, fakeDuid, String.format("%s should be added in Shelf table.", fileC.getAbsolutePath()));
    assertNull(trashDocC, String.format("%s should not be added in Trash table.", fileC.getAbsolutePath()));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    fileC.delete();
    
  }  
  
  @Test(description="Add a directory.", expectedExceptions={RuntimeException.class})
  public void addFileDirectory()
  {
    //*** Prepare data: Create a directory
    Path tmpDir = Data.createTempDir();
    try
    {
      //*** Main test: Add directory. It should throw an exception. ***
      this.manager.addFile(tmpDir.toFile());
      assertTrue(false, "This line should never be run. It should throw an exception."); 
    }
    finally
    {
      // Clean up.
      try
      {
        Files.deleteIfExists(tmpDir);
      }
      catch(IOException ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  @Test(description="Add empty file.")
  public void addFileEmpty()
  {
    //*** Prepare data: Create a empty file. 
    File uniqueFile = Data.createTempFile("addFileEmpty", null, "");
    assertEquals(uniqueFile.length(), 0, 
              String.format("File [%s] should be empty. Currently, filesize=%d.", 
                              uniqueFile.getName(), uniqueFile.length())); // Guarantee that it is an empty file.
    
    //*** Main test: Add empty file.
    this.manager.addFile(uniqueFile);
    
    //*** Validation: Empty file should exists in Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertNotNull(shelfDoc, String.format("Expected [%s] to be added in Shelf table but it is not.\n"
                                                + "%s"
                                                ,uniqueFile.getName(),
                                                Data.getFileInfo(uniqueFile, "File to add")
                                          ));

    //*** Clean up.
    uniqueFile.delete();    

  }  
  
  @Test(description="Add files renamed. E.g. Files renamed: Serie_17.txt should be Serie_18.txt and vice versa."
                      + "Assumed renaming causes an update of File.lastmodified().")
  public void addFileRenamedToExistingFilenames()
  {
    
    //*** Prepare data: Create files. 
    File serie_17 = Data.createTempFile("addFileRenamedToExistingFilenames_serie_17");
    File serie_18 = Data.createTempFile("addFileRenamedToExistingFilenames_serie_18");
    this.manager.addFile(serie_17);
    this.manager.addFile(serie_18);
    
    Shelf shelf = new Shelf();
    Document originalShelfDocSerie17 = shelf.getDocByCanonicalPath(serie_17.getAbsolutePath());
    Document originalShelfDocSerie18 = shelf.getDocByCanonicalPath(serie_18.getAbsolutePath());
        
    //*** Main test: Rename files and add them again in database.
    // Rename files(serie_17<->serie_18)
    File tmp_serie_17 = Data.createTempFile("addFileRenamedToExistingFilenames_tmp_serie_17");
    try
    {
      Files.move(serie_17.toPath(), tmp_serie_17.toPath(), REPLACE_EXISTING);
      Files.move(serie_18.toPath(), serie_17.toPath(), REPLACE_EXISTING);
      Files.move(tmp_serie_17.toPath(), serie_18.toPath(), REPLACE_EXISTING);     
    }
    catch(IOException ex){ ex.printStackTrace(); }
    
    // Guarantee content update causes an update of File.lastmodified().
    //   All platforms support file-modification times to the nearest second
    serie_17.setLastModified(System.currentTimeMillis()+1000);
    serie_18.setLastModified(System.currentTimeMillis()+1000);
    
    this.manager.addFile(serie_17);
    this.manager.addFile(serie_18);    
  
    //*** Validation: New filename should be in the Shelf and old filename should be in Trash.
    // Check hash.
    Document newShelfDocSerie18 = shelf.getDocByCanonicalPath(serie_18.getAbsolutePath());
    assertEquals(newShelfDocSerie18.hash, originalShelfDocSerie17.hash);
    Document newShelfDocSerie17 = shelf.getDocByCanonicalPath(serie_17.getAbsolutePath());
    assertEquals(newShelfDocSerie17.hash, originalShelfDocSerie18.hash);

    // Check no trash entry created.
    Trash trash = new Trash();
    Document trashDocSerie17 = trash.getDocByCanonicalPath(serie_17.getAbsolutePath());
    Document trashDocSerie18 = trash.getDocByCanonicalPath(serie_18.getAbsolutePath());
    assertThat(trashDocSerie17, is(nullValue()));
    assertThat(trashDocSerie18, is(nullValue()));
    
    //*** Clean up.
    serie_17.delete();
    serie_18.delete();
    tmp_serie_17.delete();

  }
  
  @Test(description="Add file turned directory. "
                    + "(1) Add 'animal' file."
                    + "(2) Rename 'animal' file to 'animal_cat.txt' ."
                    + "(3) Create directory called 'animal'."
                    + "(4) Move 'animal_cat.txt' file to 'animal' directory."
                    + "(5) End result: animal/animal_cat.txt"
                    )
  public void addFileShelfTurnedDirectory()
  {
    //*** Prepare data: Create a unique file. 
    File uniqueFile = Data.createTempFile("addFileShelfTurnedDirectory");
    this.manager.addFile(uniqueFile);
    
    //*** Main test: Rename the file. Create a directory using the exact same file path. Add the renamed file to the new directory. 
    File tmpUniqueFile = Data.createTempFile("addFileShelfTurnedDirectory_tmp");
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
    this.manager.addFile(newFile);
    
    //*** Validation: animal should be move to Trash. animal/animal_cat.txt should be in Shelf.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(newFile.getAbsolutePath());
    assertNotNull(shelfDoc, String.format("[%s] should be in Shelf because [%s] is now a directory.", newFile.getAbsolutePath(), uniqueFile.getAbsolutePath()));
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertNotNull(trashDoc, String.format("[%s] should be in Trash because it is now a directory.", uniqueFile.getAbsolutePath()));

    //*** Clean up.
    try
    {
      FileUtils.deleteDirectory(uniqueFile);
    }
    catch(IOException ex){ ex.printStackTrace(); }
  }
  
  @Test(description="Add duplicate Shelf file turned directory.")
  public void addFileShelfTurnedDirectoryDuplicate()
  {
    //*** Prepare data:
    File uniqueFile = Data.createTempFile("addFileShelfTurnedDirectoryDuplicate");
    this.manager.addFile(uniqueFile);
    File duplicateFile = Data.createTempFile("addFileShelfTurnedDirectoryDuplicate_duplicate_diff_hash");
    this.manager.markDuplicate(duplicateFile, uniqueFile);
    
    //*** Main test:
    // Delete unique file and make a directory with the same name.
    uniqueFile.delete();
    try { Files.createDirectory(uniqueFile.toPath()); } catch(IOException ex) { ex.printStackTrace(); }
    // Add the duplicate file in database again.
    this.manager.addFile(duplicateFile);
    
    
    //*** Validation: Duplicate file is now moved from Trash to Shelf. Shelf entry is moved to Trash because it has now a directory(not valid file anymore).
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(duplicateFile.getAbsolutePath());
    assertNotNull(shelfDoc, String.format("[%s] should be moved from Trash to Shelf.", duplicateFile.getAbsolutePath()));
    assertEquals(shelfDoc.canonical_path, Utils.getCanonicalPath(duplicateFile), String.format("[%s] should be moved from Trash to Shelf.", duplicateFile.getAbsolutePath()));
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertNotNull(trashDoc, String.format("[%s] should be moved from Shelf to Trash.", uniqueFile.getAbsolutePath()));
    assertEquals(trashDoc.canonical_path, Utils.getCanonicalPath(uniqueFile), String.format("[%s] should be moved from Shelf to Trash because it is now a directory.", uniqueFile.getAbsolutePath()));
    
    
    //*** Clean up.
    duplicateFile.delete();
    try
    {
      FileUtils.deleteDirectory(uniqueFile);
    }
    catch(IOException ex){ ex.printStackTrace(); }    
  }  
  
  @Test(description="Add duplicate different path Shelf file turned directory.")
  public void addFileShelfTurnedDirectoryDuplicateDiffPath()
  {
    //*** Prepare data:
    File uniqueFile = Data.createTempFile("addFileShelfTurnedDirectoryDuplicateDiffPath");
    this.manager.addFile(uniqueFile);
    File duplicateFile = Data.createTempFile("addFileShelfTurnedDirectoryDuplicateDiffPath_duplicate_diff_hash");
    this.manager.markDuplicate(duplicateFile, uniqueFile);
    
    //*** Main test:
    // Delete unique file and make a directory with the same name.
    uniqueFile.delete();
    try { Files.createDirectory(uniqueFile.toPath()); } catch(IOException ex) { ex.printStackTrace(); }
    File newDuplicateFile = Data.createTempFile("addFileShelfTurnedDirectoryDuplicateDiffPath_duplicate_diff_path");
    Data.copyFile(duplicateFile, newDuplicateFile);    
    // Add the newDuplicateFile file in database again.
    this.manager.addFile(newDuplicateFile);
    
    
    //*** Validation: Duplicate file is now moved from Trash to Shelf. Shelf entry is moved to Trash because it has now a directory(not valid file anymore).
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(newDuplicateFile.getAbsolutePath());
    assertNotNull(shelfDoc, String.format("[%s] should be moved from Trash to Shelf.", newDuplicateFile.getAbsolutePath()));
    assertEquals(shelfDoc.canonical_path, Utils.getCanonicalPath(newDuplicateFile), String.format("[%s] should be moved from Trash to Shelf.", newDuplicateFile.getAbsolutePath()));
    Trash trash = new Trash();
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertNotNull(trashDoc, String.format("[%s] should be moved from Shelf to Trash.", uniqueFile.getAbsolutePath()));
    assertEquals(trashDoc.canonical_path, Utils.getCanonicalPath(uniqueFile), String.format("[%s] should be moved from Shelf to Trash because it is now a directory.", uniqueFile.getAbsolutePath()));
  
    
    //*** Clean up.
    duplicateFile.delete();
    newDuplicateFile.delete();
    try
    {
      FileUtils.deleteDirectory(uniqueFile);
    }
    catch(IOException ex){ ex.printStackTrace(); }    
  }  
  
  @Test(description="Add the same file with different lastmodified time.")
  public void addFileSameFileDiffLastModifiedTime()
  {
    //*** Prepare data: Create a unique file and add it in database.   
    File uniqueFile = Data.createTempFile("addFileSameFileDiffLastModifiedTime");
    this.manager.addFile(uniqueFile);
    
    //*** Main test: Add the exact same file again with different last modified time.
    uniqueFile.setLastModified(System.currentTimeMillis()+1000); // Guarantee content update causes an update of File.lastmodified().
                                                                 //   All platforms support file-modification times to the nearest second
    this.manager.addFile(uniqueFile);

    //*** Validations: Check last_modified is updated in Shelf and no new entry is added in Trash.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertEquals(shelfDoc.last_modified, uniqueFile.lastModified(), String.format("File modification time has changed. The new time[%d] should be in Shelf table.", uniqueFile.lastModified()));
    Trash trash = new Trash(); 
    Document trashDoc = trash.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    assertNull(trashDoc, String.format("%s should not be found in Trash table.", uniqueFile.getAbsolutePath()));
    
    //*** Clean up.
    uniqueFile.delete();    
    
  }
  
  /**********************************************************************************************
   * Case: Content/hash changed to be exactly the same as another entry.
   * Initial step:
   *    - File A => size = 1234
   *    - File B => size = 0
   * 
   * Next step:
   *    - File A => size = 0 [CHANGED]
   *    - File B => size = 0
   * 
   * End result:
   *    - File A should be a duplicate of File B.
   ***********************************************************************************************/
  @Test(description="Shelf: Hash changed to match another entry.")
  public void addFileHashChangedMatchOtherEntryShelf()
  {
    //*** Main variables:
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    
    //*** Prepare data: Add 2 unique files.
    File fileA = Data.createTempFile("addFileHashChangedMatchOtherEntryShelf_fileA");
    File fileB = Data.createTempFile("addFileHashChangedMatchOtherEntryShelf_fileB");
    this.manager.addFile(fileA);
    this.manager.addFile(fileB);
    
    //*** Main test: Content of file A changed to match exactly the same as content of file B.
    Data.copyFile(fileB, fileA);
    fileA.setLastModified(System.currentTimeMillis()+1000); // Let's be more realistic. File modified => timestamp changed.
    this.manager.addFile(fileA);
    
    //*** Validations: FileA should be a duplicate of FileB.
    // - No entry of File A should be Shelf anymore.
    Document shelfDocA = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(fileA)); 
    assertThat(shelfDocA, is(nullValue()));

    // - Check File A is a duplicate of File B: Trash.FileA.duid = Shelf.FileB.uid
    Document trashDocA = trash.getDocByCanonicalPath(Utils.getCanonicalPath(fileA));
    Document shelfDocB = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(fileB));
    assertThat(trashDocA.uid, equalTo(shelfDocB.uid));
    
    //*** Clean up.
    fileA.delete();
    fileB.delete();
  }
  
  @Test(description="Add duplicate with different last modified time.")
  public void addFileDuplicateFilesWithDiffLastModifiedTime()
  {
    //*** Main variables:
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    
    //*** Prepare data: Add 2 duplicate files.
    File file = Data.createTempFile("addFileDuplicateFilesWithDiffLastModifiedTime");
    this.manager.addFile(file);
    File duplicateFile = Data.createTempFile("addFileDuplicateFilesWithDiffLastModifiedTime_copy");
    Data.copyFile(file, duplicateFile);    
    this.manager.addFile(duplicateFile);
    
    //*** Main test: Change Last modified time of duplicate file.
    file.setLastModified(System.currentTimeMillis()+1000); // Let's be more realistic. File modified => timestamp changed.
    duplicateFile.setLastModified(System.currentTimeMillis()+1000); // Let's be more realistic. File modified => timestamp changed.
    this.manager.addFile(file);
    this.manager.addFile(duplicateFile);
    
    //*** Validations: 
    Document shelfDoc = shelf.getDocByCanonicalPath(Utils.getCanonicalPath(file)); 
    Document trashDoc = trash.getDocByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    assertThat(shelfDoc.last_modified, is(equalTo(file.lastModified())));
    
    //@TODO: Manager.add(): update trash entry if it is changed.
    //assertThat(trashDoc.last_modified, is(equalTo(duplicateFile.lastModified())));
    
    //*** Clean up.
    file.delete();
    duplicateFile.delete();
 
  }
}
