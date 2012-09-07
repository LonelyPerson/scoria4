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
import com.l2scoria.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.InventoryUpdate;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.random.Rnd;

/**
 * @author l3x
 */
public class Harvest extends SkillAbst
{
	public Harvest()
	{
		_types = new SkillType[]{SkillType.HARVEST};

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

		L2Object[] targetList = skill.getTargetList(activeChar);

		if (targetList == null)
		{
			return false;
		}

		L2MonsterInstance monster;
		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

		for (L2Object aTargetList : targetList)
		{
			if (!(aTargetList.isMonster))
			{
				continue;
			}

			monster = (L2MonsterInstance) aTargetList;

			if (player != monster.getSeeder())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				player.sendPacket(sm);
				continue;
			}

			boolean send = false;
			int total = 0;
			int cropId = 0;

			// TODO: check items and amount of items player harvest
			if (monster.isSeeded())
			{
				if (calcSuccess(player, monster))
				{
					L2Attackable.RewardItem[] items = monster.takeHarvest();
					if (items != null && items.length > 0)
					{
						for (L2Attackable.RewardItem ritem : items)
						{
							cropId = ritem.getItemId(); // always got 1 type of crop as reward
							if (player.isInParty())
							{
								player.getParty().distributeItem(player, ritem, true, monster);
							}
							else
							{
								L2ItemInstance item = player.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), player, monster);
								if (iu != null)
								{
									iu.addItem(item);
								}

								send = true;
								total += ritem.getCount();
							}
						}
						if (send)
						{
							SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
							smsg.addNumber(total);
							smsg.addItemName(cropId);
							player.sendPacket(smsg);

							if (player.getParty() != null)
							{
								smsg = new SystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S);
								smsg.addString(player.getName());
								smsg.addNumber(total);
								smsg.addItemName(cropId);
								player.getParty().broadcastToPartyMembers(player, smsg);
							}

							if (iu != null)
							{
								player.sendPacket(iu);
							}
							else
							{
								player.sendPacket(new ItemList(player, false));
							}
						}
					}
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.THE_HARVEST_HAS_FAILED));
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN));
			}
		}

		return true;
	}

	private boolean calcSuccess(L2PcInstance player, L2MonsterInstance monster)
	{
		int basicSuccess = 100;
		int levelPlayer = player.getLevel();
		int levelTarget = monster.getLevel();

		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}

		// apply penalty, target <=> player levels
		// 5% penalty for each level
		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}

		// success rate cant be less than 1%
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}

		return Rnd.nextInt(99) < basicSuccess;
	}
}
