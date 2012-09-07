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

/**
 *
 * @author FBIagent
 *
 */

package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.datatables.csv.SummonItemsData;
import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.model.L2SummonItem;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.clientpackets.RequestActionUse;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.random.Rnd;

public class SummonItems extends ItemAbst
{
	public SummonItems()
	{
		_items = SummonItemsData.getInstance().itemIDs();

		_playerUseOnly = true;
		_notSitting = true;
		_notWhenSkillsDisabled = true;
		_notInObservationMode = true;
		_notOnOlympiad = true;
	}


	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return false;
		}

		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return false;
		}

		if (activeChar.isCursedWeaponEquiped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return false;
		}

		int npcID = sitem.getNpcId();

		if (npcID == 0)
		{
			return false;
		}

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

		if (npcTemplate == null)
		{
			return false;
		}

		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
				try
				{
					L2Spawn spawn = new L2Spawn(npcTemplate);
					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					L2World.storeObject(spawn.spawnOne());
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
				}
				catch (Exception e)
				{
					return false;
				}

				break;
			case 1: // pet summons
				L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);

				if (petSummon == null)
				{
					break;
				}

				petSummon.setTitle(activeChar.getName());

				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if (!petSummon.isRespawned())
				{
					petSummon.store();
				}

				activeChar.setPet(petSummon);

				activeChar.sendPacket(new MagicSkillUser(activeChar, 2046, 1, 1000, 600000));
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));
				L2World.storeObject(petSummon);
				petSummon.spawnMe(activeChar.getX() + Rnd.get(40) - 20, activeChar.getY() + Rnd.get(40) - 20, activeChar.getZ());
				activeChar.sendPacket(new PetInfo(petSummon));
				petSummon.startFeed(false);
				item.setEnchantLevel(petSummon.getLevel());

				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, petSummon), 900);

				if (petSummon.getCurrentFed() <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed(false);
				}

				break;

			case 2:
				if (activeChar._event != null && !activeChar._event.canDoAction(activeChar, RequestActionUse.ACTION_MOUNT))
				{
					return false;
				}

				if (!activeChar.disarmWeapons())
				{
					return false;
				}

				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				activeChar.sendPacket(mount);
				activeChar.broadcastPacket(mount);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(item.getObjectId());
		}

		return true;
	}

	protected class PetSummonFeedWait implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_activeChar);
				}
				else
				{
					_petSummon.startFeed(false);
				}
			} catch (Throwable e)
			{
			}
		}
	}

	protected class PetSummonFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _petSummon;

		PetSummonFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
				_petSummon.setShowSummonAnimation(false);
			} catch (Throwable e)
			{
			}
		}
	}
}
