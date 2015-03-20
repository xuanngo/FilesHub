package net.xngo.fileshub.test.upgrade;

import java.util.List;
import java.io.File;




import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.test.helpers.FHSampleDb;
import net.xngo.fileshub.upgrade.Upgrade;
import net.xngo.fileshub.struct.Document;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class Version0002Test
{
  private FHSampleDb fhSampleDb = new FHSampleDb();
  
  private Shelf shelf = new Shelf();
  private Trash trash = new Trash();  
  
  public Version0002Test()
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
  
  @Test(description="Test all existing files has correct file size.")
  public void runFileSizeMatch()
  {
    List<Document> shelfDocsList = this.shelf.getDocs();
    for(Document shelfDoc: shelfDocsList)
    {
      File file = new File(shelfDoc.canonical_path);
      if(file.exists())
      {
        long actualSize = shelfDoc.size;
        long expectedSize = file.length();
            
        assertEquals(actualSize, expectedSize, shelfDoc.getInfo("File size doesn't match:"));
      }
      
    }
  }
  
}
