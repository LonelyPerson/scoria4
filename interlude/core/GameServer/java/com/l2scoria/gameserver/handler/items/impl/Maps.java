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

import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.RadarControl;
import com.l2scoria.gameserver.network.serverpackets.ShowMiniMap;

/**
 * This class provides handling for items that should display a map when double clicked.
 *
 * @author l2j, Akumu
 * @date 22:52/06.09.12
 */

public class Maps extends ItemAbst
{
	public Maps()
	{
		_items = new int[]{1665, 1863, 7063};

		_playerUseOnly = true;
		_notInObservationMode = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		switch (item.getItemId())
		{
			case 1665:
				activeChar.sendPacket(ShowMiniMap.STATIC_PACKET);
				break;
			case 7063:
				activeChar.sendPacket(ShowMiniMap.STATIC_PACKET);
				activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
				break;
			default:
				activeChar.sendPacket(new ShowMiniMap(item.getItemId()));
				break;
		}

		return true;
	}
}
