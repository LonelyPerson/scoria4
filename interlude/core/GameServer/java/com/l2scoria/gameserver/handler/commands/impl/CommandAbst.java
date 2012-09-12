package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.handler.commands.IUserCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Akumu
 * @date 0:04/08.09.12
 */
public abstract class CommandAbst implements IUserCommandHandler
{
	protected int[] _commands;
	protected boolean _isInParty;
	protected boolean _isInClan;

	@Override
	public int[] getUserCommandList()
	{
		return _commands;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return false;
		}

		if(_isInParty && activeChar.getParty() == null)
		{
			activeChar.sendMessage("You are not in a party.");
			return false;
		}

		if(_isInClan && activeChar.getClan() == null)
		{
			activeChar.sendMessage("You are not in a clan.");
			return false;
		}

		return true;
	}
}
