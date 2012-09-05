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
import com.l2scoria.gameserver.model.BlockList;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SendTradeRequest;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.Util;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class TradeRequest extends L2GameClientPacket
{
	private static final String TRADEREQUEST__C__15 = "[C] 15 TradeRequest";
	private static Logger _log = Logger.getLogger(TradeRequest.class.getName());

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = L2World.getInstance().findObject(_objectId);
		if(target == null || !player.getKnownList().knowsObject(target) || !(target instanceof L2PcInstance) || target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2PcInstance partner = (L2PcInstance) target;

		if(!partner.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your partner");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target cant request trade in Olympiad mode");
			return;
		}

		if(partner.isStunned() || partner.isConfused() || partner.isAway() || partner.isCastingNow() || partner.isInDuel() || partner.isImobilised() || partner.isParalyzed() || partner.inObserverMode() || partner.isAttackingNow())
		{
			player.sendMessage("You cannot Request a Trade at This Time.");
			return;
		}

		if(player.isStunned() || player.isConfused() || player.isAway() || player.isCastingNow() || player.isInDuel() || player.isImobilised() || player.isParalyzed() || player.inObserverMode() || player.isAttackingNow())
		{
			player.sendMessage("You cannot Request a Trade at This Time.");
			return;
		}

		if(player.getDistanceSq(partner) > 22500) // 150
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
			return;
		}

		if (BlockList.isBlocked(partner, player))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}

		// Alt game - Karma punishment
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || partner.getKarma() > 0))
		{
			player.sendMessage("Chaotic players can't use Trade.");
			return;
		}

		if(player.getPrivateStoreType() != 0 || partner.getPrivateStoreType() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if(!Config.ALLOW_LOW_LEVEL_TRADE)
			if(player.getLevel() < 76 && partner.getLevel() >= 76 || partner.getLevel() < 76 || player.getLevel() >= 76)
			{
				player.sendMessage("You Cannot Trade a Lower Level Character");
			}

		if(player.isProcessingTransaction())
		{
			if(Config.DEBUG)
			{
				_log.info("already trading with someone");
			}

			player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_TRADING));
			return;
		}

		if(partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if(Config.DEBUG)
			{
				_log.info("transaction already in progress.");
			}

			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}

		if(Util.calculateDistance(player, partner, true) > 150)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.TARGET_TOO_FAR);
			player.sendPacket(sm);
			return;
		}

		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}

	@Override
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}
