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
package com.l2scoria.gameserver.handler.skills.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Attackable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.InventoryUpdate;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author _drunk_
 */
public class Sweep extends SkillAbst
{
	public Sweep()
	{
		_types = new SkillType[]{SkillType.SWEEP};

		_playerUseOnly = true;
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2PcInstance player = activeChar.getPlayer();
		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		boolean send = false;

		for (L2Object target1 : targets)
		{
			if (!(target1.isAttackable))
			{
				continue;
			}

			L2Attackable target = (L2Attackable) target1;
			L2Attackable.RewardItem[] items = null;
			boolean isSweeping = false;

			synchronized (target)
			{
				if (target.isSweepActive())
				{
					items = target.takeSweep();
					isSweeping = true;
				}
			}

			if (isSweeping)
			{
				if (items == null || items.length == 0)
				{
					continue;
				}

				for (L2Attackable.RewardItem ritem : items)
				{
					if (player.isInParty())
					{
						player.getParty().distributeItem(player, ritem, true, target);
					}
					else
					{
						L2ItemInstance item = player.getInventory().addItem("Sweep", ritem.getItemId(), ritem.getCount(), player, target);
						if (iu != null)
						{
							iu.addItem(item);
						}
						send = true;
						item = null;

						SystemMessage smsg;

						if (ritem.getCount() > 1)
						{
							smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S); // earned $s2$s1
							smsg.addItemName(ritem.getItemId());
							smsg.addNumber(ritem.getCount());
						}
						else
						{
							smsg = new SystemMessage(SystemMessageId.EARNED_ITEM); // earned $s1
							smsg.addItemName(ritem.getItemId());
						}

						player.sendPacket(smsg);
					}
				}
			}
			target.endDecayTask();

			if (send)
			{
				if (iu != null)
				{
					player.sendPacket(iu);
				}
				else
				{
					player.sendPacket(new ItemList(player, false));
				}
			}

			player = null;
			iu = null;
		}

		return true;
	}
}
