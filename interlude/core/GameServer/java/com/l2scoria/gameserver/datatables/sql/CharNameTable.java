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
package com.l2scoria.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.Map;

import javolution.util.FastMap;

import com.l2scoria.util.database.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.1 $ $Date: 2005/03/27 15:29:18 $
 */
public class CharNameTable
{
	private static Logger _log = Logger.getLogger(CharNameTable.class.getName());

	private static CharNameTable _instance;
	private Map<Integer, Integer> _accessLevels = new FastMap<Integer, Integer>();;

	public static CharNameTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new CharNameTable();
		}
		return _instance;
	}

	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = true;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			result = rset.next();

			statement.close();
			rset.close();
			statement = null;
			rset = null;
		}
		catch(SQLException e)
		{
			_log.warning("could not check existing charname:" + e.getMessage());
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		return result;
	}

	public int accountCharNumber(String account)
	{
		Connection con = null;
		int number = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				number = rset.getInt(1);
			}

			statement.close();
			rset.close();
			statement = null;
			rset = null;
		}
		catch(SQLException e)
		{
			_log.warning("could not check existing char number:" + e.getMessage());
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		return number;
	}

	public final String getNameById(int id)
	{
		if (id <= 0)
			return null;

		Connection con = null;
		String name = null;
		int accessLevel = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, accesslevel FROM characters WHERE obj_Id=?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				name = rset.getString(1);
				accessLevel = rset.getInt(2);
			}
			rset.close();
			statement.close();
			rset = null;
			statement = null;
		}
		catch (SQLException e)
		{
			_log.warning("Could not check existing char id: " + e.getMessage());
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		if (name != null && !name.isEmpty())
		{
			_accessLevels.put(id, accessLevel);
			return name;
		}
		
		return null; //not found
	}

	public final int getIdByName(String name)
	{
		if (name == null || name.isEmpty())
			return -1;

		int id = -1;
		int accessLevel = 0;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, accesslevel FROM characters WHERE char_name=?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				id = rset.getInt(1);
				accessLevel = rset.getInt(2);
			}
			rset.close();
			statement.close();
			rset = null;
			statement = null;
		}
		catch (SQLException e)
		{
			_log.warning("Could not check existing char name: " + e.getMessage());
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		if (id > 0)
		{
			_accessLevels.put(id, accessLevel);
			return id;
		}
		
		return -1; // not found
	}

	public final int getAccessLevelById(int objectId)
	{
		if (getNameById(objectId) != null)
			return _accessLevels.get(objectId);
		else
			return 0;
	}
}
