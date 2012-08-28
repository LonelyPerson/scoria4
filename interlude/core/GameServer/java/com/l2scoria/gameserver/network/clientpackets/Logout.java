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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.taskmanager.AttackStanceTaskManager;

import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class Logout extends L2GameClientPacket
{
	private static final String _C__09_LOGOUT = "[C] 09 Logout";
	private static Logger _log = Logger.getLogger(Logout.class.getName());

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		// Dont allow leaving if player is fighting
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
			return;

		if(player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			return;
		}

		if(player.isLocked())
		{
			_log.warning("Player " + player.getName() + " tried to logout during class change.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		player.getInventory().updateDatabase();

		if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
		{
			if(Config.DEBUG)
			{
				_log.fine("Player " + player.getName() + " tried to logout while fighting");
			}

			player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player) || Olympiad.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("You cant logout in olympiad mode");
			return;
		}

		// Prevent player from logging out if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(player.isFestivalParticipant())
		{
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot log out while you are a participant in a festival.");
				return;
			}

			L2Party playerParty = player.getParty();
			if(playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}

		if(player.isOnline() == 1 && L2World.getInstance().getPlayer(player.getName()) != null)
		{
			if(player.getLevel() >= Config.OFFLINE_MIN_LEVEL && (player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE))
			{
				try
				{
					player.setOffline(true);
					player.leaveParty();
					if (player.getPet() != null)
					{
						player.getPet().unSummon(player);
					}
					if(Config.OFFLINE_SET_NAME_COLOR)
					{
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR,false);
						player.broadcastUserInfo();
					}
					player.setOfflineStartTime(System.currentTimeMillis());
					player.store();
				}
				catch(Exception e){}
			}
		}
		else
		{
			_log.warning("[Offline Trade] Player " + player.getName() + " is already offline. Cheater?");
		}

		player.closeNetConnection();
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__09_LOGOUT;
	}
}
