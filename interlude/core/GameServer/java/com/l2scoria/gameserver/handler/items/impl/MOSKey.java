/* This program is free software; you can redistribute it and/or modify
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

import com.l2scoria.gameserver.datatables.sql.DoorTable;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.lang.ArrayUtils;

/**
 * @author chris
 */
public class MOSKey extends ItemAbst
{
	public MOSKey()
	{
		_items = new int[]{8056};

		_playerUseOnly = true;
		_notWhenSkillsDisabled = true;
		_notInCombat = true;
		_requiresTarget = true;
		_minInteractionDistance = 150;
	}

	private static int[] REGISTERED_DOORS = new int[]{23150003, 23150004};

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		L2Object target = activeChar.getTarget();

		if (!(target.isDoor))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		L2DoorInstance door = (L2DoorInstance) target;

		if(!ArrayUtils.contains(REGISTERED_DOORS, door.getDoorId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (door._lastOpen + 1800000 > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return false;
		}

		switch (door.getDoorId())
		{
			case 23150003:
			{
				door.openMe();
				door.onOpen();
				L2DoorInstance d = DoorTable.getInstance().getDoor(23150004);
				d.openMe();
				d.onOpen();
				break;
			}

			case 23150004:
			{
				door.openMe();
				door.onOpen();
				L2DoorInstance d = DoorTable.getInstance().getDoor(23150003);
				d.openMe();
				d.onOpen();
				break;
			}
		}

		activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
		return true;
	}
}
