package net.xngo.fileshub.test.upgrade;

import java.io.File;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.fileshub.test.helpers.FHSampleDb;

public class Version0002Test
{
  private FHSampleDb fhSampleDb = new FHSampleDb();
  
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
  
  @Test(description="Test upgrade to version 2 with entries where file size is null.")
  public void runWithFileSizeIsNull()
  {

  }
  
}
