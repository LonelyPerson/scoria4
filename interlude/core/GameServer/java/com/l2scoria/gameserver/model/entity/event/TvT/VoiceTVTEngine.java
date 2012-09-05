package com.l2scoria.gameserver.model.entity.event.TvT;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;

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
				activeChar.sendMessage("Вы зарегистрированы на эвенте TvT.");
			}
			return true;
		}
		else if (command.equals("tvtleave"))
		{
			if (TvT.getInstance().getState() == GameEvent.STATE_ACTIVE && TvT.getInstance().isParticipant(activeChar))
			{
				TvT.getInstance().remove(activeChar);
				activeChar.sendMessage("Ваше участие на эвенте TvT отменено.");
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