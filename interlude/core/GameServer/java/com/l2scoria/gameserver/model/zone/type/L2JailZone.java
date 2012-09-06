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
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * A jail zone
 * 
 * @author durgus
 */
public class L2JailZone extends L2ZoneDefault
{
	public L2JailZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character.isPlayer)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, true);
			character.setInsideZone(L2Character.ZONE_PVP, true);
			character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character.isPlayer)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, false);
			character.setInsideZone(L2Character.ZONE_PVP, false);
			character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}

		super.onExit(character);
	}
}
