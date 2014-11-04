package net.xngo.fileshub.test.hub;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.DataProvider;

import net.xngo.fileshub.Hub;
import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.test.helpers.Data;

public class HubTest
{
  private static final boolean DEBUG = true;
  
  private Hub hub = new Hub();
  
  @Test(description="Add deleted file.", invocationCount=3)
  public void addFilesUniqueDeleted()
  {
    //*** Prepare data: Create 2 files. ***
    File fileA = Data.createTempFile("addFilesDeleted_A_del");
    File fileB = Data.createTempFile("addFilesDeleted_B");
    HashSet<File> files = new HashSet<File>();
      files.add(fileA);
      files.add(fileB);
    ArrayList<File> addPaths = new ArrayList<File>();
    addPaths.addAll(files);

    //*** Main test: Delete 1 file and process all files. ***
    fileA.delete();
    this.hub.addFiles(files, addPaths);
    Main.connection.close();
    
    //*** Validation: Deleted file is not added in the database.
    Main.connection = new Connection();
    Shelf shelf = new Shelf();
    Document shelfDocA = shelf.getDocByFilename(fileA.getName());
    Document shelfDocB = shelf.getDocByFilename(fileB.getName());
    assertNull(shelfDocA, String.format("%s should not be added in Shelf. It was deleted.", fileA.getName()));
    assertNotNull(shelfDocB, String.format("%s should be added in Shelf.", fileB.getName()));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    
  }
  
  @Test(description="Add middle file deleted.", invocationCount=3)
  public void addFilesUniqueMiddleFileDeleted()
  {
    //*** Prepare data: Create 2 files. ***
    File fileA = Data.createTempFile("addFilesUniqueMiddleFileDeleted_A");
    File fileB = Data.createTempFile("addFilesUniqueMiddleFileDeleted_B_del");
    File fileC = Data.createTempFile("addFilesUniqueMiddleFileDeleted_C");
    HashSet<File> files = new HashSet<File>();
        files.add(fileA);
        files.add(fileB);
        files.add(fileC);
    ArrayList<File> addPaths = new ArrayList<File>();
    addPaths.addAll(files);
    
    //*** Main test: Delete the middle file and process all files. ***
    fileB.delete();
    this.hub.addFiles(files, addPaths);
    Main.connection.close();
    
    //*** Validation: Deleted file is not added in the database.
    Main.connection = new Connection();
    Shelf shelf = new Shelf();
    Document shelfDocA = shelf.getDocByFilename(fileA.getName());
    Document shelfDocB = shelf.getDocByFilename(fileB.getName());
    Document shelfDocC = shelf.getDocByFilename(fileC.getName());
    assertNotNull(shelfDocA, String.format("%s should be added in Shelf.", fileA.getName()));
    assertNull(shelfDocB, String.format("%s should not be added in Shelf. It was deleted.", fileB.getName()));
    assertNotNull(shelfDocC, String.format("%s should be added in Shelf.", fileC.getName()));
    
    // Clean up.
    fileA.delete();
    fileB.delete();
    fileC.delete();
  }
  
  @Test(description="Mark duplicate an already duplicate file.")
  public void markDuplicateAlreadyDuplicate()
  {
    //*** Prepare data ***
    File fileA = Data.createTempFile("markDuplicateAlreadyDuplicate_A");
    File fileB = Data.createTempFile("markDuplicateAlreadyDuplicate_B");
    
    //*** Main test: Mark file A is a duplicate of B and do the same again. ***
    this.hub.markDuplicate(fileA, fileB);
    this.hub.markDuplicate(fileA, fileB);
    
    //*** Validation: File A is a duplicate of B.
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    Document shelfDocB = shelf.getDocByFilename(fileB.getName());
    Document trashDocA = trash.getDocByFilename(fileA.getName());
    
    assertNotNull(shelfDocB, String.format("%s should be in Shelf table.", fileB.getName()));
    assertNotNull(trashDocA, String.format("%s should be in Trash table.", fileA.getName()));
    
    // Clean up.
    fileA.delete();
    fileB.delete();    
    
  }

  @Test(description="File A is mark as duplicate to File B but File B is deleted.")
  public void markDuplicateABFileBDeleted()
  {
    // DEBUG
    if(DEBUG)
    {
      try { Main.connection.setAutoCommit(true); }
      catch(SQLException ex) { ex.printStackTrace(); }
    }
    
    //*** Prepare data: Make File A to be a duplicate of File B ****
    File fileA = Data.createTempFile("markDuplicateFileBDeleted_A");
    File fileB = Data.createTempFile("markDuplicateFileBDeleted_B");
    // Mark File A is a duplicate of File B.
    this.hub.markDuplicate(fileA, fileB);       

    //*** Main test: Mark File A is a duplicate of File B. ****
    fileB.delete();
    this.hub.markDuplicate(fileA, fileB);
    
    //*** Validation: File A should be moved to Shelf whereas File B is moved to Trash.
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    Document shelfDocA = shelf.getDocByFilename(fileA.getName());
    Document trashDocB = trash.getDocByFilename(fileB.getName());
    
    assertEquals(shelfDocA.canonical_path, Utils.getCanonicalPath(fileA));
    assertEquals(trashDocB.canonical_path, Utils.getCanonicalPath(fileB));
    
    //*** Clean up ***
    fileA.delete();
  }
  
  @Test(description="File B is mark as duplicate to File A but File B is deleted.")
  public void markDuplicateBAFileBDeleted()
  {
    //*** Prepare data: Make File A to be a duplicate of File B ****
    File fileA = Data.createTempFile("markDuplicateFileBDeleted_A");
    File fileB = Data.createTempFile("markDuplicateFileBDeleted_B");
    // Mark File A is a duplicate of File B.
    this.hub.markDuplicate(fileA, fileB);       

    //*** Main test: Mark File A is a duplicate of File B. ****
    fileB.delete();
    this.hub.markDuplicate(fileB, fileA);
    
    //*** Validation: File A should be moved to Shelf whereas File B is moved to Trash.
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    Document shelfDocA = shelf.getDocByFilename(fileA.getName());
    Document trashDocB = trash.getDocByFilename(fileB.getName());
    
    assertEquals(shelfDocA.canonical_path, Utils.getCanonicalPath(fileA));
    assertEquals(trashDocB.canonical_path, Utils.getCanonicalPath(fileB));
    
    //*** Clean up ***
    fileA.delete();
  }
  
  @Test(description="File A is mark as duplicate to File A.")
  public void markDuplicateExactSameFile()
  {
    //*** Prepare data: Create File A.****
    File fileA = Data.createTempFile("markDuplicateExactSameFile");
    
    //*** Main test: Mark File A is a duplicate of File A. ****
    final boolean commit = this.hub.markDuplicate(fileA, fileA);
    
    //*** Validation: File A should be moved to Shelf whereas File B is moved to Trash.
    assertFalse(commit, "No commit because we are marking the exact same file.");
    
    //*** Clean up ***
    fileA.delete();
  }
  
  @DataProvider(name = "fileABExistences")
  public static Object[][] fileABExistences()
  {
    int i=0;
    return new Object[][] { 
                            // Initially, A & B are in Shelf table.
                            // By default, B is in Shelf. A is in true, rash.
                            // Fa , Fb   , DocA , DocB , Switch?
                          {i++, true,  true,  true,  true , false },    
                          {i++, true,  true,  true,  false, false },    
                          {i++, true,  true,  false, true , false },    
                          {i++, true,  true,  false, false, false },    
                          {i++, true,  false, true,  true , true },    
                          //{i++, true,  false, true,  false, true }, //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.  
                          {i++, true,  false, false, true , true },    
                          //{i++, true,  false, false, false, true }, //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.   
                          {i++, false, true,  true,  true , false },    
                          {i++, false, true,  true,  false, false },    
                          //{i++, false, true,  false, true , false }, //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
                          //{i++, false, true,  false, false, false }, //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.
                          {i++, false, false, true,  true , false },    
                          //{i++, false, false, true,  false, false }, //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.    
                          //{i++, false, false, false, true , false }, //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
                          //{i++, false, false, false, false, false }, //Unhandled case because FROM & TO files physically don't exist and they don't exist in database.
                        };
  }
  
  @Test(dataProvider = "fileABExistences")
  public void markDuplicateFileShelfToShelfABExistences(int index, boolean bFromFile, boolean bToFile, boolean bFromDoc, boolean bToDoc, boolean bSwitched)
  {
    this.markDuplicateFileFromToExistences(index, bFromFile, true, bToFile, true, bFromDoc, bToDoc, bSwitched);
  }
  
  private void markDuplicateFileFromToExistences(int index, boolean bFromFile, boolean bFromShelf, boolean bToFile, boolean bToShelf, boolean bFromDoc, boolean bToDoc, boolean bSwitched)
  {
    // To debug: Set the IF statement to satisfy your conditions and then put
    //    the breakpoint at the System.out.println().
    if(bFromFile && !bToFile && !bFromDoc && !bToDoc && bSwitched)
    {
      System.out.println("Case to debug");
    }
    //*** Prepare data.****
    // Create File FROM & TO
    File fileFrom = Data.createTempFile("markDuplicateFileFromToExistences_FROM_"+index);
    File fileTo = Data.createTempFile("markDuplicateFileFromToExistences_TO_"+index);
    
    // Add FROM & TO to Shelf or Trash table according to parameters.
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    if(bFromDoc)
    {
      if(bFromShelf)
      {
        Document shelfDocFrom = new Document(fileFrom);
        shelfDocFrom.hash = Utils.getHash(fileFrom);
        shelf.addDoc(shelfDocFrom);
      }
      else
      {
        Document trashDocFrom = new Document();
        trashDocFrom.hash = Utils.getHash(fileFrom);
        trash.addDoc(trashDocFrom);
      }
    }
    
    if(bToDoc)
    {
      if(bToShelf)
      {
        Document shelfDocTo = new Document(fileTo);
        shelfDocTo.hash = Utils.getHash(fileTo);
        shelf.addDoc(shelfDocTo);
      }
      else
      {
        Document trashDocTo = new Document(fileTo);
        trashDocTo.hash = Utils.getHash(fileTo);
        trash.addDoc(trashDocTo);
      }
    }
    
    // Delete File A or B.
    if(!bFromFile)
      fileFrom.delete();
    
    if(!bToFile)
      fileTo.delete();
    
    
    //*** Main test: Mark FROM file is a duplicate of TO file. ***
    this.hub.markDuplicate(fileFrom, fileTo);
    
    //*** Validation: Check FROM & TO files are in correct table
    if(bSwitched)
    {// FROM is moved to Shelf.
      Document shelfDoc = shelf.getDocByFilename(fileFrom.getName());
      Document trashDoc = trash.getDocByFilename(fileTo.getName());
      assertNotNull(shelfDoc, String.format("%s should be in Shelf table.", fileFrom.getName()));
      assertNotNull(trashDoc, String.format("%s should be in Trash table.", fileTo.getName()));
    }
    else
    {
      Document shelfDoc = shelf.getDocByFilename(fileTo.getName());
      Document trashDoc = trash.getDocByFilename(fileFrom.getName());
      assertNotNull(shelfDoc, String.format("%s should be in Shelf table.", fileTo.getName()));
      assertNotNull(trashDoc, String.format("%s should be in Trash table.", fileFrom.getName()));
    }
    
    
  }  
  
}
