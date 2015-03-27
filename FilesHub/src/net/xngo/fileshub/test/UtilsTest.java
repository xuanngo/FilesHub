package net.xngo.fileshub.test;

import java.io.File;

import net.xngo.fileshub.Utils;
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.utils.java.math.Hash;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class UtilsTest
{
  @Test(description="getHash(): Small file, less than 4MB.")
  public void getHashSmallFile()
  {
    //*** Prepare data: Create a small file, less than 4MB.
    File uniqueFile = Data.createTempFile("getHashSmallFile");
    
    //*** Test: Utils.getHash() should return the same hash as Hash.md5().
    String actualHash = Utils.getHash(uniqueFile);
    String expectedHash = Hash.md5(uniqueFile);
    assertEquals(actualHash, expectedHash, String.format("Hash is different for %s.", uniqueFile.getAbsolutePath()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }
}
