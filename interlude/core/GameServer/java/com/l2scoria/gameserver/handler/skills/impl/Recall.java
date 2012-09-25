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
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

public class Recall extends SkillAbst
{
	public Recall()
	{
		_types = new SkillType[]{SkillType.RECALL};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		if (activeChar.isPlayer)
		{
			if (((L2PcInstance) activeChar).isInOlympiadMode() || ((L2PcInstance) activeChar).inObserverMode())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				return false;
			}
			if (activeChar.isInsideZone(L2Character.ZONE_PVP))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
				return false;
			}
			if (activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
				return false;
			}
		}

		for (L2Object target1 : targets)
		{
			if (!(target1.isCharacter))
			{
				continue;
			}

			L2Character target = (L2Character) target1;

			if (target.isPlayer)
			{
				L2PcInstance targetChar = (L2PcInstance) target;

				if (targetChar.isFestivalParticipant())
				{
					targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
					continue;
				}

				if (targetChar.isInJail())
				{
					targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
					continue;
				}

				if (targetChar.isInDuel())
				{
					targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
					continue;
				}

				if (targetChar._event != null && targetChar._event.isRunning())
				{
					targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during an event."));
					continue;
				}

				if (targetChar.isAlikeDead())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
					sm.addString(targetChar.getName());
					activeChar.sendPacket(sm);
					continue;
				}

				if (targetChar.isInStoreMode())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
					sm.addString(targetChar.getName());
					activeChar.sendPacket(sm);
					continue;
				}

				if (Config.ALLOW_PARTY_RECAL_IN_FIGHT && (targetChar.isRooted() || targetChar.isInCombat()))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
					sm.addString(targetChar.getName());
					activeChar.sendPacket(sm);
					continue;
				}

				if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
					continue;
				}

				if (targetChar.isInOlympiadMode() || targetChar.inObserverMode())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
					continue;
				}

				if (targetChar.isInsideZone(L2Character.ZONE_PVP))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
					continue;
				}
			}

			target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}

		return true;
	}
}
