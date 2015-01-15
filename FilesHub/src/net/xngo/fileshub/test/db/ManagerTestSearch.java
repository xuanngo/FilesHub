package net.xngo.fileshub.test.db;


//TestNG
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;






// Java Library
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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


/**
 * Test net.xngo.fileshub.db.Manager class.
 * @author Xuan Ngo
 *
 */
@Test(singleThreaded=false)
public class ManagerTestSearch
{
  private static final boolean DEBUG = true;
  
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
    if(ManagerTestSearch.DEBUG)
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
  
}
