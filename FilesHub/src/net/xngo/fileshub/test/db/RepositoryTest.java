package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Repository;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.PairFile;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.ResultDocSet;
import net.xngo.fileshub.Utils;

// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;



// TestNG
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;



// Java Library
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class RepositoryTest
{
  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    Database db = new Database();
    db.create();
  }
  
  @Test(description="Add new unique file.")
  public void AddUniqueFile()
  {
    Repository repository = new Repository();
    
    File uniqueFile = Data.createUniqueFile("AddUniqueFile");
    ResultDocSet resultDocSet = repository.addFile(uniqueFile);
    uniqueFile.delete();
    
    assertEquals(resultDocSet.status, ResultDocSet.DIFF_PATH_DIFF_HASH,
        String.format("[%s] is unique. Status should be %d.", Utils.getCanonicalPath(uniqueFile), resultDocSet.status));
  }
  
  @Test(description="Add exact same file.")
  public void AddExactSameFile()
  {
    Repository repository = new Repository();
    
    File uniqueFile = Data.createUniqueFile("AddExactSameFile");
    repository.addFile(uniqueFile); // Add file 1st time.
    
    ResultDocSet resultDocSet = repository.addFile(uniqueFile); // Add the exact same file the 2nd time.
    uniqueFile.delete();
    
    assertEquals(resultDocSet.status, ResultDocSet.EXACT_SAME_FILE,
        String.format("[%s] already exists in database with the same path. Status should be %d."
                            + "uid = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.shelfDoc.filename, resultDocSet.status, 
                                resultDocSet.shelfDoc.uid, resultDocSet.shelfDoc.canonical_path, resultDocSet.shelfDoc.hash));
  }
  
  @Test(description="Add file with existing hash but different file name/path.")
  public void AddFileWithSameHash()
  {
    Repository repository = new Repository();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHash");
    repository.addFile(uniqueFile);
    
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
    
    ResultDocSet resultDocSet = repository.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    // Validate
    assertEquals(resultDocSet.status, ResultDocSet.DIFF_PATH_SAME_HASH,
        String.format("[%s] already exists in database with the same hash. Status should be %d."
                            + "uid = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.shelfDoc.filename, resultDocSet.status, 
                                resultDocSet.shelfDoc.uid, resultDocSet.shelfDoc.canonical_path, resultDocSet.shelfDoc.hash));    
  }
  

  @Test(description="Add file with existing hash but different file path and check Trash table.")
  public void AddFileWithSameHashCheckTrash()
  {
    Repository repository = new Repository();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHashCheckTrash");
    repository.addFile(uniqueFile);
    
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
    
    ResultDocSet resultDocSet = repository.addFile(duplicateFile); // Add duplicate file with different file name/path.
    
    Trash trash = new Trash();
    int actual_duid = trash.getDuidByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    assertEquals(actual_duid, resultDocSet.shelfDoc.uid, String.format("Repository.uid=%d should be equal to Trash.duid=%d", resultDocSet.shelfDoc.uid, actual_duid));
  }  
  
  @Test(description="Add the same file that has changed since FilesHub last ran.")
  public void AddFileChangedSinceLastRun()
  {
    Repository repository = new Repository();
    
    // Add unique file in Repository.
    File uniqueFile = Data.createUniqueFile("AddFileChangedSinceLastRun");
    long expected_trash_last_modified = uniqueFile.lastModified();
    repository.addFile(uniqueFile);
    
    // Update the unique file.
    try { FileUtils.touch(uniqueFile); } catch(IOException e){ e.printStackTrace(); }
    long expected_repo_last_modified = uniqueFile.lastModified();
    
    // Add the exact same file again with new last modified time.
    ResultDocSet resultDocSet = repository.addFile(uniqueFile);
    
    // Simple status check:
    assertEquals(resultDocSet.status, ResultDocSet.SAME_PATH_DIFF_HASH,
        String.format("[%s] already exists in database with the same hash. Status should be %d."
                            + "Shelf:"
                            + "uid = %d\n"
                            + "last_modified = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            
                            + "Trash:"
                            + "uid = %d\n"
                            + "last_modified = %d\n"
                            + "canonical_path = %s\n"
                            + "hash = %s\n"
                            , resultDocSet.shelfDoc.filename, resultDocSet.status, 
                                resultDocSet.shelfDoc.uid, resultDocSet.shelfDoc.last_modified, resultDocSet.shelfDoc.canonical_path, resultDocSet.shelfDoc.hash,
                                resultDocSet.trashDoc.uid, resultDocSet.trashDoc.last_modified, resultDocSet.trashDoc.canonical_path, resultDocSet.trashDoc.hash));       
    
    // Testing: Check old last modified time is moved to Trash table and new last modified time is in Repository table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc.last_modified, expected_trash_last_modified, "Check last modified time in Trash table.");
    
    Document repositoryDoc = repository.findDocByUid(resultDocSet.shelfDoc.uid);
    assertNotNull(repositoryDoc, String.format("Row in Repository table not found. Expected row: uid=%d, %s", resultDocSet.shelfDoc.uid, Utils.getCanonicalPath(uniqueFile)));
    assertEquals(repositoryDoc.last_modified, expected_repo_last_modified, "Check last modified time in Repository table.");
    
    // Clean up.
    uniqueFile.delete();    
    
  }
  
}
