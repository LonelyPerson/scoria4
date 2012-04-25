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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2scoria.gameserver.datatables.sql.CharNameTable;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.FriendPacket;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendDel extends L2GameClientPacket
{

	private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";
	private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());

	private String _name;

	@Override
	protected void readImpl()
	{
		try {
			_name = readS();
		} catch(Exception e) {
			_name = null;
		}
	}

	@Override
	protected void runImpl()
	{
		if(_name==null)
			return;

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		SystemMessage sm;
		int id = CharNameTable.getInstance().getIdByName(_name);
		
		if (id == -1)
		{
			sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		if (!activeChar.getFriendList().contains(id))
		{
			sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, id);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			statement.close();

			// Player deleted from your friendlist
			sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);

			activeChar.getFriendList().remove(new Integer(id));
			activeChar.sendPacket(new FriendPacket(false, id));

			L2PcInstance friend = L2World.getInstance().getPlayer(_name);
			if (friend != null)
			{
				friend.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
				friend.sendPacket(new FriendPacket(false, activeChar.getObjectId()));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not del friend objectid: ", e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	@Override
	public String getType()
	{
		return _C__61_REQUESTFRIENDDEL;
	}
}