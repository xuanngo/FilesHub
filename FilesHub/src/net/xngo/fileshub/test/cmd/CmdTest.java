package net.xngo.fileshub.test.cmd;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.cmd.Cmd;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.helpers.Data;

public class CmdTest
{
  @Test(description="Test --add option with 1 file.")
  public void addOptSingleFile()
  {
    File uniqueFile = Data.createUniqueFile("addOptSingleFile");
    String canonicalFilePath = Utils.getCanonicalPath(uniqueFile);
    String[] args = new String[] { "--add", canonicalFilePath };
    Cmd cmd = new Cmd(args);
    
    Shelf shelf = new Shelf();
    Document doc = shelf.findDocByCanonicalPath(canonicalFilePath);
    
    uniqueFile.delete(); // Clean up before validations.
    
    assertNotNull(doc, String.format("Command [%s %s] doesn't create an entry in database.", args[0], args[1]));
  }
  
  @Test(description="Test -a option with multiple files.")
  public void addOptMultipleFiles()
  {
    File firstUniqueFile = Data.createUniqueFile("addOptMultipleFiles1");
    File secondUniqueFile = Data.createUniqueFile("addOptMultipleFiles2");
    String firstCanonicalFilePath = Utils.getCanonicalPath(firstUniqueFile);
    String secondCanonicalFilePath = Utils.getCanonicalPath(secondUniqueFile);
    String[] args = new String[] { "-a", firstCanonicalFilePath,  secondCanonicalFilePath};
    Cmd cmd = new Cmd(args);
    
    Shelf shelf = new Shelf();
    Document firstDoc = shelf.findDocByCanonicalPath(firstCanonicalFilePath);
    Document secondDoc = shelf.findDocByCanonicalPath(firstCanonicalFilePath);
    
    // Clean up before validations.
    firstUniqueFile.delete(); 
    secondUniqueFile.delete();
    
    assertNotNull(firstDoc, String.format("Command [%s %s %s] doesn't create an entry in database for the 1st file.", args[0], args[1], args[2]));
    assertNotNull(secondDoc, String.format("Command [%s %s %s] doesn't create an entry in database for the 2nd file.", args[0], args[1], args[2]));
  }
  
  @Test(description="Test -a option with 1 directory.")
  public void addOptSingleDirectory()
  {
    String testDirectory = "./test";
    
    String[] args = new String[] { "-a", testDirectory};
    Cmd cmd = new Cmd(args);
    
    Shelf shelf = new Shelf();
    
    Collection<File> filesList = FileUtils.listFiles(new File(testDirectory), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    
    for(File file: filesList)
    {
      String canonicalPath = Utils.getCanonicalPath(file);
      Document doc = shelf.findDocByCanonicalPath(canonicalPath);
      assertNotNull(doc, String.format("Command [%s %s] doesn't work. [%s] is not found in the database.", args[0], args[1], canonicalPath));
    }
  }
  
}
