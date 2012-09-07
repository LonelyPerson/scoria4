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

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.managers.FortManager;
import com.l2scoria.gameserver.managers.FortSiegeManager;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.entity.siege.Fort;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author programmos, scoria dev
 */
public class SiegeFlag extends SkillAbst
{
	public SiegeFlag()
	{
		_types = new SkillType[]{SkillType.SIEGEFLAG};

		_playerUseOnly = true;
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) activeChar;
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
		{
			return false;
		}

		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);
		if ((castle == null) && (fort == null))
		{
			return false;
		}

		if (castle != null)
		{
			if (!checkIfOkToPlaceFlag(player, castle, true))
			{
				return false;
			}
		}
		else
		{
			if (!checkIfOkToPlaceFlag(player, fort, true))
			{
				return false;
			}
		}

		try
		{
			// Spawn a new flag
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);

			if (castle != null)
			{
				castle.getSiege().getFlag(player.getClan()).add(flag);
			}
			else
			{
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}
		} catch (Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
		}

		return true;
	}

	/**
	 * Return true if character clan place a flag<BR>
	 * <BR>
	 *
	 * @param activeChar  The L2Character of the character placing the flag
	 * @param isCheckOnly if false, it will send a notification to the player telling him why it failed
	 */
	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
	{
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);
		if ((castle == null) && (fort == null))
		{
			return false;
		}

		if (castle != null)
		{
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		}
		else
		{
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		}
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar.isPlayer))
		{
			return false;
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;

		if (castle == null || castle.getCastleId() <= 0)
		{
			sm.addString("You must be on castle ground to place a flag");
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm.addString("You can only place a flag during a siege.");
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to place a flag");
		}
		else if (!activeChar.isInsideZone(L2Character.ZONE_HQ))
		{
			sm.addString("You cannot place flag here.");
		}
		else if (player.getClan() == null || !player.isClanLeader())
		{
			sm.addString("You must be a clan leader to place a flag");
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
		{
			sm.addString("You have already placed the maximum number of flags possible");
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Fort fort, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar.isPlayer))
		{
			return false;
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;

		if (fort == null || fort.getFortId() <= 0)
		{
			sm.addString("You must be on fort ground to place a flag");
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm.addString("You can only place a flag during a siege.");
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to place a flag");
		}
		else if (player.getClan() == null || !player.isClanLeader())
		{
			sm.addString("You must be a clan leader to place a flag");
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= FortSiegeManager.getInstance().getFlagMaxCount())
		{
			sm.addString("You have already placed the maximum number of flags possible");
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}

		return false;
	}
}
