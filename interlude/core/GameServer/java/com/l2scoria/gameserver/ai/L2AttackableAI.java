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
package com.l2scoria.gameserver.ai;

import com.l2scoria.Config;
import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.datatables.sql.TerritoryTable;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.DimensionalRiftManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.actor.instance.*;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.taskmanager.DecayTaskManager;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

import java.util.concurrent.Future;

import static com.l2scoria.gameserver.ai.CtrlIntention.*;

/**
 * This class manages AI of L2Attackable.<BR>
 * <BR>
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{

	//protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());

	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds

	/**
	 * The L2Attackable AI task executed every 1s (call onEvtThink method)
	 */
	private Future<?> _aiTask;

	/**
	 * The delay after wich the attacked is stopped
	 */
	private int _attackTimeout;
	//private int _attackInteractions = 0;

	/**
	 * The L2Attackable aggro counter
	 */
	private int _globalAggro;

	/**
	 * The flag used to indicate that a thinking action is in progress
	 */
	private boolean _thinking; // to prevent recursive thinking

	private SelfAnalysis _selfAnalysis = new SelfAnalysis();
	private TargetAnalysis _mostHatedAnalysis = new TargetAnalysis();
	private TargetAnalysis _secondMostHatedAnalysis = new TargetAnalysis();

	/**
	 * Constructor of L2AttackableAI.<BR>
	 * <BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);

		_selfAnalysis.init();
		_attackTimeout = 300;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}

	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR>
	 * <BR>
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving
	 * mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>The
	 * L2PcInstance target has karma (=PK)</li> <li>The L2MonsterInstance target is aggressive</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li> <li>The target isn't dead, isn't invulnerable, isn't in silent moving
	 * mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same height</li> <li>A
	 * siege is in progress</li> <li>The L2PcInstance target isn't a Defender</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li> <li>The target isn't dead, isn't invulnerable,
	 * isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same
	 * height</li> <li>The L2PcInstance target has karma (=PK)</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li> <li>The target isn't dead, isn't invulnerable,
	 * isn't in silent moving mode AND too far (>100)</li> <li>The target is in the actor Aggro range and is at the same
	 * height</li> <li>The actor is Aggressive</li><BR>
	 * <BR>
	 *
	 * @param target The targeted L2Object
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if (target == null || !(_actor.isAttackable))
		{
			return false;
		}

		L2Attackable me = (L2Attackable) _actor;

		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			// However EffectInvincible requires to check GMs specially
			if (target.isPlayer && target.getPlayer().isGM())
			{
				return false;
			}

			if (target.isSummon && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}

		// Check if the target isn't a Folk or a Door
		if (target instanceof L2FolkInstance || target.isDoor)
		{
			return false;
		}

		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || Math.abs(_actor.getZ() - target.getZ()) > 300)
		{
			return false;
		}

		// Check if the target is a L2PcInstance
		if (target.isPlayer)
		{
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (target.getPlayer().isGM() && target.getPlayer().getAccessLevel().canTakeAggro())
			{
				return false;
			}

			// Check if the AI isn't a Raid Boss and the target isn't in silent move mode
			if (!(me.isRaid) && target.getPlayer().isSilentMoving())
			{
				return false;
			}

			// Check if player is an ally //TODO! [Nemesiss] it should be rather boolean or smth like that
			// Comparing String isnt good idea!
			if (me.getFactionId() != null)
			{
				if (me.getFactionId().equals("varka") && target.getPlayer().isAlliedWithVarka())
				{
					return false;
				}

				if (me.getFactionId().equals("ketra") && target.getPlayer().isAlliedWithKetra())
				{
					return false;
				}
			}

			// check if the target is within the grace period for JUST getting up from fake death
			if (target.getPlayer().isRecentFakeDeath())
			{
				return false;
			}

			// check player is in away mod
			if (target.getPlayer().isAway() && !Config.SCORIA_AWAY_PLAYER_TAKE_AGGRO)
			{
				return false;
			}

			if (target.isInParty() && target.getParty().isInDimensionalRift())
			{
				byte riftType = target.getParty().getDimensionalRift().getType();
				byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();

				if (me instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
				{
					return false;
				}
			}
		}

		// Check if the actor is a L2GuardInstance
		if (_actor.isGuard)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if (target.isPlayer && target.getPlayer().getKarma() > 0)
			// Los Check
			{
				return GeoEngine.canSeeTarget(me, target, false);
			}

			//if (target.isSummon)
			//    return ((L2Summon)target).getKarma() > 0;
			// Check if the L2MonsterInstance target is aggressive
			return target.isMonster && ((L2MonsterInstance) target).isAggressive() && GeoEngine.canSeeTarget(me, target, false);
		}
		else if (_actor instanceof L2FriendlyMobInstance)
		{
			// the actor is a L2FriendlyMobInstance

			// Check if the target isn't another L2NpcInstance
			if (target.isNpc)
			{
				return false;
			}

			// Check if the L2PcInstance target has karma (=PK)
			return target.isPlayer && target.getPlayer().getKarma() > 0 && GeoEngine.canSeeTarget(me, target, false);
		}
		else
		{
			//The actor is a L2MonsterInstance

			// Check if the target isn't another L2NpcInstance
			if (target.isNpc)
			{
				return false;
			}

			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE))
			{
				return false;
			}

			// Check if the actor is Aggressive
			return me.isAggressive() && GeoEngine.canSeeTarget(me, target, false);
		}
	}

	public void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}

	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
	}

	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}

	/**
	 * Set the Intention of this L2CharacterAI and create an AI Task executed every 1s (call onEvtThink method) for this
	 * L2Attackable.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in
	 * AI_INTENTION_ACTIVE</B></FONT><BR>
	 * <BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			// Check if actor is not dead
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) _actor;

				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (npc.getKnownList().getKnownPlayers().size() > 0)
				{
					intention = AI_INTENTION_ACTIVE;
				}

				npc = null;
			}

			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);

				// Stop AI task and detach AI from NPC
				if (_aiTask != null)
				{
					_aiTask.cancel(true);
					_aiTask = null;
				}

				// Cancel the AI
				_accessor.detachAI();

				return;
			}
		}

		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);

		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack
	 * and Launch Think Event.<BR>
	 * <BR>
	 *
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		if (_selfAnalysis.lastBuffTick + 100 < GameTimeController.getGameTicks())
		{
			for (L2Skill sk : _selfAnalysis.buffSkills)
			{
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN)
					{
						continue;
					}

					L2Object OldTarget = _actor.getTarget();

					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_selfAnalysis.lastBuffTick = GameTimeController.getGameTicks();
					_actor.setTarget(OldTarget);
				}
			}
		}
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li> <li>If the actor is Aggressive and can
	 * attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to
	 * attack it</li> <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home
	 * location</li> <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR>
	 * <BR>
	 */
	private void thinkActive()
	{
		L2Attackable npc = (L2Attackable) _actor;

		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}

		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			//L2Object[] objects = L2World.getInstance().getVisibleObjects(_actor, ((L2NpcInstance)_actor).getAggroRange());
			// Go through visible objects
			for (L2Object obj : npc.getKnownList().getKnownObjects().values())
			{
				if (obj == null || !(obj.isCharacter))
				{
					continue;
				}

				L2Character target = (L2Character) obj;

				/*
				 * Check to see if this is a festival mob spawn.
				 * If it is, then check to see if the aggro trigger
				 * is a festival participant...if so, move to attack it.
				 */
				if (_actor instanceof L2FestivalMonsterInstance && obj.isPlayer)
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;
					if (!targetPlayer.isFestivalParticipant())
					{
						continue;
					}

					targetPlayer = null;
				}

				if (obj.isPlayer || obj.isSummon)
				{
					if (!((L2Character) obj).isAlikeDead() && !npc.isInsideRadius(obj, npc.getAggroRange(), true, false))
					{
						L2PcInstance targetPlayer = obj.isPlayer ? (L2PcInstance) obj : ((L2Summon) obj).getOwner();

						if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
						{
							for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
							{
								quest.notifyAggroRangeEnter(npc, targetPlayer, obj.isSummon);
							}
						}
					}
				}

				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					int hating = npc.getHating(target);

					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 1);
					}
				}

				target = null;
			}

			// Chose a target from its aggroList
			L2Character hated;

			// Force mobs to attak anybody if confused
			if (_actor.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getMostHated();
			}

			// Order to the L2Attackable to attack the target
			if (hated != null)
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);
				if (aggro + _globalAggro > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}

				return;
			}

			hated = null;
		}

		// Check if the actor is a L2GuardInstance
		if (_actor.isGuard)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			((L2GuardInstance) _actor).returnHome();
		}

		// If this is a festival monster, then it remains in the same location.
		if (_actor instanceof L2FestivalMonsterInstance)
		{
			return;
		}

		// Check if the mob should not return to spawn point 
		if (!npc.canReturnToSpawnPoint())
		{
			return;
		}

		// Minions following leader
		if (_actor.isMinion && ((L2MinionInstance) _actor).getLeader() != null)
		{
			int offset;

			// for Raids - need correction
			if (_actor.isRaid())
			{
				offset = 500;
			}
			else
			{
				// for normal minions - need correction :)
				offset = 200;
			}

			if (((L2MinionInstance) _actor).getLeader().isRunning())
			{
				_actor.setRunning();
			}
			else
			{
				_actor.setWalking();
			}

			if (_actor.getPlanDistanceSq(((L2MinionInstance) _actor).getLeader()) > offset * offset)
			{
				int x1, y1, z1;

				x1 = ((L2MinionInstance) _actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				y1 = ((L2MinionInstance) _actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				z1 = ((L2MinionInstance) _actor).getLeader().getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
				return;
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				for (L2Skill sk : _selfAnalysis.buffSkills)
				{
					if (_actor.getFirstEffect(sk.getId()) == null)
					{
						if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF && Rnd.nextInt(2) != 0)
						{
							continue;
						}
						if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (_actor.isSkillDisabled(sk.getId()))
						{
							continue;
						}

						L2Object OldTarget = _actor.getTarget();

						_actor.setTarget(_actor);
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0)
		{
			int x1, y1, z1;

			for (L2Skill sk : _selfAnalysis.buffSkills)
			{
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF && Rnd.nextInt(2) != 0)
					{
						continue;
					}
					if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}

					L2Object OldTarget = _actor.getTarget();

					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					return;
				}
			}

			// If NPC with random coord in territory
			if (npc.getSpawn().getLocx() == 0 && npc.getSpawn().getLocy() == 0)
			{
				// If NPC with random fixed coord, don't move
				if (TerritoryTable.getInstance().getProcMax(npc.getSpawn().getLocation()) > 0)
				{
					return;
				}

				// Calculate a destination point in the spawn area
				int p[] = TerritoryTable.getInstance().getRandomPoint(npc.getSpawn().getLocation());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];

				// Calculate the distance between the current position of the L2Character and the target (x,y)
				double distance2 = _actor.getPlanDistanceSq(x1, y1);

				if (distance2 > Config.MAX_DRIFT_RANGE * Config.MAX_DRIFT_RANGE)
				{
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / Config.MAX_DRIFT_RANGE;
					x1 = _actor.getX() + (int) ((x1 - _actor.getX()) / delay);
					y1 = _actor.getY() + (int) ((y1 - _actor.getY()) / delay);
				}
				else
				{
					npc.setisReturningToSpawnPoint(false);
				}
			}
			else
			{
				x1 = npc.getSpawn().getLocx();
				y1 = npc.getSpawn().getLocy();
				z1 = npc.getSpawn().getLocz();

				if (!npc.isInsideRadius(x1, y1, Config.MAX_DRIFT_RANGE, false))
				{
					if (Config.ON_DRIFT_MAX_RANGE_TELEPORT)
					{
						npc.teleToLocation(x1, y1, z1);
					}
					npc.setisReturningToSpawnPoint(true);
				}
				else
				{
					x1 += Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;
					y1 += Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;
				}
			}

			if (_actor.isMonster)
			{
				L2MonsterInstance boss = (L2MonsterInstance) _actor;
				if (boss.hasMinions())
				{
					boss.callMinions();
				}
			}

			//_log.info("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)

			moveTo(x1, y1, z1);
		}

		npc = null;

	}

	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update the attack timeout if actor is running</li> <li>If target is dead or timeout is expired, stop this
	 * attack and set the Intention to AI_INTENTION_ACTIVE</li> <li>Call all L2Object of its Faction inside the Faction
	 * Range</li> <li>Chose a target and order to attack it with magic skill or physical attack</li><BR>
	 * <BR>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		if (_actor.isCastingNow())
		{
			return;
		}

		if (Config.MAX_FOLLOW_DRIFT_RANGE > 0)
		{
			try
			{
				L2Attackable npc = (L2Attackable) _actor;
				if (npc.isMonster || (npc.isRaid && npc.getNpcId() != 29014))
				{
					int x1, y1, z1;
					_actor.setTarget(_actor);
					clientStopMoving(null);
					x1 = npc.getSpawn().getLocx();
					y1 = npc.getSpawn().getLocy();
					z1 = npc.getSpawn().getLocz();

					if (!npc.isInsideRadius(x1, y1, Config.MAX_FOLLOW_DRIFT_RANGE, false))
					{
						npc.teleToLocation(x1, y1, z1);
						npc.setisReturningToSpawnPoint(true);
					}

				}
			} catch (Exception e)
			{
			}
		}

		if (_attackTimeout < GameTimeController.getGameTicks())
		{
			// Check if the actor is running
			if (_actor.isRunning())
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				_actor.setWalking();

				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}

		L2Character originalAttackTarget = getAttackTarget();

		// Check if target is dead or if timeout is expired to stop this attack
		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() || _attackTimeout < GameTimeController.getGameTicks())
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				((L2Attackable) _actor).stopHating(originalAttackTarget);
			}

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);

			_actor.setWalking();
			return;
		}

		// Call all L2Object of its Faction inside the Faction Range
		if (((L2NpcInstance) _actor).getFactionId() != null)
		{
			// Go through all L2Object that belong to its faction
			for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
			{
				if (obj.isNpc)
				{
					L2NpcInstance npc = (L2NpcInstance) obj;
					String faction_id = ((L2NpcInstance) _actor).getFactionId();

					if (npc == null || getAttackTarget() == null || !faction_id.equals(npc.getFactionId()) || npc.getFactionRange() == 0)
					{
						faction_id = null;
						continue;
					}

					// Check if the L2Object is inside the Faction Range of the actor
					if (_actor.isInsideRadius(npc, npc.getFactionRange(), true, false) && npc != null && _actor != null && npc.getAI() != null
							//&& GeoEngine.canSeeTarget(_actor, npc)
							&& Math.abs(getAttackTarget().getZ() - npc.getZ()) < 600 && _actor.getAttackByList().contains(getAttackTarget()))
					{
						if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && npc.getStatus().getCurrentHp() < npc.getMaxHp() * 0.6 && _actor.getStatus().getCurrentHp() > _actor.getMaxHp() / 2 && _actor.getStatus().getCurrentMp() > _actor.getMaxMp() / 2)
						{
							if (npc.isDead() && _actor.isMinion)
							{
								if (((L2MinionInstance) _actor).getLeader() == npc)
								{
									for (L2Skill sk : _selfAnalysis.resurrectSkills)
									{
										if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
										{
											continue;
										}
										if (_actor.isSkillDisabled(sk.getId()))
										{
											continue;
										}
										if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
										{
											continue;
										}
										if (10 >= Rnd.get(100)) // chance
										{
											continue;
										}
										if (!GeoEngine.canSeeTarget(_actor, npc, false))
										{
											break;
										}

										L2Object OldTarget = _actor.getTarget();

										_actor.setTarget(npc);
										DecayTaskManager.getInstance().cancelDecayTask(npc);
										DecayTaskManager.getInstance().addDecayTask(npc);
										clientStopMoving(null);
										_accessor.doCast(sk);
										_actor.setTarget(OldTarget);
										return;
									}
								}
							}
							else if (npc.isInCombat())
							{
								for (L2Skill sk : _selfAnalysis.healSkills)
								{
									if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
									{
										continue;
									}
									if (_actor.isSkillDisabled(sk.getId()))
									{
										continue;
									}
									if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
									{
										continue;
									}

									int chance = 4;
									if (_actor.isMinion)
									{
										// minions support boss
										if (((L2MinionInstance) _actor).getLeader() == npc)
										{
											chance = 6;
										}
										else
										{
											chance = 3;
										}
									}
									if (npc.isRaid || npc instanceof L2GrandBossInstance)
									{
										chance = 6;
									}
									if (chance >= Rnd.get(100)) // chance
									{
										continue;
									}
									if (!GeoEngine.canSeeTarget(_actor, npc, false))
									{
										break;
									}

									L2Object OldTarget = _actor.getTarget();
									_actor.setTarget(npc);
									clientStopMoving(null);
									_accessor.doCast(sk);
									_actor.setTarget(OldTarget);
									return;
								}
							}
						}
						if ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE || npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE))
						{
							if (getAttackTarget().isPlayer && getAttackTarget().isInParty() && getAttackTarget().getParty().isInDimensionalRift())
							{
								byte riftType = getAttackTarget().getParty().getDimensionalRift().getType();
								byte riftRoom = getAttackTarget().getParty().getDimensionalRift().getCurrentRoom();

								if (_actor instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
								{
									continue;
								}
							}
							// Notify the L2Object AI with EVT_AGGRESSION
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
						}
						if (getAttackTarget().isPlayer || getAttackTarget().isSummon)
						{
							if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null)
							{
								L2PcInstance player = getAttackTarget().isPlayer ? (L2PcInstance) getAttackTarget() : ((L2Summon) getAttackTarget()).getOwner();
								for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL))
								{
									quest.notifyFactionCall(npc, (L2NpcInstance) _actor, player, (getAttackTarget().isSummon));
								}
							}
						}
					}
					npc = null;
				}
			}
		}

		if (_actor.isAttackingDisabled())
		{
			return;
		}

		L2Character[] hated = ((L2Attackable) _actor).get2MostHated();
		if (_actor.isConfused())
		{
			if (hated != null)
			{
				hated[0] = originalAttackTarget; // effect handles selection
			}
			else
			{
				hated = new L2Character[]{originalAttackTarget, null};
			}
		}

		if (hated == null || hated[0] == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		if (hated[0] != originalAttackTarget)
		{
			setAttackTarget(hated[0]);
		}
		_mostHatedAnalysis.update(hated[0]);
		_secondMostHatedAnalysis.update(hated[1]);
		// Get all information needed to choose between physical or magical attack
		_actor.setTarget(_mostHatedAnalysis.character);
		double dist2 = _actor.getPlanDistanceSq(_mostHatedAnalysis.character.getX(), _mostHatedAnalysis.character.getY());
		int combinedCollision = _actor.getTemplate().getCollisionRadius() + _mostHatedAnalysis.character.getTemplate().getCollisionRadius();
		int range = _actor.getPhysicalAttackRange() + combinedCollision;

		// Reconsider target next round if _actor hasn't got hits in for last 14 seconds
		if (!_actor.isMuted() && _attackTimeout - 160 < GameTimeController.getGameTicks() && _secondMostHatedAnalysis.character != null)
		{
			if (Util.checkIfInRange(900, _actor, hated[1], true))
			{
				// take off 2* the amount the aggro is larger than second most
				((L2Attackable) _actor).reduceHate(hated[0], 2 * (((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1])));
				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}
		// Reconsider target during next round if actor is rooted and cannot reach mostHated but can
		// reach secondMostHated
		if (_actor.isRooted() && _secondMostHatedAnalysis.character != null)
		{
			if (_selfAnalysis.isMage && dist2 > _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange && _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange)
			{
				((L2Attackable) _actor).reduceHate(hated[0], 1 + (((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1])));
			}
			else if (dist2 > range * range && _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < range * range)
			{
				((L2Attackable) _actor).reduceHate(hated[0], 1 + (((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1])));
			}
		}

		// Considering, if bigger range will be attempted
		if ((dist2 < 10000 + combinedCollision * combinedCollision) && !_selfAnalysis.isFighter && !_selfAnalysis.isBalanced && (_selfAnalysis.hasLongRangeSkills || _selfAnalysis.isArcher || _selfAnalysis.isHealer) && (_mostHatedAnalysis.isBalanced || _mostHatedAnalysis.isFighter) && (_mostHatedAnalysis.character.isRooted() || _mostHatedAnalysis.isSlower) && 20 >= Rnd.get(100))
		{
			int posX = _actor.getX();
			int posY = _actor.getY();
			int posZ = _actor.getZ();
			double distance = Math.sqrt(dist2); // This way, we only do the sqrt if we need it

			int signx = -1;
			int signy = -1;
			if (_actor.getX() > _mostHatedAnalysis.character.getX())
			{
				signx = 1;
			}
			if (_actor.getY() > _mostHatedAnalysis.character.getY())
			{
				signy = 1;
			}
			posX += Math.round((float) ((signx * ((range / 2) + (Rnd.get(range)))) - distance));
			posY += Math.round((float) ((signy * ((range / 2) + (Rnd.get(range)))) - distance));
			setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
			return;
		}

		// Cannot see target, needs to go closer, currently just goes to range 300 if mage
		if ((dist2 > 96100 + combinedCollision * combinedCollision) && _selfAnalysis.hasLongRangeSkills && !GeoEngine.canSeeTarget(_actor, _mostHatedAnalysis.character, false))
		{
			if (!(_selfAnalysis.isMage && _actor.isMuted()))
			{
				moveToPawn(_mostHatedAnalysis.character, 300);
				return;
			}
		}

		if (_mostHatedAnalysis.character.isMoving())
		{
			range += 50;
		}
		// Check if the actor is far from target
		if (dist2 > range * range)
		{
			if (!_actor.isMuted() && (_selfAnalysis.hasLongRangeSkills || !_selfAnalysis.healSkills.isEmpty()))
			{
				// check for long ranged skills and heal/buff skills
				if (!_mostHatedAnalysis.isCanceled)
				{
					for (L2Skill sk : _selfAnalysis.cancelSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= 8)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_mostHatedAnalysis.isCanceled = true;
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
				{
					for (L2Skill sk : _selfAnalysis.debuffSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						int chance = 8;
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
						{
							chance = 3;
						}
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
						{
							chance = 12;
						}
						if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
						{
							chance = 10;
						}
						if (_selfAnalysis.isHealer)
						{
							chance = 12;
						}
						if (_mostHatedAnalysis.isMagicResistant)
						{
							chance /= 2;
						}

						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isMuted())
				{
					int chance = 8;
					if (!(_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
					{
						chance = 3;
					}
					for (L2Skill sk : _selfAnalysis.muteSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
				{
					double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (L2Skill sk : _selfAnalysis.muteSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (secondHatedDist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= 2)
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isSleeping())
				{
					for (L2Skill sk : _selfAnalysis.sleepSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 1))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isSleeping())
				{
					double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (L2Skill sk : _selfAnalysis.sleepSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (secondHatedDist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 3))
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isRooted())
				{
					for (L2Skill sk : _selfAnalysis.rootSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_mostHatedAnalysis.isSlower ? 3 : 8))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isAttackingDisabled())
				{
					for (L2Skill sk : _selfAnalysis.generalDisablers)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
						{
							continue;
						}
						if (Rnd.nextInt(100) <= ((_selfAnalysis.isFighter && _actor.isRooted()) ? 15 : 7))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_actor.getStatus().getCurrentHp() < _actor.getMaxHp() * 0.4)
				{
					for (L2Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk))
						{
							continue;
						}
						int chance = 7;
						if (_mostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						if (_secondMostHatedAnalysis.character == null || _secondMostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						if (Rnd.nextInt(100) <= chance)
						{
							_actor.setTarget(_actor);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}

				// chance decision for launching long range skills
				int castingChance = 5;
				if (_selfAnalysis.isMage || _selfAnalysis.isHealer)
				{
					castingChance = 50; // mages
				}
				if (_selfAnalysis.isBalanced)
				{
					if (!_mostHatedAnalysis.isFighter) // advance to mages
					{
						castingChance = 15;
					}
					else
					{
						castingChance = 25; // stay away from fighters
					}
				}
				if (_selfAnalysis.isFighter)
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 3;
					}
					else
					{
						castingChance = 7;
					}
					if (_actor.isRooted())
					{
						castingChance = 20; // doesn't matter if no success first round
					}
				}
				for (L2Skill sk : _selfAnalysis.generalSkills)
				{
					int castRange = sk.getCastRange() + combinedCollision;
					if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
					{
						continue;
					}

					if (Rnd.nextInt(100) <= castingChance)
					{
						clientStopMoving(null);
						_accessor.doCast(sk);
						_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
						return;
					}
				}
			}

			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			if (_selfAnalysis.isMage && !_actor.isMuted())
			{
				// mages stay a bit further away if not muted or low mana
				if ((_actor.getMaxMp() / 3) < _actor.getStatus().getCurrentMp())
				{
					range = _selfAnalysis.maxCastRange;
					if (dist2 < range * range) // don't move backwards here
					{
						return;
					}
				}
			}
			// healers do not even follow
			if (_selfAnalysis.isHealer)
			{
				return;
			}

			if (_mostHatedAnalysis.character.isMoving())
			{
				range -= 100;
			}
			if (range < 5)
			{
				range = 5;
			}
			moveToPawn(_mostHatedAnalysis.character, range);
			return;
		}

		if (Rnd.nextInt(100) <= 33) // check it once per 3 seconds
		{
			for (L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(10))
			{
				if (nearby.isAttackable && nearby != _mostHatedAnalysis.character)
				{
					int diffx = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
					{
						diffx = -diffx;
					}
					int diffy = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
					{
						diffy = -diffy;
					}
					moveTo(_mostHatedAnalysis.character.getX() + diffx, _mostHatedAnalysis.character.getY() + diffy, _mostHatedAnalysis.character.getZ());
					return;
				}
			}
		}

		// Calculate a new attack timeout. 
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		// check for close combat skills && heal/buff skills

		if (!_mostHatedAnalysis.isCanceled)
		{
			for (L2Skill sk : _selfAnalysis.cancelSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 8)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_mostHatedAnalysis.isCanceled = true;
					return;
				}
			}
		}
		if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
		{
			for (L2Skill sk : _selfAnalysis.debuffSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				int chance = 5;
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
				{
					chance = 3;
				}
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
				{
					chance = 3;
				}
				if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
				{
					chance = 4;
				}
				if (_selfAnalysis.isHealer)
				{
					chance = 12;
				}
				if (_mostHatedAnalysis.isMagicResistant)
				{
					chance /= 2;
				}
				if (sk.getCastRange() < 200)
				{
					chance += 3;
				}
				if (Rnd.nextInt(100) <= chance)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isMuted() && (_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
		{
			for (L2Skill sk : _selfAnalysis.muteSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 7)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
		{
			double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (L2Skill sk : _selfAnalysis.muteSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (secondHatedDist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 3)
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isSleeping() && _selfAnalysis.isHealer)
		{
			for (L2Skill sk : _selfAnalysis.sleepSkills)
			{
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 10)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
					return;
				}
			}
		}
		if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isSleeping())
		{
			double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (L2Skill sk : _selfAnalysis.sleepSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (secondHatedDist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 4))
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isRooted() && _mostHatedAnalysis.isFighter && !_selfAnalysis.isFighter)
		{
			for (L2Skill sk : _selfAnalysis.rootSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 4))
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isAttackingDisabled())
		{
			for (L2Skill sk : _selfAnalysis.generalDisablers)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
				{
					continue;
				}
				if (Rnd.nextInt(100) <= ((sk.getCastRange() < 200) ? 10 : 7))
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (_actor.getStatus().getCurrentHp() < _actor.getMaxHp() * (_selfAnalysis.isHealer ? 0.7 : 0.4))
		{
			for (L2Skill sk : _selfAnalysis.healSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk))
				{
					continue;
				}
				int chance = (_selfAnalysis.isHealer ? 15 : 7);
				if (_mostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				if (_secondMostHatedAnalysis.character == null || _secondMostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				if (Rnd.nextInt(100) <= chance)
				{
					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		for (L2Skill sk : _selfAnalysis.generalSkills)
		{
			if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPsychicalMuted() && !sk.isMagic()))
			{
				continue;
			}
			int castRange = sk.getCastRange() + combinedCollision;
			if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || (dist2 > castRange * castRange))
			{
				continue;
			}

			// chance decision for launching general skills in melee fight
			// close range skills should be higher, long range lower
			int castingChance = 5;
			if (_selfAnalysis.isMage || _selfAnalysis.isHealer)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 35;
				}
				else
				{
					castingChance = 25; // mages
				}
			}
			if (_selfAnalysis.isBalanced)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else
				{
					if (_mostHatedAnalysis.isMage) // hit mages
					{
						castingChance = 2;
					}
					else
					{
						castingChance = 5;
					}
				}
			}
			if (_selfAnalysis.isFighter)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 1;
					}
					else
					{
						castingChance = 3;
					}
				}
			}

			if (Rnd.nextInt(100) <= castingChance)
			{
				clientStopMoving(null);
				_accessor.doCast(sk);
				return;
			}
		}

		// Finally, physical attacks
		if (!_selfAnalysis.isHealer)
		{
			clientStopMoving(null);
			_accessor.doAttack(_mostHatedAnalysis.character);
		}
	}

	/**
	 * Manage AI thinking actions of a L2Attackable.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking || _actor.isAllSkillsDisabled())
		{
			return;
		}

		// Start thinking action
		_thinking = true;

		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		} finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor
	 * _aggroList</li> <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all
	 * others L2PcInstance</li> <li>Set the Intention to AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 *
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}

		// Add the attacker to the _aggroList of the actor
		((L2Attackable) _actor).addDamageHate(attacker, 0, 1);

		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}

		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (((L2Attackable) _actor).getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li> <li>Set the actor Intention to
	 * AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR>
	 * <BR>
	 *
	 * @param The   L2Character that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = (L2Attackable) _actor;

		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}

				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		me = null;
	}

	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
}
