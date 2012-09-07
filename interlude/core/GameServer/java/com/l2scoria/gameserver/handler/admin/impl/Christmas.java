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

import com.l2scoria.gameserver.managers.ChristmasManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * @version $Revision: 1.2.4.4 $ $Date: 2007/07/31 10:06:02 $
 */
public class Christmas extends AdminAbst
{
	public Christmas()
	{
		_commands = new String[]{"admin_christmas_start", "admin_christmas_end"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.equals("admin_christmas_start"))
		{
			startChristmas(activeChar);
		}
		else if(command.equals("admin_christmas_end"))
		{
			endChristmas(activeChar);
		}

		return true;
	}

	private void startChristmas(L2PcInstance activeChar)
	{
		ChristmasManager.getInstance().init(activeChar);
	}

	private void endChristmas(L2PcInstance activeChar)
	{
		ChristmasManager.getInstance().end(activeChar);
	}
}
