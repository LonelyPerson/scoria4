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
package com.l2scoria.gameserver.handler.skillhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.handler.ISkillHandler;
import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ConfirmDlg;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;
import javolution.util.FastList;

/**
 * @authors L2Scoria
 */
public class SummonFriend implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(SummonFriend.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SUMMON_FRIEND};

	public static boolean checkSummonerStatus(L2PcInstance summonerChar)
	{
		if (summonerChar == null)
		{
			return false;
		}

		if (summonerChar.isFlying())
		{
			return false;
		}

		if (summonerChar.inObserverMode())
		{
			return false;
		}

		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}

		if (summonerChar.isInsideZone(L2Character.ZONE_PVP))
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return false;
		}

		if (summonerChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
			return false;
		}

		// check for summoner not in raid areas
		FastList<L2Object> objects = L2World.getInstance().getVisibleObjects(summonerChar, Config.BOSS_LIMIT_RADIUS);
		if (objects != null)
		{
			for (L2Object object : objects)
			{
				if (object.isRaid || object instanceof L2GrandBossInstance)
				{
					summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
					return false;
				}
			}
		}

		if (summonerChar.getInstanceId() > 0)
		{
			Instance summonerInstance = InstanceManager.getInstance().getInstance(summonerChar.getInstanceId());
			if (summonerInstance != null && (!summonerInstance.isSummonAllowed()))
			{
				summonerChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}

		objects = null;
		return true;
	}

	public static boolean checkTargetStatus(L2PcInstance targetChar, L2PcInstance summonerChar)
	{
		if (targetChar == null)
		{
			return false;
		}

		if (targetChar.isAlikeDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			sm = null;
			return false;
		}

		if (targetChar.isInStoreMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			sm = null;
			return false;
		}

		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			sm = null;
			return false;
		}

		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
			return false;
		}

		if (targetChar.inObserverMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		if (targetChar.isFestivalParticipant())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		if (targetChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) || targetChar.isInsideZone(L2Character.ZONE_PVP))
		{
			summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}

		if ((summonerChar._event != null && summonerChar._event.isRunning()) || (targetChar._event != null && targetChar._event.isRunning()))
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		return true;
	}

	public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
		{
			return;
		}

		if (!checkSummonerStatus(summonerChar))
		{
			return;
		}
		if (!checkTargetStatus(targetChar, summonerChar))
		{
			return;
		}

		if (summonSkill.getId() != 1429)
		{
			if (targetChar.getInventory().getItemByItemId(8615) == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(8615);
				targetChar.sendPacket(sm);
				sm = null;
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", 8615, 1, summonerChar, targetChar);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addItemName(8615);
			targetChar.sendPacket(sm);
			sm = null;
		}

		targetChar.setInstanceId(summonerChar.getInstanceId());
		int x = summonerChar.getX();
		int y = summonerChar.getY();
		int n = 0;

		while(true)
		{
			x += Rnd.get(summonerChar.getTemplate().getCollisionRadius() * -2, summonerChar.getTemplate().getCollisionRadius() * 2);
			y += Rnd.get(summonerChar.getTemplate().getCollisionRadius() * -2, summonerChar.getTemplate().getCollisionRadius() * 2);

			if (++n > 5 || GeoEngine.canMoveToCoord(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), x, y, summonerChar.getZ(), summonerChar.getInstanceId()))
			{
				break;
			}
		}

		targetChar.teleToLocation(x, y, summonerChar.getZ(), false);
	}

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar.isPlayer)) // currently not implemented for others
		{
			return;
		}

		L2PcInstance activePlayer = (L2PcInstance) activeChar;

		if (!checkSummonerStatus(activePlayer))
		{
			return;
		}

		for (L2Object element : targets)
		{
			if (!(element.isPlayer))
			{
				continue;
			}

			L2PcInstance target = (L2PcInstance) element;
			if (activeChar == target)
			{
				continue;
			}

			if (!checkTargetStatus(target, activePlayer))
			{
				continue;
			}

			if (!Util.checkIfInRange(0, activeChar, target, false))
			{
				if (!target.teleportRequest(activePlayer, skill))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_SUMMONED);
					sm.addString(target.getName());
					activePlayer.sendPacket(sm);
					sm = null;
					continue;
				}

				if (skill.getId() == 1403) //summon friend
				{
					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
					confirm.addString(activeChar.getName());
					confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
					confirm.addTime(30000);
					confirm.addRequesterId(activePlayer.getCharId());
					target.sendPacket(confirm);
				}
				else
				{
					teleToTarget(target, activePlayer, skill);
					target.teleportRequest(null, null);
				}
				// remove ressurection
				target.removeReviving();

			}

			target = null;
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}