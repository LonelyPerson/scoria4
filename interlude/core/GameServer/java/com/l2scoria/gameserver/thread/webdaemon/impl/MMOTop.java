package com.l2scoria.gameserver.thread.webdaemon.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.util.L2Utils;
import com.l2scoria.gameserver.thread.webdaemon.IWebDaemon;
import com.l2scoria.gameserver.thread.webdaemon.SWebDaemon;
import com.l2scoria.gameserver.util.StoreVote;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.random.Rnd;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Автор: lucera, Akumu
 * MMOTop
 */

public class MMOTop implements IWebDaemon
{

	private static Logger _log = Logger.getLogger(SWebDaemon.class);
	private Timestamp _lastVote;
	private boolean _firstRun = false;

	@Override
	public String getName()
	{
		return "mmotop";
	}

	@Override
	public String getUrl()
	{
		return Config.MMOTOPDAEMON_URL;
	}

	@Override
	public boolean load()
	{
		try
		{
			if (Config.MMOTOPDAEMON_ENABLED)
			{
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("select max(votedate) from character_votes where deamon_name=?");
				stm.setString(1, getName());
				ResultSet rs = stm.executeQuery();

				if (rs.next())
				{
					_lastVote = rs.getTimestamp(1);
				}

				if (_lastVote == null)
				{
					_lastVote = new Timestamp(0);
					_firstRun = true;
				}

				rs.close();
				stm.close();
				con.close();
				return true;
			}
		} catch (Exception e)
		{}

		return false;
	}

	@Override
	public void parse(InputStream is) throws Exception
	{
		BufferedReader r = new BufferedReader(new InputStreamReader(is, "windows-1251"));
		String line;
		int nVotes = 0;
		Timestamp vote = new Timestamp(0);

		while ((line = r.readLine()) != null)
		{
			try
			{
				Pattern p = Pattern.compile("(\\d+)\\t([\\d|\\.|]+ [\\d|:]+)\\t([\\d|\\.]+)\\t(.*?)\\t.+");
				Matcher m = p.matcher(line);

				if (m.matches())
				{
					Pattern p1 = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})");
					Matcher m1 = p1.matcher(m.group(2));
					Timestamp t = null;

					if (m1.matches())
					{
						t = Timestamp.valueOf(String.format("%s-%s-%s %s:%s:%s", m1.group(3), m1.group(2), m1.group(1), m1.group(4), m1.group(5), m1.group(6)));
					}

					if (t != null)
					{
						if (t.after(_lastVote))
						{
							if (t.after(vote))
							{
								vote = t;
							}
							String charName = m.group(4);
							if (charName.length() > 0)
							{
								StoreVote.store(this, charName, t, Config.MMOTOPDAEMON_REWARD_FIRST || !_firstRun);
								nVotes++;
								if (nVotes >= Config.DEAMON_MAX_VOTES)
								{
									break;
								}
							}
						}
					}
				}
				try
				{
					Thread.sleep(20);
				} catch (InterruptedException e)
				{
					break;
				}

			} catch (Exception e)
			{
			}
		}

		r.close();

		if (vote.after(_lastVote))
		{
			_lastVote = vote;
		}

		_firstRun = false;
		_log.info("MMOTop: " + nVotes + " vote(s) processed");
	}

	@Override
	public void rewardPlayer(String playerName)
	{
		int count = Config.MMOTOPDAEMON_REWARD[0];

		if (Config.MMOTOPDAEMON_REWARD[1] > Config.MMOTOPDAEMON_REWARD[0])
		{
			count += Rnd.get(Config.MMOTOPDAEMON_REWARD[1] - Config.MMOTOPDAEMON_REWARD[0]);
		}

		L2Utils.addItem(playerName, Config.MMOTOPDAEMON_ITEM_ID, count);
	}

	@Override
	public long getPollInterval()
	{
		return Config.MMOTOPDAEMON_POLL_INVERVAL * 60000;
	}
}
