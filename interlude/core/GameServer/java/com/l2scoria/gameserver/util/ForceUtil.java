package com.l2scoria.gameserver.util;

/**
 *
 * @author scoria
 */
public final class ForceUtil 
{
	  public static int getProcessorCount()
	  {
		  return Runtime.getRuntime().availableProcessors();
	  }

	  public static long getFreeRAM()
	  {
		  return ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576L);
	  }

	  public static long getTotalRAM()
	  {
		  return (Runtime.getRuntime().maxMemory() / 1048576L);
	  }

	  public static long getUsedRAM()
	  {
		  return (Runtime.getRuntime().maxMemory() / 1048576L - ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576L));
	  }

	  public static long getUsedRAMPercentage()
	  {
		  return ((Runtime.getRuntime().maxMemory() / 1048576L - ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576L)) / Runtime.getRuntime().maxMemory() / 1048576L / 100L);
	  }

	  public static int getTimeSec(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return ((int)(diff / 1000L) % 60);
	  }

	  public static int getTimeSecTo(long time)
	  {
		  long diff = System.currentTimeMillis() - time;
		  return ((int)(diff / 1000L) % 60);
	  }

	  public static int getTimeSecEx(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return ((int)diff / 1000);
	  }

	  public static int getTimeSecExTo(long time)
	  {
		  long diff = System.currentTimeMillis() - time;
		  return ((int)diff / 1000);
	  }

	  public static int getTimeMin(long time)
	  {
		  return (int)Math.floor(getTimeSecEx(time) % 60);
	  }

	  public static int getTimeMinEx(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return (int)Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L);
	  }

	  public static int getTimeMinExTo(long time)
	  {
		  long diff = System.currentTimeMillis() - time;
		  return (int)Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L);
	  }

	  public static int getTimeHour(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return (int)Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L % 24L);
	  }

	  public static int getTimeHourEx(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return (int)Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L % 24L);
	  }

	  public static int getTimeHourExTo(long time)
	  {
		  long diff = System.currentTimeMillis() - time;
		  return (int)Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L % 24L);
	  }

	  public static int getTimeDays(long time)
	  {
		  long diff = time - System.currentTimeMillis();
		  return (int)Math.floor(((diff / 1000L - (diff / 1000L % 60L)) / 60L - Math.floor((diff / 1000L - (diff / 1000L % 60L)) / 60L % 24L)) / 24.0D);
	  }

	  public static long addTimeDays(int days)
	  {
		  return (days * 86400000);
	  }

	  public static long addTimeHours(int hours)
	  {
		  return (hours * 3600000);
	  }

	  public static long addTimeMins(int mins)
	  {
		  return (mins * 60000);
	  }

	  public static long addTimeSeconds(int secs)
	  {
		  return (secs * 1000);
	  }

	  public static boolean bIsTimeExpired(long time)
	  {
		  return (System.currentTimeMillis() > time);
	  }
}
