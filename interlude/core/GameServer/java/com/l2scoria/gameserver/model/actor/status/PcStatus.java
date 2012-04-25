/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.model.actor.status;

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.managers.DuelManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SummonInstance;
import com.l2scoria.gameserver.model.entity.Duel;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Stats;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

public class PcStatus extends PlayableStatus
{
	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}

	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if(getActiveChar().isInvul() && getActiveChar() != attacker)
			return;

		int fullValue = (int) value;

		if(attacker != null && attacker != getActiveChar())
		{
			if(getActiveChar().isInDuel())
			{
				// the duel is finishing - players do not recive damage
				if(getActiveChar().getDuelState() == Duel.DUELSTATE_DEAD)
					return;
				else if(getActiveChar().getDuelState() == Duel.DUELSTATE_WINNER)
					return;

				// cancel duel if player got hit by another player, that is not part of the duel
				if(!(attacker instanceof L2PlayableInstance) || (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getDuelId() != getActiveChar().getDuelId()))
				{
					getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
			}

			if(getActiveChar().isDead())
			{
				return;
			}

			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();
			if(summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(1000, getActiveChar(), summon, true))
			{
				int tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

				// Only transfer dmg up to current HP, it should not be killed
				if(summon.getCurrentHp() < tDmg)
				{
					tDmg = (int) summon.getCurrentHp() - 1;
				}

				if(tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker);
					value -= tDmg;
					fullValue = (int) value; // reduce the annouced value here as player will get a message about summon dammage
				}
			}

			if(attacker instanceof L2PlayableInstance || attacker instanceof L2SiegeGuardInstance)
			{
				if(getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0); // Set Cp to 0
				}
			}

			summon = null;
		}
		
		if(getActiveChar().isSitting())
		{
			getActiveChar().standUp();
		}

		if(getActiveChar().isFakeDeath())
		{
			getActiveChar().stopFakeDeath(null);
		}

		if(awake && getActiveChar().isSleeping())
		{
			getActiveChar().stopSleeping(null);
		}

		if(awake && getActiveChar().isImmobileUntilAttacked())
		{
			getActiveChar().stopImmobileUntilAttacked(null);
		}

		if(awake && getActiveChar().isStunned() && Rnd.get(10) == 0)
		{
			getActiveChar().stopStunning(null);
		}

		if(attacker != null && attacker != getActiveChar() && fullValue > 0)
		{
			// Send a System Message to the L2PcInstance
			SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);

			if(Config.DEBUG)
			{
				_log.fine("Attacker:" + attacker.getName());
			}

			if(attacker instanceof L2NpcInstance)
			{
				int mobId = ((L2NpcInstance) attacker).getTemplate().idTemplate;

				if(Config.DEBUG)
				{
					_log.fine("mob id:" + mobId);
				}

				smsg.addNpcName(mobId);
			}
			else if(attacker instanceof L2Summon)
			{
				int mobId = ((L2Summon) attacker).getTemplate().idTemplate;

				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}

			smsg.addNumber(fullValue);
			getActiveChar().sendPacket(smsg);
			smsg = null;
		}

		if(value > 0) // Reduce Hp if any
		{
			value = getCurrentHp() - value; // Get diff of Hp vs value
			if(value <= 0)
			{
				if(getActiveChar().isInDuel())
				{
					getActiveChar().disableAllSkills();
					stopHpMpRegeneration();
					if (attacker instanceof L2PcInstance)
					{
						attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						attacker.sendPacket(ActionFailed.STATIC_PACKET);
					}

					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat((L2PcInstance) getActiveChar());
					value = 1;
				}
				else
				{
					// Set value to 0 if Hp < 0
					value = 0;
				}
			}
			setCurrentHp(value); // Set Hp
		}

		if(getActiveChar().isDead())
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if(getActiveChar().isInOlympiadMode())
			{
				if(getActiveChar().getPet() != null)
				{
					getActiveChar().getPet().doDie(null);
				}
				stopHpMpRegeneration();
				return;
			}

			// first die (and calculate rewards), if currentHp < 0,
			// then overhit may be calculated
			if(Config.DEBUG)
			{
				_log.fine("char is dead.");
			}

			// Start the doDie process
			getActiveChar().doDie(attacker);
		}
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}
