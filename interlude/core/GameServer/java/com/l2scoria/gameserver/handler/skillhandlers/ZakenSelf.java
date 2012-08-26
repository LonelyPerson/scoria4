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
package com.l2scoria.gameserver.handler.skillhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.handler.ISkillHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.util.random.Rnd;

public class ZakenSelf implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(ZakenSelf.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.ZAKENSELF };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		try
		{
			for (L2Object target1 : targets)
			{
				if (!(target1 instanceof L2Character))
				{
					continue;
				}
				L2Character target = (L2Character) target1;
				int ch = (Rnd.get(14) + 1);
				if (ch == 1)
				{
					target.teleToLocation(55299, 219120, -2952, true);
				}
				else if (ch == 2)
				{
					target.teleToLocation(56363, 218043, -2952, true);
				}
				else if (ch == 3)
				{
					target.teleToLocation(54245, 220162, -2952, true);
				}
				else if (ch == 4)
				{
					target.teleToLocation(56289, 220126, -2952, true);
				}
				else if (ch == 5)
				{
					target.teleToLocation(55299, 219120, -3224, true);
				}
				else if (ch == 6)
				{
					target.teleToLocation(56363, 218043, -3224, true);
				}
				else if (ch == 7)
				{
					target.teleToLocation(54245, 220162, -3224, true);
				}
				else if (ch == 8)
				{
					target.teleToLocation(56289, 220126, -3224, true);
				}
				else if (ch == 9)
				{
					target.teleToLocation(55299, 219120, -3496, true);
				}
				else if (ch == 10)
				{
					target.teleToLocation(56363, 218043, -3496, true);
				}
				else if (ch == 11)
				{
					target.teleToLocation(54245, 220162, -3496, true);
				}
				else if (ch == 12)
				{
					target.teleToLocation(56289, 220126, -3496, true);
				}
				else
				{
					target.teleToLocation(53930, 217760, -2952, true);
				}
			}
		}
		catch(Throwable e)
		{
			if(Config.DEBUG)
				e.printStackTrace();
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
