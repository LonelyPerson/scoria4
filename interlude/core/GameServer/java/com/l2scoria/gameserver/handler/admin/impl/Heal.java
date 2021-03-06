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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands: - heal = restores HP/MP/CP on target, name or radius
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class Heal extends AdminAbst
{
	private static Logger _log = Logger.getLogger(Res.class.getName());

	public Heal()
	{
		_commands = new String[]{"admin_heal"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.equals("admin_heal"))
		{
			handleRes(activeChar);
		}
		else if(command.startsWith("admin_heal"))
		{
			try
			{
				String healTarget = command.substring(11);
				handleRes(activeChar, healTarget);
				healTarget = null;
			}
			catch(StringIndexOutOfBoundsException e)
			{
				if(Config.DEVELOPER)
				{
					System.out.println("Heal error: " + e);
				}

				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Incorrect target/radius specified.");
				activeChar.sendPacket(sm);
				sm = null;
			}
		}
		return true;
	}

	private void handleRes(L2PcInstance activeChar)
	{
		handleRes(activeChar, null);
	}

	private void handleRes(L2PcInstance activeChar, String player)
	{
		L2Object obj = activeChar.getTarget();

		if(player != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(player);

			if(plyr != null)
			{
				obj = plyr;
			}
			else
			{
				try
				{
					int radius = Integer.parseInt(player);
					for(L2Object object : activeChar.getKnownList().getKnownObjects().values())
					{
						if(object.isCharacter)
						{
							L2Character character = (L2Character) object;
							character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());

							if(object.isPlayer)
							{
								character.setCurrentCp(character.getMaxCp());
							}

							character = null;
						}
					}
					activeChar.sendMessage("Healed within " + radius + " unit radius.");
					return;
				}
				catch(NumberFormatException nbe)
				{
					//ignore
				}
			}

			plyr = null;
		}

		if(obj == null)
		{
			obj = activeChar;
		}

		if(obj != null && obj.isCharacter)
		{
			L2Character target = (L2Character) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());

			if(target.isPlayer)
			{
				target.setCurrentCp(target.getMaxCp());
			}

			if(Config.DEBUG)
			{
				_log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") healed character " + target.getName());
			}

			target = null;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}

		obj = null;
	}
}
