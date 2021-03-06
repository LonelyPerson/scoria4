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
package com.l2scoria.gameserver.communitybbs.Manager;

import com.l2scoria.gameserver.communitybbs.BB.Forum;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(ForumsBBSManager.class.getName());
	private List<Forum> _table;
	private static ForumsBBSManager _instance;
	private int _lastid = 1;

	/**
	 * @return
	 */
	public static ForumsBBSManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new ForumsBBSManager();
		}
		return _instance;
	}

	public ForumsBBSManager()
	{
		_table = new FastList<Forum>();
		load();
	}

	public void addForum(Forum ff)
	{
		if (ff == null)
			return;

		_table.add(ff);

		if(ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}

	/**
	 *
	 */
	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery();
			while(result.next())
			{
				Forum f = new Forum(result.getInt("forum_id"), null);
				addForum(f);
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("data error on Forum (root): " + e);
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public void initRoot()
	{
		for (Forum f : _table)
			f.vload();
		_log.info("Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, com.l2scoria.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
	//null;
	}

	/**
	 * @param string
	 * @return
	 */
	public Forum getForumByName(String Name)
	{
		for(Forum f : _table)
		{
			if(f.getName().equals(Name))
				return f;
		}

		return null;
	}

	/**
	 * @param name
	 * @param forumByName
	 * @return
	 */
	public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertindb();
		return forum;
	}

	/**
	 * @return
	 */
	public int getANewID()
	{
		return ++_lastid;
	}

	/**
	 * @param idf
	 * @return
	 */
	public Forum getForumByID(int idf)
	{
		for(Forum f : _table)
		{
			if(f.getID() == idf)
				return f;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.l2scoria.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	//null;
	}
}
