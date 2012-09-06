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
package com.l2scoria.gameserver.handler.itemhandlers;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.handler.IItemHandler;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2GourdInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

public class Nectar implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6391
	};

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!(playable.isPlayer))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if(!(activeChar.getTarget() instanceof L2GourdInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		if(activeChar.getName() != ((L2GourdInstance) activeChar.getTarget()).getOwner())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2Object[] targets = new L2Object[1];
		targets[0] = activeChar.getTarget();

		int itemId = item.getItemId();
		if(itemId == 6391)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(9999, 1), false, false);
		}

		activeChar = null;
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
