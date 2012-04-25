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
package com.l2scoria.gameserver.network.serverpackets;

import java.util.List;

import javolution.util.FastList;

import com.l2scoria.gameserver.datatables.sql.CharNameTable;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog. Format: ch (hdSdh) h: Total Friend Count h: Unknown d: Player Object ID S:
 * Friend Name d: Online/Offline h: Unknown
 * 
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";
	private List<FriendInfo> _info;

	private static class FriendInfo
	{
		int objId;
		String name;
		boolean online;
		
		public FriendInfo(int objId, String name, boolean online)
		{
			this.objId = objId;
			this.name = name;
			this.online = online;
		}
	}

	public FriendList(L2PcInstance character)
	{
		_info = new FastList<FriendInfo>(character.getFriendList().size());
		for (int objId : character.getFriendList())
		{
			String name = CharNameTable.getInstance().getNameById(objId);
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			boolean online = false;
			if (player != null && player.isOnline() == 1)
				online = true;
			_info.add(new FriendInfo(objId, name, online));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfa);
		writeD(_info.size());

		for (FriendInfo info : _info)
		{
			/*writeD(info.objId); // character id
			writeS(info.name);
			writeD(info.online ? 0x01 : 0x00); // online
			writeD(info.online ? info.objId : 0x00); // object id if online*/

			writeD(info.objId); // character id
			writeS(info.name);
			writeD(info.online ? 0x01 : 0x00); // online
			writeD(info.online ? info.objId : 0x00); // ??
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
