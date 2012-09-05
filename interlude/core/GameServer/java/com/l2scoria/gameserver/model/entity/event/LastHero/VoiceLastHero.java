package com.l2scoria.gameserver.model.entity.event.LastHero;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;

public class VoiceLastHero implements IVoicedCommandHandler
{
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]{"lhjoin", "lhleave"};
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}

		if (command.equals("lhjoin"))
		{
			if (LastHero.getInstance().register(activeChar))
			{
				activeChar.sendMessage("Вы зарегистрированы на эвенте Last Hero.");
			}

			return true;
		}
		else if (command.equals("lhleave"))
		{
			if (LastHero.getInstance().getState() == GameEvent.STATE_ACTIVE && LastHero.getInstance().isParticipant(activeChar))
			{
				LastHero.getInstance().remove(activeChar);
				activeChar.sendMessage("Ваше участие на эвенте Last Hero отменено.");
			}
			else
			{
				activeChar.sendMessage("Вы не участник эвента.");
			}

			return true;
		}

		return false;
	}
}