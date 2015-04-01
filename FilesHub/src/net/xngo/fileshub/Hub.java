package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.db.Repair;
import net.xngo.fileshub.report.ReportDuplicate;
import net.xngo.fileshub.report.ReportSimilar;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.Duplicate;
import net.xngo.fileshub.struct.PairFile;
import net.xngo.fileshub.upgrade.Upgrade;
import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.math.Math;
import net.xngo.utils.java.time.CalUtils;
import net.xngo.utils.java.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  final static Logger log = LoggerFactory.getLogger(Hub.class);
  
  private Manager manager = new Manager();
  
  
  public Hub()
  {
    this.manager.createDbStructure();
  }
  
  public void addFiles(Set<File> listOfFiles, List<File> addPaths)
  {
    
    ReportDuplicate reportDuplicate = new ReportDuplicate(new File(String.format("./results_%s.html", this.getResultsSuffix(addPaths))));
    
    Main.chrono.stop("Get all files to process");
    
    // Display total number of files to process.
    int numberOfFilesToProcess = listOfFiles.size();
    reportDuplicate.addTotalFilesToProcess(numberOfFilesToProcess);
    System.out.println(String.format("File(s) to process = %,d", numberOfFilesToProcess));

    // Prepare to display the progress.
    long totalFileSize = FileUtils.totalSize(listOfFiles);
    String totalReadableSize = FileUtils.readableSize(totalFileSize);
    long accumulateFileSize = 0;
    final int updateFrequency = Utils.getUpdateFrequency(numberOfFilesToProcess);
    
    List<Duplicate> duplicates = new ArrayList<Duplicate>();
    Main.chrono.stop("Get total file size");
    int filesProcessed=0;
    for (File file : listOfFiles) 
    {
      try
      {
        //*** Add file to database.
        Document conflictDoc = this.manager.addFile(file);
        
        //*** Collect duplicate entries for report.
        if(conflictDoc!=null)
        {
          if(conflictDoc.canonical_path.compareTo(Utils.getCanonicalPath(file))!=0) // Ignore if users add the exact same file and the same path.
          {
            File conflictFile = new File(conflictDoc.canonical_path);
            
            if(file.exists() && conflictFile.exists() && conflictFile.isFile())
            {// Ensure both files exist before adding them to the report as duplicate.
              duplicates.add(new Duplicate(new Document(file), conflictDoc));
            }
            else
            {
              String msg = String.format("Warning: Duplicate pair not added. Both files should exists. Currently: "
                                                                            + "\n  To add: [exists = %b] %s"
                                                                            + "\n   In DB: [exists = %b] %s", 
                                                                            file.exists(), file.getAbsolutePath(), 
                                                                            conflictFile.exists(), conflictDoc.canonical_path);
              System.out.println(msg);
            }
          }
        }
        
        //*** Print progress to console.      
        accumulateFileSize += file.length();
        filesProcessed++;
        if( (filesProcessed%updateFrequency)==0 )
        {
          Main.connection.commit();
          Main.console.printProgress(String.format("%s [%s] [%d/%d] %s", Math.getReadablePercentage(accumulateFileSize, totalFileSize), 
                                                                            totalReadableSize, 
                                                                            filesProcessed, 
                                                                            numberOfFilesToProcess,
                                                                            Utils.getRAMUsage()));
        }
        
      }
      catch(Exception e)
      {//@TODO: Exception handling is messy here. Need rework.
        
        String[] ignoreMessages = {
                                    "The process cannot access the file because another process has locked a portion of the file", // win32
                                    "The system cannot find the file specified",
                                    "Too many levels of symbolic links", // linux
                                    "Access is denied", // Win32
                                    "The process cannot access the file because it is being used by another process", // Win32
                                    "RuntimeException: Hash is null",
                                    
                                    };
        int msgIndex = -1;  // Default to Not found.
        
        if(e.getMessage()==null)
        {
          log.error("Unknown exception: Exception.getMessage() is null. Caused by {}.", file.getAbsolutePath(), e);
          RuntimeException rException = new RuntimeException("Unknown exception: Exception.getMessage() is null. Caused by "+file.getAbsolutePath());
          throw rException;          
        }
        else if(e.getMessage().indexOf("No such file or directory")!=-1)
        {// For filename with different encoding.
          if(file.getName().indexOf('\uFFFD')!=-1)
          {
            /**
             * Rename invalid filename with a valid filename. All invalid characters in the filename will be replaced
             *  with a hyphen(-).
             * Note: It is assumed invalid characters are occurring only in the filename.
             * This will not handle case where directory name has invalid characters.
             */
            final String sourcePath = file.getAbsolutePath();
            final String destinationPath = file.getParent()+File.separator+file.getName().replace("\uFFFD", "_");
            File newFile = new File(destinationPath);
            //if(file.renameTo(newFile))
            if(this.renameInvalidFilename(sourcePath, destinationPath))
            {
              log.warn("Invalid charaters in filename. Rename {} to {}.", file.getAbsolutePath(), newFile.getAbsolutePath());
              System.out.println(String.format("Warning: No such file or directory: %s.", file.getAbsolutePath()));
              System.out.println(String.format("   Renamed file from\n"
                                             + "     %s to\n"
                                             + "     %s", file.getAbsolutePath(), newFile.getAbsolutePath()));
              this.manager.addFile(newFile); // Add renamed file to the database. This might render duplicate report not accurate but it is ok.
            }
            else
            {
              log.error("Invalid charaters in filename. Failed rename {} to {}.", file.getAbsolutePath(), newFile.getAbsolutePath());
              System.out.println(String.format("Error: Failed to rename file from\n"
                                                + "   %s to\n"
                                                + "   %s", file.getAbsolutePath(), newFile.getAbsolutePath()));
            }

          }
          else
          {
            log.warn("No such file or directory: Ignore {}.", file.getAbsolutePath());
            System.out.println(String.format("Warning: No such file or directory: Ignore %s.", file.getAbsolutePath()));
          }
        }
        else if((msgIndex=StringUtils.indexOfKeywords(e.getMessage(), ignoreMessages))!=-1)
        {
          String warnMsg = String.format("Warning: %s. Ignore %s.", ignoreMessages[msgIndex], file.getAbsolutePath());
          log.warn(warnMsg);
          System.out.println(warnMsg);
        }        
        else
        {
          // Unknown error, roll back up to 'updateFrequency' commits.
          try
          {
            Main.connection.rollback();
          }
          catch(SQLException ex)
          {
            System.out.println(String.format("Rollback up to the last %d potential commits. Issue is in %s", updateFrequency, file.getAbsolutePath()));
            log.error("Unknown error. Rollback up to the last {} potential commits. Issue is in {}.", updateFrequency, file.getAbsolutePath(), e);
            ex.printStackTrace();
          }
          
          RuntimeException rException = new RuntimeException(e.getMessage());
          rException.setStackTrace(e.getStackTrace());
          throw rException;
        }
      }
      

      
    }
    try{ Main.connection.commit(); } catch(SQLException ex) { ex.printStackTrace(); }// Last commit() because of the remainder of modulus.
    Main.console.printProgress(String.format("100.00%% [%s] [%d/%d]", totalReadableSize, numberOfFilesToProcess, numberOfFilesToProcess));// Last display because of the remainder of modulus.
    
    System.out.println();
    Main.chrono.stop("Add files");

    reportDuplicate.addDirectoriesProcessed(this.getDirectoriesProcessed(addPaths));
    reportDuplicate.setData(duplicates);
    reportDuplicate.generate();
    
    Main.chrono.display("Runtime breakdown");    
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
  
  public void searchById(int id)
  {
    this.manager.searchById(id);
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
    List<PairFile> pairFileList = this.manager.searchSimilarFilenameFromCurrentDirectory(fuzzyRate);
    
    Collections.sort(pairFileList);
    
    ReportSimilar reportSimilar = new ReportSimilar(new File("./potentialDuplicates.html"));
    reportSimilar.setData(pairFileList);
    reportSimilar.generate();
    
    Main.chrono.display("Runtime breakdown"); 
    
    System.out.println("Done!");
  }
  
  public void upgrade()
  {
    Upgrade upgrade = new Upgrade();
    upgrade.run();
  }
  
  public void repair(boolean commit)
  {
    Repair repair = new Repair();
    repair.commit(commit);
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

  
  private boolean renameInvalidFilename(String sourcePath, String destinationPath)
  {
    
    // Execute move command depending on OS.
    String os_name = System.getProperty("os.name");
    if(os_name.indexOf("Windows")!=-1)
      return this.renameInvalidFilenameWin(sourcePath, destinationPath);
    else
      return this.renameInvalidFilenameUnix(sourcePath, destinationPath);  

  }

  private boolean renameInvalidFilenameWin(String sourcePath, String destinationPath)
  {
    // Explanation: http://stackoverflow.com/questions/12109520/java-cant-see-file-on-file-system-that-contains-illegal-characters#answer-12110279
    String source = sourcePath.replace("\uFFFD", "?");
    
    String mvCmd = String.format("move \"%s\" \"%s\"", source, destinationPath);
    String[] cmdLine = new String[]{"cmd", "/c", mvCmd};
    
    // Execute the command.
    try
    {
      Process process = Runtime.getRuntime().exec(cmdLine);
      try { process.waitFor(); } catch(InterruptedException ex){ ex.printStackTrace(); } // Wait for the process to terminate.
      if(process.exitValue()==0)
        return true;
      else
        return false;
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }
    
    return false;    
  }
  
  private boolean renameInvalidFilenameUnix(String sourcePath, String destinationPath)
  {
    String source = sourcePath.replace("\uFFFD", "?");
    source = source.replace(" ", "\\ ");
    source = source.replace("[", "\\[");
    source = source.replace("]", "\\]");
    source = source.replace("(", "\\(");
    source = source.replace(")", "\\)");
    source = source.replace("'", "\\'");
    source = source.replace("&", "\\&");
    
    String mvCmd = String.format("mv %s \"%s\"", source, destinationPath);
    String[] cmdLine = new String[]{"/bin/sh", "-c", mvCmd};
    
    // Execute the command.
    try
    {
      Process process = Runtime.getRuntime().exec(cmdLine);

      try { process.waitFor(); } catch(InterruptedException ex){ ex.printStackTrace(); } // Wait for the process to terminate.
      if(process.exitValue()==0)
        return true;
      else
      {
        return false;
      }
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }
    
    return false;    
  }
  
  
}
