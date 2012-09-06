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
package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;
import com.l2scoria.gameserver.network.serverpackets.CustomNpcInfo;
import com.l2scoria.gameserver.network.serverpackets.NpcInfo;

import java.util.Collection;

public class L2WaterZone extends L2ZoneDefault
{
	public L2WaterZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, true);

		if(character.isPlayer)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if(character.isNpc)
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for(L2PcInstance player : plrs)
				{
					if(((L2NpcInstance)character).getCustomNpcInstance() != null)
					{
						player.sendPacket(new CustomNpcInfo((L2NpcInstance)character));
					}
					else
					{
						player.sendPacket(new NpcInfo((L2NpcInstance) character, player));
					}
				}
			}
		}

		/*
		 * if (character.isPlayer)
		 * {
		 * 	((L2PcInstance)character).sendMessage("You entered water!");
		 *}
		 */

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, false);

		/*if (character.isPlayer)
		{
			((L2PcInstance)character).sendMessage("You exited water!");
		}*/

		// TODO: update to only send speed status when that packet is known
		if(character.isPlayer)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if(character.isNpc)
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for(L2PcInstance player : plrs)
				{
					if(((L2NpcInstance)character).getCustomNpcInstance() != null)
					{
						player.sendPacket(new CustomNpcInfo((L2NpcInstance)character));
					}
					else
					{
						player.sendPacket(new NpcInfo((L2NpcInstance) character, player));
					}
				}
			}

			plrs = null;
		}

		super.onExit(character);
	}

	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
