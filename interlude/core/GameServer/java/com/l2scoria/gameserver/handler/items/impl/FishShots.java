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
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Item;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots extends ItemAbst
{
	private static final int[] SKILL_IDS = {2181, 2182, 2183, 2184, 2185, 2186};

	public FishShots()
	{
		_items = new int[]{6535, 6536, 6537, 6538, 6539, 6540};

		_playerUseOnly = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();

		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.ROD)
		{
			return false;
		}

		if (weaponInst.getChargedFishshot()) // spiritshot is already active
		{
			return false;
		}

		int fishShotId = item.getItemId();
		int grade = weaponItem.getCrystalType();
		int count = item.getCount();

		if (grade == L2Item.CRYSTAL_NONE && fishShotId != 6535 || grade == L2Item.CRYSTAL_D && fishShotId != 6536 || grade == L2Item.CRYSTAL_C && fishShotId != 6537 || grade == L2Item.CRYSTAL_B && fishShotId != 6538 || grade == L2Item.CRYSTAL_A && fishShotId != 6539 || grade == L2Item.CRYSTAL_S && fishShotId != 6540)
		{
			//1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(new SystemMessage(SystemMessageId.WRONG_FISHINGSHOT_GRADE));
			return false;
		}

		if (count < 1)
		{
			return false;
		}

		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUser(activeChar, activeChar, SKILL_IDS[grade], 1, 0, 0));

		return true;
	}
}
