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
package com.l2scoria.gameserver.handler.itemhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.handler.IItemHandler;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

import java.util.Iterator;

public class CustomPotions implements IItemHandler
{
    /* @TODELETE
	private static final int[] ITEM_IDS =
	{
			9720, 9721, 9722, 9723, 9724, 9725, 9726, 9727, 9728, 9729, 9730, 9731,
	};
     *
     */
    // eto pzdc, no s abstrakciei ne razobralsa

	public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!Config.ENABLE_POTION_SKILL_ATTACH)
		{
			return;
		}

		L2PcInstance activeChar;
		boolean res = false;

		if(playable.isPlayer)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if(playable.isPet)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
			return;

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if(activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int itemId = item.getItemId();

                if(Config.POTION_SKILL_ATTACH.containsKey(itemId) && Config.ENABLE_POTION_SKILL_ATTACH) {
                      res = usePotion(activeChar, Config.POTION_SKILL_ATTACH.get(itemId), 1);
                }

		activeChar = null;

		if(res)
		{
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}

	public boolean usePotion(L2PcInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if(skill != null)
		{
			activeChar.doCast(skill);
			if(!((activeChar.isSitting() || activeChar.isParalyzed() || activeChar.isAway() || activeChar.isFakeDeath()) && !skill.isPotion()))
				return true;
		}
		return false;
	}

	public int[] getItemIds()
	{

            if(!Config.ENABLE_POTION_SKILL_ATTACH) {
                int [] noItems = {};
                return noItems;
            }
            int itemIds[] = new int[Config.POTION_SKILL_ATTACH.size()];
            Iterator<Integer> it = Config.POTION_SKILL_ATTACH.keySet().iterator();
            for(int i = 0;it.hasNext();i++) {
                itemIds[i] = it.next();
            }
		return itemIds;
	}
}
