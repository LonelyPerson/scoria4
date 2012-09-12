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

package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.Ride;
import com.l2scoria.gameserver.util.Broadcast;

/**
 * Support for /dismount command.
 * 
 * @author Micht
 */
public class DisMount extends CommandAbst
{
	public DisMount()
	{
		_commands = new int[]{62};
	}

	@Override
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		if(activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if(activeChar.isMounted())
		{
			if(activeChar.setMountType(0))
			{
				if(activeChar.isFlying())
				{
					activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				}

				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0), 810000/*900*/);
				activeChar.setMountObjectID(0);
			}
		}

		return true;
	}
}
