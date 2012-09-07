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
package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;

import java.util.Arrays;

public class CustomPotions extends ItemAbst
{
	public CustomPotions()
	{
		if (Config.ENABLE_POTION_SKILL_ATTACH)
		{
			_items = Arrays.copyOf(Config.POTION_SKILL_ATTACH.keys(), Config.POTION_SKILL_ATTACH.size());
			_requiresActingPlayer = true;
			_notOnOlympiad = true;
			_notWhenSkillsDisabled = true;
		}
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		if (usePotion(playable.getPlayer(), Config.POTION_SKILL_ATTACH.get(item.getItemId()), 1))
		{
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}

		return true;
	}

	public boolean usePotion(L2PcInstance activeChar, int skillId, int skillLvl)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
		if (skill != null)
		{
			if (!((activeChar.isSitting() || activeChar.isParalyzed() || activeChar.isAway() || activeChar.isFakeDeath()) && !skill.isPotion()))
			{
				activeChar.doCast(skill);
				return true;
			}
		}
		return false;
	}
}
