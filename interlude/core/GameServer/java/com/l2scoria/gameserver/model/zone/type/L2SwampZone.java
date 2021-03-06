/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;

/**
 * another type of zone where your speed is changed
 * 
 * @author kerberos
 */
public class L2SwampZone extends L2ZoneDefault
{
	private int _move_bonus;

	public L2SwampZone(int id)
	{
		super(id);

		// Setup default speed reduce (in %)
		_move_bonus = -50;
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("move_bonus"))
		{
			_move_bonus = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_SWAMP, true);
		if(character.isPlayer)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_SWAMP, false);
		if(character.isPlayer)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}

		super.onExit(character);
	}

	public int getMoveBonus()
	{
		return _move_bonus;
	}

}
