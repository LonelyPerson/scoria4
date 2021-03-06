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
package com.l2scoria.util.random;

import com.l2scoria.gameserver.model.Location;

/**
 * $
 * 
 * @author Balancer
 */
public class Rnd
{
	private static MTRandom _rnd = new MTRandom();

	public static final float get() // get random number from 0 to 1
	{
		return _rnd.nextFloat();
	}

	/**
	 * Gets a random number from 0(inclusive) to n(exclusive)
	 * 
	 * @param n The superior limit (exclusive)
	 * @return A number from 0 to n-1
	 */
	public static final int get(int n)
	{
		return (int) Math.floor(_rnd.nextDouble() * n);
	}

	public static final int get(int min, int max) // get random number from min to max (not max-1 !)
	{
		return min + (int) Math.floor(_rnd.nextDouble() * (max - min + 1));
	}

	public static final int nextInt(int n)
	{
		return (int) Math.floor(_rnd.nextDouble() * n);
	}

	public static final int nextInt()
	{
		return _rnd.nextInt();
	}

	public static final double nextDouble()
	{
		return _rnd.nextDouble();
	}

	public static final double nextGaussian()
	{
		return _rnd.nextGaussian();
	}

	public static final boolean nextBoolean()
	{
		return _rnd.nextBoolean();
	}

	public static final void nextBytes(byte[] array)
	{
		_rnd.nextBytes(array);
	}

	public static Location coordsRandomize(int x, int y, int z, int heading, int radius_min, int radius_max)
	{
		if(radius_max == 0 || radius_max < radius_min)
			return new Location(x, y, z, heading);

		int radius = get(radius_min, radius_max);
		double angle = _rnd.nextDouble() * 2 * Math.PI;

		return new Location((int) (x + radius * Math.cos(angle)), (int) (y + radius * Math.sin(angle)), z, heading);
	}
}
