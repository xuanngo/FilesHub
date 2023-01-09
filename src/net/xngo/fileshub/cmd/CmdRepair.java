package net.xngo.fileshub.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


@Parameters(commandDescription = "Repair database.")
public class CmdRepair
{
  public static String name = "repair";
  
  @Parameter(names = {"-c", "--commit"}, description = "True to commit repair. "
                                                  + "Otherwise, list all actions to be executed.", arity = 0)
  public boolean commit=false;
  
}
