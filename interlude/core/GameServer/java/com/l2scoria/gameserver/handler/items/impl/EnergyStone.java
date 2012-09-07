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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.effects.EffectCharge;
import com.l2scoria.gameserver.skills.l2skills.L2SkillCharge;

/**
 *
 * @author ProGramMoS
 */
public class EnergyStone extends ItemAbst
{

	public EnergyStone()
	{
		_items = new int[]{5589};
		_playerUseOnly = true;
		_notSitting = true;
		_notWhenSkillsDisabled = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		int classid = activeChar.getClassId().getId();
		if (classid == 2 || classid == 48 || classid == 88 || classid == 114)
		{
			L2SkillCharge skill = getChargeSkill(activeChar);
			if (skill == null)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item.getItemId()));
				return false;
			}

			EffectCharge effect = activeChar.getChargeEffect();
			if (effect == null)
			{
				L2Skill dummy = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
				if (dummy != null)
				{
					dummy.getEffects(activeChar, activeChar);
					activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
					return true;
				}

				return false;
			}

			if (effect.getLevel() < 2)
			{
				activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, skill.getId(), 1, 1, 0));

				effect.addNumCharges(1);
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			}
			else if (effect.getLevel() == 2)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
				return false;
			}

			activeChar.sendPacket(new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(effect.getLevel()));
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item.getItemId()));
		}

		return true;
	}

	/**
	 * @param activeChar
	 * @return
	 */
	private L2SkillCharge getChargeSkill(L2PcInstance activeChar)
	{
		L2Skill[] skills = activeChar.getAllSkills();
		for (L2Skill s : skills)
		{
			if (s.getId() == 50 || s.getId() == 8)
			{
				return (L2SkillCharge) s;
			}
		}

		return null;
	}
}
