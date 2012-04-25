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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.IAdminCommandHandler;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.util.database.L2DatabaseFactory;

/**
 *L2Scoria
 **/
public class AdminDonator implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_setdonator"
	};

	private final static Logger _log = Logger.getLogger(AdminDonator.class.getName());
	private static final Logger _logAudit = Logger.getLogger("gmaudit");

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

		if(Config.GMAUDIT)
		{
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setLoggerName("gmaudit");
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}

		if(activeChar == null)
			return false;

		if(command.startsWith("admin_setdonator"))
		{
			String[] cmdParams = command.split(" ");

			long premiumTime = 0;
			if(cmdParams.length > 1)
			{
				try
				{
					premiumTime = Integer.parseInt(cmdParams[1]) * 24L * 60L * 60L * 1000L;
				}
				catch(NumberFormatException nfe)
				{
					//None
				}
			}

			L2Object target = activeChar.getTarget();

			if(target instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;
				boolean newDonator = !targetPlayer.isDonator();

				if(newDonator)
				{
					targetPlayer.setDonator(true);
					targetPlayer.updateNameTitleColor();
					updateDatabase(targetPlayer, true, premiumTime);
					sendMessages(true, targetPlayer, activeChar, false, true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
					targetPlayer.broadcastUserInfo();
				}
				else
				{
					targetPlayer.setDonator(false);
					targetPlayer.updateNameTitleColor();
					updateDatabase(targetPlayer, false, 0);
					sendMessages(false, targetPlayer, activeChar, false, true);
					targetPlayer.broadcastUserInfo();
				}

				targetPlayer = null;
			}
			else
			{
				activeChar.sendMessage("Impossible to set a non Player Target as Donator.");

				return false;
			}

			target = null;
		}
		return true;
	}

	private void sendMessages(boolean forNewDonator, L2PcInstance player, L2PcInstance gm, boolean announce, boolean notifyGmList)
	{
		if(forNewDonator)
		{
			player.sendMessage(gm.getName() + " has granted Donator Status for you!");
			gm.sendMessage("You've granted Donator Status for " + player.getName());

			if(announce)
			{
				Announcements.getInstance().announceToAll(player.getName() + " has received Donator Status!");
			}

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has set " + player.getName() + " as Donator !");
			}
		}
		else
		{
			player.sendMessage(gm.getName() + " has revoked Donator Status from you!");
			gm.sendMessage("You've revoked Donator Status from " + player.getName());

			if(announce)
			{
				Announcements.getInstance().announceToAll(player.getName() + " has lost Donator Status!");
			}

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has removed Donator Status of player" + player.getName());
			}
		}
	}

	/**
	 * @param activeChar
	 * @param newDonator
	 */
	private void updateDatabase(L2PcInstance player, boolean newDonator, long premiumTime)
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
				PreparedStatement stmt = con.prepareStatement(newDonator ? UPDATE_DATA : DEL_DATA);
				if(newDonator)
				{
					stmt.setLong(1, premiumTime == 0 ? 0 : System.currentTimeMillis() + premiumTime);
					stmt.setInt(2, player.getObjectId());
					stmt.execute();
				}
				else
				{
					stmt.setInt(1, player.getObjectId());
					stmt.execute();
				}
				stmt.close();
				stmt = null;
			}
			else
			{
				if(newDonator)
				{
					PreparedStatement stmt = con.prepareStatement(INSERT_DATA);
					stmt.setInt(1, player.getObjectId());
					stmt.setString(2, player.getName());
					stmt.setInt(3, 1);
					stmt.setLong(4, premiumTime == 0 ? 0 : System.currentTimeMillis() + premiumTime);
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
			_log.warning("Error: could not update database: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	// Updates That Will be Executed by SQL
	// ----------------------------------------
	String INSERT_DATA= "INSERT INTO characters_custom_data (obj_Id, char_name, donator, prem_end_date) VALUES (?,?,?,?)";
	String UPDATE_DATA = "UPDATE characters_custom_data SET donator=1, prem_end_date=? WHERE obj_Id=?";
	String DEL_DATA = "UPDATE characters_custom_data SET donator=0, prem_end_date=0 WHERE obj_Id=?";

	/**
	 * @return
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
