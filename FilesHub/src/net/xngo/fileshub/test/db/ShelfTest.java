package net.xngo.fileshub.test.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Manager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.helpers.Data;

public class ShelfTest
{
  private static final boolean DEBUG = false;
  
  private Shelf shelf = new Shelf();
  
  @BeforeClass
  public void DatabaseCreation()
  {
    // Make sure that the database file is created.
    Manager manager = new Manager();
    manager.createDbStructure();
  }
  
  @Test(description="Search by filename without wildcard: Test SQL query.")
  public void searchDocsByFilenameNotWildcardQuery()
  {
    //*** Prepare data: Add unique file in database.
    File uniqueFile = Data.createTempFile("searchDocsByFilenameNotWildcard");
    Document shelfDoc = new Document(uniqueFile);
    shelfDoc.hash = Utils.getHash(uniqueFile);
    this.shelf.addDoc(shelfDoc);
    
    //*** Main test: Search by filename without wildcard
    this.shelf.searchDocsByFilename(uniqueFile.getName());
    
    
    //*** Validations: Check the sql query has equal sign.
    Connection connection = Main.connection;
    String expectedSQLquery = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment FROM Shelf WHERE filename = ? : %s", uniqueFile.getName());
    assertEquals(connection.getQueryString(), expectedSQLquery);
  }
  
  @Test(description="Search by filename with wildcard: Test SQL query.")
  public void searchDocsByFilenameWildcardQuery()
  {
    //*** Prepare data: Add unique file in database.
    File uniqueFile = Data.createTempFile("searchDocsByFilenameWildcardQuery");
    Document shelfDoc = new Document(uniqueFile);
    shelfDoc.hash = Utils.getHash(uniqueFile);
    this.shelf.addDoc(shelfDoc);
    
    //*** Main test: Search by filename with wildcard
    final String filenameWildcard         = uniqueFile.getName()+"*";
    final String expectedFilenameWildcard = uniqueFile.getName()+"%";
    this.shelf.searchDocsByFilename(filenameWildcard);
    
    
    //*** Validations: Check the sql query has the LIKE
    Connection connection = Main.connection;
    String expectedSQLquery = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment FROM Shelf WHERE filename like ? : %s", expectedFilenameWildcard);
    assertEquals(connection.getQueryString(), expectedSQLquery);
  }
  
  @Test(description="Search by filename with multiple adjacent wildcards: Test SQL query.")
  public void searchDocsByFilenameAdjacentWildcardQuery()
  {
    //*** Prepare data: Add unique file in database.
    File uniqueFile = Data.createTempFile("searchDocsByFilenameAdjacentWildcardQuery");
    Document shelfDoc = new Document(uniqueFile);
    shelfDoc.hash = Utils.getHash(uniqueFile);
    this.shelf.addDoc(shelfDoc);
    
    //*** Main test: Search by filename with wildcard
    final String filenameWildcard         = uniqueFile.getName()+"**";
    final String expectedFilenameWildcard = uniqueFile.getName()+"%";
    this.shelf.searchDocsByFilename(filenameWildcard);
    
    
    //*** Validations: Check the sql query has not adjacent %.
    Connection connection = Main.connection;
    String expectedSQLquery = String.format("SELECT uid, canonical_path, filename, last_modified, hash, comment FROM Shelf WHERE filename like ? : %s", expectedFilenameWildcard);
    assertEquals(connection.getQueryString(), expectedSQLquery);
  }  
  
}
