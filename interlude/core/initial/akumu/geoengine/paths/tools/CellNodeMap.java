package ru.akumu.geoengine.paths.tools;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import ru.akumu.geoengine.paths.GeoNode;
import ru.akumu.geoengine.paths.type.AbstractNodeLoc;

public class CellNodeMap
{
  private final Map<Integer, ArrayList<GeoNode>> _cellIndex;

  public CellNodeMap()
  {
	this._cellIndex = new HashMap();
  }

  public void add(GeoNode n)
  {
  }

  public boolean contains(GeoNode n)
  {
    return false;
  }
}