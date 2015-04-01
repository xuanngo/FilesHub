package net.xngo.fileshub.test.db;


//TestNG
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
// Java Library
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import net.xngo.fileshub.Main;
//FilesHub classes.
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
// FilesHub test helper classes.
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.fileshub.test.helpers.DocumentExt;
import net.xngo.utils.java.math.Random;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
@Test(singleThreaded=false)
public class ManagerSearchTest
{
  private static final boolean DEBUG = true;
  
  private final int randomInt = java.lang.Math.abs(Random.Int())+1;
  private AtomicInteger atomicInt = new AtomicInteger(randomInt); // Must set initial value to more than 0. Duid can't be 0.
  
  private Manager manager = new Manager();

  // Get the original standard out before changing it.
  private final PrintStream originalStdOut = System.out;
  private ByteArrayOutputStream consoleContent = new ByteArrayOutputStream();
  
  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    this.manager.createDbStructure();
    
    // DEBUG: Commit every single transaction in database.
    if(ManagerSearchTest.DEBUG)
    {
      try { Main.connection.setAutoCommit(true); }
      catch(SQLException ex) { ex.printStackTrace(); }
    }      
  }
  
  @BeforeMethod
  public void beforeTest()
  {
    // Redirect all System.out to consoleContent.
    System.setOut(new PrintStream(this.consoleContent));
  }
  
  @AfterMethod
  public void afterTest()
  {
    // Put back the standard out.
    System.setOut(this.originalStdOut);
    
    // Print what has been captured.
    System.out.println(this.consoleContent.toString());
    
    // Clear the consoleContent.
    this.consoleContent = new ByteArrayOutputStream(); 
  }
  
  
  @Test(description="Search by uid in Shelf.")
  public void searchByIdInShelf()
  {
    //*** Prepare data: Create a unique file and add it in database.
    File uniqueFile = Data.createTempFile("searchByIdInShelf");
    this.manager.addFile(uniqueFile);

    //*** Main test: Search by uid.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    this.manager.searchById(shelfDoc.uid);
    
    //*** Validations: Found document searched by uid.
    DocumentExt shelfDocExt = new DocumentExt(shelfDoc);
    assertTrue(shelfDocExt.foundIn(this.consoleContent.toString()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }

  @Test(description="Search by unknown uid.")
  public void searchByIdNotFound()
  {
    //*** Main test: Search by uid.
    this.manager.searchById(Integer.MAX_VALUE);
    
    //*** Validations: Searched uid not found.
    assertThat(this.consoleContent.toString(), containsString("ID '2147483647' is not found!"));
    
  }
  
  @Test(description="Search by hash in Shelf.")
  public void searchByHashInShelf()
  {
    //*** Prepare data: Create a unique file and add it in database.
    File uniqueFile = Data.createTempFile("searchByHashInShelf");
    this.manager.addFile(uniqueFile);

    //*** Main test: Search by hash.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    this.manager.searchByHash(shelfDoc.hash);
    
    //*** Validations: Found document searched by hash.
    DocumentExt shelfDocExt = new DocumentExt(shelfDoc);
    assertTrue(shelfDocExt.foundIn(this.consoleContent.toString()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="Search by unknown hash.")
  public void searchByHashNotFound()
  {
    //*** Main test: Search by unknown hash.
    this.manager.searchByHash("searchByHashNotFound");
    
    //*** Validations: Searched hash not found.
    assertThat(this.consoleContent.toString(), containsString("Hash 'searchByHashNotFound' is not found in Shelf table."));
    assertThat(this.consoleContent.toString(), containsString("Hash 'searchByHashNotFound' is not found in Trash table."));     
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
  
  @Test(description="Search by filename in Shelf.")
  public void searchByFilenameInShelf()
  {
    //*** Prepare data: Create a unique file and add it in database.
    File uniqueFile = Data.createTempFile("searchByFilenameInShelf");
    this.manager.addFile(uniqueFile);

    //*** Main test: Search by filename.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    this.manager.searchByFilename(shelfDoc.filename);
    
    //*** Validations: Found document searched by filename.
    DocumentExt shelfDocExt = new DocumentExt(shelfDoc);
    assertTrue(shelfDocExt.foundIn(this.consoleContent.toString()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="Search by unknown filename.")
  public void searchByFilenameNotFound()
  {
    //*** Main test: Search by unknown filename.
    this.manager.searchByFilename("searchByFilenameNotFound");
    
    //*** Validations: Found document searched by filename.
    assertThat(this.consoleContent.toString(), containsString("Filename 'searchByFilenameNotFound' is not found in Shelf table."));
    assertThat(this.consoleContent.toString(), containsString("Filename 'searchByFilenameNotFound' is not found in Trash table."));    
  }  
  
  @Test(description="Search by file path in Shelf.")
  public void searchByFilepathInShelf()
  {
    //*** Prepare data: Create a unique file and add it in database.
    File uniqueFile = Data.createTempFile("searchByFilepathInShelf");
    this.manager.addFile(uniqueFile);

    //*** Main test: Search by file path.
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByCanonicalPath(uniqueFile.getAbsolutePath());
    this.manager.searchByFilepath(shelfDoc.canonical_path);
    
    //*** Validations: Found document searched by file path.
    DocumentExt shelfDocExt = new DocumentExt(shelfDoc);
    assertTrue(shelfDocExt.foundIn(this.consoleContent.toString()));
    
    //*** Clean up.
    uniqueFile.delete();    
  }
  
  @Test(description="Search by unknown file path.")
  public void searchByFilepathNotFound()
  {
    //*** Main test: Search by unknown filename.
    this.manager.searchByFilepath("searchByFilepathNotFound");
    
    //*** Validations: Found document searched by filename.
    assertThat(this.consoleContent.toString(), containsString("Filepath 'searchByFilepathNotFound' is not found in Shelf table."));
    assertThat(this.consoleContent.toString(), containsString("Filepath 'searchByFilepathNotFound' is not found in Trash table."));
  }
  
}
