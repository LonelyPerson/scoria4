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

import com.l2scoria.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

public class Kick extends Admin
{
	public Kick()
	{
		_commands = new String[]{"admin_kick", "admin_kick_non_gm"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_kick"))
		{
			StringTokenizer st = new StringTokenizer(command);

			if(st.countTokens() > 1)
			{
				st.nextToken();

				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);

				if(plyr != null)
				{
					if (plyr.getClient() != null)
					{
						plyr.logout();
					}
					else
					{
						plyr.deleteMe();
					}
					activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
					RegionBBSManager.getInstance().changeCommunityBoard();
				}

				player = null;
				plyr = null;
			}

			st = null;
		}

		if(command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;

			for(L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if(!player.isGM())
				{
					counter++;
					if (player.getClient() != null)
					{
						player.logout();
					}
					else
					{
						player.deleteMe();
					}
				}
				RegionBBSManager.getInstance().changeCommunityBoard();
			}
			activeChar.sendMessage("Kicked " + counter + " players");
		}
		return true;
	}
}