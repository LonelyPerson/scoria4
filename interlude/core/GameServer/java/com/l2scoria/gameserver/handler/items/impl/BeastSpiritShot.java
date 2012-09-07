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

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2BabyPetInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.util.Broadcast;

/**
 * Beast SpiritShot Handler
 * 
 * @author programmos, l2scoria dev
 */
public class BeastSpiritShot extends ItemAbst
{
	public BeastSpiritShot()
	{
		_items = new int[]{6646, 6647};
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeOwner = playable.getPlayer();

		if(activeOwner == null)
			return false;

		L2Summon activePet = activeOwner.getPet();
		if(activePet == null)
		{
			activeOwner.sendPacket(new SystemMessage(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
			return false;
		}

		if(activePet.isDead())
		{
			activeOwner.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET));
			return false;
		}

		int itemId = item.getItemId();
		boolean isBlessed = itemId == 6647;
		int shotConsumption = 1;

		L2ItemInstance weaponInst;
		L2Weapon weaponItem;

		if(activePet.isPet && !(activePet instanceof L2BabyPetInstance))
		{
			weaponInst = activePet.getActiveWeaponInstance();
			weaponItem = activePet.getActiveWeaponItem();

			if(weaponInst == null)
			{
				activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
				return false;
			}

			if(weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
				// SpiritShots are already active.
				return false;

			int shotCount = item.getCount();
			shotConsumption = weaponItem.getSpiritShotCount();

			if(shotConsumption == 0)
			{
				activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
				return false;
			}

			if(!(shotCount > shotConsumption))
			{
				// Not enough SpiritShots to use.
				activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET));
				return false;
			}

			if(isBlessed)
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
			}
		}
		else
		{
			if(activePet.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
				return false;

			if(isBlessed)
			{
				activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
			}
		}

		// TODO: test ss
		if(!Config.DONT_DESTROY_SS)
		{
			if(!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
			{
				if(activeOwner.getAutoSoulShot().containsKey(itemId))
				{
					activeOwner.removeAutoSoulShot(itemId);
					activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));
					SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItem().getName());
					activeOwner.sendPacket(sm);

					return false;
				}

				activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
				return false;
			}
		}

		// Pet uses the power of spirit.
		activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, isBlessed ? 2009 : 2008, 1, 0, 0), 360000/*600*/);

		return true;
	}
}
