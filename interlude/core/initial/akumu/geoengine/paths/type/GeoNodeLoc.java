package ru.akumu.geoengine.paths.type;

class GeoNodeLoc extends AbstractNodeLoc
{
  private final int _x;
  private final int _y;
  private short _z;

  public GeoNodeLoc(int x, int y, short z)
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