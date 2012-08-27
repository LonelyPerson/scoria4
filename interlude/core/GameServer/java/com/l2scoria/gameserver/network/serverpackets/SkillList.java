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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Skill;

import java.util.List;

/**
 * sample 0000: 6d 0c 00 00 00 00 00 00 00 03 00 00 00 f3 03 00 m............... 0010: 00 00 00 00 00 01 00 00 00 f4 03
 * 00 00 00 00 00 ................ 0020: 00 01 00 00 00 10 04 00 00 00 00 00 00 01 00 00 ................ 0030: 00 2c 04
 * 00 00 00 00 00 00 03 00 00 00 99 04 00 .,.............. 0040: 00 00 00 00 00 02 00 00 00 a0 04 00 00 00 00 00
 * ................ 0050: 00 01 00 00 00 c0 04 00 00 01 00 00 00 01 00 00 ................ 0060: 00 76 00 00 00 01 00 00
 * 00 01 00 00 00 a3 00 00 .v.............. 0070: 00 01 00 00 00 01 00 00 00 c2 00 00 00 01 00 00 ................ 0080:
 * 00 01 00 00 00 d6 00 00 00 01 00 00 00 01 00 00 ................ 0090: 00 f4 00 00 00 format d (ddd)
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class SkillList extends L2GameServerPacket
{
	private static final String _S__6D_SKILLLIST = "[S] 58 SkillList";
	private final List<L2Skill> _skills;

	public SkillList(List<L2Skill> list)
	{
		_skills = list;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x58);
		writeD(_skills.size());

		for (L2Skill s : _skills)
		{
			writeD(s.isPassive() || s.isChance() ? 1 : 0);
			writeD(s.getLevel());
			writeD(s.getDisplayId());

			int grayed = 0;
			if (Config.DISABLE_SKILLS_ON_LEVEL_LOST && s.getMagicLevel() - getClient().getActiveChar().getLevel() >= Config.DISABLE_SKILLS_LEVEL_DIF)
			{
				grayed = 1;
			}

			writeC(grayed); // c5
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__6D_SKILLLIST;
	}
}
