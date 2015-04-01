package net.xngo.fileshub.test;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.utils.java.math.Hash;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

public class UtilsTest
{
  @Test(description="getHash(): Small file, less than 4MB.")
  public void getHashSmallFileLessThan4MB()
  {
    //*** Prepare data: Create a small file, less than 4MB.
    File uniqueFile = Data.createTempFile("getHashSmallFileLessThan4MB");
    
    //*** Test: Utils.getHash() should return the same hash as Hash.md5().
    String actualHash = Utils.getHash(uniqueFile);
    String expectedHash = Hash.md5(uniqueFile);
    assertEquals(actualHash, expectedHash, String.format("Hash is different for %s.", uniqueFile.getAbsolutePath()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="getHash(): Big file, more than 4MB.")
  public void getHashBigFileMoreThan4MB()
  {
    //*** Prepare data: Create a big file, more than 4MB.
    File uniqueFile = Data.createTempFileWithByte("getHashBigFileMoreThan4MB", Data.getRandomBytes(4*1024*1024+1));
    
    //*** Test: Utils.getHash() should return the same hash as Hash.md5().
    String actualHash = Utils.getHash(uniqueFile);
    String md5Hash = Hash.md5(uniqueFile);
    assertNotEquals(actualHash, md5Hash, String.format("Hash should be different for %s.", uniqueFile.getAbsolutePath()));
    
    //*** Clean up.
    //uniqueFile.delete();    
  }
  
  @Test(description="getDirsAsString(): Test a file.")
  public void getDirsAsStringFile()
  {
    //*** Prepare data: Create a file.
    File file = Data.createTempFile("getDirsAsStringFile");
    List<File> paths = new ArrayList<File>();
    paths.add(file);
    
    //*** Test: Should return an empty string as it is not a directory.
    String errorMsg = String.format("Should return empty string as it is not a directory: %s.", file.getAbsolutePath());
    assertThat(errorMsg, Utils.getDirsAsString(paths), isEmptyString());
    
    //*** Clean up.
    file.delete();    
  }
  
  /* @TODO: Mock File.isDirectory()
  @Test(description="getDirsAsString(): Test a mix of files and directories.")
  public void getDirsAsStringMixFileNDirs()
  {
    //*** Prepare data: Create files and directories
    File file1 = Data.createTempFile("getDirsAsStringMixFileNDirs_file1");
    File file1 = Data.createTempFile("getDirsAsStringMixFileNDirs_file1");
    
    List<File> paths = new ArrayList<File>();
    paths.add(file1);
    
    //*** Test: Should return an empty string as it is not a directory.
    assertThat(Utils.getDirsAsString(paths), is(""));
    
    //*** Clean up.\
    for(File file: paths)
      file.delete();
  }
  */
}
