package com.l2scoria.gameserver.model.entity.event.LastHero;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.Language;

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
				activeChar.sendMessage(Language.LANG_MSG_SUCC_REG + " LastHero");
			}

			return true;
		}
		else if (command.equals("lhleave"))
		{
			if (LastHero.getInstance().getState() == GameEvent.STATE_ACTIVE && LastHero.getInstance().isParticipant(activeChar))
			{
				LastHero.getInstance().remove(activeChar);
				activeChar.sendMessage(Language.LANG_MSG_CANC_REG + " LastHero");
			}
			else
			{
				activeChar.sendMessage(Language.LANG_MSG_NON_REG + " LastHero");
			}

			return true;
		}

		return false;
	}
}