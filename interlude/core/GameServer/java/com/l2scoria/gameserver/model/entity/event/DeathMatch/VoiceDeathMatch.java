package com.l2scoria.gameserver.model.entity.event.DeathMatch;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.Language;

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
				activeChar.sendMessage(Language.LANG_MSG_SUCC_REG + " DeathMatch");
			}
			return true;
		}
		else if (command.equals("dmleave"))
		{
			if (DeathMatch.getInstance().getState() == GameEvent.STATE_ACTIVE && DeathMatch.getInstance().isParticipant(activeChar))
			{
				DeathMatch.getInstance().remove(activeChar);
				activeChar.sendMessage(Language.LANG_MSG_CANC_REG + " DeathMatch");
			}
			else
			{
				activeChar.sendMessage(Language.LANG_MSG_NON_REG + " DeathMatch");
			}
			return true;
		}
		return false;
	}
}
