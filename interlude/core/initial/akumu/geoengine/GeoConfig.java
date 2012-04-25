package ru.akumu.geoengine;

import com.l2scoria.L2Properties;
import java.util.logging.Logger;

public class GeoConfig
{
  private static Logger _log = Logger.getLogger("GeoConfig");

  public static int MAP_MIN_X = -163840;
  public static int MAP_MAX_X = 229375;
  public static int MAP_MIN_Y = -262144;
  public static int MAP_MAX_Y = 294911;
  public static pf PATHFINDING = pf.GEO;
  public static boolean ENABLED = true;
  public static boolean ACTIVATED = true;
  public static String GEO_PATH = "./data/geodata/";
  public static String PN_PATH = "./data/pathnode/";

  static void load()
  {
    try
    {
      /*L2Properties p = new L2Properties("config/akumu/geoengine.properties");

      MAP_MIN_X = Integer.parseInt(p.getProperty("MinX", "-163840"));
      MAP_MAX_X = Integer.parseInt(p.getProperty("MaxX", "229375"));
      MAP_MIN_Y = Integer.parseInt(p.getProperty("MinY", "-262144"));
      MAP_MAX_Y = Integer.parseInt(p.getProperty("MaxX", "294911"));

      PATHFINDING = pf.valueOf(p.getProperty("PathFinding", "[GeoPath]: "));

      ENABLED = Boolean.valueOf(p.getProperty("Enabled", "False")).booleanValue();

      GEO_PATH = p.getProperty("GeodataPath", "./data/geodata/");
      PN_PATH = p.getProperty("PathnodePath", "./data/pathnode/");

      p = null;*/
    }
    catch (Exception e)
    {
      _log.warning("[GeoConfig]: Error loading config: " + e.getMessage());
    }
  }

  public static enum pf
  {
    GEO, 
    PN, 
    OFF;
  }
}