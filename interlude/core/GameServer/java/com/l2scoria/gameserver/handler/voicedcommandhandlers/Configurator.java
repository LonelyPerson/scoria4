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
package com.l2scoria.gameserver.handler.voicedcommandhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;

 /**
  * Author: m095, DedMoroz
  * EmuRT DevTeam, Scoria
  **/

public class Configurator implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"menu",
		"autoloot",
		"autolearnskills",
		"setxprate"
	};
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String text)
	{
		if (activeChar.isInOlympiadMode() || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cant use it right now.");
			return true;
		}

		if (command.startsWith("menu") && Config.ALLOW_SHOW_MENU)
		{
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("autoloot"))
		{
			if (!Config.ALLOW_SET_AUTOLOOT)
			{
				activeChar.sendMessage("Current option is not enabled.");
				return true;
			}

			if (activeChar.getAutoLoot())
			{
				activeChar.setAutoLoot(false);
				activeChar.sendMessage("Auto loot: off");
			}
			else
			{
				activeChar.setAutoLoot(true);
				activeChar.sendMessage("Auto loot: on");
			}
			return true;
		}
		else if (command.startsWith("autolearnskills"))
		{
			if (!Config.ALLOW_SET_AUTOLEARNSKILL)
			{
				activeChar.sendMessage("Current option is not enabled.");
				return true;
			}

			if (activeChar.getAutoLearnSkill())
			{
				activeChar.setAutoLearnSkill(false);
				activeChar.sendMessage("Auto learn skill: off");
			}
			else
			{
				activeChar.setAutoLearnSkill(true);
				activeChar.sendMessage("Auto learn skill: on");
			}
			return true;
		}
		else if (command.startsWith("setxprate"))
		{
			if (!Config.ALLOW_SET_XP_RATE)
			{
				activeChar.sendMessage("Current option is not enabled.");
				return true;
			}

			try
			{
				float rate = Float.parseFloat(text);
				if (rate > Config.RATE_XP)
				{
					activeChar.sendMessage("Value is to high, max is: " + Config.RATE_XP);
					return true;
				}
				else if (rate < 0)
				{
					activeChar.sendMessage("Value is to low, min is: 0.0");
					return true;
				}
				activeChar.setXpRate(rate);
				activeChar.sendMessage("XP rate set to: " + rate);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: .setxprate [value from 0.0 to " + Config.RATE_XP + "]");
			}

			return true;
		}
		return false;
	}

	private String getLootMode(L2PcInstance activeChar)
	{
		String result = "<font color=FF0000>OFF</font>";
		if (activeChar.getAutoLoot())
			result = "<font color=00FF00>ON</font>";
		return result;
	}

	private String getAutoLearnMode(L2PcInstance activeChar)
	{
		String result = "<font color=FF0000>OFF</font>";
		if (activeChar.getAutoLearnSkill())
			result = "<font color=00FF00>ON</font>";
		return result;
	}
	
	private String getExpRate(L2PcInstance activeChar)
	{
		if(activeChar.isDonator())
			return "<font color=FF8000>" + activeChar.getXpRate() * Config.DONATOR_XPSP_RATE + "</font>";
		else
			return "<font color=00FF00>" + activeChar.getXpRate() + "</font>";
	}

	private void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		html.setFile("data/html/custom/menu.htm");
		html.replace("%xprate%", getExpRate(activeChar));
		html.replace("%autoloot%", getLootMode(activeChar));
		html.replace("%learnskills%", getAutoLearnMode(activeChar));
		activeChar.sendPacket(html);    
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}