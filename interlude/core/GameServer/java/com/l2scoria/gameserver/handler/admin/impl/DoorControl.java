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

import com.l2scoria.gameserver.datatables.sql.DoorTable;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;

/**
 * This class handles following admin commands:<br>
 * - open1 = open coloseum door 24190001<br>
 * - open2 = open coloseum door 24190002<br>
 * - open3 = open coloseum door 24190003<br>
 * - open4 = open coloseum door 24190004<br>
 * - openall = open all coloseum door<br>
 * - close1 = close coloseum door 24190001<br>
 * - close2 = close coloseum door 24190002<br>
 * - close3 = close coloseum door 24190003<br>
 * - close4 = close coloseum door 24190004<br>
 * - closeall = close all coloseum door<br>
 * <br>
 * - open = open selected door<br>
 * - close = close selected door<br>
 * 
 * @version $Revision: 1.3 $
 * @author Akumu, ProGramMoS
 */
public class DoorControl extends AdminAbst
{
	public DoorControl()
	{
		_commands = new String[]{"admin_open", "admin_close", "admin_openall", "admin_closeall"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		L2Object target2 = null;

		if(command.startsWith("admin_close ")) //id
		{
			try
			{
				int doorId = Integer.parseInt(command.substring(12));

				if(DoorTable.getInstance().getDoor(doorId) != null)
				{
					DoorTable.getInstance().getDoor(doorId).closeMe();
				}
				else
				{
					for(Castle castle : CastleManager.getInstance().getCastles())
						if(castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.equals("admin_close")) //target
		{
			target2 = activeChar.getTarget();

			if(target2.isDoor)
			{
				((L2DoorInstance) target2).closeMe();
			}
			else
			{
				activeChar.sendMessage("Incorrect target.");
			}

			target2 = null;
		}
		else if(command.startsWith("admin_open ")) //id
		{
			try
			{
				int doorId = Integer.parseInt(command.substring(11));

				if(DoorTable.getInstance().getDoor(doorId) != null)
				{
					DoorTable.getInstance().getDoor(doorId).openMe();
				}
				else
				{
					for(Castle castle : CastleManager.getInstance().getCastles())
						if(castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.equals("admin_open")) //target
		{
			target2 = activeChar.getTarget();

			if(target2.isDoor)
			{
				((L2DoorInstance) target2).openMe();
			}
			else
			{
				activeChar.sendMessage("Incorrect target.");
			}

			target2 = null;
		}

		// need optimize cycle
		// set limits on the ID doors that do not cycle to close doors
		else if(command.equals("admin_closeall"))
		{
			try
			{
				for(L2DoorInstance door : DoorTable.getInstance().getDoors())
				{
					door.closeMe();
				}

				for(Castle castle : CastleManager.getInstance().getCastles())
				{
					for(L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else if(command.equals("admin_openall"))
		{
			// need optimize cycle
			// set limits on the PH door to do a cycle of opening doors.
			try
			{
				for(L2DoorInstance door : DoorTable.getInstance().getDoors())
				{
					door.openMe();
				}

				for(Castle castle : CastleManager.getInstance().getCastles())
				{
					for(L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}
}
