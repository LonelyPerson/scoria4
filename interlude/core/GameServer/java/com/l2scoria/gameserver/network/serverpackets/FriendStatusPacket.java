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

import com.l2scoria.gameserver.datatables.sql.CharNameTable;
import com.l2scoria.gameserver.model.L2World;

public class FriendStatusPacket extends L2GameServerPacket
{
	private static final String _S__FC_FRIENDSTATUS = "[S] FC FriendStatusPacket";
	private boolean  _online;
	private int _objid;
	private String _name;
	
	public FriendStatusPacket(int objId)
	{
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFC);
		writeD(_online ? 1 : 0);
		writeS(_name);
		writeD(_objid);
	}
	
	@Override
	public String getType()
	{
		return _S__FC_FRIENDSTATUS;
	}
}
