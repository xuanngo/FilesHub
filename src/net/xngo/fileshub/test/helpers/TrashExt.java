package net.xngo.fileshub.test.helpers;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.Trash;

/**
 * Extend the original class to help testing.
 * @author Xuan Ngo
 *
 */
public class TrashExt extends Trash
{
  /**
   * 
   * @return Total number of documents(i.e. rows).
   */
  public int getTotalDocs()
  {
    final String query = "SELECT COUNT(*) FROM " + super.tablename;
    
    try
    {
      Main.connection.prepareStatement(query);
      
      ResultSet resultSet =  Main.connection.executeQuery();
      if(resultSet.next())
      {
        return resultSet.getInt(1);
      }
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return 0;
  }
  
}
