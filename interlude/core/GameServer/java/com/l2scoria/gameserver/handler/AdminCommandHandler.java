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
import com.l2scoria.gameserver.handler.admincommandhandlers.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private final static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());

	private static AdminCommandHandler _instance;

	private Map<String, Admin> _datatable;

	public static AdminCommandHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new AdminCommandHandler();
		}
		return _instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new HashMap<String, Admin>();
		register(new AIndex());
		register(new Invul());
		register(new Boat());
		register(new Delete());
		register(new Kill());
		register(new Target());
		register(new Shop());
		register(new Announce());
		register(new CreateItem());
		register(new Heal());
		register(new HelpPage());
		register(new ServerShutdown());
		register(new Spawn());
		register(new Skill());
		register(new Script());
		register(new ExpSp());
		register(new GmChat());
		register(new EditChar());
		register(new Gm());
		register(new Teleport());
		register(new RepairChar());
		register(new ChangeAccessLevel());
		register(new Christmas());
		register(new Ban());
		register(new Polymorph());
		register(new BanChat());
		register(new Reload());
		register(new Kick());
		register(new MRace());
		register(new EditNpc());
		register(new FightCalculator());
		register(new Menu());
		register(new Siege());
		register(new FortSiege());
		register(new Petition());
		register(new PacketForge());
		register(new BBS());
		register(new Effects());
		register(new DoorControl());
		register(new Test());
		register(new Enchant());
		register(new MassRecall());
		register(new MassControl());
		register(new MonsterGroup());
		register(new Res());
		register(new Mammon());
		register(new UnblockIp());
		register(new Pledge());
		register(new RideWyvern());
		register(new Login());
		register(new Cache());
		register(new Level());
		register(new Quest());
		register(new Zone());
		register(new CursedWeapons());
		register(new Geodata());
		register(new Manor());
		register(new Donator());
		register(new Hero());
		register(new Noble());
		register(new Buffs());
		register(new InstanceControl());
		register(new Walker());
		_log.info("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void register(Admin handler)
	{
		String[] ids = handler.getAdminCommandList();
		for(String element : ids)
		{
			if(Config.DEBUG)
			{
				_log.info("Adding handler for command " + element);
			}

			if(_datatable.keySet().contains(element))
			{
				_log.warn("Duplicated command \"" + element + "\" definition in " + handler.getClass().getName() + ".");
			}
			else
			{
				_datatable.put(element, handler);
			}
		}
		ids = null;
	}

	public Admin getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;

		if(adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}

		if(Config.DEBUG)
		{
			_log.info("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		}

		return _datatable.get(command);
	}
}
