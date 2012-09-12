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
package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /olympiadstat command Added by kamy
 */
public class OlympiadStat extends CommandAbst
{
	public OlympiadStat()
	{
		_commands = new int[]{109};
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		if(activeChar.isNoble())
		{
			Olympiad olympiad = Olympiad.getInstance();
			int charId = activeChar.getObjectId();

			SystemMessage sm = new SystemMessage(SystemMessageId.THE_PRESENT_RECORD_DURING_THE_CURRENT_OLYMPIAD_SESSION_IS_S1_MATCHES_PLAYED_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS);
			sm.addNumber(olympiad.getCompetitionDone(charId)); // кол-во боев
			sm.addNumber(olympiad.getCompetitionWon(charId)); // кол-во выиграных боев
			sm.addNumber(olympiad.getCompetitionLost(charId)); // кол-во проигранных боев
			sm.addNumber(olympiad.getNoblePoints(charId)); // кол-во очков
			activeChar.sendPacket(sm);
			return true;
		}

		activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE));
		return true;
	}
}
