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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance.TimeStamp;

/**
 * @author KenM
 */
public class SkillCoolTime extends L2GameServerPacket
{
	private static final String _S__C1_SKILLCOOLTIME = "[S] c1 SkillCoolTime";
	
	public SkillCoolTime()
	{
	}

	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
	
		if (activeChar == null)
			return;

		writeC(0xc1);
		writeD(activeChar.getReuseTimeStamps().size());
		for (TimeStamp ts : activeChar.getReuseTimeStamps().values())
		{
			writeD(ts.getSkill());
			writeD(0x00);
			writeD(Math.round(ts.getReuse() / 1000));
			writeD(Math.round(ts.getRemaining() / 1000));
		}
	}

	@Override
	public String getType()
	{
		return _S__C1_SKILLCOOLTIME;
	}
}