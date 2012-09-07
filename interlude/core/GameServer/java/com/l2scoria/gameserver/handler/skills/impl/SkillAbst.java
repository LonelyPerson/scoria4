package com.l2scoria.gameserver.handler.skills.impl;

import com.l2scoria.gameserver.handler.SkillHandler;
import com.l2scoria.gameserver.handler.skills.ISkillHandler;
import com.l2scoria.gameserver.managers.GrandBossManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.GArray;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author Akumu
 * @date 15:05/07.09.12
 */
public abstract class SkillAbst implements ISkillHandler
{
	protected static Logger _log = Logger.getLogger(SkillAbst.class);
	protected SkillType[] _types = null;

	protected boolean _playerUseOnly;
	protected boolean _notAlikeDead;
	protected boolean _notOnOlympiad;
	protected boolean _notInObservationMode;
	protected boolean _notInGrandBossZone;
	protected int[] _forbiddenZones;

	// target related
	protected enum TargetReq
	{
		PLAYER, NOT_DEAD, NOT_FAKE_DEATH
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(_playerUseOnly)
		{
			if(!activeChar.isPlayer)
			{
				return false;
			}

			L2PcInstance player = activeChar.getPlayer();
			if(_notOnOlympiad && player.isInOlympiadMode())
			{
				player.sendMessage("You can not use this spell here.");
				return false;
			}

			// в режиме обзора
			if(_notInObservationMode && player.inObserverMode())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if(_forbiddenZones != null)
			{
				for(int zone : _forbiddenZones)
				{
					if(player.isInsideZone(zone))
					{
						player.sendMessage("You can not use this spell here.");
						return false;
					}
				}
			}

			if(_notInGrandBossZone && GrandBossManager.getInstance().getZone(player) != null)
			{
				player.sendMessage("You can not use this spell in a Grand Boss zone.");
				return false;
			}
		}

		if(_notAlikeDead && activeChar.isAlikeDead())
		{
			return false;
		}

		return true;
	}

	protected void callSkillHandler(SkillType stype, L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		SkillAbst handler = SkillHandler.getInstance().getSkillHandler(stype);
		if (handler != null)
		{
			handler.useSkill(activeChar, skill, targets);
		}
	}

	protected GArray<L2Object> getTargets(L2Character activeChar, L2Object[] targets, TargetReq... tr)
	{
		GArray<L2Object> result = new GArray<L2Object>();
		result.addAll(Arrays.asList(targets));

		if(tr.length < 1)
		{
			return result;
		}

		loop1: for(L2Object obj : targets)
		{
			if(obj == null)
			{
				continue;
			}

			for(TargetReq t : tr)
			{
				switch (t)
				{
					case PLAYER:
						if(!obj.isPlayer)
						{
							result.remove(obj);
							continue loop1;
						}
						break;

					case NOT_DEAD:
						if(!obj.isCharacter || ((L2Character)obj).isDead())
						{
							result.remove(obj);
							continue loop1;
						}
						break;

					case NOT_FAKE_DEATH:
						if(!obj.isCharacter || ((L2Character)obj).isDead())
						{
							result.remove(obj);
							continue loop1;
						}
						break;
				}
			}
		}

		return result;
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return _types;
	}
}
