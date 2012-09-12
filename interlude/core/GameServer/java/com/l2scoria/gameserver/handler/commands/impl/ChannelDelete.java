/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.model.L2CommandChannel;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelDelete extends CommandAbst
{
	public ChannelDelete()
	{
		_commands = new int[]{93};
		_isInParty = true;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		if(activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
		{
			L2CommandChannel channel = activeChar.getParty().getCommandChannel();
			channel.broadcastToChannelMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			channel.disbandChannel();
			return true;
		}

		return false;
	}
}
