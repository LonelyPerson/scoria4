/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;

/**
 * Bighead zones give entering players big heads
 * 
 * @author durgus
 */
public class L2BigheadZone extends L2ZoneDefault
{
	public L2BigheadZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		startEffect(character);
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		stopEffect(character);
		super.onExit(character);
	}

	@Override
	protected void onDieInside(L2Character character)
	{
		stopEffect(character);
	}

	@Override
	protected void onReviveInside(L2Character character)
	{
		startEffect(character);
	}

	private void startEffect(L2Character character)
	{
		if(character.isPlayer)
		{
			character.stopAbnormalEffect((short) 0x2000);
		}
	}


	private void stopEffect(L2Character character)
	{
		if(character.isPlayer)
		{
			character.startAbnormalEffect(0x2000);
		}
	}
}
