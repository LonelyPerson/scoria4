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

import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.TradeList;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2scoria.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final String _C__74_SETPRIVATESTORELISTSELL = "[C] 74 SetPrivateStoreListSell";
	private static Logger _log = Logger.getLogger(SetPrivateStoreListSell.class.getName());

	private boolean _packageSale;
	private Item[] _items = null;

	@Override
	protected void readImpl()
	{
		_packageSale = readD() == 1;
		int count = readD();

		if(count < 1 || count * 12 > _buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			return;
		}

		_items = new Item[count];

		for(int i = 0; i < count; i++)
		{
			int objectId = readD();
			int cnt = readD();
			int price = readD();

			if(objectId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(objectId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_SELL + 1)
		{
			_log.info("Player " + player.getName() + " trying to set PrivateStoreListSell without RequestPrivateStoreManageSell!");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (_items == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ITEM_COUNT));
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.isCastingNow())
		{
			player.sendMessage("Trade are disabled while casting");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.isProcessingTransaction())
		{
			player.sendMessage("Store mode are disable while trading");
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			return;
		}

		if(player.getActiveEnchantItem() != null)
		{
			_log.info("Player " + player.getName() + " trying to set privat store list sell while enchanting item");
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
					player.sendPacket(new PrivateStoreManageListSell(player));
					return;
				}
			}
		}

		// Check maximum number of allowed slots for pvt shops
		if(_items.length > player.GetPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		if(_items.length < 1)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);

		long totalCost = player.getAdena();
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}

			totalCost += i.getPrice();
			if (totalCost > Integer.MAX_VALUE)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
		}

		player.sitDown();
		if(_packageSale)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);
		}
		else
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
		}
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}

	private static class Item
	{
		private final int _itemId;
		private final int _count;
		private final int _price;
		
		public Item(int id, int num, int pri)
		{
			_itemId = id;
			_count = num;
			_price = pri;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((Integer.MAX_VALUE / _count) < _price)
				return false;
			
			list.addItem(_itemId, _count, _price);
			return true;
		}
		
		public long getPrice()
		{
			return _count * _price;
		}
	}

	@Override
	public String getType()
	{
		return _C__74_SETPRIVATESTORELISTSELL;
	}
}
