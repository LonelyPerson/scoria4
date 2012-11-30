package com.l2scoria.gameserver.model.entity.event;

import com.l2scoria.L2Properties;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.event.CTF.CTF;
import com.l2scoria.gameserver.model.entity.event.DeathMatch.DeathMatch;
import com.l2scoria.gameserver.model.entity.event.LastHero.LastHero;
import com.l2scoria.gameserver.model.entity.event.TvT.TvT;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import sun.misc.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GameEventManager
{
	private static Logger _log = Logger.getLogger("Event");
	private L2Properties _eventStartup;
	private static GameEventManager _instance;
	private Map<GameEvent, Long> _events = new FastMap<GameEvent, Long>();

	public static GameEventManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new GameEventManager();
		}
		return _instance;
	}


	private GameEventManager()
	{
		try
		{
			_eventStartup = new L2Properties("./config/events/events_start.properties");
		} catch (IOException e)
		{
			_log.warn("GameEventManager: Unable to read startup file. Startup will be disabled");
			_eventStartup = null;
		}
		Iterator<?> iterator = Service.providers(GameEvent.class);
		while (iterator.hasNext())
		{
			GameEvent evt = (GameEvent) iterator.next();
			//registerEvent(evt);
		}
                Language.load();
		registerEvent(TvT.getInstance());
		registerEvent(LastHero.getInstance());
		registerEvent(CTF.getInstance());
		registerEvent(DeathMatch.getInstance());
		_log.info("GameEventManager: Loaded " + _events.size() + " events.");
	}

	private class EventStart implements Runnable
	{
		private GameEvent _evt;

		public EventStart(GameEvent evt)
		{
			_evt = evt;
		}

		@Override
		public void run()
		{
			if (_evt.getState() == GameEvent.STATE_INACTIVE)
			{
				_log.info("GameEventManager: Starting event " + _evt.getName());
				_evt.start();
			}
			else
			{
				_log.info("GameEventManager: Event " + _evt.getName() + " active now unable to start");
			}

			if (_events.get(_evt) != -1)
			{
                            String conf = _eventStartup.getProperty(_evt.getName() + ".Runtime");
                            Calendar starttime = null;
                            long current = System.currentTimeMillis();

                            boolean first = true;
                            if (((conf != null ? 1 : 0) & (conf.length() > 0 ? 1 : 0)) != 0)
                            {
                              for (String tmp : conf.split(";"))
                              {
                                if (first)
                                {
                                  String[] hm = tmp.split(":");

                                  int h = Integer.parseInt(hm[0]);
                                  int m = Integer.parseInt(hm[1]);
                                  starttime = Calendar.getInstance();
                                  starttime.set(Calendar.HOUR_OF_DAY, h);
                                  starttime.set(Calendar.MINUTE, m);
                                  long restart = starttime.getTimeInMillis() - current;
                                  if (restart > 0)
                                  {
                                    first = false;
                                    Date readable = new Date(starttime.getTimeInMillis());
                                    GameEventManager._log.info("GameEventManager[b]: Event " + _evt.getName() + " scheduled at " + readable);
                                    ThreadPoolManager.getInstance().scheduleGeneral(this, restart);
                                  }
                                }
                              }

                              if (first)
                              {
                                String[] tmp = conf.split(";");
                                String[] hm = tmp[0].split(":");

                                int first_h = Integer.parseInt(hm[0]);
                                int first_m = Integer.parseInt(hm[1]);
                                starttime = Calendar.getInstance();
                                starttime.set(Calendar.HOUR_OF_DAY, first_h);
                                starttime.set(Calendar.MINUTE, first_m);
                                starttime.add(5, 1);
                                long restart = starttime.getTimeInMillis() - current;
                                if (restart > 0)
                                {
                                  first = false;
                                  Date readable = new Date(starttime.getTimeInMillis());
                                  _log.info("GameEventManager[c]: Event " + _evt.getName() + " scheduled at " + readable);
                                  ThreadPoolManager.getInstance().scheduleGeneral(this, restart);
                                }
                              }
                            }
			}
		}
	}

        public void registerEvent(GameEvent evt)
        {
          if (evt.load())
          {
            long restart = -1L;
            long current = System.currentTimeMillis();
            Calendar starttime = null;
            boolean first = true;
            if (_eventStartup.getProperty(evt.getName() + ".AutoStart") != null)
            {
              if (Boolean.parseBoolean(_eventStartup.getProperty(evt.getName() + ".AutoStart")))
              {
                String conf = _eventStartup.getProperty(evt.getName() + ".Runtime");
                if ((conf != null) && (conf.length() != 0))
                {
                  for (String tmp : conf.split(";"))
                  {
                    if (first)
                    {
                      String[] hm = tmp.split(":");

                      int h = Integer.parseInt(hm[0]);
                      int m = Integer.parseInt(hm[1]);
                      starttime = Calendar.getInstance();
                      starttime.set(Calendar.HOUR_OF_DAY, h);
                      starttime.set(Calendar.MINUTE, m);
                      restart = starttime.getTimeInMillis() - current;
                      if (restart > 0)
                      {
                        first = false;
                        ThreadPoolManager.getInstance().scheduleGeneral(new EventStart(evt), restart);
                        Date readableDate = new Date(starttime.getTimeInMillis());
                        _log.info("GameEventManager[a]: Event " + evt.getName() + " scheduled at " + readableDate);
                      }
                    }
                  }
                  if (first)
                  {
                    String[] tmp = conf.split(";");
                    String[] hm = tmp[0].split(":");

                    int first_h = Integer.parseInt(hm[0]);
                    int first_m = Integer.parseInt(hm[1]);
                    starttime = Calendar.getInstance();
                    starttime.set(Calendar.HOUR_OF_DAY, first_h);
                    starttime.set(Calendar.MINUTE, first_m);
                    starttime.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                    restart = starttime.getTimeInMillis() - current;
                    if (restart > 0)
                    {
                      first = false;
                      Date readable = new Date(starttime.getTimeInMillis());
                      _log.info("GameEventManager[d]: Event " + evt.getName() + " scheduled at " + readable);
                      ThreadPoolManager.getInstance().scheduleGeneral(new EventStart(evt), restart);
                    }
                  }
                }
              }
            }
            _events.put(evt, Long.valueOf(restart));
          }
        }

	public GameEvent findEvent(String name)
	{
		for (GameEvent evt : _events.keySet())
		{
			if (name.equals(evt.getName()))
			{
				return evt;
			}
		}
		return null;
	}

	public Set<GameEvent> getAllEvents()
	{
		return _events.keySet();
	}

	public GameEvent participantOf(L2PcInstance player)
	{
		for (GameEvent evt : _events.keySet())
		{
			if (evt.isParticipant(player))
			{
				return evt;
			}
		}
		return null;
	}
        
        public String getEventConfigStart(String evo)
        {
            String result = _eventStartup.getProperty(evo + ".Runtime");
            if(result == null || result.length() < 1)
            {
                result = "not defined";
            }
            return result;
        }
}
