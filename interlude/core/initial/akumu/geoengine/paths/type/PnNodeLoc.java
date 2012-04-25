package ru.akumu.geoengine.paths.type;

import com.l2scoria.gameserver.model.L2World;

class PnNodeLoc extends AbstractNodeLoc
{

 private final short _x;
  private final short _y;
  private final short _z;

  public PnNodeLoc(short x, short y, short z)
  {
    this._x = x;
    this._y = y;
    this._z = z;
  }

  public int getX()
  {
    return 1;
  }

  public int getY()
  {
    return 1;
  }

  public short getZ()
  {
    return 1;
  }

  public void setZ(short z)
  {
  }

  public int getNodeX()
  {
    return 1;
  }

  public int getNodeY()
  {
    return 1;
  }
}