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
import com.l2scoria.gameserver.model.ItemRequest;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.TradeList;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreSell extends L2GameClientPacket
{
	//	private static final String _C__96_SENDPRIVATESTOREBUYBUYLIST = "[C] 96 SendPrivateStoreBuyBuyList";
	private static final String _C__96_REQUESTPRIVATESTORESELL = "[C] 96 RequestPrivateStoreSell";
	private static Logger _log = Logger.getLogger(RequestPrivateStoreSell.class.getName());

	private int _storePlayerId;
	private ItemRequest[] _items = null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if(count <= 0 || count * 20 > _buf.remaining() || count > Config.MAX_ITEM_IN_PACKET)
		{
			return;
		}
		_items = new ItemRequest[count];

		for(int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readD(); //TODO analyse this
			int cnt = readD();
			int price = readD();

			if (objectId < 1 || itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object object = L2World.getInstance().findObject(_storePlayerId);
		if(object == null || !(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance) object;
		if(storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
			return;

		if(!player.isInsideRadius(storePlayer, 150, true, false))
			return;

		TradeList storeList = storePlayer.getBuyList();
		if(storeList == null)
			return;

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(!storeList.PrivateStoreSell(player, _items))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			_log.warn("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}

		if(storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return _C__96_REQUESTPRIVATESTORESELL;
	}
}
