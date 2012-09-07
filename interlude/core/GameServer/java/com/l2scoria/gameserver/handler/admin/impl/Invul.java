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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2007/07/31 10:06:02 $
 */
public class Invul extends AdminAbst
{
	private static Logger _log = Logger.getLogger(Invul.class.getName());

	public Invul()
	{
		_commands = new String[]{"admin_invul", "admin_setinvul"};
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}
		if(command.equals("admin_invul"))
		{
			handleInvul(activeChar);
		}

		if(command.equals("admin_setinvul"))
		{
			L2Object target = activeChar.getTarget();

			if(target.isPlayer)
			{
				handleInvul(target.getPlayer());
			}

			target = null;
		}

		return true;
	}

	private void handleInvul(L2PcInstance activeChar)
	{
		String text;

		if(activeChar.isInvul())
		{
			activeChar.setIsInvul(false);
			text = activeChar.getName() + " is now mortal";
			if(Config.DEBUG)
			{
				_log.info("GM: Gm removed invul mode from character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
			}
		}
		else
		{
			activeChar.setIsInvul(true);
			text = activeChar.getName() + " is now invulnerable";
			if(Config.DEBUG)
			{
				_log.info("GM: Gm activated invul mode for character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
			}
		}

		activeChar.sendMessage(text);
		text = null;
	}
}
