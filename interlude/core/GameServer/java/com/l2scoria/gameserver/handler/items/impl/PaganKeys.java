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

import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.PlaySound;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.lang.ArrayUtils;
import com.l2scoria.util.random.Rnd;

/**
 * @author Akumu
 */
public class PaganKeys extends ItemAbst
{
	public PaganKeys()
	{
		_items = new int[]{8273, 8274, 8275};

		_playerUseOnly = true;
		_notInCombat = true;
		_requiresTarget = true;
		_minInteractionDistance = 100;
	}

	private int[] CHAPEL_DOORS = new int[]{19160010, 19160011};
	private int[] ANTEROOM_DOORS = new int[]{19160009, 19160008, 19160007, 19160006, 19160005, 19160004, 19160003, 19160002};
	private int[] DARKNESS_DOORS = new int[]{19160012, 19160013};

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance player = playable.getPlayer();
		L2Object target = player.getTarget();

		if (!(target.isDoor))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		L2DoorInstance door = (L2DoorInstance) target;

		if (!isCorrectDoor(item, door))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return false;
		}

		if (Rnd.get(100) < 35)
		{
			door.openMe();
			door.onOpen();
			player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR));
			player.broadcastPacket(new SocialAction(player.getObjectId(), 13));
			player.sendPacket(new PlaySound("interfacesound.system_close_01"));
		}

		return true;
	}

	protected boolean isCorrectDoor(L2ItemInstance key, L2DoorInstance door)
	{
		switch (key.getItemId())
		{
			case 8273: // Anteroom
			{
				return ArrayUtils.contains(ANTEROOM_DOORS, door.getDoorId());
			}

			case 8274: // Chapel
			{
				return ArrayUtils.contains(CHAPEL_DOORS, door.getDoorId());
			}

			case 8275: // Key of Darkness
			{
				return ArrayUtils.contains(DARKNESS_DOORS, door.getDoorId());
			}
		}

		return false;
	}
}
