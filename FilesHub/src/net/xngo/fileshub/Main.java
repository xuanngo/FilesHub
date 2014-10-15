package net.xngo.fileshub;

import net.xngo.fileshub.cmd.Cmd;
import net.xngo.fileshub.db.Connection;
import net.xngo.fileshub.report.Chronometer;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{
  public static Chronometer chrono = new Chronometer();
  public static Connection connection = new Connection();
  
  public static void main(String[] args)
  {
    Main.chrono.start();
    Cmd cmd = new Cmd(args);
    Main.connection.close();
    
    //try { Thread.sleep(3*1000*60); } catch(InterruptedException ex) { ex.printStackTrace(); }
  }
 
}
