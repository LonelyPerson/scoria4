package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.admin.IAdminCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Logger;

/**
 * @author Akumu
 * @date 19:50/05.09.12
 */
public abstract class AdminAbst implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger("gmaudit");
	protected String[] _commands = null;

	public String[] getAdminCommandList()
	{
		return _commands;
	}

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}

		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			return false;
		}

		if (Config.GMAUDIT)
		{
			_log.info("GM: " + activeChar.getName()+ " to target [" + activeChar.getTarget() + "]: " + command);
		}

		return true;
	}
}
