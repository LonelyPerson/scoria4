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
import com.l2scoria.gameserver.model.TradeList;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2scoria.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListBuy";

	private static Logger _log = Logger.getLogger(SetPrivateStoreListBuy.class.getName());

	private Item[] _items = null;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if(count < 1 || count * 12 > _buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			return;
		}

		_items = new Item[count];

		for(int i = 0; i < count; i++)
		{
			int itemId = readD();

			int enchant = readD();

			int cnt = readD();
			int price = readD();

			if (itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}

			_items[i] = new Item(itemId, cnt, price, enchant);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY + 1)
		{
			_log.info("Player " + player.getName() + " trying to set PrivateStoreListBuy without RequestPrivateStoreManageBuy!");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		if (_items == null)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		if(player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place.");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.isCastingNow())
		{
			player.sendMessage("Trade are disabled while casting.");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.isProcessingTransaction())
		{
			player.sendMessage("Store mode are disable while trading.");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.getActiveEnchantItem() != null)
		{
			_log.info("Player " + player.getName() + " trying to set privat store list buy while enchanting item");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Config.ALT_PRIVATE_STORE_DISTANCE > 0)
		{
			for(L2PcInstance knownChar : player.getKnownList().getKnownPlayersInRadius(Config.ALT_PRIVATE_STORE_DISTANCE))
			{
				if(knownChar != null &&
					(knownChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY ||
					knownChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL ||
					knownChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL ||
					knownChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE))
				{
					player.sendMessage("You can`t open private store beside other trader.");
					player.sendPacket(new PrivateStoreManageListBuy(player));
					return;
				}
			}
		}

		// Check maximum number of allowed slots for pvt shops
		if(_items.length > player.GetPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		if(_items.length < 1)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		TradeList tradeList = player.getBuyList();
		tradeList.clear();

		long totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				_log.info("Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + Integer.MAX_VALUE + " adena in Private Store - Buy.");
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
			
			totalCost += i.getCost();
			if (totalCost > Integer.MAX_VALUE)
			{
				_log.info("Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + Integer.MAX_VALUE + " adena in Private Store - Buy.");
				player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
				return;
			}
		}

		// Check for available funds
		if(totalCost > player.getAdena() || totalCost < 1)
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
			return;
		}

		player.sitDown();
		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}

	private static class Item
	{
		private final int _itemId;
		private final int _count;
		private final int _price;
		private final int _enchant;
		public Item(int id, int num, int pri, int enchant)
		{
			_itemId = id;
			_count = num;
			_price = pri;
            _enchant = enchant;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / _count) < _price)
				return false;
			
			list.addItemByItemId(_itemId, _count, _price, _enchant);
			return true;
		}
		
		public long getCost()
		{
			return _count * _price;
		}
	}

	@Override
	public String getType()
	{
		return _C__91_SETPRIVATESTORELISTBUY;
	}

}
