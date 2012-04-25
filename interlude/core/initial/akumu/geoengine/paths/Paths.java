package ru.akumu.geoengine.paths;

import java.util.List;
import java.util.LinkedList;

import ru.akumu.geoengine.MoveAbstract;
import ru.akumu.geoengine.paths.tools.BinaryNodeHeap;
import ru.akumu.geoengine.paths.tools.CellNodeMap;
import ru.akumu.geoengine.paths.tools.FastNodeList;
import ru.akumu.geoengine.paths.type.AbstractNodeLoc;
import ru.akumu.geoengine.paths.type.GeoPath;
import ru.akumu.geoengine.paths.type.PnPath;
import ru.akumu.geoengine.tools.SimpleMaths;

public abstract class Paths
{
  public static Paths getInstance()
  {
    return PnPath.getInstance();
  }

  public abstract boolean pathNodesExist(short paramShort);

  public abstract List<AbstractNodeLoc> findPath(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);

  public abstract GeoNode[] readNeighbors(GeoNode paramGeoNode, int paramInt);

  public List<AbstractNodeLoc> search(GeoNode start, GeoNode end)
  {
    return null;
  }

  protected List<AbstractNodeLoc> searchByClosest(GeoNode start, GeoNode end)
  {
    return null;
  }

  protected List<AbstractNodeLoc> searchByClosest2(GeoNode start, GeoNode end)
  {
    return null;
  }

  public List<AbstractNodeLoc> searchAStar(GeoNode start, GeoNode end)
  {
    return null;
  }

  List<AbstractNodeLoc> constructPath(GeoNode node)
  {
	LinkedList path = new LinkedList();
    return path;
  }

  List<AbstractNodeLoc> constructPath2(GeoNode node)
  {
	LinkedList path = new LinkedList();
    return path;
  }

  protected short getNodePos(int geo_pos)
  {
    return (short)(geo_pos >> 3);
  }

  protected short getNodeBlock(int node_pos)
  {
    return (short)(node_pos % 256);
  }

  protected byte getRegionX(int node_pos)
  {
    return (byte)((node_pos >> 8) + 15);
  }

  protected byte getRegionY(int node_pos)
  {
    return (byte)((node_pos >> 8) + 10);
  }

  protected short getRegionOffset(byte rx, byte ry)
  {
    return (short)((rx << 5) + ry);
  }

  public int calculateWorldX(short node_x)
  {
    return 48;
  }

  public int calculateWorldY(short node_y)
  {
    return 48;
  }
}