package net.xngo.fileshub.test.struct;

import java.io.File;
import java.sql.SQLException;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.helpers.Data;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class DocumentTest
{
  
  private static final boolean DEBUG = true;
  
  
  @Test(description="Test document constructor with file.")
  public void documentFile()
  {
    //*** Prepare data: Create a unique file.
    File uniqueFile = Data.createTempFile("documentFile");
    
    //*** Main test: Add File into Document.
    Document doc = new Document(uniqueFile);
    
    //*** Validations: Check file info are transfer into Document.
    assertEquals(doc.canonical_path, Utils.getCanonicalPath(uniqueFile));
    assertEquals(doc.filename, uniqueFile.getName());
    assertEquals(doc.last_modified, uniqueFile.lastModified());
    assertEquals(doc.size, uniqueFile.length());
    
  }
  
  @Test(description="Test equals() with canonical path = null.")
  public void equalsCanonicalPathNull()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsCanonicalPathNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set canonical path to null.
    actualDoc.canonical_path = null;
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));

  }

  @Test(description="Test equals() with filename = null.")
  public void equalsFilenameNull()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsFilenameNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set filename to null.
    actualDoc.filename = null;
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
  }
  
  
  @Test(description="Test equals() with hash = null.")
  public void equalsHashNull()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsHashNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set hash to null.
    actualDoc.hash = null;
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }  
  
  @Test(description="Test equals() with comment = null.")
  public void equalsCommentNull()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsCommentNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set comment to null.
    actualDoc.comment = null;
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }
  
  @Test(description="Test equals() with canonical path = empty.")
  public void equalsCanonicalPathEmpty()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsCanonicalPathNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set canonical path to null.
    actualDoc.canonical_path = "";
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }
  
  @Test(description="Test equals() with filename = empty.")
  public void equalsFilenameEmpty()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsFilenameNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set filename to null.
    actualDoc.filename = "";
    
    //*** Validations: Documents should be equals.
    assertNotEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }
  
  
  @Test(description="Test equals() with hash = empty.")
  public void equalsHashEmpty()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsHashNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set hash to null.
    actualDoc.hash = "";
    
    //*** Validations: Documents should be equals.
    assertEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }  
  
  @Test(description="Test equals() with comment = empty.")
  public void equalsCommentEmpty()
  {
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("equalsCommentNull");
    Document expectedDoc = new Document(uniqueFile);
    Document actualDoc = new Document(uniqueFile);
    
    
    //*** Main test: Set comment to null.
    actualDoc.comment = "";
    
    //*** Validations: Documents should be equals.
    assertEquals(actualDoc, expectedDoc, String.format("\n%s\n"
                                                + "%s", actualDoc.getInfo("Actual"), expectedDoc.getInfo("Expected")));
    
  }
  
  @DataProvider(name = "sanityCheckNulls")
  public static Object[][] sanityCheckNulls()
  {
    int i=0;
    return new Object[][] { 
              // Path, filename, hash
              { true,  false,  false  },     
              { false,  true,  false  },     
              { false,  false,  true  },     
     
                        };
  }  

  
  @Test(description="Test sanityCheck() with null values.", dataProvider = "sanityCheckNulls")
  public void sanityCheckNulls(boolean bCanonicalPathNull, boolean bFilenameNull, boolean bHashNull)
  {
    
    // To debug: Set the IF statement to satisfy your conditions and then put
    //    the breakpoint at the System.out.println().
    if(bCanonicalPathNull && bFilenameNull && bHashNull)
    {
      System.out.println("Case to debug");
    }
    
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("sanityCheckNulls");
    Document doc = new Document(uniqueFile);
    doc.hash = Utils.getHash(uniqueFile);
    
    //*** Main test: Set to null.
    if(bCanonicalPathNull)
      doc.canonical_path = null;
    
    if(bFilenameNull)
      doc.filename = null;
    
    if(bHashNull)
      doc.hash = null;
    
    try
    {
     doc.sanityCheck();
     assertTrue(false);// Fail the unit. Expected NullPointerException to be thrown.
    }
    catch(NullPointerException nullEx)
    {
      assertTrue(true);
    }
  }  
  

  @DataProvider(name = "sanityCheckEmpty")
  public static Object[][] sanityCheckEmpty()
  {
    int i=0;
    return new Object[][] { 
              // Path, filename, hash
              { true,  false,  false  },     
              { false,  true,  false  },     
              { false,  false,  true  },     
     
                        };
  }  

  
  @Test(description="Test sanityCheck() with empty values.", dataProvider = "sanityCheckEmpty")
  public void sanityCheckEmpty(boolean bCanonicalPathEmpty, boolean bFilenameEmpty, boolean bHashEmpty)
  {
    
    // To debug: Set the IF statement to satisfy your conditions and then put
    //    the breakpoint at the System.out.println().
    if(bCanonicalPathEmpty && bFilenameEmpty && bHashEmpty)
    {
      System.out.println("Case to debug");
    }
    
    //*** Prepare data: Create a unique file and a document.
    File uniqueFile = Data.createTempFile("sanityCheckEmpty");
    Document doc = new Document(uniqueFile);
    doc.hash = Utils.getHash(uniqueFile);
    
    //*** Main test: Set to null.
    if(bCanonicalPathEmpty)
      doc.canonical_path = "";
    
    if(bFilenameEmpty)
      doc.filename = "";
    
    if(bHashEmpty)
      doc.hash = "";
    
    try
    {
     doc.sanityCheck();
     assertTrue(false);// Fail the unit. Expected RuntimeException to be thrown.
    }
    catch(RuntimeException runtimeEx)
    {
      assertTrue(true);
    }
  }  
  
  
  
}
