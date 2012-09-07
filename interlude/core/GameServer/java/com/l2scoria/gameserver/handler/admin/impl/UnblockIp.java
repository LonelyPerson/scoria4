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

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>admin_unblockip</li>
 * </ul>
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class UnblockIp extends AdminAbst
{
	private static final Logger _log = Logger.getLogger(Teleport.class.getName());

	public UnblockIp()
	{
		_commands = new String[]{"admin_unblockip"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_unblockip "))
		{
			try
			{
				String ipAddress = command.substring(16);

				if(unblockIp(ipAddress, activeChar))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Removed IP " + ipAddress + " from blocklist!");
					activeChar.sendPacket(sm);
					sm = null;
				}

				ipAddress = null;
			}
			catch(StringIndexOutOfBoundsException e)
			{
				// Send syntax to the user
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Usage mode: //unblockip <ip>");
				activeChar.sendPacket(sm);
				sm = null;
			}
		}

		return true;
	}

	private boolean unblockIp(String ipAddress, L2PcInstance activeChar)
	{
		//LoginServerThread.getInstance().unBlockip(ipAddress);
		_log.warn("IP removed by GM " + activeChar.getName());

		return true;
	}

}
