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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.model.L2CommandChannel;
import com.l2scoria.gameserver.model.L2Party;

/**
 *
 * @author  chris_00
 * 
 * ch Sddd
 */
public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private static final String _S__FE_30_EXMPCCPARTYINFOUPDATE = "[S] FE:30 ExMPCCPartyInfoUpdate";
	private L2CommandChannel _cc;
	private int _mode;
	
	/**
	 * 
	 * @param party
	 */
	public ExMPCCPartyInfoUpdate(L2CommandChannel cc, int mode)
	{
		_cc = cc;
		_mode = mode;
	}
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x30);
		writeS(_cc.getChannelLeader().getName());
		writeD(_mode);
		writeD(_cc.getMemberCount());
		writeD(_cc.getPartys().size());
		for (L2Party party: _cc.getPartys())
		{
			writeS(party.getLeader().getName());
			writeD(party.getLeader().getObjectId());
			writeD(party.getMemberCount());
		}
	}
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_30_EXMPCCPARTYINFOUPDATE;
	}
	
}
