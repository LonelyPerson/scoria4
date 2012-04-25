package com.l2scoria.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2scoria.gameserver.model.entity.event.FunEvent;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.templates.L2NpcTemplate;

public class L2CustomEventManagerInstance extends L2NpcInstance
{
	public FunEvent _event = null;

	public L2CustomEventManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void showChatWindow(L2PcInstance player, int val)
	{
		if ((player == null) || (_event == null))
		{
			return;
		}

		if (!player.getEventName().equals("") && !player.getEventName().equalsIgnoreCase(this._event.EVENT_NAME))
		{
			player.sendMessage("You are already registered in other Fun Event.");
			player.sendPacket(new ActionFailed());
			return;
		}

		if ((Olympiad.getInstance().isRegistered(player)) || (player.getOlympiadGameId() != -1))
		{
			player.sendMessage("You are already registered in olympiad match.");
			player.sendPacket(new ActionFailed());
			return;
		}

		NpcHtmlMessage npcHtmlMessage = _event.getChatWindow(player);

		if (npcHtmlMessage != null)
		{
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(npcHtmlMessage);
		}

		player.sendPacket(new ActionFailed());
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();

		if (currentCommand.startsWith("join"))
		{
			int joinTeamId = Integer.parseInt(st.nextToken());
			_event.addPlayer(player, joinTeamId);
			showChatWindow(player, 0);
		}
		else if (currentCommand.startsWith("leave"))
		{
			_event.removePlayer(player);
			showChatWindow(player, 0);
		}
		else
		{
			showChatWindow(player, 0);
		}
	}
}