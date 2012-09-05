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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.clientpackets.RequestActionUse;
import com.l2scoria.gameserver.network.serverpackets.Ride;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Akumu, TODO nothing.
 */
public class RideWyvern extends Admin
{
	public RideWyvern()
	{
		_commands = new String[]{"admin_ride_wyvern", "admin_ride_strider", "admin_unride_wyvern", "admin_unride_strider", "admin_unride"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_ride"))
		{
			if(activeChar.isMounted() || activeChar.getPet() != null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Already Have a Pet or Mounted.");
				activeChar.sendPacket(sm);
				sm = null;

				return false;
			}

			if (activeChar._event!=null && !activeChar._event.canDoAction(activeChar, RequestActionUse.ACTION_MOUNT))
			{
				return false;
			}

			int _petRideId;
			if(command.startsWith("admin_ride_wyvern"))
			{
				_petRideId = 12621;
			}
			else if(command.startsWith("admin_ride_strider"))
			{
				_petRideId = 12526;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Command '" + command + "' not recognized");
				activeChar.sendPacket(sm);
				sm = null;

				return false;
			}

			if(!activeChar.disarmWeapons())
				return false;

			Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, _petRideId);
			activeChar.sendPacket(mount);
			activeChar.broadcastPacket(mount);
			activeChar.setMountType(mount.getMountType());
			mount = null;
		}
		else if(command.startsWith("admin_unride"))
		{
			if(activeChar.setMountType(0))
			{
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				activeChar.broadcastPacket(dismount);
				dismount = null;
			}
		}
		return true;
	}
}
