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
      
      if(!options.paths.isEmpty())
      {
        hub.addFiles(options.getAllUniqueFiles());
      }
      else if(options.help)
      {
        jc.usage();
      }
     
    }
    catch(ParameterException e)
    {
      System.out.println(e.getMessage());
      System.out.println("======================");
      jc.usage();
    }    
  }
}
