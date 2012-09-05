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
package com.l2scoria.gameserver.handler;

import com.l2scoria.Config;
import com.l2scoria.gameserver.GameServer;
import com.l2scoria.gameserver.handler.usercommandhandlers.*;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class UserCommandHandler
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private static UserCommandHandler _instance;

	private Map<Integer, IUserCommandHandler> _datatable;

	public static UserCommandHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new UserCommandHandler();
		}

		return _instance;
	}

	private UserCommandHandler()
	{
		_datatable = new FastMap<Integer, IUserCommandHandler>();
		registerUserCommandHandler(new Time());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new ChannelLeave());
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelListUpdate());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new DisMount());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new Mount());
		registerUserCommandHandler(new PartyInfo());
		_log.info("UserCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();

		for(int id : ids)
		{
			if(Config.DEBUG)
			{
				_log.info("Adding handler for user command " + id);
			}
			_datatable.put(new Integer(id), handler);
		}
		ids = null;
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		if(Config.DEBUG)
		{
			_log.info("getting handler for user command: " + userCommand);
		}

		return _datatable.get(new Integer(userCommand));
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}
