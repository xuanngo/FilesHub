package net.xngo.fileshub;

import java.io.File;
import java.util.ArrayList;

import net.xngo.fileshub.Hub;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Main
{

  public static void main(String[] args)
  {
    if(args.length > 0)
    {
      Hub hub = new Hub(args);
    }
    else
    {
      System.out.println(String.format("Please supply directory path."));
    }
    
  }
 
}
