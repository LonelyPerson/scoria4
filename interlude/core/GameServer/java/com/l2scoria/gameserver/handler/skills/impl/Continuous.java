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
package com.l2scoria.gameserver.handler.skills.impl;

//import org.apache.log4j.Logger;

import com.l2scoria.gameserver.ai.CtrlEvent;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.managers.DuelManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Formulas;
import com.l2scoria.util.random.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous extends SkillAbst
{
	public Continuous()
	{
		_types = new SkillType[]{L2Skill.SkillType.BUFF, L2Skill.SkillType.DEBUFF, L2Skill.SkillType.DOT, L2Skill.SkillType.MDOT, L2Skill.SkillType.POISON, L2Skill.SkillType.BLEED, L2Skill.SkillType.HOT, L2Skill.SkillType.CPHOT, L2Skill.SkillType.MPHOT,
				//L2Skill.SkillType.MANAHEAL,
				//L2Skill.SkillType.MANA_BY_LEVEL,
				L2Skill.SkillType.FEAR, L2Skill.SkillType.CONT, L2Skill.SkillType.WEAKNESS, L2Skill.SkillType.REFLECT, L2Skill.SkillType.UNDEAD_DEFENSE, L2Skill.SkillType.AGGDEBUFF, L2Skill.SkillType.FORCE_BUFF};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}
		L2Character target;
		L2PcInstance player = activeChar.getPlayer();

		if (skill.getEffectId() != 0)
		{
			int skillLevel = skill.getEffectLvl();
			int skillEffectId = skill.getEffectId();
			L2Skill _skill;
			if (skillLevel == 0)
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if (_skill != null)
			{
				skill = _skill;
			}
		}

		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;

			if (target != null && target.isPlayer && activeChar != null && activeChar.isPlayable && skill.isOffensive())
			{
				L2PcInstance _char = (activeChar.isPlayer) ? (L2PcInstance) activeChar : ((L2Summon) activeChar).getOwner();
				L2PcInstance _attacked = (L2PcInstance) target;
				if (_attacked.getClanId() != 0 && _char.getClanId() != 0 &&
						_attacked.getClanId() == _char.getClanId() && _attacked.getPvpFlag() == 0 && !_attacked.isInOlympiadMode())
				{
					continue;
				}
				if (_attacked.getAllyId() != 0 && _char.getAllyId() != 0 &&
						_attacked.getAllyId() == _char.getAllyId() && _attacked.getPvpFlag() == 0 && !_attacked.isInOlympiadMode())
				{
					continue;
				}
			}

			if (skill.getSkillType() != L2Skill.SkillType.BUFF && skill.getSkillType() != L2Skill.SkillType.HOT && skill.getSkillType() != L2Skill.SkillType.CPHOT && skill.getSkillType() != L2Skill.SkillType.MPHOT && skill.getSkillType() != L2Skill.SkillType.UNDEAD_DEFENSE && skill.getSkillType() != L2Skill.SkillType.AGGDEBUFF && skill.getSkillType() != L2Skill.SkillType.CONT)
			{
				if (target.reflectSkill(skill))
				{
					target = activeChar;
				}
			}

			// Walls and Door should not be buffed
			if (target.isDoor && (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.HOT))
			{
				continue;
			}

			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2Skill.SkillType.BUFF)
			{
				if (target != activeChar)
				{
					if (target.isPlayer && target.getPlayer().isCursedWeaponEquiped())
					{
						continue;
					}
					else if (player != null && player.isCursedWeaponEquiped())
					{
						continue;
					}
				}
			}

			//Possibility of a lethal strike
			if (!target.isRaid() && !(target.isNpc && ((L2NpcInstance) target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				if (skill.getLethalChance2() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if (target.isNpc)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
				else if (skill.getLethalChance1() > 0 && chance < Formulas.getInstance().calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if (target.isNpc)
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
			}
			if (skill.isOffensive() || skill.isDebuff())
			{
				boolean ss = false;
				boolean sps = false;
				boolean bss = false;

				if (player != null)
				{
					L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
					if (weaponInst != null)
					{
						if (skill.isMagic())
						{
							if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
							{
								bss = true;
								if (skill.getId() != 1020) // vitalize
								{
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
								}
							}
							else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
							{
								sps = true;
								if (skill.getId() != 1020) // vitalize
								{
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
								}
							}
						}
						else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
						{
							ss = true;
							if (skill.getId() != 1020) // vitalize
							{
								weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
							}
						}
					}
				}
				else if (activeChar.isSummon)
				{
					L2Summon activeSummon = (L2Summon) activeChar;
					if (skill.isMagic())
					{
						if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							bss = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
						else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							sps = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
					}
					else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
					{
						ss = true;
						activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
					}
				}

				boolean acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);

				if (!acted)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					continue;
				}
			}
			else if (skill.getSkillType() == L2Skill.SkillType.BUFF)
			{
				if (!Formulas.getInstance().calcBuffSuccess(target, skill))
				{
					if (player != null)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						activeChar.sendPacket(sm);
						continue;
					}
					else
					{
						continue;
					}
				}
			}

			L2Effect[] effects = target.getAllEffects();
			boolean stopped = false;
			if (effects != null)
			{
				for (L2Effect e : effects)
				{
					if (e != null && skill != null)
					// tyt figniy a ne kod... nado peresmotret...
					{
						if (!target.isInvul() && e.getSkill().getId() == skill.getId() && skill.getLevel() >= e.getSkill().getLevel() &&
								e.getSkill().getId() != 2031 && e.getSkill().getId() != 2032 && e.getSkill().getId() != 2037)
						{
							e.exit();
							stopped = true;
						}
					}
				}
			}

			if (skill.isToggle() && stopped)
			{
				return false;
			}

			/*if (skill.isToggle())
			{
				L2Effect[] effects = target.getAllEffects();
				if(effects != null)
				{
					for(L2Effect e : effects)
					{
						if(e != null && skill != null)
						{
							if(e.getSkill().getId() == skill.getId())
							{
								e.exit();
								return;
							}
						}
					}
				}
			}*/

			// If target is not in game anymore...
			if (target == null)
			{
				continue;
			}

			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if (player != null && target.isPlayer && target.getPlayer().isInDuel() && (skill.getSkillType() == L2Skill.SkillType.DEBUFF || skill.getSkillType() == L2Skill.SkillType.BUFF) && player.getDuelId() == target.getPlayer().getDuelId())
			{
				DuelManager dm = DuelManager.getInstance();
				for (L2Effect buff : skill.getEffects(activeChar, target))
				{
					if (buff != null)
					{
						dm.onBuff(target.getPlayer(), buff);
					}
				}
			}
			else
			{
				skill.getEffects(activeChar, target);
			}

			if (skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF)
			{
				if (target.isAttackable)
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
				}
				else if (target.isPlayable)
				{
					if (target.getTarget() == activeChar)
					{
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
					}
					else
					{
						target.setTarget(activeChar);
					}
				}
			}

			if (target.isDead() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB && target.isNpc)
			{
				((L2NpcInstance) target).endDecayTask();
			}
		}

		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);

		return true;
	}
}
