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

import com.l2scoria.gameserver.model.PartyMatchRoom;
import com.l2scoria.gameserver.model.PartyMatchRoomList;
import com.l2scoria.gameserver.model.PartyMatchWaitingList;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2scoria.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2scoria.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;


public final class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_15_ANSWERJOINPARTYROOM = "[C] D0:15 AnswerJoinPartyRoom";

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	/**
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
			
		if(player == null || player.getPartyRoomRequestId() == 0)
		{
			return;
		}

		if(_response == 1)// && !partner.isRequestExpired())
		{
			PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(player.getPartyRoomRequestId());
			if (_room == null)
				return;
			
			if ((player.getLevel() >= _room.getMinLvl()) && (player.getLevel() <= _room.getMaxLvl()))
			{
				// Remove from waiting list
				PartyMatchWaitingList.getInstance().removePlayer(player);
				
				player.setPartyRoom(player.getPartyRoomRequestId());
				
				player.sendPacket(new PartyMatchDetail(player, _room));
				player.sendPacket(new ExPartyRoomMember(player, _room, 0));
				
				for(L2PcInstance _member : _room.getPartyMembers())
				{
					if(_member == null)
						continue;
					
					_member.sendPacket(new ExManagePartyRoomMember(player, _room, 0));
					
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM);
					sm.addString(player.getName());
					_member.sendPacket(sm);
				}
				_room.addMember(player);
				
				// Info Broadcast
				player.broadcastUserInfo();
			}
		}
		player.setPartyRoomRequestId(0);
	}

	/**
	 * @see com.l2scoria.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_15_ANSWERJOINPARTYROOM;
	}

}
