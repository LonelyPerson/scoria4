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

import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2scoria.Config;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminBBS;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminBanChat;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminBoat;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminBuffs;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminCache;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminChristmas;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminDonator;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminFortSiege;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminGeodata;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminHero;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminKill;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminLogin;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMassControl;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMassRecall;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMobGroup;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminNoble;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminQuest;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminReload;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminRepairChar;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminScript;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminShutdown;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminTest;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import com.l2scoria.gameserver.handler.admincommandhandlers.AdminZone;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private final static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());

	private static AdminCommandHandler _instance;

	private FastMap<String, IAdminCommandHandler> _datatable;

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
		_datatable = new FastMap<String, IAdminCommandHandler>();
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminBoat());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminScript());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminChristmas());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminBanChat());
		registerAdminCommandHandler(new AdminReload());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminFightCalculator());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminFortSiege());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminBBS());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminTest());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminMassRecall());
		registerAdminCommandHandler(new AdminMassControl());
		registerAdminCommandHandler(new AdminMobGroup());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminUnblockIp());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminRideWyvern());
		registerAdminCommandHandler(new AdminLogin());
		registerAdminCommandHandler(new AdminCache());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminQuest());
		registerAdminCommandHandler(new AdminZone());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminDonator());
		registerAdminCommandHandler(new AdminHero());
		registerAdminCommandHandler(new AdminNoble());
		registerAdminCommandHandler(new AdminBuffs());
		_log.info("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for(String element : ids)
		{
			if(Config.DEBUG)
			{
				_log.fine("Adding handler for command " + element);
			}

			if(_datatable.keySet().contains(new String(element)))
			{
				_log.warning("Duplicated command \"" + element + "\" definition in " + handler.getClass().getName() + ".");
			}
			else
			{
				_datatable.put(element, handler);
			}
		}
		ids = null;
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;

		if(adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}

		if(Config.DEBUG)
		{
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		}

		return _datatable.get(command);
	}
}