package ru.akumu.geoengine.paths.tools;

import ru.akumu.geoengine.paths.GeoNode;

public class FastNodeList
{
  private final GeoNode[] _list;
  private int _size;

  public FastNodeList(int size)
  {
  this._list = new GeoNode[size];
  this._size = 0;
  }

  public void add(GeoNode n)
  {
  }

  public boolean contains(GeoNode n)
  {

    return false;
  }

  public boolean containsRev(GeoNode n)
  {
    return false;
  }
}