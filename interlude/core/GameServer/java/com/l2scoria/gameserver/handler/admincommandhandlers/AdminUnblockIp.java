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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.IAdminCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>admin_unblockip</li>
 * </ul>
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminUnblockIp implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_unblockip"
	};

	private static final Logger _logAudit = Logger.getLogger("gmaudit");

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, com.l2scoria.gameserver.model.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

		if(Config.GMAUDIT)
		{
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setLoggerName("gmaudit");
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
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

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean unblockIp(String ipAddress, L2PcInstance activeChar)
	{
		//LoginServerThread.getInstance().unBlockip(ipAddress);
		_log.warning("IP removed by GM " + activeChar.getName());

		return true;
	}

}