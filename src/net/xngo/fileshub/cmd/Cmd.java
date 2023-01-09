package net.xngo.fileshub.cmd;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Hub;
import net.xngo.utils.java.io.FileUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Cmd
{

  public Cmd(String[] args)
  {

    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName(Config.NAME);
    
    // Register commands here.
    CmdHash cmdHash = new CmdHash();
    CmdSearch cmdSearch = new CmdSearch();
    CmdRepair cmdRepair = new CmdRepair();
    jc.addCommand(CmdHash.name, cmdHash);
    jc.addCommand(CmdSearch.name, cmdSearch);
    jc.addCommand(CmdRepair.name, cmdRepair);
    
    Hub hub = new Hub();
    try
    {
      jc.parse(args);
      
      if(options.addPaths!=null)
      {
        hub.addFiles(FileUtils.listFiles(options.addPaths), options.addPaths);
      }
      else if(options.update)
      {
        hub.update();
      }
      else if(options.duplicateFiles != null)
      {
        hub.markDuplicate(options.duplicateFiles.get(0), options.duplicateFiles.get(1));
      }
      else if(options.upgrade)
      {
        hub.upgrade();
      }
      else
      { 
        /******************************
         * Commands parsing start here.
         ******************************/
        
        String parsedCmd = jc.getParsedCommand();
        if(parsedCmd != null)
        {
          if(parsedCmd.compareTo(CmdHash.name)==0)
          {
            // Hash command starts here.
            
            if(cmdHash.paths!=null)
              hub.hash(cmdHash.getAllUniqueFiles());
            else
              this.displayUsage(jc);
          }
          else if(parsedCmd.compareTo(CmdSearch.name)==0)
          {
            // Search command starts here.
            
            if(cmdSearch.id!=0)
            {
              hub.searchById(cmdSearch.id);
            }
            else if(cmdSearch.hash!=null)
            {
              hub.searchByHash(cmdSearch.hash);
            }
            else if(cmdSearch.filename!=null)
            {
              hub.searchByFilename(cmdSearch.filename);
            }
            else if(cmdSearch.filepath!=null)
            {
              hub.searchByFilepath(cmdSearch.filepath);
            }
            else if(cmdSearch.fuzzyRate!=0)
            {
              hub.searchSimilarFilename(cmdSearch.fuzzyRate);
            }              
            else
              this.displayUsage(jc);

          }
          else if(parsedCmd.compareTo(CmdRepair.name)==0)
          {
            // Repair command starts here.
            hub.repair(cmdRepair.commit);
          }          
          else
            this.displayUsage(jc);
        }
        else
          this.displayUsage(jc);
      }
     
    }
    catch(ParameterException e)
    {
      System.out.println("\nError: Wrong usage!");
      System.out.println(e.getMessage());
      System.out.println("====================================");
      System.out.println();
      jc.usage();
    }
  }

  private void displayUsage(JCommander jc)
  {
    System.out.println("\nError: Wrong usage!\n");
    jc.usage();     
  }
  
}
