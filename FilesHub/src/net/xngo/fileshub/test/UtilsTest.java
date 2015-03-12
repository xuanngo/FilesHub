package net.xngo.fileshub.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.File;


import net.xngo.fileshub.Utils;
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.math.Hash;

import org.testng.annotations.Test;

public class UtilsTest
{
  @Test(description="Test getHash() with file size less than 4MB.")
  public void getHashFileLessThan4MB()
  {
    //*** Prepare data: Create a unique file with size less than 4MB.
    File fileLess4MB = Data.createTempFile("getHashFileLessThan4MB");
    
    //*** Main test: Since file size is less than 4MB, it should use Hash.xxhash32().
    String hashFileLess4MB = Utils.getHash(fileLess4MB);
    String expectedHash    = Hash.xxhash32(fileLess4MB);    
    assertEquals(hashFileLess4MB, expectedHash, 
        String.format("File size of %s(%s) is less than 4MB. Therefore, it should use Hash.xxhash32().", 
            fileLess4MB.getName(), fileLess4MB.length()));
    
    //*** Clean up
    fileLess4MB.delete();
    
  }
  
  @Test(description="Test getHash() with file size bigger than 4MB(4194304 bytes).")
  public void getHashFileBiggerThan4MB()
  {
    //*** Prepare data: Create a unique file with size bigger than 4MB.
    File fileBigger4MB = Data.createTempFileWithByte("getHashFileBiggerThan4MB", Data.getRandomBytes(4194304+1));
    
   
    //*** Main test: Since file size is bigger than 4MB, it should use Hash.xxhash32Spread().
    String actualHash  = Utils.getHash(fileBigger4MB);
    String expectedHashSpread = Hash.xxhash32Spread(fileBigger4MB, 1048576); // Utils.getHash.bufferSize= 1048576
    assertEquals(actualHash, expectedHashSpread, 
          String.format("File size of %s(%s) is bigger than 4MB. Therefore, it should use Hash.xxhash32Spread().", 
              fileBigger4MB.getName(), fileBigger4MB.length()));
    
    // Extra validation:
    String expectedDiffHash = Hash.xxhash32(fileBigger4MB);
    assertNotEquals(actualHash, expectedDiffHash, "Utils.getHash() should return the hash value from Hash.xxhash32Spread() and not from Hash.xxhash32().");

    //*** Clean up
    fileBigger4MB.delete();    
  }  
  
}
