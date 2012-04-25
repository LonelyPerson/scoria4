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
package com.l2scoria.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

public class L2SiegeSummonInstance extends L2SummonInstance
{
	public static final int SIEGE_GOLEM_ID = 14737;
	public static final int HOG_CANNON_ID = 14768;
	public static final int SWOOP_CANNON_ID = 14839;

	private boolean onSiegeMode = false;
	public ScheduledFuture<?> changeModeThread = null;

	public L2SiegeSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner, skill);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if(!getOwner().isGM() && !isInsideZone(L2Character.ZONE_SIEGE))
		{
			unSummon(getOwner());
			getOwner().sendMessage("Summon was unsummoned because it exited siege zone");
		}
	}

	public boolean isOnSiegeMode()
	{
		return onSiegeMode;
	}

	public boolean isSiegeModeChanging()
	{
		if (changeModeThread!=null && changeModeThread.isDone())
			return true;
		return false;
	}

	public void changeSiegeMode()
	{
		if (changeModeThread!=null && !changeModeThread.isDone())
		{
			getOwner().sendMessage("Wait while siege mode change end.");
			return;
		}
		getOwner().sendMessage("Siege mode change begin.");
		changeModeThread = ThreadPoolManager.getInstance().scheduleGeneral(new changeSiegeMode(),30000);
		setFollowStatus(false);
	}

	public void resetSiegeModeChange()
	{
		if (changeModeThread!=null && !changeModeThread.isDone())
		{
			getOwner().sendMessage("Siege mode change canceled.");
			changeModeThread.cancel(true);
		}
	}

	private class changeSiegeMode implements Runnable
	{
		public void run()
		{
			getOwner().sendMessage("Siege mode change end.");
			if (isOnSiegeMode())
			{
				onSiegeMode = false;
				setFollowStatus(true);
			}
			else
				onSiegeMode = true;
		}
	}
}
