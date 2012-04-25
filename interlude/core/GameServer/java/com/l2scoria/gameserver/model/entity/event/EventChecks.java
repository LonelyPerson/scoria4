package com.l2scoria.gameserver.model.entity.event;

import java.util.Vector;

import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

public class EventChecks
{
	private static boolean checkIfOtherEvent(L2PcInstance player)
	{
		/*if (player.inSoloEvent || player.inPartyEvent || player.inClanEvent)
		{
			player.sendMessage("You're alredy registered in another event.");
			return true;
		}*/
		return false;
	}

	public static boolean checkPlayer(L2PcInstance player, int eventType, int points, int minPeople, Vector<L2PcInstance> _eventPlayers)
	{
		int eventPoints = 0;

		if (player == null)
		{
			return false;
		}

		if ((_eventPlayers.size() <= minPeople) && (eventType == 3))
		{
			player.sendMessage("Not enough " + eType(eventType) + " members of the connected at this mommtent, try again later.");
			return false;
		}

		for (L2PcInstance member : _eventPlayers)
		{
			if (member == null)
			{
				continue;
			}
			if (checkIfOtherEvent(member))
			{
				String badRequestor = member.getName();
				notifyBadRequestor(player, badRequestor, 2, _eventPlayers);
				return false;
			}

			switch (eventType)
			{
				case 2:
					if ((!_eventPlayers.contains(player)) || (!member.getClan().getName().equals(player.getClan().getName())))
						continue;
					eventPoints += member.getEventPoints();
					break;
				case 3:
					eventPoints += member.getEventPoints();
					break;
				default:
					eventPoints = member.getEventPoints();
			}

		}

		if (eventPoints >= points)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				for (L2Effect effect : member.getAllEffects())
				{
					if (effect != null)
					{
						effect.exit();
					}
				}
			}
			return true;
		}

		if (eventType != 1)
		{
			player.sendMessage("The totality of your " + eType(eventType) + " members don't have enough Event Points to participate.");
			return false;
		}

		player.sendMessage("Not enough Event Points to participate into the Event.");
		return false;
	}

	private static void notifyBadRequestor(L2PcInstance player, String badRequestor, int type, Vector<L2PcInstance> _eventPlayers)
	{
		if (type == 2)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				member.sendMessage("You can't access the event while " + badRequestor + "is singed up for another event.");
			}
		}
		if (type == 3)
		{
			for (L2PcInstance member : _eventPlayers)
			{
				member.sendMessage("You can't access the event while " + badRequestor + "is singed up for another event.");
			}
		}
	}

	public static boolean usualChecks(L2PcInstance player, int minLevel)
	{
		if (player.getLevel() < minLevel)
		{
			player.sendMessage("The minimum level to participate in this Event is " + minLevel + ". You cannot participate.");
			return false;
		}
		/*if (player.inClanEvent || player.inPartyEvent || player.inSoloEvent)
		{
			player.sendMessage("You're alredy registered in another Event.");
			return false;
		}*/
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("You can Not register while Having a Cursed Weapon.");
			return false;
		}
		if (player.isInStoreMode())
		{
			player.sendMessage("Cannot Participate while in Store Mode.");
			return false;
		}
		if (player.isInJail())
		{
			player.sendMessage("Cannot Participate while in Jail.");
			return false;
		}
		return true;
	}

	public static String eType(int type)
	{
		String sType;
		if (type == 1)
		{
			sType = "Single";
		}
		else
		{
			if (type == 2) {
				sType = "Clan";
			}
			else
			{
				if (type == 3)
					sType = "Party";
				else
					sType = "error ocurred while getting type of Event."; 
			}
		}
		return sType;
	}
}