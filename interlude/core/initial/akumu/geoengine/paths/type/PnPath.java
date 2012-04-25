package ru.akumu.geoengine.paths.type;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import ru.akumu.geoengine.GeoConfig;
import ru.akumu.geoengine.MoveAbstract;
import ru.akumu.geoengine.paths.GeoNode;
import ru.akumu.geoengine.paths.Paths;

public class PnPath extends Paths
{
  private static PnPath _instance;
  private static final Map<Short, ByteBuffer> _pathNodes = new HashMap();
  private static final Map<Short, IntBuffer> _pathNodesIndex = new HashMap();

  public static PnPath getInstance()
  {
    return PnPath._instance = new PnPath();
  }

  public boolean pathNodesExist(short regionoffset)
  {
    return true;
  }

  public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz)
  {
     return null;
  }

  public GeoNode[] readNeighbors(GeoNode n, int idx)
  {
    return null;
  }

  private GeoNode readNode(short node_x, short node_y, byte layer)
  {
     return null;
  }

  private GeoNode readNode(int gx, int gy, int z)
  {
    return null;
  }

  private PnPath()
  {
  }

  private void loadFile(byte rx, byte ry)
  {
	 IntBuffer indexs = IntBuffer.allocate(65536);
  }
}