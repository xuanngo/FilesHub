package net.xngo.fileshub;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.Document;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Database database = new Database();
  
  public Hub()
  {
    database.create();
    
    this.addDirectory(new File("./"));
  }
  
  public void addDirectory(File directory)
  {
    Collection<File> filesList = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    
    Document doc = new Document();
    for (File file : filesList) 
    {
      doc.addFile(file);
    }    
  }
}
