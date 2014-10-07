package net.xngo.fileshub.db;

import java.util.ArrayList;
/**
 * http://stackoverflow.com/questions/1963806/is-there-a-fixed-sized-queue-which-removes-excessive-elements
 * @author Xuan Ngo
 *
 * @param <K>
 */
public class LimitedSizeQueue<K> extends ArrayList<K> 
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private int maxSize;

  public LimitedSizeQueue(int size)
  {
    this.maxSize = size;
  }

  public boolean add(K k)
  {
    boolean r = super.add(k);
    if (size() > maxSize)
    {
      removeRange(0, size() - maxSize - 1);
    }
    return r;
  }

  public K getYongest() 
  {
    return get(size() - 1);
  }

  public K getOldest() 
  {
    return get(0);
  }
  
  public int getMaxSize()
  {
    return this.maxSize;
  }
}