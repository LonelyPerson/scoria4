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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;

/**
 * This class handles following admin commands: - admin_banchat = Imposes a chat ban on the specified player/target. -
 * admin_unbanchat = Removes any chat ban on the specified player/target. Uses: admin_banchat [<player_name>]
 * [<ban_duration>] admin_unbanchat [<player_name>] If <player_name> is not specified, the current target player is
 * used.
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class BanChat extends Admin
{
	public BanChat()
	{
		_commands = new String[]{"admin_banchat", "admin_unbanchat"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		String[] cmdParams = command.split(" ");
		long banLength = -1;

		L2Object targetObject = null;
		L2PcInstance targetPlayer = null;

		if(cmdParams.length > 1)
		{
			targetPlayer = L2World.getInstance().getPlayer(cmdParams[1]);

			if(cmdParams.length > 2)
			{
				try
				{
					banLength = Integer.parseInt(cmdParams[2]);
				}
				catch(NumberFormatException nfe)
				{
					//ignore
				}
			}
		}
		else
		{
			if(activeChar.getTarget() != null)
			{
				targetObject = activeChar.getTarget();

				if(targetObject != null && targetObject instanceof L2PcInstance)
				{
					targetPlayer = (L2PcInstance) targetObject;
				}
			}
		}

		if(targetPlayer == null)
		{
			activeChar.sendMessage("Incorrect parameter or target.");

			return false;
		}

		if(command.startsWith("admin_banchat"))
		{
			if (targetPlayer.isChatBanned())
			{
				activeChar.sendMessage(targetPlayer.getName() + " chat is already banned for " + targetPlayer.getChatBanTimer()/60/1000 + " minutes.");
			}
			else
			{
				if(banLength == -1)
				{
					targetPlayer.setChatBanned(true, -1);
					activeChar.sendMessage(targetPlayer.getName() + " is now chat banned for ever.");
				}
				else
				{
					targetPlayer.setChatBanned(true, banLength * 60000L);
					activeChar.sendMessage(targetPlayer.getName() + " is now chat banned for " + banLength + " minutes.");
				}

				if(Config.ANNOUNCE_BAN)
				{
					if(cmdParams.length > 3)
						Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " забанил чат игроку " + targetPlayer.getName() + " на " + (banLength > 0 ? banLength + " минут." : "всегда.") + " Причина: " + command.substring(cmdParams[0].length()+cmdParams[1].length()+cmdParams[2].length()+3));
					else
						Announcements.getInstance().specialAnnounceToAll(activeChar.getName() + " забанил чат игроку " + targetPlayer.getName() + " на " + (banLength > 0 ? banLength + " минут." : "всегда."));
				}
			}
		}
		else if(command.startsWith("admin_unbanchat"))
		{
			activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
			targetPlayer.setChatBanned(false, 0);
		}

		targetPlayer = null;

		cmdParams = null;
		targetObject = null;

		return true;
	}
}
