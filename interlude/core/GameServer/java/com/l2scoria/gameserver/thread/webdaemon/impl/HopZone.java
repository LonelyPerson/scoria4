package com.l2scoria.gameserver.thread.webdaemon.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.thread.webdaemon.IWebDaemon;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Akumu
 * @date 14:54/27.08.12
 */
public class HopZone implements IWebDaemon
{
	private static Logger _log = Logger.getLogger(HopZone.class);
	private int _newVoteCount = 0;
	private int _lastVoteCount = 0;
	private static final String GET_ONLINE_CHAR = "SELECT ch.char_name FROM characters ch, accounts acc WHERE ch.online = 1 and ch.account_name = acc.login GROUP BY acc.lastIP";

	@Override
	public String getName()
	{
		return "hopzone";
	}

	@Override
	public String getUrl()
	{
		return Config.HOPZONEDAEMON_URL;
	}

	@Override
	public boolean load()
	{
		return false;
	}

	@Override
	public void parse(InputStream is) throws Exception
	{
		try
		{
			_log.info("HopeZoneDeamon: Checking HopeZone...");
			URL url = new URL(Config.HOPZONEDAEMON_URL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			if (reader != null)
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.contains("Anonymous User Votes"))
					{
						_newVoteCount = Integer.valueOf(line.split(">")[2].replace("</span", ""));
						break;
					}
				}
			}

			if (_newVoteCount != 0 && _lastVoteCount != 0 && _newVoteCount >= _lastVoteCount + Config.HOPZONEDAEMON_VOTES_FOR_REWARD)
			{
				L2PcInstance player;
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(GET_ONLINE_CHAR);
					ResultSet rset = statement.executeQuery();
					
					while (rset.next())
					{
						player = L2World.getInstance().getPlayer(rset.getString("char_name"));

						if (player == null)
						{
							continue;
						}
						
						if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE && !Config.HOPZONEDAEMON_REWARD_PRIVATE_STORE)
						{
							continue;
						}
						
						if (Config.HOPZONEDAEMON_MESSAGE != null && Config.HOPZONEDAEMON_MESSAGE.length() > 0)
						{
							player.sendMessage(Config.HOPZONEDAEMON_MESSAGE);
						}

						for (int[] item : Config.HOPZONEDAEMON_REWARDS)
						{
							player.addItem("reward", item[0], item[1], player, true);
						}
					}
					_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount + ", reward for " + Config.HOPZONEDAEMON_VOTES_FOR_REWARD + " vote(s).");
					_lastVoteCount += Config.HOPZONEDAEMON_VOTES_FOR_REWARD;

					rset.close();
					statement.close();
				} catch (SQLException SQLe)
				{
					_log.info("HopeZoneDeamon: SQL error. " + SQLe.getMessage());
				} finally
				{
					try
					{
						con.close();
					} catch (Exception e)
					{
					}
				}

				return;
			}

			if (_lastVoteCount == 0)
			{
				_lastVoteCount = _newVoteCount;
				_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount + ".");
			}
			else
			{
				_log.info("HopeZoneDeamon: Vote count is: " + _newVoteCount + ". Need " + (_lastVoteCount + Config.HOPZONEDAEMON_VOTES_FOR_REWARD - _newVoteCount) + " more vote(s) for reward.");
			}
		}
		catch (Exception e)
		{
			_log.error("HopeZoneDeamon: Error while reading data. " + e.getMessage());
		}
	}

	@Override
	public void rewardPlayer(String playerName)
	{

	}

	@Override
	public long getPollInterval()
	{
		return Config.HOPZONEDAEMON_INTERVAL * 60000L;
	}
}
