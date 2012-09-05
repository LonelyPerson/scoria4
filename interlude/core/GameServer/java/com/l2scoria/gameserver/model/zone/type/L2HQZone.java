package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;

/**
 *
 * @author zenn
 */
public class L2HQZone extends L2ZoneDefault
{
	private int _castleId;
	private Castle _castle;
	
	public L2HQZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("castleId"))
		{
			_castleId = Integer.parseInt(value);
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			if(_castle.getSiege().getIsInProgress())
			{
				character.setInsideZone(L2Character.ZONE_HQ, true);
			}
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_HQ, false);
		}

		super.onExit(character);
	}
}