package com.l2scoria.gameserver.model.entity.event.CTF;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.Language;

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
				activeChar.sendMessage(Language.LANG_MSG_SUCC_REG + " CTF");
			}
			return true;
		}
		else if (command.equals("ctfleave"))
		{
			if (CTF.getInstance().getState() == GameEvent.STATE_ACTIVE && CTF.getInstance().isParticipant(activeChar))
			{
				CTF.getInstance().remove(activeChar);
				activeChar.sendMessage(Language.LANG_MSG_CANC_REG + " CTF");
			}
			else
			{
				activeChar.sendMessage(Language.LANG_MSG_NON_REG + " CTF");
			}
			return true;
		}
		return false;
	}
}