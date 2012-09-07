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
package com.l2scoria.gameserver.handler;

import com.l2scoria.gameserver.GameServer;
import com.l2scoria.gameserver.handler.skills.impl.*;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.4 $ $Date: 2005/04/03 15:55:06 $
 */
public class SkillHandler
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private static SkillHandler _instance;

	private Map<L2Skill.SkillType, SkillAbst> _datatable;

	public static SkillHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new SkillHandler();
		}

		return _instance;
	}

	private SkillHandler()
	{
		_datatable = new TreeMap<SkillType, SkillAbst>();
		register(new Blow());
		register(new Pdam());
		register(new Mdam());
		register(new CpDam());
		register(new Manadam());
		register(new Heal());
		register(new CombatPointHeal());
		register(new ManaHeal());
		register(new BalanceLife());
		register(new Charge());
		register(new ClanGate());
		register(new Continuous());
		register(new Resurrect());
		register(new Spoil());
		register(new Sweep());
		register(new StrSiegeAssault());
		register(new SummonFriend());
		register(new SummonTreasureKey());
		register(new Disablers());
		register(new Recall());
		register(new SiegeFlag());
		register(new TakeCastle());
		register(new Unlock());
		register(new DrainSoul());
		register(new Craft());
		register(new Fishing());
		register(new FishingSkill());
		register(new BeastFeed());
		register(new DeluxeKey());
		register(new Sow());
		register(new Harvest());
		register(new GetPlayer());
		register(new ZakenPlayer());
		register(new ZakenSelf());
		_log.info("SkillHandler: Loaded " + _datatable.size() + " handlers.");

	}

	public void register(SkillAbst handler)
	{
		SkillType[] types = handler.getSkillIds();

		if(types == null || types.length < 1)
		{
			return;
		}

		for(SkillType t : types)
		{
			_datatable.put(t, handler);
		}
	}

	public SkillAbst getSkillHandler(SkillType skillType)
	{
		return _datatable.get(skillType);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}
