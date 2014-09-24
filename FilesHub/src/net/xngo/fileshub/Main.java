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
    Config.chrono.start();
    Cmd cmd = new Cmd(args);
    
    //try { Thread.sleep(3*1000*60); } catch(InterruptedException ex) { ex.printStackTrace(); }
  }
 
}
