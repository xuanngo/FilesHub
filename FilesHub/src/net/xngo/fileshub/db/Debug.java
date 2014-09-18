package net.xngo.fileshub.db;

import net.xngo.fileshub.AppInfo;

public class Debug
{
  private static boolean debug = false;
  
  /**
   * Display debug message.
   * @param s
   */
  public static void msg(String s)
  {
    if(Debug.activate())
      System.out.println("DEBUG: "+s);
  }
  
  /**
   * 
   * @return True is debug mode is ON. Otherwise, false.
   */
  public static boolean activate()
  {
    
    if(AppInfo.DEBUG!=null)
      debug = Boolean.parseBoolean(AppInfo.DEBUG);    
    return debug;
  }  
}
