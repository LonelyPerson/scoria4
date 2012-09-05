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
package com.l2scoria.gameserver.model;

import com.l2scoria.gameserver.datatables.sql.CharNameTable;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class BlockList
{
	private static Logger _log = Logger.getLogger(BlockList.class.getName());

	private final L2PcInstance _owner;
	private List<Integer> _blockList;

	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockList = loadList(_owner.getObjectId());
	}

	private synchronized void addToBlockList(int target)
	{
		_blockList.add(target);
		updateInDB(target, true);
	}

	private synchronized void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		updateInDB(target, false);
	}

	private static List<Integer> loadList(int ObjId)
	{
		Connection con = null;
		List<Integer> list = new FastList<Integer>();

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=? AND relation=1");
			statement.setInt(1, ObjId);
			ResultSet rset = statement.executeQuery();

			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friend_id");
				if (friendId ==  ObjId)
					continue;
				list.add(friendId);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error found in " + ObjId + " FriendList while loading BlockList: " + e.getMessage(), e);
		}
		finally
		{
			try { con.close(); }
			catch(Exception e) { }
			con = null;
		}
		return list;
	}

	private void updateInDB(int targetId, boolean state)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			if (state) //add
			{
				statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			else //remove
			{
				statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=? AND relation=1");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not add block player: " + e.getMessage(), e);
		}
		finally
		{
			try { con.close(); }
			catch(Exception e) { }
			con = null;
		}
	}

	public boolean isInBlockList(L2PcInstance target)
	{
		return _blockList.contains(target.getObjectId());
	}

	public boolean isInBlockList(int targetId)
	{
		return _blockList.contains(targetId);
	}

	private boolean isBlockAll()
	{
		return _owner.getMessageRefusal();
	}

	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance target)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}

	public static boolean isBlocked(L2PcInstance listOwner, int targetId)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}

	private void setBlockAll(boolean state)
	{
		_owner.setMessageRefusal(state);
	}

	private List<Integer> getBlockList()
	{
		return _blockList;
	}

	public static void addToBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
			return;

		String charName = CharNameTable.getInstance().getNameById(targetId);
		
		if (listOwner.getFriendList().contains(targetId))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(charName);
			listOwner.sendPacket(sm);
			return;
		}

		if (listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendMessage("Already in ignore list.");
			return;
		}

		listOwner.getBlockList().addToBlockList(targetId);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);

		L2PcInstance player = L2World.getInstance().getPlayer(targetId);

		if (player != null)
		{
			sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			player.sendPacket(sm);
		}
	}

	public static void removeFromBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
			return;

		SystemMessage sm;
		
		String charName = CharNameTable.getInstance().getNameById(targetId);

		if (!listOwner.getBlockList().getBlockList().contains(targetId))
		{
			sm = new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
			listOwner.sendPacket(sm);
			return;
		}

		listOwner.getBlockList().removeFromBlockList(targetId);

		sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
	}

	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}

	public boolean isBlockAll(L2PcInstance listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}

	public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}

	public static void sendListToOwner(L2PcInstance listOwner)
	{
		int i = 1;
		listOwner.sendPacket(new SystemMessage(SystemMessageId.BLOCK_LIST_HEAD));
		for (int playerId : listOwner.getBlockList().getBlockList())
		{
			listOwner.sendMessage((i++)+". "+CharNameTable.getInstance().getNameById(playerId));
		}
		listOwner.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
	}
}
