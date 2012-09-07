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
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;

public class Crystals extends ItemAbst
{
	public Crystals()
	{
		_items = new int[]{7906, 7907, 7908, 7909, 7910, 7911, 7912, 7913, 7914, 7915, 7916, 7917};
		_notWhenSkillsDisabled = true;
		_notOnOlympiad = true;

	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2Skill skill = null;

		switch(item.getItemId())
		{
			case 7906:
				skill = SkillTable.getInstance().getInfo(2248, 1);
				break;
			case 7907:
				skill = SkillTable.getInstance().getInfo(2249, 1);
				break;
			case 7908:
				skill = SkillTable.getInstance().getInfo(2250, 1);
				break;
			case 7909:
				skill = SkillTable.getInstance().getInfo(2251, 1);
				break;
			case 7910:
				skill = SkillTable.getInstance().getInfo(2252, 1);
				break;
			case 7911:
				skill = SkillTable.getInstance().getInfo(2253, 1);
				break;
			case 7912:
				skill = SkillTable.getInstance().getInfo(2254, 1);
				break;
			case 7913:
				skill = SkillTable.getInstance().getInfo(2255, 1);
				break;
			case 7914:
				skill = SkillTable.getInstance().getInfo(2256, 1);
				break;
			case 7915:
				skill = SkillTable.getInstance().getInfo(2257, 1);
				break;
			case 7916:
				skill = SkillTable.getInstance().getInfo(2258, 1);
				break;
			case 7917:
				skill = SkillTable.getInstance().getInfo(2259, 1);
				break;
			default:
		}

		if(skill != null)
		{
			playable.getPlayer().doCast(skill);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}

		return true;
	}
}