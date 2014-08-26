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
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.helpers.Data;

public class CmdTest
{
  @Test(description="Test --add option with 1 file.")
  public void addOptSingleFile()
  {
    File uniqueFile = Data.createTempFile("addOptSingleFile");
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
    File firstUniqueFile = Data.createTempFile("addOptMultipleFiles1");
    File secondUniqueFile = Data.createTempFile("addOptMultipleFiles2");
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
    // Create test data in temporary path
    String testDirectoryString = System.getProperty("java.io.tmpdir")+System.nanoTime();
    File testDirectory = new File(testDirectoryString);
    testDirectory.mkdir();
    for(int i=0; i<5; i++)
    {
      Data.createTempFile("addOptSingleDirectory_"+i, testDirectory);
    }
    
    // Run command.
    String[] args = new String[] { "-a", testDirectoryString };
    Cmd cmd = new Cmd(args);

    // Validate
    Shelf shelf = new Shelf();
    Collection<File> filesList = FileUtils.listFiles(testDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    
    for(File file: filesList)
    {
      String canonicalPath = Utils.getCanonicalPath(file);
      Document doc = shelf.findDocByCanonicalPath(canonicalPath);
      assertNotNull(doc, String.format("Command [%s %s] doesn't work. [%s] is not found in the database.", args[0], args[1], canonicalPath));
    }
    
    // Clean up. Directory will be not be cleaned up if assertions failed. But at least the created directory is in the temporary directory.
    FileUtils.deleteQuietly(testDirectory);    
  }
  
  @Test(description="Test -u option with file content changed.")
  public void updateContentChanged()
  {
    // Add unique file in Shelf.
    File uniqueFile = Data.createTempFile("updateContentChanged");
    String oldHash = Utils.getHash(uniqueFile);
    String[] args = new String[] { "-a", uniqueFile.getAbsolutePath() };
    Cmd cmd = new Cmd(args);    
    
    
    // Execute update option with file where its content has changed.
    Data.writeStringToFile(uniqueFile, "new content");
    String newHash = Utils.getHash(uniqueFile);
    args = new String[] { "-u" };
    cmd = new Cmd(args);    
    
    
    // Validations
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.findDocByHash(newHash);
    assertNotNull(shelfDoc, String.format("The new hash, %s, should be found in Shelf table.\n"
                                              + "%s", 
                                              newHash, Data.getFileInfo(uniqueFile, "Info of file where its content has changed")));


    Trash trash = new Trash();
    Document trashDoc = trash.findDocByHash(oldHash);
    assertNotNull(trashDoc, String.format("The old hash, %s, should be found in Trash table.\n"
                                              + "%s", 
                                              newHash, Data.getFileInfo(uniqueFile, "Info of file where its content has changed")));    
    
    // Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="Test -d option with files that have different content/hash.")
  public void markDuplicateDiffContent()
  {// Mark file B is a duplicate of file A.
    
    // Add unique file in Shelf.
    File fileA = Data.createTempFile("markDuplicateDiffContent_A");
    String[] args = new String[] { "-a", fileA.getAbsolutePath() };
    Cmd cmd = new Cmd(args);    
    
    
    // Execute duplicate option with file where its content has changed.
    File fileB = Data.createTempFile("markDuplicateDiffContent_B");
    Data.copyFile(fileA, fileB);
    Data.writeStringToFile(fileB, "new content");
    args = new String[] { "-d", fileB.getAbsolutePath(), fileA.getAbsolutePath()};
    cmd = new Cmd(args);    
    
    
    // Validations
    Trash trash = new Trash();
    Document trashDoc = trash.findDocByCanonicalPath(Utils.getCanonicalPath(fileB));
    assertNotNull(trashDoc, String.format("[%s] should be found in Trash table. [%s] is a duplicated of [%s].", fileB.getAbsolutePath(), fileB.getAbsolutePath(), fileA.getAbsolutePath() )); 
    
    // Clean up.
    fileA.delete();
    fileB.delete();
  }  
  
  
}
