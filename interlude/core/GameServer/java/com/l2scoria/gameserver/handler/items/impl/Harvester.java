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

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.managers.CastleManorManager;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvester extends ItemAbst
{
	public Harvester()
	{
		_items = new int[]{5125};

		_playerUseOnly = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		if (CastleManorManager.getInstance().isDisabled())
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		if (activeChar.getTarget() == null || !activeChar.getTarget().isMonster || !((L2MonsterInstance)activeChar.getTarget()).isDead())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); //harvesting skill
		activeChar.useMagic(skill, false, false);
		return true;
	}
}
