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
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - kill = kills target L2Character - kill_monster = kills target
 * non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. -
 * kill_monster <radius> = If radius is specified, then ALL non-players only in that radius will be killed.
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class Kill extends Admin
{
	private static Logger _log = Logger.getLogger(Kill.class.getName());

	public Kill()
	{
		_commands = new String[]{"admin_kill", "admin_kill_monster"};
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if(st.hasMoreTokens())
			{
				String firstParam = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);

				if(plyr != null)
				{
					if(st.hasMoreTokens())
					{
						try
						{
							int radius = Integer.parseInt(st.nextToken());

							for(L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if(knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
								{
									continue;
								}

								kill(activeChar, knownChar);
							}

							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");

							return true;
						}
						catch(NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					else
					{
						kill(activeChar, plyr);
					}
				}
				else
				{
					try
					{
						int radius = Integer.parseInt(firstParam);

						for(L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if(knownChar == null || knownChar instanceof L2ControllableMobInstance || knownChar.equals(activeChar))
							{
								continue;
							}

							kill(activeChar, knownChar);
						}

						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");

						return true;
					}
					catch(NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
				}

				firstParam = null;
				plyr = null;
			}
			else
			{
				L2Object obj = activeChar.getTarget();

				if(obj == null || obj instanceof L2ControllableMobInstance || !(obj.isCharacter))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				}
				else
				{
					kill(activeChar, (L2Character) obj);
				}

				obj = null;
			}

			st = null;
		}

		return true;
	}

	private void kill(L2PcInstance activeChar, L2Character target)
	{
		if(target.isPlayer)
		{
			// e.g. invincibility effect
			if(!target.getPlayer().isGM())
			{
				target.stopAllEffects();
			}

			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else if(Config.L2JMOD_CHAMPION_ENABLE && target.isChampion())
		{
			target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar);
		}
		else
		{
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}

		if(Config.DEBUG)
		{
			_log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
		}
	}
}
