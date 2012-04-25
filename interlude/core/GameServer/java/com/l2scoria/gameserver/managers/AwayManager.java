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
package com.l2scoria.gameserver.managers;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.Map;
import java.util.WeakHashMap;

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.network.serverpackets.SetupGauge;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

/**
 * @author Michiru
 */
public final class AwayManager
{
	private static final Logger _log = Logger.getLogger(AwayManager.class.getName());
	private static AwayManager _instance;
	private Map<L2PcInstance, RestoreData> _awayPlayers;
	public static final int ZONE_PEACE = 2;

	public static final AwayManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new AwayManager();
			_log.info("AwayManager: initialized.");
		}
		return _instance;
	}

	private final class RestoreData
	{
		private final String _originalTitle;
		private final int _originalTitleColor;
		private final boolean _sitForced;

		public RestoreData(L2PcInstance activeChar)
		{
			_originalTitle = activeChar.getTitle();
			_originalTitleColor = activeChar.getAppearance().getTitleColor();
			_sitForced = !activeChar.isSitting();
		}

		public boolean isSitForced()
		{
			return _sitForced;
		}

		public void restore(L2PcInstance activeChar)
		{
			activeChar.getAppearance().setTitleColor(_originalTitleColor);
			activeChar.setTitle(_originalTitle);
		}
	}

	private AwayManager()
	{
		_awayPlayers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RestoreData>());
	}

	/**
	 * @param activeChar
	 * @param text
	 */
	public void setAway(L2PcInstance activeChar, String text)
	{
		activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 9));
		activeChar.sendMessage("Your status is Away in " + Config.SCORIA_AWAY_TIMER + " Sec.");
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.SCORIA_AWAY_TIMER * 1000);
		activeChar.sendPacket(sg);
		sg = null;
		activeChar.setIsImobilised(true);
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerAwayTask(activeChar, text), Config.SCORIA_AWAY_TIMER * 1000);
	}

	/**
	 * @param activeChar
	 */
	public void setBack(L2PcInstance activeChar)
	{
		activeChar.sendMessage("You are back from Away Status in " + Config.SCORIA_BACK_TIMER + " Sec.");
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.SCORIA_BACK_TIMER * 1000);
		activeChar.sendPacket(sg);
		sg = null;
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerBackTask(activeChar), Config.SCORIA_BACK_TIMER * 1000);
	}

	public void extraBack(L2PcInstance activeChar)
	{
		if(activeChar == null)
			return;
		RestoreData rd = _awayPlayers.get(activeChar);
		if(rd == null)
			return;

		rd.restore(activeChar);
		rd = null;
		_awayPlayers.remove(activeChar);
	}

	class setPlayerAwayTask implements Runnable
	{

		private final L2PcInstance _activeChar;
		private final String _awayText;

		setPlayerAwayTask(L2PcInstance activeChar, String awayText)
		{
			_activeChar = activeChar;
			_awayText = awayText;
		}

		public void run()
		{
			if(!check(_activeChar, true))
			{
				_activeChar.setIsImobilised(false);
				return;
			}

			_awayPlayers.put(_activeChar, new RestoreData(_activeChar));

			_activeChar.disableAllSkills();
			_activeChar.abortAttack();
			_activeChar.abortCast();
			_activeChar.setTarget(null);
			_activeChar.setIsImobilised(false);
			if(!_activeChar.isSitting())
			{
				_activeChar.sitDown();
			}
			if(_awayText.length() <= 1)
			{
				_activeChar.sendMessage("You are now *Away*");
			}
			else
			{
				_activeChar.sendMessage("You are now Away *" + _awayText + "*");
			}

			_activeChar.getAppearance().setTitleColor(Config.SCORIA_AWAY_TITLE_COLOR, false);

			if(_awayText.length() <= 1)
			{
				_activeChar.setTitle("*Away*");
			}
			else
			{
				_activeChar.setTitle("Away*" + _awayText + "*");
			}

			_activeChar.broadcastUserInfo();
			_activeChar.setIsParalyzed(true);
			_activeChar.setIsAway(true);
		}
	}

	class setPlayerBackTask implements Runnable
	{

		private final L2PcInstance _activeChar;

		setPlayerBackTask(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}

		public void run()
		{
			if(_activeChar == null)
				return;
			RestoreData rd = _awayPlayers.get(_activeChar);

			if(rd == null)
				return;

			_activeChar.setIsParalyzed(false);
			_activeChar.enableAllSkills();
			_activeChar.setIsAway(false);

			if(rd.isSitForced())
			{
				_activeChar.standUp();
			}

			rd.restore(_activeChar);
			rd = null;
			_awayPlayers.remove(_activeChar);
			_activeChar.broadcastUserInfo();
			_activeChar.sendMessage("You are Back now!");
		}
	}
	
	public boolean check(L2PcInstance activeChar, boolean setAway)
	{
		if(activeChar == null)
			return false;

		//check char is all ready in away mode
		if(activeChar.isAway())
		{
			activeChar.sendMessage("You are already Away.");
			return false;
		}

		if(activeChar.isAttackingNow() || activeChar.isCastingNow())
			return false;

		if(!activeChar.isInsideZone(ZONE_PEACE) && Config.SCORIA_AWAY_PEACE_ZONE)
		{
			activeChar.sendMessage("You can only Away in peace zone.");
			return false;
		}

		//check player is movement disable
		if(activeChar.isMovementDisabled() && !setAway)
		{
			activeChar.sendMessage("You can't go Afk! Your movement disabled");
			return false;
		}

		//check player is death/fake death
		if(activeChar.isAlikeDead())
		{
			activeChar.sendMessage("You can't go Afk! You are dead");
			return false;
		}

		Siege siege = SiegeManager.getInstance().getSiege(activeChar);
		// Check if player is in Siege
		if(siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go Afk.");
			return false;
		}

		// Check if player is a Cursed Weapon owner
		if(activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You can't go Afk! You are currently holding a cursed weapon.");
			return false;
		}

		// Check if player is in Duel
		if(activeChar.isInDuel())
		{
			activeChar.sendMessage("You can't go Afk! You are in a duel!");
			return false;
		}

		//check is in DimensionsRift
		if(activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You can't go Afk! You are in the dimensional rift.");
			return false;
		}

		//check player is in Olympiade
		if(activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() != -1)
		{
			activeChar.sendMessage("You can't go Afk! Your are fighting in Olympiad!");
			return false;
		}

		// Check player is in observer mode
		if(activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can't go Afk in Observer mode!");
			return false;
		}

		//check player have karma/pk/pvp status
		if(activeChar.getKarma() > 0 || activeChar.getPvpFlag() > 0)
		{
			activeChar.sendMessage("Player in PVP or with Karma can't use the Away command!");
			return false;
		}

		if(activeChar.isImobilised() && !setAway)
			return false;

		if(activeChar.getTarget() != null)
		{
			activeChar.sendMessage("You can't have any one in your target.");
			return false;
		}
			
		return true;
	}
}
