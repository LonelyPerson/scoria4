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
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.StatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Stats;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal extends SkillAbst
{
	public Heal()
	{
		_types = new SkillType[]{SkillType.HEAL, SkillType.HEAL_PERCENT, SkillType.HEAL_STATIC};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		callSkillHandler(SkillType.BUFF, activeChar, skill, targets);

		L2Character target;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		L2PcInstance player = activeChar.getPlayer();

		boolean clearSpiritShot = false;

		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;

			// We should not heal if char is dead
			if (target == null || target.isDead())
			{
				continue;
			}

			// We should not heal walls and door
			if (target.isDoor)
			{
				continue;
			}

			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target.isPlayer && target.getPlayer().isCursedWeaponEquiped())
				{
					continue;
				}
				if (player != null && player.isCursedWeaponEquiped())
				{
					continue;
				}
				if (Config.CANNOT_HEAL_RBGB && activeChar.isPlayer && target.isRaid())
				{
					continue;
				}
			}

			double hp = skill.getPower();

			if (skill.getSkillType() == SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else
			{
				//Added effect of SpS and Bsps
				if (weaponInst != null)
				{
					if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						hp *= 1.5;
						clearSpiritShot = true;
					}
					else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					{
						hp *= 1.3;
						clearSpiritShot = true;
					}
				}
				// If there is no weapon equipped, check for an active summon.
				else if (activeChar.isSummon)
				{
					L2Summon activeSummon = (L2Summon) activeChar;

					if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						hp *= 1.5;
						clearSpiritShot = true;
					}
					else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					{
						hp *= 1.3;
						clearSpiritShot = true;
					}
				}
			}

			//int cLev = activeChar.getLevel();
			//hp += skill.getPower()/*+(Math.sqrt(cLev)*cLev)+cLev*/;
			if (skill.getSkillType() == SkillType.HEAL_STATIC)
			{
				hp = skill.getPower();
			}
			else if (skill.getSkillType() != SkillType.HEAL_PERCENT)
			{
				hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			}

			target.setCurrentHp(hp + target.getCurrentHp());
			target.setLastHealAmount((int) hp);
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);

			if (target.isPlayer)
			{
				if (skill.getId() == 4051)
				{
					target.sendPacket(new SystemMessage(SystemMessageId.REJUVENATING_HP));
				}
				else
				{
					if (activeChar.isPlayer && activeChar != target)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
				}
			}
		}

		if (clearSpiritShot)
		{
			if (activeChar.isSummon)
			{
				L2Summon activeSummon = (L2Summon) activeChar;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				if (weaponInst != null)
				{
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
			}
		}

		return true;
	}
}
