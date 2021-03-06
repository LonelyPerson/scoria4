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

import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.UserInfo;

/**
 * Itemhhandler for Character Appearance Change Potions
 * 
 * @author Tempy
 */
public class CharChangePotions extends ItemAbst
{
	public CharChangePotions()
	{
		_items = new int[]
				{
				5235, 5236, 5237, // Face
				5238, 5239, 5240, 5241, // Hair Color
				5242, 5243, 5244, 5245, 5246, 5247, 5248 // Hair Style
				};
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		int itemId = item.getItemId();

		L2PcInstance activeChar = playable.getPlayer();
		if(activeChar == null)
		{
			return false;
		}

		if(activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		switch(itemId)
		{
			case 5235:
				activeChar.getAppearance().setFace(0);
				break;
			case 5236:
				activeChar.getAppearance().setFace(1);
				break;
			case 5237:
				activeChar.getAppearance().setFace(2);
				break;
			case 5238:
				activeChar.getAppearance().setHairColor(0);
				break;
			case 5239:
				activeChar.getAppearance().setHairColor(1);
				break;
			case 5240:
				activeChar.getAppearance().setHairColor(2);
				break;
			case 5241:
				activeChar.getAppearance().setHairColor(3);
				break;
			case 5242:
				activeChar.getAppearance().setHairStyle(0);
				break;
			case 5243:
				activeChar.getAppearance().setHairStyle(1);
				break;
			case 5244:
				activeChar.getAppearance().setHairStyle(2);
				break;
			case 5245:
				activeChar.getAppearance().setHairStyle(3);
				break;
			case 5246:
				activeChar.getAppearance().setHairStyle(4);
				break;
			case 5247:
				activeChar.getAppearance().setHairStyle(5);
				break;
			case 5248:
				activeChar.getAppearance().setHairStyle(6);
				break;
		}

		// Create a summon effect!
		activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2003, 1, 1, 0));

		// Update the changed stat for the character in the DB.
		activeChar.store();

		// Remove the item from inventory.
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

		// Broadcast the changes to the char and all those nearby.
		activeChar.broadcastPacket(new UserInfo(activeChar));
		return true;
	}
}
