package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Repository;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.PairFile;
import net.xngo.fileshub.Utils;

// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;



// TestNG
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;



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
    Repository doc = new Repository();
    
    File uniqueFile = Data.createUniqueFile("AddUniqueFile");
    int generalKey = doc.addFile(uniqueFile).uid;
    uniqueFile.delete();
    
    if(generalKey > 0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] is unique. Generated key should be greater than 0. But it returned generalKey=%d.", Utils.getCanonicalPath(uniqueFile), generalKey));
  }
  
  @Test(description="Add exact file.")
  public void AddExactFile()
  {
    Repository doc = new Repository();
    
    File uniqueFile = Data.createUniqueFile("AddExactFile");
    doc.addFile(uniqueFile); // Add file 1st time.
    
    int generalKey = doc.addFile(uniqueFile).uid; // Add the exact same file the 2nd time.
    uniqueFile.delete();
    if(generalKey==PairFile.EXACT_SAME_FILE)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] already exists in database. Generated key should be equal to %d. But it returned generalKey=%d.", Utils.getCanonicalPath(uniqueFile), PairFile.EXACT_SAME_FILE, generalKey));
  }
  
  @Test(description="Add file with existing hash but different filename.")
  public void AddFileWithSameHash()
  {
    Repository doc = new Repository();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHash");
    doc.addFile(uniqueFile);
    
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
    
    int generalKey = doc.addFile(duplicateFile).uid; // Add duplicate file.
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    // Validate
    if(generalKey==0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s]'s hash already exists in database. Generated key should be equal to 0.", Utils.getCanonicalPath(uniqueFile)));
  }
  

  @Test(description="Add file with existing hash but different filename and check Duplicate table.")
  public void AddFileWithSameHashCheckDuplicate()
  {
    Repository doc = new Repository();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile("AddFileWithSameHashCheckDuplicate");
    int expected_duid = doc.addFile(uniqueFile).uid;
    
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
    
    doc.addFile(duplicateFile); // Add duplicate file.
    Trash dup = new Trash();
    int actual_duid = dup.getDuidByCanonicalPath(Utils.getCanonicalPath(duplicateFile));
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    assertEquals(actual_duid, expected_duid, String.format("Document.uid=%d should be equal to Duplicate.duid=%d", expected_duid, actual_duid));
  }  
  
}
