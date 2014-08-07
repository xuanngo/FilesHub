package net.xngo.fileshub.cmd;

import net.xngo.fileshub.Hub;
import net.xngo.fileshub.cmd.Options;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Cmd
{

  public Cmd(String[] args)
  {

    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName("FilesHub");
    
    Hub hub = new Hub();
    try
    {
      jc.parse(args);
      
      if(options.addPaths!=null)
      {
        hub.addFiles(options.getAllUniqueFiles());
      }
      else if(options.update)
      {
        System.out.println("update");
      }
      else
      { // Anything else, display the help.
        System.out.println();
        jc.usage();
      }
     
    }
    catch(ParameterException e)
    {
      System.out.println();
      System.out.println("ERROR:");
      System.out.println("======");
      System.out.println(e.getMessage());
      System.out.println("====================================");
      System.out.println();
      jc.usage();
    }


    
    
  }
  
  
}
