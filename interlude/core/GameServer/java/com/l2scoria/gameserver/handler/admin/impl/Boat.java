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
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.model.actor.instance.L2BoatInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

public class Boat extends AdminAbst
{
	public Boat()
	{
		_commands = new String[]{"admin_boat"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		L2BoatInstance boat = activeChar.getBoat();

		if(boat == null)
		{
			activeChar.sendMessage("Usage only possible while riding a boat.");
			return false;
		}

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if(st.hasMoreTokens())
		{
			String cmd = st.nextToken();
			if(cmd.equals("cycle"))
			{
				if(boat.isInCycle())
				{
					boat.stopCycle();
					activeChar.sendMessage("Boat cycle stopped.");
				}
				else
				{
					boat.startCycle();
					activeChar.sendMessage("Boat cycle started.");
				}
			}
			else if(cmd.equals("reload"))
			{
				boat.reloadPath();
				activeChar.sendMessage("Boat path reloaded.");
			}
			else
			{
				showUsage(activeChar);
			}
		}
		else
		{
			activeChar.sendMessage("====== Boat Information ======");
			activeChar.sendMessage("Name: " + boat.getBoatName() + " (" + boat.getId() + ") ObjId: " + boat.getObjectId());
			activeChar.sendMessage("Cycle: " + boat.isInCycle() + " (" + boat.getCycle() + ")");
			activeChar.sendMessage("Players inside: " + boat.getSizeInside());
			activeChar.sendMessage("Position: " + boat.getX() + " " + boat.getY() + " " + boat.getZ() + " " + boat.getPosition().getHeading());
			activeChar.sendMessage("==============================");
		}

		st = null;
		boat = null;

		return true;
	}

	private void showUsage(L2PcInstance cha)
	{
		cha.sendMessage("Usage: //boat [cycle|reload]");
	}
}