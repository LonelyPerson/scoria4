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

import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/06 16:13:51 $
 */

public class Remedy extends ItemAbst
{
	public Remedy()
	{
		_items = new int[]{1831, 1832, 1833, 1834, 3889};

		_requiresActingPlayer = true;
		_notOnOlympiad = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar = playable.getPlayer();

		int itemId = item.getItemId();
		if(itemId == 1831) // antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for(L2Effect e : effects)
			{
				if(e.getSkill().getSkillType() == L2Skill.SkillType.POISON && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}

			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2042, 1, 0, 0));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if(itemId == 1832) // advanced antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for(L2Effect e : effects)
			{
				if(e.getSkill().getSkillType() == L2Skill.SkillType.POISON && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}

			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2043, 1, 0, 0));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if(itemId == 1833) // bandage
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for(L2Effect e : effects)
			{
				if(e.getSkill().getSkillType() == L2Skill.SkillType.BLEED && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}

			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 34, 1, 0, 0));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if(itemId == 1834) // emergency dressing
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for(L2Effect e : effects)
			{
				if(e.getSkill().getSkillType() == L2Skill.SkillType.BLEED && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}

			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2045, 1, 0, 0));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if(itemId == 3889) // potion of recovery
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for(L2Effect e : effects)
			{
				if(e.getSkill().getId() == 4082)
				{
					e.exit();
				}
			}

			activeChar.setIsImobilised(false);

			if(activeChar.getFirstEffect(L2Effect.EffectType.ROOT) == null)
			{
				activeChar.stopRooting(null);
			}

			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2042, 1, 0, 0));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}

		return true;
	}
}
