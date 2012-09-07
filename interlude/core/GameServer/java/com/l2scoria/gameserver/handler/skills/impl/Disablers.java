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

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlEvent;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.ai.L2AttackableAI;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.NegateStats;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2scoria.gameserver.model.base.Experience;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Formulas;
import com.l2scoria.gameserver.skills.Stats;
import com.l2scoria.util.GArray;
import com.l2scoria.util.random.Rnd;

/**
 * This Handles Disabler skills
 *
 * @author _drunk_
 */
public class Disablers extends SkillAbst
{
	public Disablers()
	{
		_types = new SkillType[]{L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT, L2Skill.SkillType.SLEEP, L2Skill.SkillType.CONFUSION, L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE, L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE, L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON, L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH, L2Skill.SkillType.CONFUSE_MOB_ONLY, L2Skill.SkillType.NEGATE, L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE, L2Skill.SkillType.ERASE, L2Skill.SkillType.MAGE_BANE, L2Skill.SkillType.WARRIOR_BANE, L2Skill.SkillType.BETRAY};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		SkillType type = skill.getSkillType();

		boolean ss = false;
		boolean sps = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (activeChar.isPlayer)
		{
			if (weaponInst == null && skill.isOffensive())
			{
				SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S2);
				sm2.addString("You must equip a weapon before casting a spell.");
				activeChar.sendPacket(sm2);
				return false;
			}
		}

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
		// If there is no weapon equipped, check for an active summon.
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

		for (L2Object target2 : targets)
		{
			// Get a target
			if (!(target2.isCharacter))
			{
				continue;
			}

			L2Character target = (L2Character) target2;

			if (target == null || target.isDead()) //bypass if target is null or dead
			{
				continue;
			}

			switch (type)
			{
				case BETRAY:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
					}
					break;
				}
				case FAKE_DEATH:
				{
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target);
					break;
				}
				case STUN:
					// Calculate skill evasion
					if (Formulas.getInstance().calcPhysicalSkillEvasion(target, skill))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
						break;
					}
					// Calculate vengeance
					if (target.vengeanceSkill(skill))
					{
						target = activeChar;
					}
				case ROOT:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}

					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case SLEEP:
				case PARALYZE: //use same as root for now
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}

					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSION:
				case MUTE:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}

					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					// do nothing if not on mob
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
						{
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
						}
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (target.isAttackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					}
					//TODO [Nemesiss] should this have 100% chance?
					skill.getEffects(activeChar, target);
					break;
				}
				case AGGREDUCE:
				{
					// these skills needs to be rechecked
					if (target.isAttackable)
					{
						skill.getEffects(activeChar, target);

						double aggdiff = ((L2Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target, skill);

						if (skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR:
				{
					// these skills needs to be rechecked
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (target.isAttackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null)
							{
								((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case AGGREMOVE:
				{
					// these skills needs to be rechecked
					if (target.isAttackable && !target.isRaid())
					{
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
								{
									((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
								}
							}
							else
							{
								((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
							}
						}
						else
						{
							if (activeChar.isPlayer)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getId());
								activeChar.sendPacket(sm);
							}
						}
					}
					break;
				}
				case UNBLEED:
				{
					negateEffect(target, SkillType.BLEED, skill.getPower());
					break;
				}
				case UNPOISON:
				{
					negateEffect(target, SkillType.POISON, skill.getPower());
					break;
				}
				case ERASE:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss)
							// doesn't affect siege golem or wild hog cannon
							&& !(target instanceof L2SiegeSummonInstance))
					{
						L2PcInstance summonOwner;
						L2Summon summonPet;
						summonOwner = ((L2Summon) target).getOwner();
						summonPet = summonOwner.getPet();
						summonPet.unSummon(summonOwner);

						summonOwner.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
					else
					{
						if (activeChar.isPlayer)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case MAGE_BANE:
				{
					for (L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;

						if (target1.reflectSkill(skill))
						{
							target1 = activeChar;
						}

						if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}

						L2Effect[] effects = target1.getAllEffects();

						for (L2Effect e : effects)
						{
							if (e.getSkill().getId() == 1059 || e.getSkill().getId() == 1085 ||
									e.getSkill().getId() == 4356 || e.getSkill().getId() == 4355)
							{
								e.exit();
							}
						}
					}
					break;
				}
				case WARRIOR_BANE:
				{
					for (L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;

						if (target1.reflectSkill(skill))
						{
							target1 = activeChar;
						}

						if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}

						L2Effect[] effects = target1.getAllEffects();

						for (L2Effect e : effects)
						{
							if (e.getSkill().getId() == 1204 || e.getSkill().getId() == 1086 ||
									e.getSkill().getId() == 4342 || e.getSkill().getId() == 4357)
							{
								e.exit();
							}
						}
					}
					break;
				}
				case CANCEL:
				{
					if (target.reflectSkill(skill))
					{
						target = activeChar;
					}

					if (target.isInvul())
					{
						break;
					}

					if (skill.getId() == 1056)
					{
						if (target.isRaid())
						{
							if (activeChar.isPlayer)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
							break;
						}
						int lvlmodifier = 52 + skill.getLevel() * 2;
						if (skill.getLevel() == 12)
						{
							lvlmodifier = (Experience.MAX_LEVEL - 1);
						}

						int landrate = (int) skill.getPower();
						if ((target.getLevel() - lvlmodifier) > 0)
						{
							landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
						}

						landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);

						if (Rnd.get(100) < landrate)
						{
							L2Effect[] effects = target.getAllEffects();
							int maxfive = 5;
							for (L2Effect e : effects)
							{
								switch (e.getEffectType())
								{
									case SIGNET_GROUND:
									case SIGNET_EFFECT:
										continue;
								}

								if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 5182 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 && e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325)
								// Cannot cancel skills 4082, 4215, 4515, 110, 111, 1323, 1325
								{
									if (e.getSkill().getSkillType() != SkillType.BUFF) //sleep, slow, surrenders etc
									{
										e.exit();
									}
									else
									{
										int rate = 100;
										int level = e.getLevel();
										if (level > 0)
										{
											rate = 150 / (1 + level);
										}

										if (rate > 95)
										{
											rate = 95;
										}
										else if (rate < 5)
										{
											rate = 5;
										}

										if (Rnd.get(100) < rate)
										{
											e.exit();
											maxfive--;
											if (maxfive == 0)
											{
												break;
											}
										}
									}
								}
							}
						}
						else
						{
							if (activeChar.isPlayer)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
						}
						break;
					}
					else
					// all others cancel type skills
					{
						int landrate = (int) skill.getPower();
						landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);
						if (Rnd.get(100) < landrate)
						{
							L2Effect[] effects = target.getAllEffects();
							int maxdisp = (int) skill.getNegatePower();
							if (maxdisp == 0)
							{
								maxdisp = Config.BUFFS_MAX_AMOUNT + Config.DEBUFFS_MAX_AMOUNT + 6;
							}
							for (L2Effect e : effects)
							{
								switch (e.getEffectType())
								{
									case SIGNET_GROUND:
									case SIGNET_EFFECT:
										continue;
								}

								if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 5182 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 && e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325)
								{
									if (e.getSkill().getSkillType() == SkillType.BUFF)
									{
										int rate = 100;
										int level = e.getLevel();
										if (level > 0)
										{
											rate = 150 / (1 + level);
										}

										if (rate > 95)
										{
											rate = 95;
										}
										else if (rate < 5)
										{
											rate = 5;
										}

										if (Rnd.get(100) < rate)
										{
											e.exit();
											maxdisp--;
											if (maxdisp == 0)
											{
												break;
											}
										}
									}
								}
							}
						}
						else
						{
							if (activeChar.isPlayer)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
						}
						break;
					}
				}
				case NEGATE:
				{
					if (skill.getId() == 2275) // fishing potion
					{
						negateEffect(target, SkillType.BUFF, skill.getNegatePower(), skill.getNegateId());
					}
					else // all others negate type skills
					{
						GArray<NegateStats> _negateStats = skill.getNegateStats();

						for (NegateStats stat : _negateStats)
						{
							switch (stat)
							{
								case BUFF:
								{
									int lvlmodifier = 52 + skill.getMagicLevel() * 2;
									if (skill.getMagicLevel() == 12)
									{
										lvlmodifier = (Experience.MAX_LEVEL - 1);
									}

									int landrate = 90;
									if ((target.getLevel() - lvlmodifier) > 0)
									{
										landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
									}

									landrate = (int) target.calcStat(Stats.CANCEL_VULN, landrate, target, null);

									if (Rnd.get(100) < landrate)
									{
										negateEffect(target, SkillType.BUFF, -1);
									}
									break;
								}

								case DEBUFF:
									negateEffect(target, SkillType.DEBUFF, -1);
									break;
								case WEAKNESS:
									negateEffect(target, SkillType.WEAKNESS, -1);
									break;
								case STUN:
									negateEffect(target, SkillType.STUN, -1);
									break;
								case SLEEP:
									negateEffect(target, SkillType.SLEEP, -1);
									break;
								case CONFUSION:
									negateEffect(target, SkillType.CONFUSION, -1);
									break;
								case MUTE:
									negateEffect(target, SkillType.MUTE, -1);
									break;
								case FEAR:
									negateEffect(target, SkillType.FEAR, -1);
									break;
								case POISON:
									negateEffect(target, SkillType.POISON, -1);
									break;
								case BLEED:
									negateEffect(target, SkillType.BLEED, -1);
									break;
								case PARALYZE:
									negateEffect(target, SkillType.PARALYZE, -1);
									break;
								case ROOT:
									negateEffect(target, SkillType.ROOT, -1);
									break;
								case HEAL:
								{
									if (Config.CANNOT_HEAL_RBGB && activeChar.isPlayer && target.isRaid())
									{
										activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
										activeChar.sendPacket(ActionFailed.STATIC_PACKET);
										continue;
									}

									callSkillHandler(SkillType.HEAL, activeChar, skill, new L2Object[]{target});
									break;
								}

							}
						}
					}
				}
			}
		}

		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}

		skill.getEffectsSelf(activeChar);
		return true;
	}

	private void negateEffect(L2Character target, SkillType type, double power)
	{
		negateEffect(target, type, power, 0);
	}

	private void negateEffect(L2Character target, SkillType type, double power, int skillId)
	{
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
		{
			if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
			{
				if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type))
				{
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId())
						{
							e.exit();
						}
					}
					else
					{
						e.exit();
					}
				}
			}
			else if ((e.getSkill().getSkillType() == type && e.getSkill().getPower() <= power) || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type && e.getSkill().getEffectLvl() <= power))
			{
				if (skillId != 0)
				{
					if (skillId == e.getSkill().getId())
					{
						e.exit();
					}
				}
				else
				{
					e.exit();
				}
			}
		}
	}
}
