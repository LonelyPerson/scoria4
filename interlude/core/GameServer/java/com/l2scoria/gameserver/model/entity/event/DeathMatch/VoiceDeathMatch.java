package com.l2scoria.gameserver.model.entity.event.DeathMatch;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;

/**
 * @author m095
 * @version 1.0
 */

public class VoiceDeathMatch implements IVoicedCommandHandler
{
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]{"dmjoin", "dmleave"};
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}

		if (command.equals("dmjoin"))
		{
			if (DeathMatch.getInstance().register(activeChar))
			{
				activeChar.sendMessage("Вы зарегистрированы на эвенте DeathMatch.");
			}
			return true;
		}
		else if (command.equals("dmleave"))
		{
			if (DeathMatch.getInstance().getState() == GameEvent.STATE_ACTIVE && DeathMatch.getInstance().isParticipant(activeChar))
			{
				DeathMatch.getInstance().remove(activeChar);
				activeChar.sendMessage("Ваше участие на эвенте DeathMatch отменено.");
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
