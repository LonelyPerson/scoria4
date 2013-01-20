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

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.ai.L2CharacterAI;
import com.l2scoria.gameserver.ai.L2DoorAI;
import com.l2scoria.gameserver.geodata.GeoCollision;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.managers.FortManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.actor.knownlist.DoorKnownList;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.model.actor.stat.DoorStat;
import com.l2scoria.gameserver.model.actor.status.DoorStatus;
import com.l2scoria.gameserver.model.entity.ClanHall;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.entity.siege.DevastatedCastle;
import com.l2scoria.gameserver.model.entity.siege.Fort;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.templates.L2CharTemplate;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import gnu.trove.map.hash.TLongByteHashMap;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character implements GeoCollision
{
	protected static final Logger log = Logger.getLogger(L2DoorInstance.class.getName());

	/**
	 * The castle index in the array of L2Castle this L2NpcInstance belongs to
	 */
	private int _castleIndex = -2;
	/**
	 * fort index in array L2Fort -> L2NpcInstance
	 */
	private int _fortIndex = -2;

	private boolean _siegeWeaponOlnyAttackable;

	protected final int _doorId;
	protected final String _name;
        private boolean _attackable;
	private boolean _unlockable;
	private boolean _isHPVisible;

	public long _lastOpen;

	public boolean _geoOpen;
	public boolean _open = false;
	private boolean _geodata = true;
	private TLongByteHashMap geoAround;

	private ClanHall _clanHall;

	private L2Territory _pos;

	protected int _autoActionDelay = -1;
	private ScheduledFuture<?> _autoActionTask;

	/**
	 * This class may be created only by L2Character and only for AI
	 */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
			//null;
		}

		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
			//null;
		}

		@Override
		public void moveTo(int x, int y, int z)
		{
			//null;
		}

		@Override
		public void stopMove(L2CharPosition pos)
		{
			//null;
		}

		@Override
		public void doAttack(L2Character target)
		{
			//null;
		}

		@Override
		public void doCast(L2Skill skill)
		{
			//null;
		}
	}

	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2DoorAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}

	@Override
	public boolean hasAI()
	{
		return _ai != null;
	}

	class CloseTask implements Runnable
	{
		public void run()
		{
			try
			{
				onClose();
			} catch (Throwable e)
			{
				log.fatal("", e);
			}
		}
	}

	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			if (!getOpen())
			{
				openMe();
			}
			else
			{
				closeMe();
			}
		}
	}

	/**
	 */
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean attackable, boolean unlockable, boolean showHp)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		_doorId = doorId;
		_name = name;
                _attackable = attackable;
		_unlockable = unlockable;
		_isHPVisible = showHp;
		_geoOpen = true;
	}

	@Override
	public final DoorKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof DoorKnownList))
		{
			setKnownList(new DoorKnownList(this));
		}

		return (DoorKnownList) super.getKnownList();
	}

	@Override
	public final DoorStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}

		return (DoorStat) super.getStat();
	}

	@Override
	public final DoorStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}

		return (DoorStatus) super.getStatus();
	}
        
        public final boolean isDefaultAttackable()
        {
            return _attackable;
        }

	public final boolean isUnlockable()
	{
		return _unlockable;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}

	public boolean isHPVisible()
	{
		return _isHPVisible;
	}

	/**
	 * @return Returns the open.
	 */
	public boolean getOpen()
	{
		return _open;
	}

	/**
	 * @param open The open to set.
	 */
	public void setOpen(boolean open)
	{
		_open = open;
	}

	/**
	 * Устанавливает значение закрытости\открытости в геодате<br>
	 * @param val новое значение
	 */
	private void setGeoOpen(boolean val)
	{
		if(_geoOpen == val)
			return;

		_geoOpen = val;

		if(!getGeodata())
			return;

		if(val)
			GeoEngine.removeGeoCollision(this, getInstanceId());
		else
			GeoEngine.applyGeoCollision(this, getInstanceId());
	}

	/**
	 * Дверь/стена может быть атаоквана только осадным орудием
	 *
	 * @return true если дверь/стену можно атаковать только осадным орудием
	 */
	public boolean isSiegeWeaponOnlyAttackable()
	{
		return _siegeWeaponOlnyAttackable;
	}

	/**
	 * Устанавливает двери/стене признак возможности атаковать только осадным оружием
	 *
	 * @param val true - дверь/стену можно атаковать только осадным орудием
	 */
	public void setSiegeWeaponOlnyAttackable(boolean val)
	{
		_siegeWeaponOlnyAttackable = val;
	}

	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door instance. <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 *
	 * @param int actionDelay
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
		{
			return;
		}

		if (actionDelay > -1)
		{
			_autoActionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoOpenClose(), actionDelay, actionDelay);
		}
		else
		{
			if (_autoActionTask != null)
			{
				_autoActionTask.cancel(false);
			}
		}

		_autoActionDelay = actionDelay;
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
		{
			return 6;
		}
		if (dmg < 0)
		{
			return 0;
		}
		return dmg;
	}

	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}

		if (_castleIndex < 0)
		{
			return null;
		}

		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}

	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		}

		if (_fortIndex < 0)
		{
			return null;
		}

		return FortManager.getInstance().getForts().get(_fortIndex);
	}

	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public boolean isEnemyOf(L2Character cha)
	{
		return true;
	}

	public boolean isEnemy()
	{
		if (getCastle() != null && getCastle().getSiege().getIsInProgress())
		{
			return true;
		}
		return getFort() != null && getFort().getSiege().getIsInProgress();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
                // only "unlockable" params is not full support of dors
                // many doors can be opened by keys but can`t be attacked on PTS
                if(!isDefaultAttackable())
                {
                        return false;
                }
                
		if (isUnlockable())
		{
			return true;
		}

		// Doors can`t be attacked by NPCs
		if (attacker == null || !(attacker.isPlayable))
		{
			return false;
		}

		L2PcInstance character;

		if (attacker.isSummon)
		{
			character = ((L2Summon) attacker).getOwner();
		}
		else
		{
			character = (L2PcInstance) attacker;
		}

		// Attackable during siege by attacker only

		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(character.getClan());

		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && getFort().getSiege().checkIsAttacker(character.getClan());

		if (isFort)
		{
			L2Clan clan = character.getClan();
			if (clan != null && clan == getFort().getOwnerClan())
			{
				clan = null;
				return false;
			}
		}
		else if (isCastle)
		{
			L2Clan clan = character.getClan();
			if (clan != null && clan.getClanId() == getCastle().getOwnerId())
			{
				clan = null;
				return false;
			}
		}

		return isCastle || isFort || DevastatedCastle.getInstance().getIsInProgress();
	}

	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object.isPlayer))
		{
			return 0;
		}
		return 2000;
	}

	/**
	 * Return null.<BR>
	 * <BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;

			//            if (isAutoAttackable(player))
			//            {
			DoorStatusUpdate su = new DoorStatusUpdate(this);
			player.sendPacket(su);
			su = null;
			//            }

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			//            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			//            player.sendPacket(my);
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			else if (player.getClan() != null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
			{
				if (!isInsideRadius(player, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					//need find serverpacket which ask open/close gate. now auto
					//if (getOpen() == 1) player.sendPacket(new SystemMessage(1140));
					//else player.sendPacket(new SystemMessage(1141));
					if (!getOpen())
					{
						openMe();
					}
					else
					{
						closeMe();
					}
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}

		if (player.getAccessLevel().isGm())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			my = null;

			if (isAutoAttackable(player))
			{
				DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
				su = null;
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
			html1.append("<tr><td>Scoria Says:</td></tr>");
			html1.append("<tr><td>Current HP  ").append(getCurrentHp()).append("</td></tr>");
			html1.append("<tr><td>Max HP       ").append(getMaxHp()).append("</td></tr>");

			html1.append("<tr><td>Object ID: ").append(getObjectId()).append("</td></tr>");
			html1.append("<tr><td>Door ID: ").append(getDoorId()).append("</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: ").append(getClass().getName()).append("</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");

			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open ").append(getDoorId()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close ").append(getDoorId()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("</tr></table></body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
			html1 = null;
			html = null;

			//openMe();
		}
		else
		{
			// ATTACK the mob without moving?
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
		player = null;
	}

	@Override
	public void broadcastStatusUpdate()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();

		if (knownPlayers == null || knownPlayers.isEmpty())
		{
			return;
		}

		DoorStatusUpdate su = new DoorStatusUpdate(this);

		for (L2PcInstance player : knownPlayers)
		{
			player.sendPacket(su);
		}
	}

	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}

	public void onClose()
	{
		closeMe();
	}

	public final void closeMe()
	{
		synchronized (this)
		{
			if (!_open)
			{
				return;
			}

			setOpen(false);
		}

		setGeoOpen(false);
		broadcastStatusUpdate();
	}

	public final void openMe()
	{
		synchronized (this)
		{
			if (_open)
			{
				return;
			}

			setOpen(true);
			_lastOpen = System.currentTimeMillis();
		}
		setGeoOpen(true);
		broadcastStatusUpdate();
	}

	@Override
	public String toString()
	{
		return "door " + _doorId;
	}

	public String getDoorName()
	{
		return _name;
	}

	public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
	{
		FastList<L2SiegeGuardInstance> result = new FastList<L2SiegeGuardInstance>();

		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj.isSiegeGuard)
			{
				result.add((L2SiegeGuardInstance) obj);
			}
		}

		return result;
	}

	public Collection<L2FortSiegeGuardInstance> getKnownFortSiegeGuards()
	{
		FastList<L2FortSiegeGuardInstance> result = new FastList<L2FortSiegeGuardInstance>();

		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		//synchronized (getKnownList().getKnownObjects()) 
		{
			for (L2Object obj : objs)
			{
				if (obj instanceof L2FortSiegeGuardInstance)
				{
					result.add((L2FortSiegeGuardInstance) obj);
				}
			}
		}
		return result;
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if (this.isAutoAttackable(attacker) || (attacker.isPlayer && ((L2PcInstance) attacker).isGM()))
		{
			super.reduceCurrentHp(damage, attacker, awake);
		}
		else
		{
			super.reduceCurrentHp(0, attacker, awake);
		}
	}

	public void setGeodata(boolean value)
	{
		_geodata = value;
	}

	public boolean getGeodata()
	{
		return _geodata;
	}

	public void setGeoAround(TLongByteHashMap value)
	{
		geoAround = value;
	}

	public TLongByteHashMap getGeoAround()
	{
		return geoAround;
	}

	@Override
	public boolean isGeoCloser()
	{
		return true;
	}

	public void setGeoPos(L2Territory pos)
	{
	 	_pos = pos;
	}

	@Override
	public L2Territory getGeoPos()
	{
		return _pos;
	}

	/*@Override
	public void onSpawn()
	{
		super.onSpawn();
		if(!getOpen() && _geoOpen)
			setGeoOpen(false);

		closeMe();
	}*/

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		setGeoOpen(true);
		return true;
	}
}