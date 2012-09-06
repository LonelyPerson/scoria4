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

import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *L2Scoria
 **/
public class Noble extends Admin
{
	public Noble()
	{
		_commands = new String[]{"admin_setnoble"};
	}

	private final static Logger _log = Logger.getLogger(Noble.class.getName());

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_setnoble"))
		{
			L2Object target = activeChar.getTarget();

			if(target.isPlayer)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;

				boolean newNoble = !targetPlayer.isNoble();

				if(newNoble)
				{
					targetPlayer.setNoble(true);
					targetPlayer.sendMessage("You are now a noblesse.");
					updateDatabase(targetPlayer, true);
					sendMessages(true, targetPlayer, activeChar, true, true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
				}
				else
				{
					targetPlayer.setNoble(false);
					targetPlayer.sendMessage("You are no longer a noblesse.");
					updateDatabase(targetPlayer, false);
					sendMessages(false, targetPlayer, activeChar, true, true);
				}

				targetPlayer = null;
			}
			else
			{
				activeChar.sendMessage("Impossible to set a non Player Target as noble.");

				return false;
			}

			target = null;
		}

		return true;
	}

	private void sendMessages(boolean forNewNoble, L2PcInstance player, L2PcInstance gm, boolean announce, boolean notifyGmList)
	{
		if(forNewNoble)
		{
			player.sendMessage(gm.getName() + " has granted Noble Status from you!");
			gm.sendMessage("You've granted Noble Status from " + player.getName());

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has set " + player.getName() + " as Noble !");
			}
		}
		else
		{
			player.sendMessage(gm.getName() + " has revoked Noble Status for you!");
			gm.sendMessage("You've revoked Noble Status for " + player.getName());

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has removed Noble Status of player" + player.getName());
			}
		}
	}

	/**
	 * @param activeChar
	 * @param newNoble
	 */
	private void updateDatabase(L2PcInstance player, boolean newNoble)
	{
		Connection con = null;

		try
		{
			if(player == null)
				return;

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters_custom_data WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			ResultSet result = statement.executeQuery();

			if(result.next())
			{
				PreparedStatement stmt = con.prepareStatement(newNoble ? UPDATE_DATA : DEL_DATA);
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			else
			{
				if(newNoble)
				{
					PreparedStatement stmt = con.prepareStatement(INSERT_DATA);
					stmt.setInt(1, player.getObjectId());
					stmt.setString(2, player.getName());
					stmt.setInt(3, 1);
					stmt.execute();
					stmt.close();
					stmt = null;
				}
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("Error: could not update database: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	// Updates That Will be Executed by MySQL
	// ----------------------------------------
	String INSERT_DATA= "INSERT INTO characters_custom_data (obj_Id, char_name, noble) VALUES (?,?,?)";
	String UPDATE_DATA = "UPDATE characters_custom_data SET noble=1 WHERE obj_Id=?";
	String DEL_DATA = "UPDATE characters_custom_data SET noble=0 WHERE obj_Id=?";
}
