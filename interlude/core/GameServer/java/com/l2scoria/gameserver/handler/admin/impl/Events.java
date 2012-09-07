package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.handler.AdminCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.GameEventManager;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Akumu
 * @date 21:33/05.09.12
 */
public class Events extends AdminAbst
{
	public Events()
	{
		_commands = new String[]{"events"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		String[] params = command.split(" ");

		if (params[0].equals("events"))
		{
			if (params.length == 3)
			{
				GameEvent evt = GameEventManager.getInstance().findEvent(params[1]);
				if (evt != null)
				{
					if (params[2].equals("start"))
					{
						if (evt.getState() == GameEvent.STATE_INACTIVE)
						{
							evt.start();
						}
					}
					else if (params[2].equals("stop"))
					{
						if (evt.getState() != GameEvent.STATE_INACTIVE)
						{
							evt.finish();
						}
					}
				}
			}

			NpcHtmlMessage msg = new NpcHtmlMessage(1);
			msg.setFile("data/html/admin/events.htm");
			String html = "";

			for (GameEvent evt : GameEventManager.getInstance().getAllEvents())
			{
				html += "<tr><td>";

				if (AdminCommandHandler.getInstance().getAdminCommandHandler(evt.getName()) != null)
				{
					html += "<a action=\"bypass admin_" + evt.getName() + "\">" + evt.getName() + "</a>";
				}
				else
				{
					html += evt.getName();
				}

				html += "</td><td><font color=\"LEVEL\">";

				if (evt.getState() == GameEvent.STATE_INACTIVE)
				{
					html += "остановлен";
				}
				else if (evt.getState() == GameEvent.STATE_ACTIVE)
				{
					html += "регистрация";
				}
				else if (evt.getState() == GameEvent.STATE_RUNNING)
				{
					html += "игра запущена";
				}

				html += "</font></td><td>";

				html += "<button width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_events ";
				html += (evt.getName() + " " + (evt.getState() == GameEvent.STATE_INACTIVE ? "start" : "stop"));
				html += "\" value=\"";
				html += ((evt.getState() == GameEvent.STATE_INACTIVE ? "СТАРТ" : "СТОП") + "\"></td>");
				html += "</tr>";
			}
			msg.replace("%events%", html);
			activeChar.sendPacket(msg);
		}

		return true;
	}
}
