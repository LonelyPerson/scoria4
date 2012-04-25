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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;


public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_14_REQUESTASKJOINPARTYROOM = "[C] D0:14 RequestAskJoinPartyRoom";

	private String _player;

	@Override
	protected void readImpl()
	{
		_player = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_player);

		if(requestor == null || !requestor.isInPartyMatchRoom())
		{
			return;
		}

		if(target == null || target.isInPartyMatchRoom())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		if (target.getPartyRoomRequestId() == 0)
		{
			target.setPartyRoomRequestId(requestor.getPartyRoom());
			target.sendPacket(new ExAskJoinPartyRoom(requestor.getName()));
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_14_REQUESTASKJOINPARTYROOM;
	}

}
