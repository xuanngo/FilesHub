package net.xngo.fileshub.test.cmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.cmd.Cmd;
import net.xngo.fileshub.db.Shelf;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.test.db.ManagerTestSearch;
import net.xngo.fileshub.test.helpers.Data;
import net.xngo.utils.java.math.Random;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;




import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CmdConsoleTest
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
    if(CmdConsoleTest.DEBUG)
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
  
  
  @Test(description="-a ..: Check console output skeleton.")
  public void addOutputConsoleBasic()
  {
    //*** Prepare data: Create 2 duplicate files.
    File uniqueFile = Data.createTempFile("addOutputConsoleBasic");
    File duplicateFile = Data.createTempFile("addOutputConsoleBasic_duplicate_hash");
    Data.copyFile(uniqueFile, duplicateFile);    
    
    //*** Main test: Copy unique file and then add to database.
    String[] args = new String[] { "-a", uniqueFile.getAbsolutePath(), duplicateFile.getAbsolutePath() };
    Cmd cmd = new Cmd(args);
    
    assertThat(this.consoleContent.toString(), containsString("Summary:"));

    assertThat(this.consoleContent.toString(), containsString("2 files processed."));
    assertThat(this.consoleContent.toString(), containsString("1 duplicate file(s) found totalling"));

    
    assertThat(this.consoleContent.toString(), containsString("Start at"));
    assertThat(this.consoleContent.toString(), containsString("End   at"));
    assertThat(this.consoleContent.toString(), containsString("RAM:"));
    
    assertThat(this.consoleContent.toString(), containsString("Runtime breakdown:"));
    assertThat(this.consoleContent.toString(), containsString("Get total file size ="));
    assertThat(this.consoleContent.toString(), containsString("Add files ="));
    assertThat(this.consoleContent.toString(), containsString("Sort duplicates ="));
    assertThat(this.consoleContent.toString(), containsString("Write HTML file ="));
    assertThat(this.consoleContent.toString(), containsString("[Total] ="));    
  }
  
  @Test(description="search: Check console output skeleton.")
  public void searchSimilarOutputConsoleBasic()
  {
    //*** Prepare data: Create and add files in database. Guarantee that there is something to compare.
    File fileA = new File("./FHTest_searchSimilarOutputConsoleBasic.txt");
    File fileB = new File("./FHTest_searchSimilarOutputConsoleBasic_2.txt");
    Data.writeStringToFile(fileA, "searchSimilarOutputConsoleBasic 1");
    Data.writeStringToFile(fileB, "searchSimilarOutputConsoleBasic 2");
    this.manager.addFile(fileA);
    this.manager.addFile(fileB);
    
    //*** Main test: Copy unique file and then add to database.
    String[] args = new String[] { "search" };
    Cmd cmd = new Cmd(args);
    
    assertThat(this.consoleContent.toString(), containsString("Comparing "));
    assertThat(this.consoleContent.toString(), containsString(" files against "));
    assertThat(this.consoleContent.toString(), containsString("from the database for a total of "));
    assertThat(this.consoleContent.toString(), containsString("combinations."));
    
    assertThat(this.consoleContent.toString(), not(containsString("Comparing 0 files against ")));
    
    assertThat(this.consoleContent.toString(), containsString("Results are stored in "));
    assertThat(this.consoleContent.toString(), containsString("potentialDuplicates"));
    
    
    assertThat(this.consoleContent.toString(), containsString("Summary:"));
    assertThat(this.consoleContent.toString(), containsString("Start at"));
    assertThat(this.consoleContent.toString(), containsString("End   at"));
    assertThat(this.consoleContent.toString(), containsString("RAM:"));
    
    assertThat(this.consoleContent.toString(), containsString("Runtime breakdown:"));
    assertThat(this.consoleContent.toString(), containsString("Compare similar files ="));
    assertThat(this.consoleContent.toString(), containsString("Write HTML file ="));
    assertThat(this.consoleContent.toString(), containsString("[Total] ="));
    
    //*** Clean up.
    fileA.delete();
    fileB.delete();
  }
  
}
