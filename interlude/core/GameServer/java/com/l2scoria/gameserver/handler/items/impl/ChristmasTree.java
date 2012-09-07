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

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

public class ChristmasTree extends ItemAbst
{
	private static final int[] NPC_IDS =
	{
			13006, /* Christmas tree w. flashing lights and snow */
			13007
	};

	public ChristmasTree()
	{
		_items = new int[]
				{
						5560, // x-mas tree
						5561 // Special x-mas tree
				};
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		L2NpcTemplate template1 = null;

		int itemId = item.getItemId();
		for(int i = 0; i < getItemIds().length; i++)
		{
			if(getItemIds()[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
				break;
			}
		}

		if(template1 == null)
			return false;

		L2Object target = activeChar.getTarget();
		if(target == null)
		{
			target = activeChar;
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			L2NpcInstance result = spawn.spawnOne();

			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

			ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawn(result), 3600000);
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}

	public class DeSpawn implements Runnable
	{
		L2NpcInstance _npc = null;

		public DeSpawn(L2NpcInstance npc)
		{
			_npc = npc;
		}

		public void run()
		{
			_npc.onDecay();
		}
	}
}
