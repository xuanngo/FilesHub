package net.xngo.fileshub.test.db;

// FilesHub classes.
import net.xngo.fileshub.db.Document;
import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.Utils;

// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;


// TestNG
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.assertTrue;


// Java Library
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DocumentTest
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
    Document doc = new Document();
    
    File uniqueFile = Data.createUniqueFile();
    int generalKey = doc.addFile(uniqueFile);
    uniqueFile.delete();
    
    if(generalKey!=0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] is unique. Generated key should not be equal to 0.", Utils.getCanonicalPath(uniqueFile)));
  }
  
  @Test(description="Add exact file.")
  public void AddExactFile()
  {
    Document doc = new Document();
    
    File uniqueFile = Data.createUniqueFile();
    doc.addFile(uniqueFile); // Add file 1st time.
    
    int generalKey = doc.addFile(uniqueFile); // Add the exact same file the 2nd time.
    uniqueFile.delete();
    if(generalKey==0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s] already exists in database. Generated key should be equal to 0.", Utils.getCanonicalPath(uniqueFile)));
  }
  
  @Test(description="Add file with existing hash but different filename.")
  public void AddFileWithSameHash()
  {
    Document doc = new Document();
    
    // Add unique file.
    File uniqueFile = Data.createUniqueFile();
    doc.addFile(uniqueFile);
    
    // Copy unique file and then add the copy.
    File duplicateFile = null;
    try
    {
      duplicateFile = File.createTempFile("duplicate_same_hash", null);
      FileUtils.copyFile(uniqueFile, duplicateFile);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    int generalKey = doc.addFile(duplicateFile); // Add duplicate file.
    
    // Clean up.
    uniqueFile.delete();
    duplicateFile.delete();
    
    // Validate
    if(generalKey==0)
      assertTrue(true);
    else
      assertTrue(false, String.format("[%s]'s hash already exists in database. Generated key should be equal to 0.", Utils.getCanonicalPath(uniqueFile)));
  }  
  
}
