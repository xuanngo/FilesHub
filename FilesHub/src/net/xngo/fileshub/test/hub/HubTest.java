package net.xngo.fileshub.test.hub;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import net.xngo.fileshub.Hub;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.test.helpers.Data;

public class HubTest
{
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
}
