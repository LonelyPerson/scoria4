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
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.StatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class BalanceLife extends SkillAbst
{
	public BalanceLife()
	{
		_types = new SkillType[]{SkillType.BALANCE_LIFE};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		callSkillHandler(SkillType.BUFF, activeChar, skill, targets);

		L2Character target;
		double fullHP = 0;
		double currentHPs = 0;

		for (L2Object obj : targets)
		{
			target = (L2Character) obj;

			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}

		double percentHP = currentHPs / fullHP;

		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;

			if (target == null || target.isDead())
			{
				continue;
			}

			double newHP = target.getMaxHp() * percentHP;
			double totalHeal = newHP - target.getCurrentHp();

			target.setCurrentHp(newHP);

			if (totalHeal > 0)
			{
				target.setLastHealAmount((int) totalHeal);
			}

			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);

			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("HP of the party has been balanced.");
			target.sendPacket(sm);

		}

		return true;
	}
}
