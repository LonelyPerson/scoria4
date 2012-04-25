package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.entity.event.CTF;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MyTargetSelected;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.templates.L2NpcTemplate;

public class L2CustomCTFFlagInstance extends L2NpcInstance
{
	public String _mode;
	public int _teamId;
	public CTF _event;

	public L2CustomCTFFlagInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}

		if ((_mode != null) && (_mode.equals("THRONE")))
		{
			player.sendPacket(new ActionFailed());
			return;
		}

		if (this != player.getTarget())
		{
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else if (player._CTFHaveFlagOfTeam > 0 && player._eventTeamId == _teamId)
		{
			_event.onPlayerBringFlag(player);
		}
		else if (player._CTFHaveFlagOfTeam == 0 && player._eventTeamId != _teamId)
		{
			_event.onPlayerTakeFlag(player, _teamId);
		}

		player.sendPacket(new ActionFailed());
	}

	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
	}
}