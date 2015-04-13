package net.xngo.fileshub;

import net.xngo.fileshub.cmd.Cmd;
import net.xngo.fileshub.db.Connection;
import net.xngo.utils.java.io.Console;
import net.xngo.utils.java.time.Chronometer;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{
  public static Chronometer chrono = new Chronometer();
  public static Connection connection = new Connection();
  public static Console console = new Console();
  
  public static void main(String[] args)
  {
    Main.chrono.start();
    Cmd cmd = new Cmd(args);
    Main.connection.close();
  }
 
}
