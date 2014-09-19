package net.xngo.fileshub.report;


import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Measure time elapsed.
 * @author Xuan Ngo
 *
 */
public class Chronometer 
{
  private class Period
  {
    private String   vname = null;
    private Calendar vtime = null;
    
    public Period(String name, Calendar c)
    {
      this.vname = name;
      this.vtime = c;
    }
    
    public String   name() { return this.vname; }
    public Calendar time() { return this.vtime; }
  }
  
  private ArrayList<Period> periods = new ArrayList<Period>();
  
  public Chronometer(){}
 
  public void start()
  {
    this.periods.add(new Period(null, Calendar.getInstance())); // Period.name = null to make it brittle.
  }
  
  public void stop(String name)
  {
    this.periods.add(new Period(name, Calendar.getInstance()));
  }
  
  public void stop()
  {
    this.stop("");
  }

  /**
   * The 1st stop is 1.
   * @param stop
   * @return
   */
  public final long getStop(int stop)
  {
    long start = this.periods.get(stop-1).time().getTimeInMillis();
    long end   = this.periods.get(stop).time().getTimeInMillis();
    return end-start;
  }

  /**
   * The 1st stop is 1.
   * @param stop
   * @return
   */
  public String getStopName(int stop)
  {
    return this.periods.get(stop).name();
  }
  
  
  /************************************************************************************************************
   *                                    Superfluous functions
   ************************************************************************************************************/
  public void display()
  {
    for(int i=1; i<this.periods.size(); i++)
    {
System.out.println(String.format("%d / %d",i, this.periods.size()));      
      System.out.println(String.format("%s ran for %,d ms.", this.getStopName(i), this.getStop(i+1)));
    }
  }
  
  /**
   * The 1st stop is 1.
   * @param stop
   * @return
   */
  public String getFormattedStop(int stop)
  {
    return this.formatTime(this.getStop(stop));
  }
  
  /**
   * @return Return the elapsed time measured as HH:MM:SS.mmmm.
   */
  public final String formatTime(long millis)
  {

    long lTotalRuntime = millis;
    long lRuntime      = lTotalRuntime;
 
    // Calculate hours, minutes and seconds.     
    long lRuntimeHrs = lRuntime/(1000*3600);
    lRuntime         = lRuntime - (lRuntimeHrs*1000*3600);// Runtime remaining.
    long lRuntimeMin = (lRuntime)/(1000*60);
    lRuntime         = lRuntime - (lRuntimeMin*1000*60);  // Runtime remaining.
    long lRuntimeSec = lRuntime/(1000);
    lRuntime         = lRuntime - (lRuntimeSec*1000);     // Runtime remaining.
 
    return String.format("%02d:%02d:%02d.%d", lRuntimeHrs, lRuntimeMin, lRuntimeSec, lRuntime);
  }
  
  private final String getDateTimeFormatted(Calendar oCalendar)
  {
    final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSSS";
 
    Date currentDate = oCalendar.getTime();
    SimpleDateFormat oSimpleDateFormat = new SimpleDateFormat(dateFormat);
    return oSimpleDateFormat.format(currentDate);
  }
}

