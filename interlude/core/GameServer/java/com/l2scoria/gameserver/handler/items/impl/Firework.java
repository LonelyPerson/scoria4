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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.FloodProtector;

/**
 * This class ...
 * 
 * @version $Revision: 1.0.0.0.0.0 $ $Date: 2005/09/02 19:41:13 $
 */

public class Firework extends ItemAbst
{
	public Firework()
	{
		_items = new int[]{6403, 6406, 6407};

		_playerUseOnly = true;
		_notCasting = true;
		_notOnOlympiad = true;
		_notSitting = true;
		_notWhenSkillsDisabled = true;
		_notInObservationMode = true;
	}

	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		int itemId = item.getItemId();

		if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_FIREWORK))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
			return false;
		}

		switch (itemId)
		{
			case 6403: // elven_firecracker, xml: 2023
			{
				activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2023, 1, 1, 0));
				useFw(activeChar, 2023, 1);
				break;
			}

			case 6406: // firework, xml: 2024
			{
				activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2024, 1, 1, 0));
				useFw(activeChar, 2024, 1);
				break;
			}

			case 6407: // large_firework, xml: 2025
			{
				activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2025, 1, 1, 0));
				useFw(activeChar, 2025, 1);
				break;
			}
		}

		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);

		return true;
	}

	private void useFw(L2PcInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if(skill != null)
		{
			activeChar.useMagic(skill, false, false);
		}
	}
}
