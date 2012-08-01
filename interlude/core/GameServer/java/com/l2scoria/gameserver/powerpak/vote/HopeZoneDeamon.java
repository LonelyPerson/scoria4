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
 
package com.l2scoria.gameserver.powerpak.vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.database.L2DatabaseFactory;


public class HopeZoneDeamon implements Runnable
{
	private static Logger _log = Logger.getLogger(HopeZoneDeamon.class.getName());
	private ScheduledFuture<?> _task;
	private int	_newVoteCount = 0;
	private int	_lastVoteCount = 0;

	private static final String GET_ONLINE_CHAR = "SELECT ch.char_name FROM characters ch, accounts acc WHERE ch.online = 1 and ch.account_name = acc.login GROUP BY acc.lastIP";

	private static HopeZoneDeamon _instance = null;
	public static HopeZoneDeamon getInstance()
	{
		if(_instance == null)
			_instance = new HopeZoneDeamon();
		return _instance;
	}
	
	private class Terminator extends Thread
	{
		@Override
		public void run()
		{
			_log.info("HopeZoneDeamon: stopped");
			try
			{
				if(HopeZoneDeamon.getInstance()._task!=null)
				{
					HopeZoneDeamon.getInstance()._task.cancel(true);
				}
			} catch(Exception e) { }
		}
	}
	
	private HopeZoneDeamon()
	{
		if (PowerPakConfig.HOPEZONEDEMON_ENABLED)
		{
			try
			{
				_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000, PowerPakConfig.HOPEZONEDEMON_INTERVAL*60000);
				Runtime.getRuntime().addShutdownHook(new Terminator());
				_log.info("HopeZoneDeamon: Started with poll interval "+PowerPakConfig.HOPEZONEDEMON_INTERVAL+" minute(s)");
			}
			catch(Exception e)
			{
				_log.warning("HopeZoneDeamon: Error while starting main thread: " + e);
			}
		}
	}

	@Override
	public void run()
	{
		checkVotes();
	}
	
	private boolean checkVotes()
	{
		try
		{
			_log.info("HopeZoneDeamon: Checking HopeZone...");
			URL url = new URL(PowerPakConfig.HOPEZONEDEMON_URL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			if(reader!=null)
			{
				String line;
				while((line = reader.readLine())!=null)
				{
					if (line.contains("Anonymous User Votes"))
					{
						_newVoteCount = Integer.valueOf(line.split(">")[2].replace("</span", ""));
						break;
					}
				}
			}
			if (_newVoteCount != 0 && _lastVoteCount != 0 && _newVoteCount >= _lastVoteCount + PowerPakConfig.HOPEZONEDEMON_VOTES_FOR_REWARD)
			{
				L2PcInstance player;
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(GET_ONLINE_CHAR);
					ResultSet rset = statement.executeQuery();
					while(rset.next())
					{
						player = L2World.getInstance().getPlayer(rset.getString("char_name"));

						if (player == null)
							continue;
						if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE && !PowerPakConfig.HOPEZONEDEMON_REWARD_PRIVATE_STORE)
							continue;
						if (PowerPakConfig.HOPEZONEDEMON_MESSAGE != null && PowerPakConfig.HOPEZONEDEMON_MESSAGE.length() >0)
							player.sendMessage(PowerPakConfig.HOPEZONEDEMON_MESSAGE);

						for (int[] item: PowerPakConfig.HOPEZONEREWARDS)
							player.addItem("reward",item[0],item[1], player, true);
					}
					_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount  + ", reward for " + PowerPakConfig.HOPEZONEDEMON_VOTES_FOR_REWARD + " vote(s).");
					_lastVoteCount += PowerPakConfig.HOPEZONEDEMON_VOTES_FOR_REWARD;

					rset.close();
					statement.close();
				}
				catch(SQLException SQLe)
				{
					_log.info("HopeZoneDeamon: SQL error. " + SQLe.getMessage());
				}
				finally
				{
					try { con.close(); } catch(Exception e) { }
				}

				return true;
			}
			if (_lastVoteCount == 0)
			{
				_lastVoteCount = _newVoteCount;
				_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount + ".");
			}
			else
			{
				_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount + ". Need " + (_lastVoteCount + PowerPakConfig.HOPEZONEDEMON_VOTES_FOR_REWARD - _newVoteCount) + " more vote(s) for reward.");
			}
		}
		catch(Exception e)
		{
			_log.warning("HopeZoneDeamon: Error while reading data. " + e.getMessage());
		}
		return false;
	}
}