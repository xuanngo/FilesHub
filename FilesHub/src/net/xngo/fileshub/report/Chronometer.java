package net.xngo.fileshub.report;


import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import net.xngo.fileshub.Config;

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
  private int maxPeriodNameLength = 0;
  
  public Chronometer(){}
 
  public void start()
  {
    this.periods.add(new Period(null, Calendar.getInstance())); // Period.name = null to make it brittle.
  }
  
  public void stop(String name)
  {
    this.periods.add(new Period(name, Calendar.getInstance()));
    
    if(name.length() > maxPeriodNameLength)
      maxPeriodNameLength = name.length();
  }
  
  public void stop()
  {
    this.stop("");
  }
  
  /**
   * Return the total number of stops.
   * @return
   */
  public int getNumberOfStops()
  {
    return this.periods.size();
  }  
  
  public String getStartTime()
  {
    return this.getDateTimeFormatted(this.periods.get(0).time());
  }
  public String getEndTime()
  {
    return this.getDateTimeFormatted(this.periods.get(this.periods.size()-1).time());
  }

  
  public long getRuntime(int fromStopA, int toStopB)
  {
    if(fromStopA<0) throw new RuntimeException(String.format("%d can't be less than 1.", fromStopA));
    if(toStopB>this.periods.size()) throw new RuntimeException(String.format("%d can't be bigger than %d.", toStopB, this.periods.size()));
    
    long start = this.periods.get(fromStopA).time().getTimeInMillis();
    long end   = this.periods.get(toStopB).time().getTimeInMillis();
    return end-start;    
  }

  public String getRuntimeName(int fromStopA, int toStopB)
  {
    if(fromStopA<0) throw new RuntimeException(String.format("%d can't be less than 1.", fromStopA));
    if(toStopB>this.periods.size()) throw new RuntimeException(String.format("%d can't be bigger than %d.", toStopB, this.periods.size()));
    
    return this.periods.get(toStopB).name();
  }
  
  public String getRuntimeString(int fromStopA, int toStopB)
  {
    return this.formatTime(this.getRuntime(fromStopA, toStopB));
  }
  
  public String getTotalRuntimeString()
  {
    return this.getRuntimeString(0, this.periods.size()-1);
  }

  public int getMaxPeriodNameLength()
  {
    return this.maxPeriodNameLength;
  }
  /************************************************************************************************************
   *                                    Superfluous functions
   ************************************************************************************************************/
  public void display()
  {
    System.out.println("========================================================");
    System.out.println("Chronometer periods:");
    int i=0;
    for(; i<this.periods.size()-1; i++) // The 1st period is discard because it is the start.
    {
      System.out.println(String.format("\t[%02d] %"+this.getMaxPeriodNameLength()+"s = %s", i+1, this.getRuntimeName(i, i+1), this.getRuntimeString(i, i+1)));
    }
    System.out.println(String.format("\t[%02d] %"+this.getMaxPeriodNameLength()+"s = %s", i+1, "[Total]", this.getTotalRuntimeString()));
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

