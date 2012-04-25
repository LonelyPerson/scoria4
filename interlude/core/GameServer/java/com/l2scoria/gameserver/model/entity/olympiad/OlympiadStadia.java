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
package com.l2scoria.gameserver.model.entity.olympiad;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.util.L2FastList;

public class OlympiadStadia extends Olympiad
{
	private boolean _freeToUse = true;
	public L2FastList<L2PcInstance> _spectators;

	public boolean isFreeToUse()
	{
		return _freeToUse;
	}

	public void setStadiaBusy()
	{
		_freeToUse = false;
	}

	public void setStadiaFree()
	{
		_freeToUse = true;
	}

	private int[] _coords = new int[3];

	public int[] getCoordinates()
	{
		return _coords;
	}

	public OlympiadStadia(int[] coords)
	{
		_coords = coords;
		_spectators = new L2FastList<L2PcInstance>();
	}

	public OlympiadStadia(int x, int y, int z)
	{
		_coords[0] = x;
		_coords[1] = y;
		_coords[2] = z;
		_spectators = new L2FastList<L2PcInstance>();
	}

	protected L2FastList<L2PcInstance> getSpectators()
	{
		return _spectators;
	}

	protected void addSpectator(L2PcInstance spec)
	{
		_spectators.add(spec);
	}

	protected void removeSpectator(L2PcInstance spec)
	{
		if(_spectators != null && _spectators.contains(spec))
		{
			_spectators.remove(spec);
		}
	}
}
