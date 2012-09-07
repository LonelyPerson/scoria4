/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2GourdInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.templates.L2NpcTemplate;

/**
 * @author Scoria dev.
 */

public class JackpotSeed extends ItemAbst
{

	private static int[] _npcIds =
	{
			12774, //Young Pumpkin
			12777 //Large Young Pumpkin
	};

	public JackpotSeed()
	{
		_items = new int[]{6389, // small seed
				6390 // large seed
		};

		_playerUseOnly = true;
		_notSitting = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		L2NpcTemplate template1 = null;

		for (int i = 0; i < getItemIds().length; i++)
		{
			if (getItemIds()[i] == item.getItemId())
			{
				template1 = NpcTable.getInstance().getTemplate(_npcIds[i]);
				break;
			}
		}

		if (template1 == null)
		{
			return false;
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(activeChar.getX());
			spawn.setLocy(activeChar.getY());
			spawn.setLocz(activeChar.getZ());
			L2GourdInstance _gourd = (L2GourdInstance) spawn.spawnOne();
			L2World.getInstance();
			L2World.storeObject(_gourd);
			_gourd.setOwner(activeChar);
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}
}
