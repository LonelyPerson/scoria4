package com.l2scoria.gameserver.handler.skills.impl;

import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

public class ClanGate extends SkillAbst
{
	public ClanGate()
	{
		_types = new SkillType[]{SkillType.CLAN_GATE};

		_playerUseOnly = true;
		_notInGrandBossZone = true;
		_notInObservationMode = true;
		_forbiddenZones = new int[]{L2Character.ZONE_NOLANDING, L2Character.ZONE_PVP};
	}

	@Override
	public boolean useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!super.useSkill(activeChar, skill, targets))
		{
			return false;
		}

		L2PcInstance player = activeChar.getPlayer();

		L2Clan clan = player.getClan();
		if (clan != null)
		{
			if (CastleManager.getInstance().getCastleByOwner(clan) != null)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (player.isCastleLord(castle.getCastleId()))
				{
					//please note clan gate expires in two minutes WHATEVER happens to the clan leader.
					ThreadPoolManager.getInstance().scheduleGeneral(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
					castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
					player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_PORTAL_HAS_BEEN_CREATED));
					player.setIsParalyzed(true);
				}
			}
		}

		L2Effect effect = player.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(player);

		return true;
	}

	private class RemoveClanGate implements Runnable
	{
		private final int castle;
		private final L2PcInstance player;

		private RemoveClanGate(int castle, L2PcInstance player)
		{
			this.castle = castle;
			this.player = player;
		}

		@Override
		public void run()
		{
			if (player != null)
			{
				player.setIsParalyzed(false);
			}

			CastleManager.getInstance().getCastleById(castle).destroyClanGate();
		}
	}
}
