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
package com.l2scoria.gameserver.handler.skills.impl;

import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Formulas;
import com.l2scoria.gameserver.skills.Stats;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;

public class Blow extends SkillAbst
{
	public Blow()
	{
		_types = new SkillType[]{SkillType.BLOW};

		_notAlikeDead = true;
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			if (target.isAlikeDead())
			{
				continue;
			}

			// Calculate skill evasion
			if (Formulas.getInstance().calcPhysicalSkillEvasion(target, skill))
			{
				if (activeChar.isPlayer)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_DODGES_ATTACK);
					sm.addString(target.getName());
					activeChar.sendPacket(sm);
				}
				if (target.isPlayer)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
					sm.addString(activeChar.getName());
					target.sendPacket(sm);
				}
				continue;
			}

			if (Formulas.getInstance().calcBlow(activeChar, target, skill))
			{
				// Calculate vengeance
				if (target.vengeanceSkill(skill))
				{
					if (target.isPlayer)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.COUNTERED_S1_ATTACK);
						sm.addString(activeChar.getName());
						target.sendPacket(sm);
					}
					if (activeChar.isPlayer)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK);
						sm.addString(target.getName());
						activeChar.sendPacket(sm);
					}
					target = activeChar;
				}

				if (skill.hasEffects())
				{
					if (target.reflectSkill(skill))
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(null, activeChar);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false))
						{
							target.stopSkillEffects(skill.getId());
							skill.getEffects(activeChar, target);
							SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill.getId());
							target.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
				}

				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER);
				boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);

				double damage = (int) Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);

				// Crit rate base crit rate for skill, modified with STR bonus
				if (Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
				{
					damage *= 2;
				}

				if (soul)
				{
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				}

				if (skill.getDmgDirectlyToHP() && target.isPlayer)
				{
					L2PcInstance player = (L2PcInstance) target;
					if (!player.isInvul())
					{
						// Check and calculate transfered damage 
						L2Summon summon = player.getPet();
						if (summon != null && summon.isSummonInstance && Util.checkIfInRange(900, player, summon, true))
						{
							int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

							// Only transfer dmg up to current HP, it should not be killed 
							if (summon.getCurrentHp() < tDmg)
							{
								tDmg = (int) summon.getCurrentHp() - 1;
							}
							if (tDmg > 0)
							{
								summon.reduceCurrentHp(tDmg, activeChar);
								damage -= tDmg;
							}
						}

						if (damage >= player.getCurrentHp())
						{
							if (player.isInDuel())
							{
								player.setCurrentHp(1);
							}
							else
							{
								player.setCurrentHp(0);
								if (player.isInOlympiadMode())
								{
									player.abortAttack();
									player.abortCast();
									player.getStatus().stopHpMpRegeneration();
									if (player.getPet() != null)
									{
										player.getPet().doDie(null);
									}
								}
								else
								{
									player.doDie(activeChar);
								}
							}
						}
						else
						{
							player.setCurrentHp(player.getCurrentHp() - damage);
						}

						SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
						smsg.addString(activeChar.getName());
						smsg.addNumber((int) damage);
						player.sendPacket(smsg);
					}
				}
				else
				{
					target.reduceCurrentHp(damage, activeChar);
				}

				activeChar.sendDamageMessage(target, (int) damage, false, true, false);
			}
			//Possibility of a lethal strike
			if (!target.isRaid() && !(target.isDoor) && !(target.isNpc && ((L2NpcInstance) target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				//2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
				if (skill.getLethalChance2() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if (target.isNpc)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
					}
					else if (target.isPlayer) // If is a active player set his HP and CP to 1
					{
						L2PcInstance player = target.getPlayer();
						if (!player.isInvul() && !player.isDead())
						{
							player.setCurrentHp(1);
							player.setCurrentCp(1);
						}
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
				else if (skill.getLethalChance1() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if (target.isPlayer)
					{
						L2PcInstance player = target.getPlayer();
						if (!player.isInvul())
						{
							player.setCurrentCp(1); // Set CP to 1
						}
					}
					else if (target.isNpc) // If is a monster remove first damage and after 50% of current hp
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
					}
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
				}
			}

			L2Effect effect = activeChar.getFirstEffect(skill.getId());

			//Self Effect
			if (effect != null && effect.isSelfEffect())
			{
				effect.exit();
			}

			skill.getEffectsSelf(activeChar);
		}

		return true;
	}
}