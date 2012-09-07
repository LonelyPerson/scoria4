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
import com.l2scoria.gameserver.handler.admin.impl.AdminAbst;
import com.l2scoria.gameserver.handler.admin.impl.AIndex;
import com.l2scoria.gameserver.handler.admin.impl.Announce;
import com.l2scoria.gameserver.handler.admin.impl.BBS;
import com.l2scoria.gameserver.handler.admin.impl.Ban;
import com.l2scoria.gameserver.handler.admin.impl.BanChat;
import com.l2scoria.gameserver.handler.admin.impl.Boat;
import com.l2scoria.gameserver.handler.admin.impl.Buffs;
import com.l2scoria.gameserver.handler.admin.impl.Cache;
import com.l2scoria.gameserver.handler.admin.impl.ChangeAccessLevel;
import com.l2scoria.gameserver.handler.admin.impl.CreateItem;
import com.l2scoria.gameserver.handler.admin.impl.CursedWeapons;
import com.l2scoria.gameserver.handler.admin.impl.Delete;
import com.l2scoria.gameserver.handler.admin.impl.Donator;
import com.l2scoria.gameserver.handler.admin.impl.DoorControl;
import com.l2scoria.gameserver.handler.admin.impl.EditChar;
import com.l2scoria.gameserver.handler.admin.impl.EditNpc;
import com.l2scoria.gameserver.handler.admin.impl.Effects;
import com.l2scoria.gameserver.handler.admin.impl.Enchant;
import com.l2scoria.gameserver.handler.admin.impl.ExpSp;
import com.l2scoria.gameserver.handler.admin.impl.FightCalculator;
import com.l2scoria.gameserver.handler.admin.impl.FortSiege;
import com.l2scoria.gameserver.handler.admin.impl.Geodata;
import com.l2scoria.gameserver.handler.admin.impl.Gm;
import com.l2scoria.gameserver.handler.admin.impl.GmChat;
import com.l2scoria.gameserver.handler.admin.impl.Heal;
import com.l2scoria.gameserver.handler.admin.impl.HelpPage;
import com.l2scoria.gameserver.handler.admin.impl.Hero;
import com.l2scoria.gameserver.handler.admin.impl.InstanceControl;
import com.l2scoria.gameserver.handler.admin.impl.Invul;
import com.l2scoria.gameserver.handler.admin.impl.Kick;
import com.l2scoria.gameserver.handler.admin.impl.Kill;
import com.l2scoria.gameserver.handler.admin.impl.Level;
import com.l2scoria.gameserver.handler.admin.impl.Login;
import com.l2scoria.gameserver.handler.admin.impl.MRace;
import com.l2scoria.gameserver.handler.admin.impl.Mammon;
import com.l2scoria.gameserver.handler.admin.impl.Manor;
import com.l2scoria.gameserver.handler.admin.impl.MassControl;
import com.l2scoria.gameserver.handler.admin.impl.MassRecall;
import com.l2scoria.gameserver.handler.admin.impl.Menu;
import com.l2scoria.gameserver.handler.admin.impl.MonsterGroup;
import com.l2scoria.gameserver.handler.admin.impl.Noble;
import com.l2scoria.gameserver.handler.admin.impl.PacketForge;
import com.l2scoria.gameserver.handler.admin.impl.Petition;
import com.l2scoria.gameserver.handler.admin.impl.Pledge;
import com.l2scoria.gameserver.handler.admin.impl.Polymorph;
import com.l2scoria.gameserver.handler.admin.impl.Quest;
import com.l2scoria.gameserver.handler.admin.impl.Reload;
import com.l2scoria.gameserver.handler.admin.impl.RepairChar;
import com.l2scoria.gameserver.handler.admin.impl.Res;
import com.l2scoria.gameserver.handler.admin.impl.RideWyvern;
import com.l2scoria.gameserver.handler.admin.impl.Script;
import com.l2scoria.gameserver.handler.admin.impl.ServerShutdown;
import com.l2scoria.gameserver.handler.admin.impl.Shop;
import com.l2scoria.gameserver.handler.admin.impl.Siege;
import com.l2scoria.gameserver.handler.admin.impl.Skill;
import com.l2scoria.gameserver.handler.admin.impl.Spawn;
import com.l2scoria.gameserver.handler.admin.impl.Target;
import com.l2scoria.gameserver.handler.admin.impl.Teleport;
import com.l2scoria.gameserver.handler.admin.impl.Test;
import com.l2scoria.gameserver.handler.admin.impl.UnblockIp;
import com.l2scoria.gameserver.handler.admin.impl.Walker;
import com.l2scoria.gameserver.handler.admin.impl.Zone;
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

	private Map<String, AdminAbst> _datatable;

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
		_datatable = new HashMap<String, AdminAbst>();
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
		register(new com.l2scoria.gameserver.handler.admin.impl.Christmas());
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

	public void register(AdminAbst handler)
	{
		String[] ids = handler.getAdminCommandList();

		if(ids == null)
		{
			return;
		}

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
	}

	public AdminAbst getAdminCommandHandler(String adminCommand)
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
