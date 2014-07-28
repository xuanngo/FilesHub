package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.xngo.fileshub.Hub;
import net.xngo.fileshub.cmd.Cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{

  public static void main(String[] args)
  {
    Cmd cmd = new Cmd();
    JCommander jc = new JCommander(cmd);
    jc.setProgramName("FilesHub");
 
    try
    {
      jc.parse(args);
      Hub hub = new Hub();
      hub.addFiles(cmd.getAllUniqueFiles());
    }
    catch(ParameterException e)
    {
      System.out.println(e.getMessage());
      System.out.println("======================");
      jc.usage();
    }
    
    

  }
 
}
