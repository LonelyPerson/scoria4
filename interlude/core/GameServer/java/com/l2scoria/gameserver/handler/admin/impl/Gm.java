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
import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands: - gm = turns gm mode on/off
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class Gm extends AdminAbst
{
	private static Logger _log = Logger.getLogger(Gm.class.getName());

	public Gm()
	{
		_commands = new String[]{"admin_gm"};
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.equals("admin_gm"))
		{
			handleGm(activeChar);
		}

		return true;
	}

	private void handleGm(L2PcInstance activeChar)
	{
		if(activeChar.isGM())
		{
			GmListTable.getInstance().deleteGm(activeChar);
//			activeChar.setIsGM(false);

			activeChar.sendMessage("You no longer have GM status.");

			if(Config.DEBUG)
			{
				_log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status off");
			}
		}
		else
		{
			GmListTable.getInstance().addGm(activeChar, false);
//			activeChar.setIsGM(true);

			activeChar.sendMessage("You now have GM status.");

			if(Config.DEBUG)
			{
				_log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status on");
			}
		}
	}
}
