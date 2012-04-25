package ru.akumu.geoengine.paths.tools;

import ru.akumu.geoengine.paths.GeoNode;

public class BinaryNodeHeap
{
  private final GeoNode[] _list;
  private int _size;

  public BinaryNodeHeap(int size)
  {
	this._list = new GeoNode[size + 1];
	this._size = 0;
  }

  public void add(GeoNode n)
  {
  }

  public GeoNode removeFirst()
  {
    return null;
  }

  public boolean contains(GeoNode n)
  {

    return false;
  }

  public boolean isEmpty()
  {
    return false;
  }
}