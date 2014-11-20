package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.*;

/**
 * This class will create a sample database with different types of data.
 * Note: Each type of data will have 3 set of data.
 * @author Xuan Ngo
 *
 */
public class FHSampleDb
{
  private Manager manager = new Manager();
  private List<File> files = new ArrayList<File>();
  
  private final int REPEAT_DATA = 3;
  
  public void createSampleData()
  {
    this.manager.createDbStructure();
    this.addUniqueFiles();
    this.addFilesSameHash();
    this.addFilesSameHashMultiple();
    this.addDuplicateFilesDiffHash();
    this.addDuplicateFilesDiffHashMultiple();
    this.addDuplicateFilesMixHash();
    
    try { Main.connection.commit(); } catch(SQLException ex) { ex.printStackTrace(); }
  }
  
  public void deleteData()
  {
    for(File file: this.files)
    {
      file.delete();
    }
  }
  
  public List<File> getFiles()
  {
    return this.files;
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  private void addUniqueFiles()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("UniqueFiles_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
    }
  }
  
  private void addFilesSameHash()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("FilesSameHash_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
      
      // Copy unique file and then add to database.
      File duplicateFile = Data.createTempFile("FilesSameHash_"+i+"_TRASH");
      Data.copyFile(uniqueFile, duplicateFile);
      
      this.manager.addFile(duplicateFile);
      this.files.add(duplicateFile);
    }
  }
  
  private void addFilesSameHashMultiple()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("FilesSameHashMultiple_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
      
      for(int j=0; j<this.REPEAT_DATA; j++)
      {
        // Copy unique file and then add to database.
        File duplicateFile = Data.createTempFile("FilesSameHashMultiple_"+i+"_TRASH_"+j);
        Data.copyFile(uniqueFile, duplicateFile);
        
        this.manager.addFile(duplicateFile);
        this.files.add(duplicateFile);
      }
    }
  }  
  
  private void addDuplicateFilesDiffHash()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("DuplicateFilesDiffHash_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
      
      File duplicateFile = Data.createTempFile("DuplicateFilesDiffHash_"+i+"_TRASH");
      this.manager.markDuplicate(duplicateFile, uniqueFile);
      this.files.add(duplicateFile);
      
    }    
  }
  
  private void addDuplicateFilesDiffHashMultiple()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("DuplicateFilesDiffHashMultiple_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
      
      for(int j=0; j<this.REPEAT_DATA; j++)
      {
        File duplicateFile = Data.createTempFile("DuplicateFilesDiffHashMultiple_"+i+"_TRASH_"+j);
        this.manager.addFile(duplicateFile);
        this.files.add(duplicateFile);
        
        this.manager.markDuplicate(duplicateFile, uniqueFile);
      }
    }    
  }
  
  private void addDuplicateFilesMixHash()
  {
    for(int i=0; i<this.REPEAT_DATA; i++)
    {
      File uniqueFile = Data.createTempFile("DuplicateFilesMixHash_"+i+"_SHELF");
      this.manager.addFile(uniqueFile);
      this.files.add(uniqueFile);
      
      
      for(int a=0; a<this.REPEAT_DATA; a++)
      {
        // Copy unique file and then add to database.
        File duplicateFile = Data.createTempFile("DuplicateFilesMixHash_"+i+"_TRASH_A"+a);
        Data.copyFile(uniqueFile, duplicateFile);
        
        this.manager.markDuplicate(duplicateFile, uniqueFile);
        this.files.add(duplicateFile);
      }
      
      File fileB = Data.createTempFile("DuplicateFilesMixHash_TRASH_B");
      for(int b=0; b<this.REPEAT_DATA; b++)
      {
        // Copy unique file and then add to database.
        File duplicateFile = Data.createTempFile("DuplicateFilesMixHash_"+i+"_TRASH_B"+b);
        Data.copyFile(fileB, duplicateFile);
        
        this.manager.markDuplicate(duplicateFile, uniqueFile);
        this.files.add(duplicateFile);
      }
      fileB.delete();
      
      File fileC = Data.createTempFile("DuplicateFilesMixHash_TRASH_C");
      for(int c=0; c<this.REPEAT_DATA; c++)
      {
        // Copy unique file and then add to database.
        File duplicateFile = Data.createTempFile("DuplicateFilesMixHash_"+i+"_TRASH_C"+c);
        Data.copyFile(fileC, duplicateFile);
        
        this.manager.markDuplicate(duplicateFile, uniqueFile);
        this.files.add(duplicateFile);
      }
      fileC.delete();
      
     
    }    
  }
  
}
