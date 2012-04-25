package ru.akumu.geoengine.paths;

import ru.akumu.geoengine.paths.type.AbstractNodeLoc;

public class GeoNode
{
  private final AbstractNodeLoc _loc;
  private final int _neighborsIdx;
  private GeoNode[] _neighbors;
  private GeoNode _parent;
  private short _cost;

  public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
  {
    this._loc = Loc;
    this._neighborsIdx = Neighbors_idx;
  }

  public void setParent(GeoNode p)
  {
    this._parent = p;
  }

  public void setCost(int cost)
  {
    this._cost = (short)cost;
  }

  public void attachNeighbors()
  {
    this._neighbors = (this._loc != null ? Paths.getInstance().readNeighbors(this, this._neighborsIdx) : null);
  }

  public GeoNode[] getNeighbors()
  {
    return this._neighbors;
  }

  public GeoNode getParent()
  {
    return this._parent;
  }

  public AbstractNodeLoc getLoc()
  {
    return this._loc;
  }

  public short getCost()
  {
    return this._cost;
  }

  public boolean equals(Object arg0)
  {
    return ((arg0 instanceof GeoNode)) && (this._loc.getX() == ((GeoNode)arg0).getLoc().getX()) && (this._loc.getY() == ((GeoNode)arg0).getLoc().getY()) && (this._loc.getZ() == ((GeoNode)arg0).getLoc().getZ());
  }

  public int getX()
  {
    return this._loc.getX();
  }

  public int getY()
  {
    return this._loc.getY();
  }

  public int getZ()
  {
    return this._loc.getZ();
  }
}