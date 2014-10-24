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
import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.test.helpers.Data;

public class HubTest
{
  private Hub hub = new Hub();
  
  @Test(description="Add deleted file.")
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
    
  }
  
  @Test(description="Add middle file deleted.")
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
    
  }

}
