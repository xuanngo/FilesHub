package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Repository;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.PairFile;
import net.xngo.fileshub.struct.Document;
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
    int generalKey = repository.addFile(uniqueFile).uid;
    uniqueFile.delete();
    
    if(generalKey > 0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] is unique. Generated key should be greater than 0. But it returned generalKey=%d.", Utils.getCanonicalPath(uniqueFile), generalKey));
  }
  
  @Test(description="Add exact file.")
  public void AddExactFile()
  {
    Repository repository = new Repository();
    
    File uniqueFile = Data.createUniqueFile("AddExactFile");
    repository.addFile(uniqueFile); // Add file 1st time.
    
    int generalKey = repository.addFile(uniqueFile).uid; // Add the exact same file the 2nd time.
    uniqueFile.delete();
    if(generalKey==PairFile.EXACT_SAME_FILE)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] already exists in database. Generated key should be equal to %d. But it returned generalKey=%d.", Utils.getCanonicalPath(uniqueFile), PairFile.EXACT_SAME_FILE, generalKey));
  }
  
  @Test(description="Add file with existing hash but different filename.")
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
    
    int generalKey = repository.addFile(duplicateFile).uid; // Add duplicate file.
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    // Validate
    if(generalKey==0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s]'s hash already exists in database. Generated key should be equal to 0.", Utils.getCanonicalPath(uniqueFile)));
  }
  

  @Test(description="Add file with existing hash but different filename and check Trash table.")
  public void AddFileWithSameHashCheckDuplicate()
  {
    Repository repository = new Repository();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHashCheckDuplicate");
    int expected_duid = repository.addFile(uniqueFile).uid;
    
    // Copy unique file and then add to database.
    File duplicateFile = null;
    try
    {
      duplicateFile = File.createTempFile("AddFileWithSameHashCheckDuplicate_duplicate_hash_", null);
      FileUtils.copyFile(uniqueFile, duplicateFile);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    repository.addFile(duplicateFile); // Add duplicate file.
    Trash trash = new Trash();
    int actual_duid = trash.getDuidByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    assertEquals(actual_duid, expected_duid, String.format("Repository.uid=%d should be equal to Trash.duid=%d", expected_duid, actual_duid));
  }  
  
  @Test(description="Add the same file that has changed since FilesHub last ran.")
  public void AddFileChangedSinceLastRun()
  {
    Repository repository = new Repository();
    
    // Add unique file in Repository.
    File uniqueFile = Data.createUniqueFile("AddFileChangedSinceLastRun");
    long expected_trash_last_modified = uniqueFile.lastModified();
    int duid = repository.addFile(uniqueFile).uid;
    
    // Update the unique file.
    try { FileUtils.touch(uniqueFile); } catch(IOException e){ e.printStackTrace(); }
    long expected_repo_last_modified = uniqueFile.lastModified();
    
    // Add the exact same file again with new last modified time.
    repository.addFile(uniqueFile);
    
    // Testing: Check old last modified time is moved to Trash table and new last modified time is in Repository table.
    Trash trash = new Trash();
    Document trashDoc = trash.findDocumentByCanonicalPath(Utils.getCanonicalPath(uniqueFile));
    assertEquals(trashDoc.last_modified, expected_trash_last_modified, "Check last modified time in Trash table.");
    
    Document repositoryDoc = repository.findDocumentByUid(duid);
    assertNotNull(repositoryDoc, String.format("Row in Repository table not found. Expected row: uid=%d, %s", duid, Utils.getCanonicalPath(uniqueFile)));
    assertEquals(repositoryDoc.last_modified, expected_repo_last_modified, "Check last modified time in Repository table.");
    
    // Clean up.
    uniqueFile.delete();    
    
  }
}
