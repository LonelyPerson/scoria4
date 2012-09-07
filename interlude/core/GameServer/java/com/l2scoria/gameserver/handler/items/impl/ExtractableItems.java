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

package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.datatables.csv.ExtractableItemsData;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.model.L2ExtractableItem;
import com.l2scoria.gameserver.model.L2ExtractableProductItem;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.random.Rnd;

/**
 * @author FBIagent 11/12/2006
 */

public class ExtractableItems extends ItemAbst
{

	public ExtractableItems()
	{
		_items = ExtractableItemsData.getInstance().itemIDs();
		_playerUseOnly = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		if (item.getCount() > 1)
		{
			String message = HtmCache.getInstance().getHtm("data/html/others/extractable.htm");
			if (message != null)
			{
				message = message.replace("%objectId%", String.valueOf(item.getObjectId()));
				message = message.replace("%itemname%", item.getItemName());
				message = message.replace("%count%", String.valueOf(item.getCount()));
				playable.sendPacket(new NpcHtmlMessage(5, message));
				return true;
			}
		}

		doExtract(playable, item, 1);
		return true;
	}

	public void doExtract(L2PlayableInstance playable, L2ItemInstance item, int count)
	{
		if (!(playable.isPlayer))
		{
			return;
		}

		L2PcInstance activeChar = playable.getPlayer();
		int itemID = item.getItemId();

		if (count > item.getCount())
		{
			return;
		}

		while (count-- > 0)
		{
			L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);
			if (exitem == null)
			{
				return;
			}

			int createItemID = 0, createAmount = 0, rndNum = Rnd.get(100), chanceFrom = 0;
			for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
			{
				int chance = expi.getChance();

				if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
				{
					createItemID = expi.getId();
					createAmount = expi.getAmmount();
					break;
				}

				chanceFrom += chance;
			}

			if (createItemID == 0)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
				return;
			}

			if (createItemID > 0)
			{
				if (ItemTable.getInstance().createDummyItem(createItemID) == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}

				if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
				{
					activeChar.addItem("Extract", createItemID, createAmount, item, false);
				}
				else
				{
					for (int i = 0; i < createAmount; i++)
					{
						activeChar.addItem("Extract", createItemID, 1, item, false);
					}
				}
				SystemMessage sm;

				if (createAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID);
					sm.addNumber(createAmount);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(createItemID);
				}

				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_INSIDE_THAT));
			}

			activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
		}
	}
}
