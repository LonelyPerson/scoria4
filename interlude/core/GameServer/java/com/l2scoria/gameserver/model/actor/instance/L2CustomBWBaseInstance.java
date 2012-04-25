package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.entity.event.BW;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MyTargetSelected;
import com.l2scoria.gameserver.network.serverpackets.StatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.templates.L2NpcTemplate;

public class L2CustomBWBaseInstance extends L2NpcInstance
{
	public int _teamId;
	public BW _event;

	public L2CustomBWBaseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public boolean canAttack(L2PcInstance player)
	{
		return (player._eventTeamId != 0) && (player._eventTeamId != _teamId);
	}

	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) 
		{
			return;
		}

		if (this != player.getTarget())
		{
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(9, (int)getCurrentHp());
			su.addAttribute(10, getMaxHp());
			player.sendPacket(su);

			player.sendPacket(new ValidateLocation(this));
		}
		else if ((!isAlikeDead()) && (Math.abs(player.getZ() - getZ()) < 400) && (canAttack(player)))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		player.sendPacket(new ActionFailed());
	}

	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		L2PcInstance player = null;
		if ((attacker instanceof L2PcInstance))
			player = (L2PcInstance)attacker;
		else if ((attacker instanceof L2Summon))
			player = ((L2Summon)attacker).getOwner();
		else
		{
			return;
		}
		if (!canAttack(player))
		{
			return;
		}
		player.updatePvPStatus();

		if (damage < getStatus().getCurrentHp())
			getStatus().setCurrentHp(getStatus().getCurrentHp() - damage);
		else
			doDie(attacker);
	}

	public boolean isAttackable()
	{
		return true;
	}

	public boolean isAutoAttackable(L2Character attacker)
	{
		return true;
	}

	public boolean doDie(L2Character killer)
	{
		L2PcInstance player = null;
		if ((killer instanceof L2PcInstance))
			player = (L2PcInstance)killer;
		else if ((killer instanceof L2Summon))
			player = ((L2Summon)killer).getOwner();
		else
		{
			return false;
		}
		if (!super.doDie(killer))
		{
			return false;
		}
		_event.onPlayerKillBase(player, _teamId);
		return true;
	}
}