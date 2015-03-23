package net.xngo.fileshub.test.upgrade;

import java.util.List;
import java.io.File;



import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.test.helpers.FHSampleDb;
import net.xngo.fileshub.upgrade.Upgrade;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.lang.StringUtils;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class Version0003Test
{
  private FHSampleDb fhSampleDb = new FHSampleDb();
  
  private Shelf shelf = new Shelf();
  private Trash trash = new Trash();  
  
  public Version0003Test()
  {
    Upgrade upgrade = new Upgrade();
    upgrade.run();
    
    // Need to open connection because Flywaydb closes it after Upgrade is ran.
    Main.connection = new Connection(); 
  }
  
  @BeforeTest
  public void createSampleData()
  {
    this.fhSampleDb.createSampleData();
  }
  
  @AfterTest
  public void deleteData()
  {
    this.fhSampleDb.deleteData();
  }
  
  @Test(description="Test all existing files has correct MD5 hash.")
  public void runMD5HashMatch()
  {
    List<Document> shelfDocsList = this.shelf.getDocs();
    for(Document shelfDoc: shelfDocsList)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        String actualHash = shelfDoc.hash;
        String expectedHash = Utils.getHash(file);
        
        // Check hash is in hexadecimal form.
        assertTrue(StringUtils.isHex(actualHash));
        
        // Check hash is 32 characters long.
        assertEquals(actualHash.length(), 32);
        assertEquals(expectedHash.length(), 32);
        
        
        // Main check: Check database hash is equal to file hash.
        assertEquals(actualHash, expectedHash, shelfDoc.getInfo("MD5 Hash doesn't match:"));
      }
      
    }
  }
  
}
