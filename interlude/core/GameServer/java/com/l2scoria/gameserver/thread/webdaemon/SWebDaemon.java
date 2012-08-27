package com.l2scoria.gameserver.thread.webdaemon;

import com.l2scoria.Config;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.thread.webdaemon.impl.HopZone;
import com.l2scoria.gameserver.thread.webdaemon.impl.L2Top;
import com.l2scoria.gameserver.thread.webdaemon.impl.MMOTop;
import com.l2scoria.gameserver.util.sql.SQLQueue;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import sun.misc.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author Azagthtot, Akumu
 *         Класс, управляющий всеми демонами, которые обрабатываю результаты топов<br>
 *         В настоящий момент интервал полла определяется старым параметром L2TopDeamonPoll
 */
public class SWebDaemon
{
	private static SWebDaemon _instance = null;
	private static Logger _log = Logger.getLogger(SWebDaemon.class);
	private List<IWebDaemon> _registredDeamons;

	public static SWebDaemon getInstance()
	{
		if (_instance == null)
		{
			_instance = new SWebDaemon();
		}
		return _instance;
	}

	private SWebDaemon()
	{
		_registredDeamons = new FastList<IWebDaemon>();
		Iterator<?> deamons = Service.providers(IWebDaemon.class);
		while (deamons.hasNext())
		{
			Object clazz = deamons.next();
			if (IWebDaemon.class.isAssignableFrom(clazz.getClass()))
			{
				IWebDaemon deamon = (IWebDaemon) clazz;
				if (deamon.load())
				{
					_registredDeamons.add(deamon);
				}
			}
		}

		L2Top l2top = new L2Top();
		if (l2top.load())
		{
			_registredDeamons.add(l2top);
		}

		MMOTop mmtop = new MMOTop();
		if (mmtop.load())
		{
			_registredDeamons.add(mmtop);
		}

		HopZone hopzone = new HopZone();
		if(hopzone.load())
		{
			_registredDeamons.add(hopzone);
		}

		if (_registredDeamons.size() > 0)
		{
			for(IWebDaemon daemon : _registredDeamons)
			{
				ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WebDaemonPoll(daemon), daemon.getPollInterval() / 2, daemon.getPollInterval());
				_log.info("[WebDaemon]: Registered '" + daemon.getName() + "' daemon successfully.");
			}
		}
	}

	protected class WebDaemonPoll implements Runnable
	{
		IWebDaemon _daemon;

		public WebDaemonPoll(IWebDaemon iwd)
		{
			_daemon = iwd;
		}

		@Override
		public void run()
		{
			try
			{
				String[] urls = _daemon.getUrl().split("@@@@");
				for (String s : urls)
				{
					if (s.trim().length() == 0)
					{
						continue;
					}
					URL url = new URL(s.trim());
					InputStream stream = url.openStream();
					_daemon.parse(stream);
					stream.close();
					url = null;
				}
			} catch (Exception e)
			{
				if (Config.DEBUG)
				{
					_log.warn("DaemonPoller: Error while running " + _daemon.getName() + ". Error: " + e);
				}
				else
				{
					_log.info("DaemonPoller: Error while running " + _daemon.getName() + ".");
				}
			}

			SQLQueue.getInstance().run();
		}
	}
}
