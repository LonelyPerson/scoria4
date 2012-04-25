package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.zone.L2ZoneType;

/**
 *
 * @author zenn
 */
public class L2HQZone extends L2ZoneType
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
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_HQ, false);
		}
	}

	@Override
	protected void onDieInside(L2Character character)
	{}

	@Override
	protected void onReviveInside(L2Character character)
	{}

}