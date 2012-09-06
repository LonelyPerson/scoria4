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

package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;
import javolution.util.FastList;

import java.util.StringTokenizer;

public class L2CustomZone extends L2ZoneDefault
{

	public L2CustomZone(int id)
	{
		super(id);
		_IsFlyingEnable = true;
	}

	@Override
	protected void onDieInside(L2Character l2character)
	{}

	@Override
	protected void onReviveInside(L2Character l2character)
	{}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("name"))
		{
			_zoneName = value;
		}
		else if(name.equals("flying"))
		{
			_IsFlyingEnable = Boolean.parseBoolean(value);
		}
		else if(name.equals("removeSkill"))
		{
			_skills = new FastList<L2Skill>();
			StringTokenizer st = new StringTokenizer(value, ";");
			while (st.hasMoreTokens())
			{
				int skillId = Integer.parseInt(st.nextToken());
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
				if (skill != null)
					_skills.add(skill);
			}
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) character;
			if(!player.isGM() && player.isFlying() && !player.isInJail() && !_IsFlyingEnable)
			{
				player.teleToLocation(com.l2scoria.gameserver.datatables.csv.MapRegionTable.TeleportWhereType.Town);
			}

			if(_zoneName.equalsIgnoreCase("tradeoff"))
			{
				player.sendMessage("Trade restrictions are involved.");
				player.setTradeDisabled(true);
			}

			if (_skills != null)
			{
				for(L2Skill sk : _skills)
					player.stopSkillEffects(sk.getId());
			}
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) character;

			if(_zoneName.equalsIgnoreCase("tradeoff"))
			{
				player.sendMessage("Trade restrictions removed.");
				player.setTradeDisabled(false);
			}
		}

		super.onExit(character);
	}

	public String getZoneName()
	{
		return _zoneName;
	}

	public boolean isFlyingEnable()
	{
		return _IsFlyingEnable;
	}

	private String _zoneName;
	private boolean _IsFlyingEnable;
	private FastList<L2Skill> _skills;
}
