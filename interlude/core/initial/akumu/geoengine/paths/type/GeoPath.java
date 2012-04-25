package ru.akumu.geoengine.paths.type;

import java.util.List;

import ru.akumu.geoengine.MoveAbstract;
import ru.akumu.geoengine.MovementEngine;
import ru.akumu.geoengine.paths.GeoNode;
import ru.akumu.geoengine.paths.Paths;

public class GeoPath extends Paths
{
  private static GeoPath _instance;

  public static GeoPath getInstance()
  {
    return GeoPath._instance = new GeoPath();
  }

  GeoPath()
  {
  }

  public boolean pathNodesExist(short regionoffset)
  {
    return false;
  }

  public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz)
  {
      return null;
  }

  public GeoNode[] readNeighbors(GeoNode n, int idx)
  {
    return MoveAbstract.getInstance().getNeighbors(n);
  }

  public GeoNode readNode(int gx, int gy, short z)
  {
    return new GeoNode(new GeoNodeLoc(gx, gy, z), 0);
  }
}