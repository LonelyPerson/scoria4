package ru.akumu.geoengine;

import com.l2scoria.gameserver.geo.GeoData;
import com.l2scoria.gameserver.geo.pathfinding.Node;
import com.l2scoria.gameserver.geo.pathfinding.cellnodes.CellPathFinding;
import com.l2scoria.gameserver.geo.util.L2Arrays;
import com.l2scoria.gameserver.datatables.csv.DoorTable;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2scoria.util.Point3D;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import ru.akumu.geoengine.paths.GeoNode;
import ru.akumu.geoengine.paths.Paths;
import ru.akumu.geoengine.paths.type.AbstractNodeLoc;
import ru.akumu.geoengine.paths.type.GeoPath;

public class MovementEngine extends GeoData
{
  private static final Logger _log = Logger.getLogger("MovementEngine");
  private static MovementEngine _instance;
  public static String VERSION = "1.2.0.7";
  public static final byte BLOCKTYPE_FLAT = 0;
  public static final byte BLOCKTYPE_COMPLEX = 1;
  public static final byte BLOCKTYPE_MULTILEVEL = 2;
  private static final byte _e = 1;
  private static final byte _w = 2;
  private static final byte _s = 4;
  private static final byte _n = 8;
  private static final ConcurrentMap<Short, MappedByteBuffer> _geodata = new ConcurrentHashMap();
  private static final ConcurrentMap<Short, IntBuffer> _geodataIndex = new ConcurrentHashMap();
  private static BufferedOutputStream _geoBugsOut;

  public static MovementEngine getInstance()
  {
	if(MovementEngine._instance == null)
	{
		MovementEngine._instance = new MovementEngine();
	}
	return MovementEngine._instance;
  }

  public MovementEngine()
  {
	GeoConfig.load();
    init();
  }

  public short getType(int x, int y)
  {
    return nGetType(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4);
  }

  public short getHeight(int x, int y, int z)
  {
    return nGetHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, (short)z);
  }

  public short getSpawnHeight(int x, int y, int zmin, int zmax, int id)
  {
    return nGetSpawnHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, (short)zmin, (short)zmax, id);
  }

  public String geoPosition(int x, int y)
  {
    int gx = x - L2World.MAP_MIN_X >> 4;
    int gy = y - L2World.MAP_MIN_Y >> 4;
    return "bx: " + getBlock(gx) + " by: " + getBlock(gy) + " cx: " + getCell(gx) + " cy: " + getCell(gy) + "  region offset: " + getRegionOffset(gx, gy);
  }

  public boolean canSeeTarget(L2Object cha, Point3D target)
  {
    if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
    {
      return false;
    }

    if (cha.getZ() >= target.getZ())
    {
      return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
    }

    return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
  }

  public boolean canSeeTarget(L2Object cha, L2Object target)
  {
    int z = cha.getZ() + 45;
    int z2 = target.getZ() + 45;

    if (((cha instanceof L2SiegeGuardInstance)) || ((cha instanceof L2FortSiegeGuardInstance)))
    {
      z += 30;
    }

    if ((!(target instanceof L2DoorInstance)) && (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2)))
    {
      return false;
    }

    if ((target instanceof L2DoorInstance))
    {
      return true;
    }

    if (((target instanceof L2SiegeGuardInstance)) || ((target instanceof L2FortSiegeGuardInstance)))
    {
      z2 += 30;
    }

    if (cha.getZ() >= target.getZ())
    {
      return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
    }

    return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
  }

  public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
  {
    int z = gm.getZ() + 45;
    int z2 = target.getZ() + 45;

    if ((target instanceof L2DoorInstance))
    {
      gm.sendMessage("door always true");
      return true;
    }

    if (gm.getZ() >= target.getZ())
    {
      return canSeeDebug(gm, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2);
    }

    return canSeeDebug(gm, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z);
  }

  public short getNSWE(int x, int y, int z)
  {
    return nGetNSWE(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
  }

  public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    Location destiny = moveCheck(x, y, z, tx, ty, tz);
    return (destiny.getX() == tx) && (destiny.getY() == ty) && (destiny.getZ() == tz);
  }

  public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
  {
    if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
    {
      return new Location(x, y, z);
    }

    return moveCheck(new Location(x, y, z), new Location(tx, ty, tz), x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
  }

  public void addGeoDataBug(L2PcInstance gm, String comment)
  {
    int gx = gm.getX() - L2World.MAP_MIN_X >> 4;
    int gy = gm.getY() - L2World.MAP_MIN_Y >> 4;
    int bx = getBlock(gx);
    int by = getBlock(gy);
    int cx = getCell(gx);
    int cy = getCell(gy);
    int rx = (gx >> 11) + 16;
    int ry = (gy >> 11) + 10;
    String out = rx + ";" + ry + ";" + bx + ";" + by + ";" + cx + ";" + cy + ";" + gm.getZ() + ";" + comment + "\n";
    try
    {
      _geoBugsOut.write(out.getBytes());
      _geoBugsOut.flush();
      gm.sendMessage("GeoData bug saved!");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      gm.sendMessage("GeoData bug save Failed!");
    }
  }

  public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    return canSee(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
  }

  public boolean hasGeo(int x, int y)
  {
    int gx = x - L2World.MAP_MIN_X >> 4;
    int gy = y - L2World.MAP_MIN_Y >> 4;
    short region = getRegionOffset(gx, gy);
    return _geodata.get(Short.valueOf(region)) != null;
  }

  private static boolean canSee(int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    double dz = tz - z;
    int distance2 = dx * dx + dy * dy;

    if (distance2 > 90000)
    {
      return false;
    }
    if (distance2 < 82)
    {
      if (dz * dz > 22500.0D)
      {
        short region = getRegionOffset(x, y);
        if (_geodata.get(Short.valueOf(region)) != null)
        {
          return false;
        }
      }
      return true;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);
    double inc_z_directionx = dz * dx / distance2;
    double inc_z_directiony = dz * dy / distance2;

    int next_x = x;
    int next_y = y;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, false))
          {
            return false;
          }
          next_y += inc_y;
          z += inc_z_directiony;

          if (!nLOS(next_x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false))
          {
            return false;
          }
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, false))
          {
            return false;
          }
        }
      }
    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false))
          {
            return false;
          }
          next_x += inc_x;
          z += inc_z_directionx;

          if (!nLOS(x, next_y, (int)z, inc_x, 0, inc_z_directionx, tz, false))
          {
            return false;
          }
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false))
          {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean canSeeDebug(L2PcInstance gm, int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    double dz = tz - z;
    int distance2 = dx * dx + dy * dy;

    if (distance2 > 90000)
    {
      gm.sendMessage("dist > 300");
      return false;
    }

    if (distance2 < 82)
    {
      if (dz * dz > 22500.0D)
      {
        short region = getRegionOffset(x, y);

        if (_geodata.get(Short.valueOf(region)) != null)
        {
          return false;
        }
      }
      return true;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);
    double inc_z_directionx = dz * dx / distance2;
    double inc_z_directiony = dz * dy / distance2;

    gm.sendMessage("Los: from X: " + x + "Y: " + y + "--->> X: " + tx + " Y: " + ty);

    int next_x = x;
    int next_y = y;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, true))
          {
            return false;
          }
          next_y += inc_y;
          z += inc_z_directiony;

          if (!nLOS(next_x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true))
          {
            return false;
          }
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, true))
          {
            return false;
          }
        }
      }
    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true))
          {
            return false;
          }
          next_x += inc_x;
          z += inc_z_directionx;

          if (!nLOS(x, next_y, (int)z, inc_x, 0, inc_z_directionx, tz, true))
          {
            return false;
          }
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true))
          {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static Location moveCheck(Location startpoint, Location destiny, int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    int distance2 = dx * dx + dy * dy;

    if (distance2 == 0)
    {
      return destiny;
    }

    if (distance2 > 36100)
    {
      double divider = Math.sqrt(30000.0D / distance2);
      tx = x + (int)(divider * dx);
      ty = y + (int)(divider * dy);
      int dz = tz - startpoint.getZ();
      tz = startpoint.getZ() + (int)(divider * dz);
      dx = tx - x;
      dy = ty - y;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);

    int next_x = x;
    int next_y = y;
    double tempz = z;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;

          next_y += inc_y;

          tempz = nCanMoveNext(next_x, y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
      }

    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;

          next_x += inc_x;

          tempz = nCanMoveNext(x, next_y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == Double.MIN_VALUE)
          {
            return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
      }
    }

    if (z == startpoint.getZ())
    {
      return destiny;
    }

    return new Location(destiny.getX(), destiny.getY(), (int)z);
  }

  private static byte sign(int x)
  {
    if (x >= 0)
    {
      return 1;
    }

    return -1;
  }

  private static void init()
  {
    _log.info("|  AKUMU GEOENGINE ver. " + VERSION);
	
    long st = System.currentTimeMillis();

    File f = new File(GeoConfig.GEO_PATH);
    if ((!f.exists()) || (!f.isDirectory()))
    {
      _log.info("|- WARN! Could not load directory!");
      return;
    }

    for (File q : f.listFiles())
    {
      if ((q.isHidden()) || (q.isDirectory()) || (!q.getName().endsWith(".l2j")))
      {
        continue;
      }

      loadGeo(q);
    }

    try
    {
      _geoBugsOut = new BufferedOutputStream(new FileOutputStream(new File(GeoConfig.GEO_PATH + "geo_bugs.txt"), true));
    
    }
	catch (Exception e)
    {
      e.printStackTrace();
      throw new Error("Failed to Load geobugs.txt File.");
    }

    _log.info("|- loaded regions: " + _geodata.size());
    _log.info("|- initialized in " + (System.currentTimeMillis() - st) + " ms.");
	

    //Paths.getInstance();
  }

  public void unloadGeodata(byte rx, byte ry)
  {
    short regionoffset = (short)((rx << 5) + ry);
    _geodataIndex.remove(Short.valueOf(regionoffset));
    _geodata.remove(Short.valueOf(regionoffset));
  }

  public static boolean loadGeo(File Geo)
  {
    Scanner scanner = new Scanner(Geo.getName());
    scanner.useDelimiter("([_|\\.]){1}");
    int rx = scanner.nextInt();
    int ry = scanner.nextInt();
    scanner.close();

    short regionoffset = (short)((rx << 5) + ry);

    int index = 0; int block = 0; int flor = 0;
    FileChannel roChannel = null;
    try
    {
      roChannel = new RandomAccessFile(Geo, "r").getChannel();
      int size = (int)roChannel.size();

      MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size).load();
      geo.order(ByteOrder.LITTLE_ENDIAN);

      if (size > 196608)
      {
        IntBuffer indexs = IntBuffer.allocate(65536);
        while (block < 65536)
        {
          byte type = geo.get(index);
          indexs.put(block, index);
          block++;
          index++;

          switch (type)
          {
          case 0:
            index += 2;
            break;
          case 1:
            index += 128;
            break;
          default:
            for (int b = 0; b < 64; b++)
            {
              byte layers = geo.get(index);
              index += (layers << 1) + 1;
              if (layers <= flor)
                continue;
              flor = layers;
            }

          }

        }

        _geodataIndex.put(Short.valueOf(regionoffset), indexs);
      }
      _geodata.put(Short.valueOf(regionoffset), geo);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Failed to Load GeoFile at block: " + block + "\n");
      return false;
    }
    finally
    {
      try
      {
        roChannel.close();
      }
      catch (Exception ignored)
      {
      }
    }
    return true;
  }

  static short makeShort(byte b1, byte b0)
  {
    return (short)(b1 << 8 | b0 & 0xFF);
  }

  private static short getRegionOffset(int x, int y)
  {
    return (short)(((x >> 11) + 16 << 5) + ((y >> 11) + 10));
  }

  private static int getBlock(int geo_pos)
  {
    return (geo_pos >> 3) % 256;
  }

  private static int getCell(int geo_pos)
  {
    return geo_pos % 8;
  }

  private static short nGetType(int x, int y)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return 0;
    }
    return (short)geo.get(index);
  }

  private static short nGetHeight(int geox, int geoy, short z)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return z;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
    {
      return geo.getShort(index);
    }
    if (type == 1)
    {
      index += ((getCell(geox) << 3) + getCell(geoy) << 1);
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }

    for (int offset = (getCell(geox) << 3) + getCell(geoy); offset > 0; offset--)
    {
      index += (geo.get(index) << 1) + 1;
    }

    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return z;
    }

    short temph = Short.MIN_VALUE;
    for (; layers > 0; layers = (byte)(layers - 1))
    {
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if ((z - temph) * (z - temph) > (z - height) * (z - height))
      {
        temph = height;
      }

      index += 2;
    }

    return temph;
  }

  private static short nGetUpperHeight(int geox, int geoy, int z)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return (short)z;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
    {
      return geo.getShort(index);
    }
    if (type == 1)
    {
      index += ((getCell(geox) << 3) + getCell(geoy) << 1);
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }

    for (int offset = (getCell(geox) << 3) + getCell(geoy); offset > 0; offset--)
    {
      index += (geo.get(index) << 1) + 1;
    }

    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return (short)z;
    }
    short temph = 32767;

    for (; layers > 0; layers = (byte)(layers - 1))
    {
      temph = geo.getShort(index);
      temph = (short)(temph & 0xFFF0);
      temph = (short)(temph >> 1);

      if (temph < z)
      {
        return temph;
      }

      index += 2;
    }
    return temph;
  }

  private static short nGetSpawnHeight(int geox, int geoy, short zmin, short zmax, int id)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return zmin;
    }

    byte type = geo.get(index);
    index++;
    short temph = Short.MIN_VALUE;

    if (type == 0)
    {
      temph = geo.getShort(index);
    }
    else if (type == 1)
    {
      index += ((getCell(geox) << 3) + getCell(geoy) << 1);
      temph = geo.getShort(index);
      temph = (short)(temph & 0xFFF0);
      temph = (short)(temph >> 1);
    }
    else
    {
      for (int offset = (getCell(geox) << 3) + getCell(geoy); offset > 0; offset--)
      {
        index += (geo.get(index) << 1) + 1;
      }

      byte layers = geo.get(index);
      index++;
      if ((layers <= 0) || (layers > 125))
      {
        _log.warning("Broken geofile (multilevel), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
        return zmin;
      }

      for (; layers > 0; layers = (byte)(layers - 1))
      {
        short height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);
        if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height))
        {
          temph = height;
        }

        index += 2;
      }
      if ((temph > zmax + 200) || (temph < zmin - 200))
      {
        return zmin;
      }
    }
    if ((temph > zmax + 1000) || (temph < zmin - 1000))
    {
      return zmin;
    }
    return temph;
  }

  private static double nCanMoveNext(int x, int y, int z, int tx, int ty, int tz)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return z;
    }

    byte type = geo.get(index);
    index++;
    short NSWE = 0;

    if (type == 0)
    {
      return geo.getShort(index);
    }
    if (type == 1)
    {
      index += ((getCell(x) << 3) + getCell(y) << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0x0F);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);

      return checkNSWE(NSWE, x, y, tx, ty) ? height : Double.MIN_VALUE;
    }

    for (int offset = (getCell(x) << 3) + getCell(y); offset > 0; offset--)
    {
      index += (geo.get(index) << 1) + 1;
    }

    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case3), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return z;
    }
    short tempz = Short.MIN_VALUE;

    for (; layers > 0; layers = (byte)(layers - 1))
    {
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);

      if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
      {
        tempz = height;
        NSWE = geo.getShort(index);
        NSWE = (short)(NSWE & 0xF);
      }

      index += 2;
    }

    return checkNSWE(NSWE, x, y, tx, ty) ? tempz : Double.MIN_VALUE;
  }

  private static boolean nLOS(int x, int y, int z, int inc_x, int inc_y, double inc_z, int tz, boolean debug)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return true;
    }

    byte type = geo.get(index);
    index++;
    short NSWE = 0;

    if (type == 0)
    {
      short height = geo.getShort(index);
      if (debug)
      {
        _log.warning("flatheight:" + height);
      }
      if (z > height)
      {
        return inc_z > height;
      }

      return inc_z < height;
    }
    if (type == 1)
    {
      index += ((getCell(x) << 3) + getCell(y) << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
      {
        if (debug)
        {
          _log.warning("height:" + height + " z" + z);
        }

        return z >= nGetUpperHeight(x + inc_x, y + inc_y, height);
      }

      return true;
    }

    for (int offset = (getCell(x) << 3) + getCell(y); offset > 0; offset--)
    {
      index += (geo.get(index) << 1) + 1;
    }

    byte layers = geo.get(index);

    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case4), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return false;
    }
    short upperHeight = Short.MAX_VALUE;
    short lowerHeight = Short.MIN_VALUE;

    for (; layers > 0; layers = (byte)(layers - 1))
    {
      short tempZ = geo.getShort(index);
      tempZ = (short)(tempZ & 0x0FFF0);
      tempZ = (short)(tempZ >> 1);

      if (z > tempZ)
      {
        lowerHeight = tempZ;
        NSWE = geo.getShort(index);
        NSWE = (short)(NSWE & 0x0F);
        break;
      }

      upperHeight = tempZ;
      index += 2;
    }

    if ((z - upperHeight < -10) && (z - upperHeight > inc_z - 10.0D) && (z - lowerHeight > 40))
    {
      if (debug)
      {
        _log.warning("false, incz" + inc_z);
      }
      return false;
    }

    return (checkNSWE(NSWE, x, y, x + inc_x, y + inc_y)) || (z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight));
  }

  private short nGetNSWE(int x, int y, int z)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return 15;
    }

    byte type = geo.get(index);
    index++;
    short NSWE = 0;

    if (type == 0)
    {
      return 15;
    }
    if (type == 1)
    {
      index += ((getCell(x) << 3) + getCell(y) << 1);
      NSWE = (short)(geo.getShort(index) & 0x0F);
    }
    else
    {
      for (int offset = (getCell(x) << 3) + getCell(y); offset > 0; offset--)
      {
        index += (geo.get(index) << 1) + 1;
      }

      byte layers = geo.get(index);
      index++;

      if ((layers <= 0) || (layers > 125))
      {
        _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
        return 15;
      }

      for (short tempz = Short.MIN_VALUE; layers > 0; layers = (byte)(layers - 1))
      {
        short height = geo.getShort(index);
        height = (short)(height & 0x0fff0);
        height = (short)(height >> 1);

        if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
        {
          tempz = height;
          NSWE = (short)geo.get(index);
          NSWE = (short)(NSWE & 0x0F);
        }

        index += 2;
      }
    }
    return NSWE;
  }

  	@Override
	public Node[] getNeighbors(Node n)
	{
		Node newNode;
		int x = n.getNodeX();
		int y = n.getNodeY();
		int parentdirection = 0;
		if (n.getParent() != null) // check for not adding parent again
		{
			if (n.getParent().getNodeX() > x) parentdirection = 1;
			if (n.getParent().getNodeX() < x) parentdirection = -1;
			if (n.getParent().getNodeY() > y) parentdirection = 2;
			if (n.getParent().getNodeY() < y) parentdirection = -2;
		}
		short z = n.getZ();
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		int index = 0;
		//Geodata without index - it is just empty so index can be calculated on the fly
		if (_geodataIndex.get(region) == null)
			index = ((blockX << 8) + blockY) * 3;
		//Get Index for current block of current region geodata
		else
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		//Buffer that Contains current Region GeoData
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			return null;
		}
		
		final Node[] Neighbors = new Node[4];
		int arrayIndex = 0;
		
		//Read current block type: 0-flat,1-complex,2-multilevel
		byte type = geo.get(index);
		index++;
		if (type == 0)//flat
		{
			short height = geo.getShort(index);
			n.setZ(height);
			if (parentdirection != 1)
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				//newNode.setCost(0);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1)
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, height);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		else if (type == 1)//complex
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += ((cellX << 3) + cellY) << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short)(height >> 1); //height / 2
			n.setZ(height);
			if (NSWE != 15 && parentdirection != 0)
				return null; // no node with a block will be used
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				//newNode.setCost(basecost+50);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x-1, y, height);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		else//multilevel
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				return null;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); //height / 2

				if ((z-tempz) * (z-tempz) > (z-height) * (z-height))
				{
					tempz = height;
					NSWE = geo.get(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			n.setZ(tempz);
			if (NSWE != 15 && parentdirection != 0)
				return null; // no node with a block will be used
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x+1,y,tempz);
				//newNode.setCost(basecost+50);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, tempz);
				Neighbors[arrayIndex++] = newNode;
			}
		}
		return L2Arrays.compact(Neighbors);
	}
  
  /*public GeoNode[] getNeighbors(GeoNode n)
  {
    int x = n.getLoc().getNodeX();
    int y = n.getLoc().getNodeY();
    int parentdirection = 0;
    if (n.getParent() != null)
    {
      if (n.getParent().getLoc().getNodeX() > x)
      {
        parentdirection = 1;
      }
      else if (n.getParent().getLoc().getNodeX() < x)
      {
        parentdirection = -1;
      }

      if (n.getParent().getLoc().getNodeY() > y)
      {
        parentdirection = 2;
      }
      else if (n.getParent().getLoc().getNodeY() < y)
      {
        parentdirection = -2;
      }
    }

    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);
    short NSWE = 0;
    int index;
    if (_geodataIndex.get(Short.valueOf(region)) == null)
    {
      index = ((blockX << 8) + blockY) * 3;
    }
    else
    {
      index = ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((blockX << 8) + blockY);
    }

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      return null;
    }

    List Neighbors = new ArrayList(4);

    byte type = geo.get(index);
    index++;
    if (type == 0)
    {
      short height = geo.getShort(index);
      n.getLoc().setZ(height);
      if (parentdirection != 1)
      {
        Neighbors.add(GeoPath.getInstance().readNode(x + 1, y, height));
      }
      if (parentdirection != 2)
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y + 1, height));
      }
      if (parentdirection != -2)
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y - 1, height));
      }
      if (parentdirection != -1)
      {
        Neighbors.add(GeoPath.getInstance().readNode(x - 1, y, height));
      }
    }
    else if (type == 1)
    {
      index += ((getCell(x) << 3) + getCell(y) << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      n.getLoc().setZ(height);

      if ((NSWE != 15) && (parentdirection != 0))
      {
        return null;
      }
      if ((parentdirection != 1) && (checkNSWE(NSWE, x, y, x + 1, y)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x + 1, y, height));
      }
      if ((parentdirection != 2) && (checkNSWE(NSWE, x, y, x, y + 1)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y + 1, height));
      }
      if ((parentdirection != -2) && (checkNSWE(NSWE, x, y, x, y - 1)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y - 1, height));
      }
      if ((parentdirection != -1) && (checkNSWE(NSWE, x, y, x - 1, y)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x - 1, y, height));
      }
    }
    else
    {
      for (int offset = (getCell(x) << 3) + getCell(y); offset > 0; offset--)
      {
        index += (geo.get(index) << 1) + 1;
      }

      byte layers = geo.get(index);
      index++;

      if ((layers <= 0) || (layers > 125))
      {
        _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
        return null;
      }
      short tempz = -32768;

      for (short z = n.getLoc().getZ(); layers > 0; layers = (byte)(layers - 1))
      {
        short height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);

        if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
        {
          tempz = height;
          NSWE = (short)geo.get(index);
          NSWE = (short)(NSWE & 0xF);
        }

        index += 2;
      }

      n.getLoc().setZ(tempz);

      if ((NSWE != 15) && (parentdirection != 0))
      {
        return null;
      }
      if ((parentdirection != 1) && (checkNSWE(NSWE, x, y, x + 1, y)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x + 1, y, tempz));
      }
      if ((parentdirection != 2) && (checkNSWE(NSWE, x, y, x, y + 1)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y + 1, tempz));
      }
      if ((parentdirection != -2) && (checkNSWE(NSWE, x, y, x, y - 1)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x, y - 1, tempz));
      }
      if ((parentdirection != -1) && (checkNSWE(NSWE, x, y, x - 1, y)))
      {
        Neighbors.add(GeoPath.getInstance().readNode(x - 1, y, tempz));
      }
    }

    return (GeoNode[])Neighbors.toArray(new GeoNode[Neighbors.size()]);
  }*/

  private static boolean checkNSWE(short NSWE, int x, int y, int tx, int ty)
  {
		if (NSWE == 15)
		   return true;
		if (tx > x)//E
		{
			if ((NSWE & _e) == 0)
				return false;
		}
		else if (tx < x)//W
		{
			if ((NSWE & _w) == 0)
				return false;
		}
		if (ty > y)//S
		{
			if ((NSWE & _s) == 0)
				return false;
		}
		else if (ty < y)//N
		{
			if ((NSWE & _n) == 0)
				return false;
		}
		return true;
  }
}