package net.xngo.fileshub.test.upgrade;

import net.xngo.fileshub.test.helpers.FHSampleDb;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
