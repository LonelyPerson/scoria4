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
package com.l2scoria.gameserver.model.quest;

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.random.Rnd;
import org.apache.log4j.Logger;

/**
 * @author programmos
 */
public final class QuestSpawn
{
	private Logger _log = Quest._log;
	private static QuestSpawn instance;

	public static QuestSpawn getInstance()
	{
		if(instance == null)
		{
			instance = new QuestSpawn();
		}

		return instance;
	}

	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2NpcInstance _npc = null;

		public DeSpawnScheduleTimerTask(L2NpcInstance npc)
		{
			_npc = npc;
		}

		public void run()
		{
			_npc.onDecay();
		}
	}

	// Method - Public
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Uses player's coords and heading. Adds
	 * a little randomization in the x y coords Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, cha.getInstanceId());
	}

	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, int instanceId)
	{
		L2NpcInstance result = null;

		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

			if(template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
				// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the 
				// default spawn location, which is at the player's loc.
				if(x == 0 && y == 0)
				{
					_log.fatal("Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}

				if(randomOffset)
				{
					int offset;

					// Get the direction of the offset
					offset = Rnd.get(2);
					if(offset == 0)
					{
						offset = -1;
					}

					// make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;

					// Get the direction of the offset
					offset = Rnd.get(2);
					if(offset == 0)
					{
						offset = -1;
					}

					// make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);
				spawn.stopRespawn();
				result = spawn.spawnOne();
				spawn = null;

				if(despawnDelay > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
				}

				return result;
			}

			template = null;
		}
		catch(Exception e1)
		{
			_log.warn("Could not spawn Npc " + npcId);
		}

		return null;
	}

}
