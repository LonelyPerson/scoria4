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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

/**
 * Format:(ch) h
 * 
 * @author -Wooden-
 */
public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private static final String _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO = "[C] D0:26 RequestExMPCCShowPartyMembersInfo";
	private int _partyLeaderId;

	@Override
	protected void readImpl()
	{
		_partyLeaderId = readD();
	}

	/**
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(_partyLeaderId);
		if (player != null && player.getParty() != null)
		{
			getClient().getActiveChar().sendPacket(new ExMPCCShowPartyMemberInfo(player.getParty()));
		}
	}

	/**
	 * @see com.l2scoria.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO;
	}

}
