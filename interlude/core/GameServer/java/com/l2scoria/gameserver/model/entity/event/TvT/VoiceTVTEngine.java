package com.l2scoria.gameserver.model.entity.event.TvT;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.Language;

public class VoiceTVTEngine implements IVoicedCommandHandler
{
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]{"tvtjoin", "tvtleave"};
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}

		if (command.equals("tvtjoin"))
		{
			if (TvT.getInstance().register(activeChar))
			{
				activeChar.sendMessage(Language.LANG_MSG_SUCC_REG + " TvT");
			}
			return true;
		}
		else if (command.equals("tvtleave"))
		{
			if (TvT.getInstance().getState() == GameEvent.STATE_ACTIVE && TvT.getInstance().isParticipant(activeChar))
			{
				TvT.getInstance().remove(activeChar);
				activeChar.sendMessage(Language.LANG_MSG_CANC_REG + " TvT");
			}
			else
			{
				activeChar.sendMessage(Language.LANG_MSG_NON_REG + " TvT");
			}
			return true;
		}
		return false;
	}
}