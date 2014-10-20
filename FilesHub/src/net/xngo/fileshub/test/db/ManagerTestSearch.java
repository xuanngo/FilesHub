package net.xngo.fileshub.test.db;


//TestNG
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;



import java.io.ByteArrayOutputStream;
// Java Library
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


import java.util.concurrent.atomic.AtomicInteger;

//FilesHub classes.
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;

// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.utils.java.math.Random;


/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
@Test(singleThreaded=false)
public class ManagerTestSearch
{
  private Manager manager = new Manager();
  private AtomicInteger atomicInt = new AtomicInteger(Random.Int()+1); // Must set initial value to more than 0. Duid can't be 0.
  
  // Get the original standard out before changing it.
  private final PrintStream originalStdOut = System.out;
  private ByteArrayOutputStream consoleContent = null;
  
  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    this.manager.createDbStructure();
  }
  
  @BeforeTest
  public void beforeTest()
  {
    this.consoleContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(this.consoleContent));
  }
  
  @AfterTest
  public void afterTest()
  {
    System.setOut(this.originalStdOut);
  }
  
  @Test(description="Search by hash: Multiple hashes in Trash table")
  public void searchByHashMultipleHashesInTrash()
  {
    /** Prepare data: Hash not found in Shelf but hashes(FileA & FileB) found in Trash table. **/
    // Create duplicate files.
    File fileA = Data.createTempFile("searchByHashMultipleHashesInTrash_fileA");
    File fileB = Data.createTempFile("searchByHashMultipleHashesInTrash_fileB");
    Data.copyFile(fileA, fileB);
    
    // Add duplicate files directly in Trash table.
    final int fakeUid = atomicInt.get();
    Document trashDocA = new Document(fileA);
      trashDocA.uid = fakeUid;
      trashDocA.hash = Utils.getHash(fileA);
    Document trashDocB = new Document(fileB);
      trashDocB.uid = fakeUid;
      trashDocB.hash = Utils.getHash(fileB);
    Trash trash = new Trash();
    trash.addDoc(trashDocA);
    trash.addDoc(trashDocB);
    
    /** Main test: It should not throw any exception **/
    this.manager.searchByHash(trashDocA.hash);
  }
  
  
}
