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
import com.l2scoria.util.random.Rnd;

public class ZakenPlayer extends SkillAbst
{
	public ZakenPlayer()
	{
		_types = new SkillType[]{SkillType.ZAKENPLAYER};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		try
		{
			for (L2Object target1 : targets)
			{
				if (!(target1.isCharacter))
				{
					continue;
				}

				L2Character target = (L2Character) target1;
				int mode = (Rnd.get(14) + 1);
				switch (mode)
				{
					case 1:
						target.teleToLocation(55299,219120,-2952, true);
						break;
					case 2:
						target.teleToLocation(56363,218043,-2952, true);
						break;
					case 3:
						target.teleToLocation(54245,220162,-2952, true);
						break;
					case 4:
						target.teleToLocation(56289,220126,-2952, true);
						break;
					case 5:
						target.teleToLocation(55299,219120,-3224, true);
						break;
					case 6:
						target.teleToLocation(56363,218043,-3224, true);
						break;
					case 7:
						target.teleToLocation(54245,220162,-3224, true);
						break;
					case 8:
						target.teleToLocation(56289,220126,-3224, true);
						break;
					case 9:
						target.teleToLocation(55299,219120,-3496, true);
						break;
					case 10:
						target.teleToLocation(56363,218043,-3496, true);
						break;
					case 11:
						target.teleToLocation(54245,220162,-3496, true);
						break;
					case 12:
						target.teleToLocation(56289,220126,-3496, true);
						break;
					default:
						target.teleToLocation(53930,217760,-2944, true);
						break;
				}
			}
		}
		catch(Throwable e)
		{
			_log.error(e.getMessage(), e);
			return false;
		}

		return true;
	}
}
