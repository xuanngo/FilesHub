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
import static org.testng.Assert.assertNotNull;



// Java Library
import java.io.File;
import java.io.IOException;

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
    
    File uniqueFile = Data.createUniqueFile("AddUniqueFile");
    ResultDocSet resultDocSet = this.manager.addFile(uniqueFile);
    uniqueFile.delete();
    
    assertEquals(resultDocSet.status, ResultDocSet.DIFF_PATH_DIFF_HASH,
        String.format("[%s] is unique. Status should be %d.", Utils.getCanonicalPath(uniqueFile), resultDocSet.status));
  }
  
  @Test(description="Add exact same file.")
  public void addExactSameFile()
  {
    File uniqueFile = Data.createUniqueFile("AddExactSameFile");
    this.manager.addFile(uniqueFile); // Add file 1st time.
    
    ResultDocSet resultDocSet = this.manager.addFile(uniqueFile); // Add the exact same file the 2nd time.
    uniqueFile.delete();
    
    assertEquals(resultDocSet.status, ResultDocSet.EXACT_SAME_FILE,
        String.format("[%s] already exists in database with the same path. Status should be %d."
                            + "uid = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.document.filename, resultDocSet.status, 
                                resultDocSet.document.uid, resultDocSet.document.canonical_path, resultDocSet.document.hash));
  }
  
  @Test(description="Add file with existing hash but different file name/path.")
  public void addFileWithSameHash()
  {
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHash");
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database.
    File duplicateFile = null;
    try
    {
      duplicateFile = File.createTempFile("AddFileWithSameHash_duplicate_hash_", null);
      FileUtils.copyFile(uniqueFile, duplicateFile);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    ResultDocSet resultDocSet = this.manager.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    // Validate
    assertEquals(resultDocSet.status, ResultDocSet.DIFF_PATH_SAME_HASH,
        String.format("[%s] already exists in database with the same hash. Status should be %d."
                            + "uid = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.document.filename, resultDocSet.status, 
                                resultDocSet.document.uid, resultDocSet.document.canonical_path, resultDocSet.document.hash));    
  }
  

  @Test(description="Add file with existing hash but different file path and check Trash table.")
  public void addFileWithSameHashCheckTrash()
  {
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHashCheckTrash");
    this.manager.addFile(uniqueFile);
    
    // Copy unique file and then add to database.
    File duplicateFile = null;
    try
    {
      duplicateFile = File.createTempFile("AddFileWithSameHashCheckTrash_duplicate_hash_", null);
      FileUtils.copyFile(uniqueFile, duplicateFile);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    ResultDocSet resultDocSet = this.manager.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    Trash trash = new Trash();
    int actual_duid = trash.getDuidByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    assertEquals(actual_duid, resultDocSet.document.uid, String.format("Shelf.uid=%d should be equal to Trash.duid=%d", resultDocSet.document.uid, actual_duid));
  }  
  
  @Test(description="Add the same file that has changed since FilesHub last ran.")
  public void addFileChangedSinceLastRun()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createUniqueFile("AddFileChangedSinceLastRun");
    long expected_trash_last_modified = uniqueFile.lastModified();
    this.manager.addFile(uniqueFile);
    
    // Update the unique file.
    try { FileUtils.touch(uniqueFile); } catch(IOException e){ e.printStackTrace(); }
    long expected_repo_last_modified = uniqueFile.lastModified();
    
    // Add the exact same file again with new last modified time.
    ResultDocSet resultDocSet = this.manager.addFile(uniqueFile);
    
    // Simple status check:
    assertEquals(resultDocSet.status, ResultDocSet.SAME_PATH_DIFF_HASH,
        String.format("[%s] already exists in database with the same hash. Status should be %d."
                            + "File to add:"
                            + "last_modified = %d\n"
                            + "canonical_path = %s\n"
                            
                            + "\n"
                            + "Shelf:"
                            + "uid = %d\n"
                            + "last_modified = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.file.getName(), resultDocSet.status, 
                                resultDocSet.file.lastModified(), Utils.getCanonicalPath(resultDocSet.file),
                                resultDocSet.document.uid, resultDocSet.document.last_modified, resultDocSet.document.canonical_path, resultDocSet.document.hash));       
    
    // Testing: Check old last modified time is moved to Trash table and new last modified time is in Manager table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc.last_modified, expected_trash_last_modified, "Check last modified time in Trash table.");
    
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByUid(resultDocSet.document.uid);
    assertNotNull(shelfDoc, String.format("Row in Manager table not found. Expected row: uid=%d, %s", resultDocSet.document.uid, Utils.getCanonicalPath(uniqueFile)));
    assertEquals(shelfDoc.last_modified, expected_repo_last_modified, "Check last modified time in Manager table.");
    
    // Clean up.
    uniqueFile.delete();    
    
  }
  
}
