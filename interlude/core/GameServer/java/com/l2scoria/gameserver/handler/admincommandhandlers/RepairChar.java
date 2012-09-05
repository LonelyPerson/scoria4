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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class handles following admin commands: - delete = deletes target
 * 
 * @version $Revision: 1.1.2.6.2.3 $ $Date: 2005/04/11 10:05:59 $
 */

public class RepairChar extends Admin
{
	private static Logger _log = Logger.getLogger(RepairChar.class.getName());

	public RepairChar()
	{
		_commands = new String[]{"admin_restore", "admin_repair"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		handleRepair(command);

		return true;
	}

	private void handleRepair(String command)
	{
		String[] parts = command.split(" ");

		if(parts.length != 2)
			return;

		String cmd = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";
		Connection connection = null;

		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(cmd);
			statement.setString(1, parts[1]);
			statement.execute();
			statement.close();
			statement = null;

			statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
			statement.setString(1, parts[1]);
			ResultSet rset = statement.executeQuery();

			int objId = 0;

			if(rset.next())
			{
				objId = rset.getInt(1);
			}

			rset.close();
			statement.close();
			rset = null;
			statement = null;

			if(objId == 0)
			{
				connection.close();
				return;
			}

			//connection = L2DatabaseFactory.getInstance().getConnection();
			statement = connection.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = null;

			//connection = L2DatabaseFactory.getInstance().getConnection();
			statement = connection.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("Could not repair char:", e);
		}
		finally
		{
			try {connection.close(); } catch(Exception e) { }
			connection = null;
			cmd = null;
			parts = null;
		}
	}
}