/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2scoria.gameserver.model;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location
{
	public int x;
	public int y;
	public int z;
	public int h;

	public Location(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location(int x, int y, int z, int heading)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		h = heading;
	}

	public void set(int x, int y, int z, int h)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.h = h;
	}

	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public int getHeading()
	{
		return h;
	}
        /*
	public boolean equals(int _x, int _y, int _z)
	{
		return _x == x && _y == y && _z == z;
	}

	public Location world2geo()
	{
		x = x - L2World.MAP_MIN_X >> 4;
		y = y - L2World.MAP_MIN_Y >> 4;
		return this;
	}

	public Location geo2world()
	{
		x = (x << 4) + L2World.MAP_MIN_X + 8;
		y = (y << 4) + L2World.MAP_MIN_Y + 8;
		return this;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public void setZ(int z)
	{
		this.z = z;
	}

	public Location setH(int h)
	{
		this.h = h;
		return this;
	}

	@Override
	public Location clone()
	{
		return new Location(x, y, z, h);
	} */
}
