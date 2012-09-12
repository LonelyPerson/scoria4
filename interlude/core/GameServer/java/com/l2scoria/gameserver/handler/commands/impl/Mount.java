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

package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.Inventory;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.clientpackets.RequestActionUse;
import com.l2scoria.gameserver.network.serverpackets.Ride;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.Broadcast;

/**
 * Support for /mount command.
 *
 * @author Tempy
 */
public class Mount extends CommandAbst
{
	public Mount()
	{
		_commands = new int[]{61};
	}

	@Override
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		L2Summon pet = activeChar.getPet();

		if (pet != null && pet.isMountable() && !activeChar.isMounted())
		{
			if (activeChar.isDead())
			{
				// A strider cannot be ridden when player is dead.
				activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD));
			}
			else if (pet.isDead())
			{
				// A dead strider cannot be ridden.
				activeChar.sendPacket(new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN));
			}
			else if (pet.isInCombat())
			{
				// A strider in battle cannot be ridden.
				activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN));
			}
			else if (activeChar.isInCombat())
			{
				// A pet cannot be ridden while player is in battle.
				activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			}
			else if (activeChar.isSitting() || activeChar.isMoving())
			{
				// A strider can be ridden only when player is standing.
				activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING));
			}
			else if (!pet.isDead() && !activeChar.isMounted())
			{
				if (activeChar._event != null && !activeChar._event.canDoAction(activeChar, RequestActionUse.ACTION_MOUNT))
				{
					return false;
				}

				if (!activeChar.disarmWeapons())
				{
					return false;
				}

				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000/*900*/);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(pet.getControlItemId());
				pet.unSummon(activeChar);

				if (activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null || activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND) != null)
				{
					if (activeChar.setMountType(0))
					{
						if (activeChar.isFlying())
						{
							activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
						}

						Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
						Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
						activeChar.setMountObjectID(0);
					}
				}

				return true;
			}
		}
		else if (activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if (activeChar.isMounted())
		{
			// Dismount
			if (activeChar.setMountType(0))
			{
				if (activeChar.isFlying())
				{
					activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				}

				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
				activeChar.setMountObjectID(0);

				return true;
			}
		}

		return false;
	}
}
