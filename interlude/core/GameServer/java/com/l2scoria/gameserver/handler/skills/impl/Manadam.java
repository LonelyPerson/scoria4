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

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.StatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Formulas;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam extends SkillAbst
{
	public Manadam()
	{
		_types = new SkillType[]{SkillType.MANADAM};

		_notAlikeDead = true;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2Character target;
		boolean ss = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}

		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;

			if (target.reflectSkill(skill))
			{
				target = activeChar;
			}

			boolean acted = Formulas.getInstance().calcMagicAffected(activeChar, target, skill);
			if (target.isInvul() || !acted)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
			}
			else
			{
				double damage = Formulas.getInstance().calcManaDam(activeChar, target, skill, ss, bss);

				double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);

				if (damage > 0)
				{
					if (target.isSleeping())
					{
						target.stopSleeping(null);
					}
				}

				StatusUpdate sump = new StatusUpdate(target.getObjectId());
				sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				target.sendPacket(sump);

				SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1);

				if (activeChar.isNpc)
				{
					int mobId = ((L2NpcInstance) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else if (activeChar.isSummon)
				{
					int mobId = ((L2Summon) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(activeChar.getName());
				}

				sm.addNumber((int) mp);
				target.sendPacket(sm);

				if (activeChar.isPlayer)
				{
					SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.sendPacket(sm2);
				}
			}
		}

		return true;
	}
}
