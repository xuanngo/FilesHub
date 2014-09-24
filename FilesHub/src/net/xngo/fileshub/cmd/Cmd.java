package net.xngo.fileshub.cmd;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Hub;
import net.xngo.fileshub.cmd.Options;
import net.xngo.fileshub.cmd.CmdHash;


import net.xngo.fileshub.db.Debug;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Cmd
{

  public Cmd(String[] args)
  {

    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName(Config.NAME);
    
    CmdHash cmdHash = new CmdHash();
    jc.addCommand(CmdHash.name, cmdHash);
    
    Hub hub = new Hub();
    try
    {
      jc.parse(args);
      
      if(options.addPaths!=null)
      {
        hub.addFiles(options.getAllUniqueFiles(), options.addPaths);
      }
      else if(options.update)
      {
        hub.update();
      }
      else if(options.duplicateFiles != null)
      {
        hub.markDuplicate(options.duplicateFiles.get(0), options.duplicateFiles.get(1));
      }
      else
      { // Check if there is a command passed.
        String parsedCmd = jc.getParsedCommand();
        if(parsedCmd != null)
        {
          if(parsedCmd.compareTo(CmdHash.name)==0)
          {
            if(cmdHash.paths!=null)
              hub.hash(cmdHash.getAllUniqueFiles());
            else
              this.displayUsage(jc);
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
      if(Debug.activate()){e.printStackTrace();}
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
