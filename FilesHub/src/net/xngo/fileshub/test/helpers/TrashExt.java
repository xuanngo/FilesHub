package net.xngo.fileshub.test.helpers;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.db.Trash;
import net.xngo.fileshub.struct.Document;

/**
 * Extend the original class to help testing.
 * @author Xuan Ngo
 *
 */
public class TrashExt extends Trash
{
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
