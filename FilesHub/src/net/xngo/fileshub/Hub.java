package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.xngo.fileshub.db.Debug;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.report.Report;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.PairFile;
import net.xngo.fileshub.upgrade.Upgrade;
import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.math.Math;
import net.xngo.utils.java.time.CalUtils;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Manager manager = new Manager();
  
  public Hub()
  {
    this.manager.createDbStructure();
  }
  
  public void addFiles(Set<File> listOfFiles, List<File> addPaths)
  {
    Main.chrono.stop("Get all files to process");
    
    // Display total number of files to process.
    Report report = new Report();
    Report.FILES_TO_PROCESS = listOfFiles.size();
    report.displayTotalFilesToProcess();

      
    
    // Preparation to display the progress.
    Report.FILES_SIZE = FileUtils.totalSize(listOfFiles);
    String totalReadableSize = FileUtils.readableSize(Report.FILES_SIZE);
    long totalFilesize = 0;
    final int updateFrequency = Utils.getUpdateFrequency(Report.FILES_TO_PROCESS);
    
        Main.chrono.stop("Get total file size");
    int filesProcessed=0;
    for (File file : listOfFiles) 
    {
      Debug.msg(String.format("Adding [%s]", file.getAbsolutePath()));
      
      try
      {
        //*** Add file to database.
        Document doc = this.manager.addFile(file);
        
        //*** Collect duplicate entries for report.
        if(doc!=null)
        {
          if(doc.canonical_path.compareTo(Utils.getCanonicalPath(file))!=0) // Ignore if users add the exact same file and the same path.
          {
            if(file.exists() && new File(doc.canonical_path).exists())
            {// Ensure both files exist before adding them to the report as duplicate.
              report.addDuplicate(new Document(file), doc);
            }
            else
            {
              String msg = String.format("Warning: Duplicate pair not added. Both files should exists. Currently: "
                                                                            + "\n  To add: [exists = %b] %s"
                                                                            + "\n   In DB: [exists = %b] %s", 
                                                                            file.exists(), file.getAbsolutePath(), 
                                                                            new File(doc.canonical_path).exists(), doc.canonical_path);
              System.out.println(msg);
            }
          }
        }
        
        //*** Print progress to console.      
        totalFilesize += file.length();
        filesProcessed++;
        if( (filesProcessed%updateFrequency)==0 )
        {
          Main.connection.commit();
          report.console.printProgress(String.format("%s [%s] [%d/%d] %s", Math.getReadablePercentage(totalFilesize, Report.FILES_SIZE), 
                                                                            totalReadableSize, 
                                                                            filesProcessed, 
                                                                            Report.FILES_TO_PROCESS,
                                                                            report.getRAMUsage()));
        }
        
      }
      catch(Exception e)
      {
        if(e.getMessage().indexOf("The process cannot access the file because another process has locked a portion of the file")!=-1)
        {
          System.out.println(String.format("Warning: Ignore locked file %s.", file.getAbsolutePath()));
        }
        else if(e.getMessage().indexOf("The system cannot find the file specified")!=-1)
        {// For case where filename=..\est.
          System.out.println(String.format("Warning: The system cannot find the file specified: Ignore %s.", file.getAbsolutePath()));
        }
        else if(e.getMessage().indexOf("Too many levels of symbolic links")!=-1)
        {
          System.out.println(String.format("Warning: Too many levels of symbolic links: Ignore %s.", file.getAbsolutePath()));
        }
        else if(e.getMessage().indexOf("No such file or directory")!=-1)
        {// For filename with different encoding.
          if(file.getName().indexOf('\uFFFD')!=-1)
          {
            /**
             * Assuming invalid characters are occurring only in the filename.
             * This will not handle case where directory name has invalid characters.
             */
            File newFile = new File(file.getName().replaceAll("\uFFFD", "_"));
            if(file.renameTo(newFile))
            {
              System.out.println(String.format("Warning: No such file or directory: %s.", file.getAbsolutePath()));
              System.out.println(String.format("\tWarning: Renamed file from\n"
                                             + "\t\t%s to\n"
                                             + "\t\t%s", file.getAbsolutePath(), newFile.getAbsolutePath()));
              listOfFiles.add(newFile); // Add renamed file to the list for later processing.
            }
            else
            {
              System.out.println(String.format("Error: Failed to rename file from\n"
                                                + "\t%s to\n"
                                                + "\t%s", file.getAbsolutePath(), newFile.getAbsolutePath()));
            }

          }
          else
          {
            System.out.println(String.format("Warning: No such file or directory: Ignore %s.", file.getAbsolutePath()));
          }
        }
        else if(e.getMessage().indexOf("RuntimeException: Hash is null")!=-1)
        {
          System.out.println(String.format("Warning: RuntimeException: Hash is null: Ignore %s.", file.getAbsolutePath()));
        }        
        else
        {
          // Unknown error, roll back up to 'updateFrequency' commits.
          try
          {
            Main.connection.rollback();
            System.out.println(String.format("Rollback up to the last %d potential commits. Issue is in %s", updateFrequency, file.getAbsolutePath()));
          }
          catch(SQLException ex)
          {
            ex.printStackTrace();
          }
          
          RuntimeException rException = new RuntimeException(e.getMessage());
          rException.setStackTrace(e.getStackTrace());
          throw rException;
        }
      }
      

      
    }
    try{ Main.connection.commit(); } catch(SQLException ex) { ex.printStackTrace(); }// Last commit() because of the remainder of modulus.
    report.console.printProgress(String.format("100.00%% [%s] [%d/%d]", totalReadableSize, Report.FILES_TO_PROCESS, Report.FILES_TO_PROCESS));// Last display because of the remainder of modulus.
    
    System.out.println();
        Main.chrono.stop("Add files");
    
    Report.START_TIME   = Main.chrono.getStartTime();
    Report.END_TIME     = Main.chrono.getEndTime();
    Report.ELAPSED_TIME = Main.chrono.getTotalRuntimeString();
    
    Report.DIRECTORIES = this.getDirectoriesProcessed(addPaths);
    
    report.sort();
        Main.chrono.stop("Sort duplicates");
    report.constructSummary();
    report.writeHtml(String.format("./results_%s.html", this.getResultsSuffix(addPaths)));
        Main.chrono.stop("Write HTML file");
    report.displaySummary();
        Main.chrono.display("Add Files Runtime");
    
    System.out.println("Done!");
  }
  
  public void update()
  {
    List<Document> missingFileList = null;
    try
    {
      missingFileList = this.manager.update();
      Main.connection.commit();
    }
    catch(SQLException ex) 
    { 
      ex.printStackTrace(); 
    }
    
    if(missingFileList.size()>0)
    {
      System.out.println(String.format("\nMissing files not in your system: %d", missingFileList.size()));
      System.out.println("===================================");
      for(Document doc: missingFileList)
      {
        System.out.println("\t"+doc.canonical_path);
      }
      System.out.println(String.format("%d files are missing from your system!", missingFileList.size()));
    }
  }
  
  
  public boolean markDuplicate(File duplicate, File of)
  {
    try
    {
      boolean commit = this.manager.markDuplicate(duplicate, of);
      Main.connection.commit();
      
      return commit;
    }
    catch(SQLException ex) 
    { 
      ex.printStackTrace(); 
    }
    return false;
  }
  
  public void hash(Set<File> files)
  {
    ArrayList<File> sortedFiles = new ArrayList<File>(files);
    
    Collections.sort(sortedFiles);
    for(File file: sortedFiles)
    {
      String hash = Utils.getHash(file);
      try
      {
        System.out.println(String.format("%12s %s", hash, file.getCanonicalPath()));
      }
      catch(IOException ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  public void searchByUid(int uid)
  {
    this.manager.searchByUid(uid);
  }
  
  public void searchByHash(String hash)
  {
    this.manager.searchByHash(hash);
  }
  
  public void searchByFilename(String filename)
  {
    this.manager.searchByFilename(filename);
  }
  
  public void searchByFilepath(String filepath)
  {
    this.manager.searchByFilepath(filepath);
  }
  
  public void searchSimilarFilename(int fuzzyRate)
  {
    List<PairFile> pairFileList = this.manager.searchSimilarFilename(fuzzyRate);
    
    Collections.sort(pairFileList);
    Report report = new Report();
    report.writePotentialDuplicatesInHtml("./potentialDuplicates.html", pairFileList);
  }
  
  public void upgrade()
  {
    Upgrade upgrade = new Upgrade();
    upgrade.run();
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/  
  
  private String getResultsSuffix(List<File> addPaths)
  {
    StringBuilder directories = new StringBuilder();
    for(File path: addPaths)
    {
      if(path.exists())
      {
        File canonicalPath = null;
        try
        {
          canonicalPath = path.getCanonicalFile();
        }
        catch(IOException ex) { ex.printStackTrace(); }
        
        if(canonicalPath.isDirectory())
        {
          if(!canonicalPath.getName().isEmpty())
          {
            directories.append(canonicalPath.getName());
            directories.append("_");
          }
        }
      }
    }
    
    String timestamp = CalUtils.toString("yyyy-MM-dd_HHmmss"); // Use in filename.
    
    if(directories.toString().isEmpty())
      return timestamp;
    else
      return directories.toString()+timestamp;

  }
  
  private String getDirectoriesProcessed(List<File> addPaths)
  {
    StringBuilder directories = new StringBuilder();
    String dirSeparator = ", ";
    for(File path: addPaths)
    {
      if(path.exists())
      {
        File canonicalPath = null;
        try
        {
          canonicalPath = path.getCanonicalFile();
        }
        catch(IOException ex) { ex.printStackTrace(); }
        
        if(canonicalPath.isDirectory())
        {
          if(!canonicalPath.getName().isEmpty())
          {
            directories.append(canonicalPath.getName());
            directories.append(File.separator+dirSeparator);
          }
        }
      }
    }
    
    // Remove the last separator.
    if(directories.length()>0)
    {
      int startpos = directories.lastIndexOf(dirSeparator);
      int endpos   = startpos + dirSeparator.length();
      return directories.replace(startpos, endpos, "").toString();
    }
    else
      return directories.toString();
  }
}
