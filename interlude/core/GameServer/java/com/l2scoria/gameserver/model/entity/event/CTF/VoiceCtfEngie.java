package com.l2scoria.gameserver.model.entity.event.CTF;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;

public class VoiceCtfEngie implements IVoicedCommandHandler
{
	@Override
	public String[] getVoicedCommandList()
	{
		return new String[]{"ctfjoin", "ctfleave"};
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}

		if (command.equals("ctfjoin"))
		{
			if (CTF.getInstance().register(activeChar))
			{
				activeChar.sendMessage("Вы зарегистрированы на эвенте CTF.");
			}
			return true;
		}
		else if (command.equals("ctfleave"))
		{
			if (CTF.getInstance().getState() == GameEvent.STATE_ACTIVE && CTF.getInstance().isParticipant(activeChar))
			{
				CTF.getInstance().remove(activeChar);
				activeChar.sendMessage("Ваше участие на эвенте CTF отменено.");
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