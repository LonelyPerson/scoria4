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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * <b>This class handles Access Level Management commands:</b><br>
 * <br>
 */
public class ChangeAccessLevel extends Admin
{
	public ChangeAccessLevel()
	{
		_commands = new String[]{"admin_changelvl"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		handleChangeLevel(command, activeChar);

		return true;
	}

	/**
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, L2PcInstance activeChar)
	{
		if(activeChar == null)
			return;

		String[] parts = command.split(" ");

		if(parts.length == 2)
		{
			int lvl = Integer.parseInt(parts[1]);

			if(activeChar.getTarget() instanceof L2PcInstance)
			{
				((L2PcInstance) activeChar.getTarget()).setAccessLevel(lvl);
                                if(lvl < 0) 
                                {
                                    ((L2PcInstance) activeChar.getTarget()).closeNetConnection();
                                }
				activeChar.sendMessage("You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}
		else if(parts.length == 3)
		{
			int lvl = Integer.parseInt(parts[2]);

			L2PcInstance player = L2World.getInstance().getPlayer(parts[1]);

			if(player != null)
			{
				player.setAccessLevel(lvl);
                                if(lvl < 0) 
                                {
                                    ((L2PcInstance) activeChar.getTarget()).closeNetConnection();
                                }
				activeChar.sendMessage("You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}

		parts = null;
	}
}
