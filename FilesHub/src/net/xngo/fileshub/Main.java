package net.xngo.fileshub;

import net.xngo.fileshub.cmd.Cmd;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{

  public static void main(String[] args)
  {
    AppInfo.chrono.start();
    Cmd cmd = new Cmd(args);
  }
 
}
