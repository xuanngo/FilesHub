/**
 * Example showing how to get the hash value of a file using XXHash.
 * @author Xuan Ngo
 */
package net.xngo.tutorial.xxhash;
 
import net.jpountz.xxhash.XXHashFactory;
import net.jpountz.xxhash.StreamingXXHash32;
 
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
 
public class XxhashExampleFile
{
 
  public static void main(String[] args) 
  {
    XXHashFactory factory = XXHashFactory.fastestInstance();
    int seed = 0x9747b28c;  // used to initialize the hash value, use whatever
                            // value you want, but always the same
    StreamingXXHash32 hash32 = factory.newStreamingHash32(seed);
 
    try
    {
      byte[] bufferBlock = new byte[8192]; // 8192 bytes
      FileInputStream fileInputStream = new FileInputStream(new File("C:\\temp\\Xuan\\test.txt"));
 
      int read;
      while ((read = fileInputStream.read(bufferBlock))!=-1) 
      {
        hash32.update(bufferBlock, 0, read);
      }
      fileInputStream.close();
 
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

