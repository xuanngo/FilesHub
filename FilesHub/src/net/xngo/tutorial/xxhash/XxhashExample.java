package net.xngo.tutorial.xxhash;

import net.jpountz.xxhash.XXHashFactory;
import net.jpountz.xxhash.StreamingXXHash32;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class XxhashExample
{

  public static void main(String[] args) 
  {
    XXHashFactory factory = XXHashFactory.fastestInstance();

    try
    {
      byte[] data = "12345345234572".getBytes("UTF-8");
      ByteArrayInputStream in = new ByteArrayInputStream(data);
  
      int seed = 0x9747b28c; // used to initialize the hash value, use whatever
                             // value you want, but always the same
      StreamingXXHash32 hash32 = factory.newStreamingHash32(seed);
      byte[] buf = new byte[8]; // for real-world usage, use a larger buffer, like 8192 bytes
      for (;;) {
        int read = in.read(buf);
        if (read == -1) {
          break;
        }
        hash32.update(buf, 0, read);
      }
      int hash = hash32.getValue();
      
      System.out.println(hash);
    }
    catch(UnsupportedEncodingException ex)
    {
      System.out.println(ex);
    }
    catch(IOException ex)
    {
      System.out.println(ex);
    }

  }	
}
