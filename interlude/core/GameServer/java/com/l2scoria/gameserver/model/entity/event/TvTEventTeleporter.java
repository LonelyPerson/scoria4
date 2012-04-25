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
package com.l2scoria.gameserver.model.entity.event;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.random.Rnd;

public class TvTEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;

	/**
	 * Initialize the teleporter and start the delayed task<br>
	 * <br>
	 * 
	 * @param playerInstance as L2PcInstance<br>
	 * @param coordinates as int[]<br>
	 * @param fastShedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;

		long delay = (TvTEvent.isStarted() ? 1 : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br>
	 * <br>
	 * 
	 * @see java.lang.Runnable#run()<br>
	 */
	public void run()
	{
		if(_playerInstance == null)
			return;

		L2Summon summon = _playerInstance.getPet();

		if(summon != null)
		{
			summon.unSummon(_playerInstance);
		}

		for(L2Effect effect : _playerInstance.getAllEffects())
		{
			if(effect != null)
			{
				effect.exit();
			}
		}
		if (Config.TVT_EVENT_BUFF){
		    if (TvTEvent.isStarted())
		        {
		                  L2Skill skill;
		                  SystemMessage sm;
		                  L2PcInstance player = _playerInstance;
		                  
		                  if(!player.isMageClass())
		                  {  
		                	   for(final int[] skill1 : Config.TVT_BUFF_WAR)
		                	   {
		                	   skill = SkillTable.getInstance().getInfo(skill1[0],skill1[1]);
		                       skill.getEffects(player, player);
		                       sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
		                       sm.addSkillName(1086);
		                       player.sendPacket(sm);
		                  }
		                  }
		                  else
		                  {
		                	   for(final int[] skill1 : Config.TVT_BUFF_MAGE){
		                       skill = SkillTable.getInstance().getInfo(skill1[0],skill1[1]);
		                       skill.getEffects(player, player);
		                       sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
		                       sm.addSkillName(1085);
		                       player.sendPacket(sm);
		                  }
		                  }
		             }
		        }
		

		_playerInstance.doRevive();
		_playerInstance.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], false);
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());

		if(TvTEvent.isStarted() && !_adminRemove)
		{
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		}
		else
		{
			_playerInstance.setTeam(0);
		}

		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();

		summon = null;
	}
}
