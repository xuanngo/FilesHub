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
  
  @Test(description="Add empty file.", invocationCount=3)
  public void addFilesEmpty()
  {
    //*** Prepare data: Create a empty file. 
    File uniqueFile = Data.createTempFile("addFileEmpty", null, "");
    assertEquals(uniqueFile.length(), 0, "File size should be zero."); // Guarantee that it is an empty file.
    HashSet<File> files = new HashSet<File>();
      files.add(uniqueFile);
    ArrayList<File> addPaths = new ArrayList<File>();
    addPaths.addAll(files);

    //*** Main test: Add an empty file. ***
    this.hub.addFiles(files, addPaths);
    Main.connection.close();
    
    //*** Validation: Empty file should be added in Shelf table..
    Main.connection = new Connection();
    Shelf shelf = new Shelf();
    Document shelfDoc = shelf.getDocByFilename(uniqueFile.getName());
    assertNotNull(shelfDoc, String.format("%s should be added in Shelf.", uniqueFile.getAbsolutePath()));
    
    // Clean up.
    uniqueFile.delete();
    
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
    if(this.DEBUG)
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
  
  @DataProvider(name = "ShelfToShelf")
  public static Object[][] shelfToShelf()
  {
    int i=0;
    return new Object[][] { 
                // Initially, FROM & TO are in Shelf table.
                // By default, TO is in Shelf. FROM is in Trash.
                // Fa , Fb   , DocA , DocB , Switch?
              {i++, true,  true,  true,  true , false, true},     
              {i++, true,  true,  true,  false, false, true},     
              {i++, true,  true,  false, true , false, true},     
              {i++, true,  true,  false, false, false, true},     
              {i++, true,  false, true,  true , true, false},     
              //{i++, true,  false, true,  false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.  
              {i++, true,  false, false, true , true, false},     
              //{i++, true,  false, false, false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.   
              {i++, false, true,  true,  true , false, true},     
              {i++, false, true,  true,  false, false, true},     
              //{i++, false, true,  false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, true,  false, false, false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.
              {i++, false, false, true,  true , false, true},     
              //{i++, false, false, true,  false, false, true},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, false, false, true},  //Unhandled case because FROM & TO files physically don't exist and they don't exist in database.
      
                        };
  }
  @Test(dataProvider = "ShelfToShelf")
  public void markDuplicateShelfToShelf(int index, boolean bFromFile, boolean bToFile, boolean bFromDoc, boolean bToDoc, boolean bFromInShelf, boolean bToInShelf)
  {
    this.markDuplicateFromToGenericCheck(index, bFromFile, true, bToFile, true, bFromDoc, bToDoc, bFromInShelf, bToInShelf);
  }
  
  
  @DataProvider(name = "ShelfToTrash")
  public static Object[][] shelfToTrash()
  {
    int i=0;
    return new Object[][] { 
                // Initially, FROM & TO are in Shelf table.
                // By default, TO is in Shelf. FROM is in Trash.
                // FFROM , FTO, DocFROM , DocTO , FROMShelf, TOShelf
              {i++, true,  true,  true,  true , false, false},     
              {i++, true,  true,  true,  false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.     
              {i++, true,  true,  false, true , false, false},     
              {i++, true,  true,  false, false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.    
              {i++, true,  false, true,  true , true, false},     
              //{i++, true,  false, true,  false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.  
              {i++, true,  false, false, true , true, false},     
              //{i++, true,  false, false, false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.   
              {i++, false, true,  true,  true , false, false},     
              {i++, false, true,  true,  false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.     
              //{i++, false, true,  false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, true,  false, false, false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.
              {i++, false, false, true,  true , false, false},     
              //{i++, false, false, true,  false, false, true},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, false, false, true},  //Unhandled case because FROM & TO files physically don't exist and they don't exist in database.
      
                        };
  }  
  @Test(dataProvider = "ShelfToTrash")
  public void markDuplicateShelfToTrash(int index, boolean bFromFile, boolean bToFile, boolean bFromDoc, boolean bToDoc, boolean bFromInShelf, boolean bToInShelf)
  {
    this.markDuplicateFromToGenericCheck(index, bFromFile, true, bToFile, false, bFromDoc, bToDoc, bFromInShelf, bToInShelf);
  }
  
  
  
  @DataProvider(name = "TrashToTrash")
  public static Object[][] trashToTrash()
  {
    int i=0;
    return new Object[][] { 
                // Initially, FROM & TO are in Shelf table.
                // By default, TO is in Shelf. FROM is in Trash.
                // FFROM , FTO, DocFROM , DocTO , FROMShelf, TOShelf
              {i++, true,  true,  true,  true , false, false},     
              {i++, true,  true,  true,  false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.     
              {i++, true,  true,  false, true , false, false},     
              {i++, true,  true,  false, false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.    
              {i++, true,  false, true,  true , true, false},     
              //{i++, true,  false, true,  false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.  
              {i++, true,  false, false, true , true, false},     
              //{i++, true,  false, false, false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.   
              {i++, false, true,  true,  true , false, false},     
              {i++, false, true,  true,  false, false, true}, // Special case: TO doesn't exist in DB. Thus, using addFile(). TO is added in Shelf.     
              //{i++, false, true,  false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, true,  false, false, false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.
              {i++, false, false, true,  true , false, false},     
              //{i++, false, false, true,  false, false, true},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, false, false, true},  //Unhandled case because FROM & TO files physically don't exist and they don't exist in database.
      
                        };
  }  
  @Test(dataProvider = "TrashToTrash")
  public void markDuplicateTrashToTrash(int index, boolean bFromFile, boolean bToFile, boolean bFromDoc, boolean bToDoc, boolean bFromInShelf, boolean bToInShelf)
  {
    this.markDuplicateFromToGenericCheck(index, bFromFile, false, bToFile, false, bFromDoc, bToDoc, bFromInShelf, bToInShelf);
  }
  
  
  @DataProvider(name = "TrashToShelf")
  public static Object[][] trashToShelf()
  {
    int i=0;
    return new Object[][] { 
                // Initially, FROM & TO are in Shelf table.
                // By default, TO is in Shelf. FROM is in Trash.
                // FFROM , FTO, DocFROM , DocTO , FROMShelf, TOShelf
              {i++, true,  true,  true,  true , false, true},     
              {i++, true,  true,  true,  false, false, true},     
              {i++, true,  true,  false, true , false, true},     
              {i++, true,  true,  false, false, false, true},     
              {i++, true,  false, true,  true , true, false},     
              //{i++, true,  false, true,  false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.  
              {i++, true,  false, false, true , true, false},     
              //{i++, true,  false, false, false, true, false},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.   
              {i++, false, true,  true,  true , false, true},     
              {i++, false, true,  true,  false, false, true},     
              //{i++, false, true,  false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, true,  false, false, false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.
              {i++, false, false, true,  true , false, true},     
              //{i++, false, false, true,  false, false, true},  //Unhandled case because TO file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, true , false, true},  //Unhandled case because FROM file physically doesn't exist and it doesn't exist in database.    
              //{i++, false, false, false, false, false, true},  //Unhandled case because FROM & TO files physically don't exist and they don't exist in database.
      
                        };
  }  
  @Test(dataProvider = "TrashToShelf")
  public void markDuplicateTrashToShelf(int index, boolean bFromFile, boolean bToFile, boolean bFromDoc, boolean bToDoc, boolean bFromInShelf, boolean bToInShelf)
  {
    this.markDuplicateFromToGenericCheck(index, bFromFile, false, bToFile, true, bFromDoc, bToDoc, bFromInShelf, bToInShelf);
  }  
  
  
  /************************************************************************************************************************
   *                                                          Helpers
   ************************************************************************************************************************/
  
  
  /**
   * @param index         Index set of dataprovider.
   * @param bFromFile     True if FROM file should physically exist.
   * @param bFromShelf    True if FROM will exist in database.
   * @param bToFile       True if TO file should physically exist.
   * @param bToShelf      True if TO will exist in database.
   * @param bFromDoc      True if FROM file will be added in Shelf table. Otherwise, it will be added in Trash table.
   * @param bToDoc        True if TO   file will be added in Shelf table. Otherwise, it will be added in Trash table.
   * @param bFromInShelf  True if FROM will end in Shelf table. Otherwise, in Trash table.
   * @param bToInShelf    True if TO will end in Shelf table. Otherwise, in Trash table.
   */
  private void markDuplicateFromToGenericCheck(int index, 
                                    boolean bFromFile, boolean bFromShelf,
                                    boolean bToFile, boolean bToShelf, 
                                    boolean bFromDoc, boolean bToDoc, 
                                    boolean bFromInShelf, boolean bToInShelf)
  {
    // To debug: Set the IF statement to satisfy your conditions and then put
    //    the breakpoint at the System.out.println().
    if(bFromFile && !bToFile && bFromDoc && bToDoc && bFromInShelf && !bToInShelf)
    {
      System.out.println("Case to debug");
      
      // DEBUG
      if(this.DEBUG)
      {
        try { Main.connection.setAutoCommit(true); }
        catch(SQLException ex) { ex.printStackTrace(); }
      }      
    }
    
    //*** Prepare data.****
    // Create File FROM & TO
    File fileFrom = Data.createTempFile("markDuplicateFromToGenericCheck_FROM_"+index);
    File fileTo = Data.createTempFile("markDuplicateFromToGenericCheck_TO_"+index);
    
    // Add FROM & TO to Shelf or Trash table according to parameters.
    Shelf shelf = new Shelf();
    Trash trash = new Trash();
    // FROM
    if(bFromDoc)
    {
      if(bFromShelf)
      {
        // Create a FROM entry in Shelf.
        Document shelfDocFrom = new Document(fileFrom);
        shelfDocFrom.hash = Utils.getHash(fileFrom);
        shelf.addDoc(shelfDocFrom);
      }
      else
      {
        File dummy = Data.createTempFile("markDuplicateFromToGenericCheck_FROM_DUMMY_"+index);
        Document shelfDoc = new Document(dummy);
        shelfDoc.hash = Utils.getHash(dummy);
        int uid = shelf.addDoc(shelfDoc);
        
        // Create a FROM entry in Trash.
        Document trashDocFrom = new Document(fileFrom);
        trashDocFrom.hash = Utils.getHash(fileFrom);
        trashDocFrom.uid = uid;
        trash.addDoc(trashDocFrom);
        
        // Delete dummy.
        if(!bFromFile)
          dummy.delete();        
      }
    }
    
    // TO
    if(bToDoc)
    {
      if(bToShelf)
      {
        // Create a TO entry in Shelf.
        Document shelfDocTo = new Document(fileTo);
        shelfDocTo.hash = Utils.getHash(fileTo);
        shelf.addDoc(shelfDocTo);
      }
      else
      {
        File dummy = Data.createTempFile("markDuplicateFromToGenericCheck_TO_DUMMY_"+index);
        Document shelfDoc = new Document(dummy);
        shelfDoc.hash = Utils.getHash(dummy);
        int uid = shelf.addDoc(shelfDoc);
        
        // Create a TO entry in Trash.        
        Document trashDocTo = new Document(fileTo);
        trashDocTo.hash = Utils.getHash(fileTo);
        trashDocTo.uid = uid;
        trash.addDoc(trashDocTo);
        
        // Delete dummy.
        if(!bToFile)
          dummy.delete();
        
      }
    }
    
    // Delete physically FROM or TO file.
    if(!bFromFile)
      fileFrom.delete();
    
    if(!bToFile)
      fileTo.delete();
    
    
    //*** Main test: Mark FROM file is a duplicate of TO file. ***
    this.hub.markDuplicate(fileFrom, fileTo);
    
    //*** Validation: Check FROM & TO files are in correct table
    
    // Check FROM file.
    if(bFromInShelf)
    {
      Document shelfDoc = shelf.getDocByFilename(fileFrom.getName());
      assertNotNull(shelfDoc, String.format("%s should be in Shelf table.", fileFrom.getName()));
    }
    else
    {
      Document trashDoc = trash.getDocByFilename(fileFrom.getName());
      assertNotNull(trashDoc, String.format("%s should be in Trash table.", fileFrom.getName()));
    }
    
    // Check TO file.    
    if(bToInShelf)
    {
      Document shelfDoc = shelf.getDocByFilename(fileTo.getName());
      assertNotNull(shelfDoc, String.format("%s should be in Shelf table.", fileTo.getName()));
    }
    else
    {
      Document trashDoc = trash.getDocByFilename(fileTo.getName());
      assertNotNull(trashDoc, String.format("%s should be in Trash table.", fileTo.getName()));
    }    

  }  
  
  
}
