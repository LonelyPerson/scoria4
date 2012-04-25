/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.handler.voicedcommandhandlers;

import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.managers.AwayManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;


/**
 * @author Michiru
 */
public class Away implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
			"away", "back"
	};

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(String, com.l2scoria.gameserver.model.L2PcInstance), String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String text)
	{
		if(command.startsWith("away"))
			return away(activeChar, text);
		else if(command.startsWith("back"))
			return back(activeChar);
		return false;
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */

	private boolean away(L2PcInstance activeChar, String text)
	{
		if(!AwayManager.getInstance().check(activeChar, false))
			return false;

		if(text == null)
		{
			text = "";
		}

		//check away text have not more then 10 letter
		if(text.length() > 10)
		{
			activeChar.sendMessage("You can't set your status Away with more then 10 letters.");
			return false;
		}

		AwayManager.getInstance().setAway(activeChar, text);

		return true;
	}

	private boolean back(L2PcInstance activeChar)
	{
		if(!activeChar.isAway())
		{
			activeChar.sendMessage("You are not Away!");
			return false;
		}
		AwayManager.getInstance().setBack(activeChar);
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
