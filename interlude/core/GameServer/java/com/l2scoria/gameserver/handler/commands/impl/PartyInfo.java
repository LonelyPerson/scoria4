/*
 * This program is free software; you can redistribute it and/or modify
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

import com.l2scoria.gameserver.model.L2Party;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /partyinfo command Added by Tempy - 28 Jul 05
 */
public class PartyInfo extends CommandAbst
{
	public PartyInfo()
	{
		_commands = new int[]{81};
		_isInParty = true;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		L2Party playerParty = activeChar.getParty();

		activeChar.sendPacket(new SystemMessage(SystemMessageId.PARTY_INFORMATION));

		switch (playerParty.getLootDistribution())
		{
			case L2Party.ITEM_LOOTER:
				activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_FINDERS_KEEPERS));
				break;
			case L2Party.ITEM_ORDER:
				activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_BY_TURN));
				break;
			case L2Party.ITEM_ORDER_SPOIL:
				activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL));
				break;
			case L2Party.ITEM_RANDOM:
				activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_RANDOM));
				break;
			case L2Party.ITEM_RANDOM_SPOIL:
				activeChar.sendPacket(new SystemMessage(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL));
				break;
		}

		activeChar.sendPacket(new SystemMessage(SystemMessageId.PARTY_LEADER_S1).addString(playerParty.getPartyMembers().get(0).getName()));
		activeChar.sendMessage("Members: " + playerParty.getMemberCount() + "/9");
		activeChar.sendPacket(new SystemMessage(SystemMessageId.WAR_LIST));
		return true;
	}
}
