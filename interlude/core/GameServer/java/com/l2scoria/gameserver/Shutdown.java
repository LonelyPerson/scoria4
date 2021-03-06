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
package com.l2scoria.gameserver;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.OfflineTradersTable;
import com.l2scoria.gameserver.managers.*;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.gameserverpackets.ServerStatus;
import com.l2scoria.gameserver.network.serverpackets.ServerClose;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.thread.LoginServerThread;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.sql.SQLQueue;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.database.SqlUtils;
import org.apache.log4j.Logger;

/**
 * This class provides the functions for shutting down and restarting the server It closes all open client connections
 * and saves all data.
 * 
 * @version $Revision: 1.2.4.6 $ $Date: 2009/05/12 19:45:09 $
 */
public class Shutdown extends Thread
{
	public enum ShutdownModeType1
	{
		SIGTERM("Terminating"),
		SHUTDOWN("Shutting down"),
		RESTART("Restarting"),
		ABORT("Aborting"),
		TASK_SHUT("Shuting down"),
		TASK_RES("Restarting"),
		TELL_SHUT("Shuting down"),
		TELL_RES("Restarting");

		private final String _modeText;

		ShutdownModeType1(String modeText)
		{
			_modeText = modeText;
		}

		public String getText()
		{
			return _modeText;
		}
	}

	private static Logger _log = Logger.getLogger(Shutdown.class);
	private static Shutdown _instance;
	private static Shutdown _counterInstance = null;

	private int _secondsShut;

	private int _shutdownMode;

	/** 0 */
	public static final int SIGTERM = 0;
	/** 1 */
	public static final int GM_SHUTDOWN = 1;
	/** 2 */
	public static final int GM_RESTART = 2;
	/** 3 */
	public static final int ABORT = 3;
	/** 4 */
	public static final int TASK_SHUTDOWN = 4;
	/** 5 */
	public static final int TASK_RESTART = 5;
	/** 6 */
	public static final int TELL_SHUTDOWN = 6;
	/** 7 */
	public static final int TELL_RESTART = 7;

	private static final String[] MODE_TEXT =
	{
			"SIGTERM", "shutting down", "restarting", "aborting", //standart
			"shutting down",
			"restarting", //task
			"shutting down",
			"restarting"
	}; //telnet

	/**
	 * This function starts a shutdown countdown from Telnet (Copied from Function startShutdown())
	 * 
	 * @param ip IP Which Issued shutdown command
	 * @param seconds seconds untill shutdown
	 * @param restart true if the server will restart after shutdown
	 */

	public void startTelnetShutdown(String IP, int seconds, boolean restart)
	{
		Announcements _an = Announcements.getInstance();
		_log.warn("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		_an.announceToAll("Server " + Config.ABORT_RR + " is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

		if(restart)
		{
			_shutdownMode = TELL_RESTART;
		}
		else
		{
			_shutdownMode = TELL_SHUTDOWN;
		}

		if(_shutdownMode > 0)
		{
			_an.announceToAll("Attention players!");
			_an.announceToAll("Server " + Config.ABORT_RR + " is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
			if(_shutdownMode == 1 || _shutdownMode == 2)
			{
				_an.announceToAll("Please, avoid to use Gatekeepers/SoE");
				_an.announceToAll("during server " + MODE_TEXT[_shutdownMode] + " procedure.");
			}
		}

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, restart, false, true);
		_counterInstance.start();
	}

	/**
	 * This function aborts a running countdown
	 * 
	 * @param IP IP Which Issued shutdown command
	 */
	public void telnetAbort(String IP)
	{
		Announcements _an = Announcements.getInstance();
		_log.warn("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		_an.announceToAll("Server " + Config.ABORT_RR + " aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		_an = null;

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}
	}

	/**
	 * Default constucter is only used internal to create the shutdown-hook instance
	 */
	public Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}

	/**
	 * This creates a countdown instance of Shutdown.
	 * 
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 */
	public Shutdown(int seconds, boolean restart, boolean task, boolean telnet)
	{
		if(seconds < 0)
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		if(restart)
		{
			if(!task)
			{
				_shutdownMode = GM_RESTART;
			}
			else if(telnet)
			{
				_shutdownMode = TELL_RESTART;
			}
			else
			{
				_shutdownMode = TASK_RESTART;
			}
		}
		else
		{
			if(!task)
			{
				_shutdownMode = GM_SHUTDOWN;
			}
			else if(telnet)
			{
				_shutdownMode = TELL_SHUTDOWN;
			}
			else
			{
				_shutdownMode = TASK_SHUTDOWN;
			}
		}
	}

	/**
	 * get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it
	 * has to be registrered externaly.
	 * 
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		if(_instance == null)
		{
			_instance = new Shutdown();
		}
		return _instance;
	}

	public static Shutdown getCounterInstance()
	{
		return _counterInstance;
	}

	/**
	 * this function is called, when a new thread starts if this thread is the thread of getInstance, then this is the
	 * shutdown hook and we save all data and disconnect all clients. after this thread ends, the server will completely
	 * exit if this is not the thread of getInstance, then this is a countdown thread. we start the countdown, and when
	 * we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the
	 * exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{

		// disallow new logins
		try
		{
			//Doesnt actually do anything
			//Server.gameServer.getLoginController().setMaxAllowedOnlinePlayers(0);
		}
		catch(Throwable t)
		{
			// ignore
		}

		if(this == _instance)
		{
			try 
			{
				if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
					OfflineTradersTable.storeOffliners();
			}
			catch (Throwable t)
			{
				_log.warn("Error saving offline shops." + t);
			}

			disconnectAllCharacters();

			SQLQueue.getInstance().run();

			// ensure all services are stopped
			try
			{
				GameTimeController.getInstance().stopTimer();
			}
			catch(Throwable t)
			{
				// ignore
			}

			// stop all threadpolls
			try
			{
				ThreadPoolManager.getInstance().shutdown();
			}
			catch(Throwable t)
			{
				// ignore
			}

			// last byebye, save all data and quit this server
			// logging doesnt work here :(
			saveData();

			try
			{
				LoginServerThread.getInstance().interrupt();
			}
			catch(Throwable t)
			{
				// ignore
			}

			// saveData sends messages to exit players, so sgutdown selector after it
			try
			{
				GameServer.getSelectorThread().shutdown();
				GameServer.getSelectorThread().setDaemon(true);
			}
			catch(Throwable t)
			{
				// ignore
			}

			try
			{
				SqlUtils.OpzGame();
			}
			catch(Throwable t)
			{
				//null
			}

			// commit data, last chance
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch(Throwable t)
			{

			}

			System.runFinalization();
			System.gc();

			// server will quit, when this function ends.
			if(_instance._shutdownMode == GM_RESTART)
			{
				Runtime.getRuntime().halt(2);
			}
			else if(_instance._shutdownMode == TASK_RESTART)
			{
				Runtime.getRuntime().halt(5);
			}
			else if(_instance._shutdownMode == TASK_SHUTDOWN)
			{
				Runtime.getRuntime().halt(4);
			}
			else if(_instance._shutdownMode == TELL_RESTART)
			{
				Runtime.getRuntime().halt(7);
			}
			else if(_instance._shutdownMode == TELL_SHUTDOWN)
			{
				Runtime.getRuntime().halt(6);
			}
			else
			{
				Runtime.getRuntime().halt(0);
			}
		}
		else
		{
			// gm shutdown: send warns and then call exit to start shutdown sequence
			countdown();
			// last point where logging is operational :(
			_log.warn("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch(_shutdownMode)
			{
				case GM_SHUTDOWN:
					_instance.setMode(GM_SHUTDOWN);
					System.exit(0);
					break;

				case GM_RESTART:
					_instance.setMode(GM_RESTART);
					System.exit(2);
					break;

				case TASK_SHUTDOWN:
					_instance.setMode(TASK_SHUTDOWN);
					System.exit(4);
					break;

				case TASK_RESTART:
					_instance.setMode(TASK_RESTART);
					System.exit(5);
					break;

				case TELL_SHUTDOWN:
					_instance.setMode(TELL_SHUTDOWN);
					System.exit(6);
					break;

				case TELL_RESTART:
					_instance.setMode(TELL_RESTART);
					System.exit(7);
					break;
			}
		}
	}

	/**
	 * This functions starts a shutdown countdown
	 * 
	 * @param activeChar GM who issued the shutdown command
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		Announcements _an = Announcements.getInstance();
		_log.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

		if(restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}

		if(_shutdownMode > 0)
		{
			_an.announceToAll("Attention players!");
			_an.announceToAll("Server " + Config.ABORT_RR + " is " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
			if(_shutdownMode == 1 || _shutdownMode == 2)
			{
				_an.announceToAll("Please, avoid to use Gatekeepers/SoE");
				_an.announceToAll("during server " + MODE_TEXT[_shutdownMode] + " procedure.");
				_an = null;
			}
			_an = null;
		}

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}

		//		 the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart, false, false);
		_counterInstance.start();
	}

	public int getCountdown()
	{
		return _secondsShut;
	}

	/**
	 * This function aborts a running countdown
	 * 
	 * @param activeChar GM who issued the abort command
	 */
	public void abort(L2PcInstance activeChar)
	{
		Announcements _an = Announcements.getInstance();
		_log.warn("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		_an.announceToAll("Server " + Config.ABORT_RR + " aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		_an = null;

		if(_counterInstance != null)
		{
			_counterInstance._abort();
		}
	}

	/**
	 * set the shutdown mode
	 * 
	 * @param mode what mode shall be set
	 */
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}

	/**
	 * set shutdown mode to ABORT
	 */
	private void _abort()
	{
		_shutdownMode = ABORT;
	}

	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{

		try
		{
			while(_secondsShut > 0)
			{

				int _seconds;
				//int _minutes;
				//int _hours;

				_seconds = _secondsShut;
				//_minutes = Math.round(_seconds / 60);
				//_hours = Math.round(_seconds / 3600);

				// announce only every minute after 10 minutes left and every second after 20 seconds
				if((_seconds <= 20 || _seconds % 300 == 0) && _seconds <= 1800)// && _hours <= 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
					sm.addString(Integer.toString(_seconds));
					Announcements.getInstance().announceToAll(sm);
					sm = null;
				}

				try
				{
					if(_seconds <= 60)
					{
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
					}
				}
				catch(Exception e)
				{
					// do nothing, we maybe are not connected to LS anymore
				}

				_secondsShut--;

				int delay = 1000; // milliseconds
				Thread.sleep(delay);

				if(_shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch(InterruptedException e)
		{
			// this will never happen
		}
	}

	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private void saveData()
	{
		Announcements _an = Announcements.getInstance();
		switch(_shutdownMode)
		{
			case SIGTERM:
				System.err.println("SIGTERM received. Shutting down NOW!");
				break;

			case GM_SHUTDOWN:
				System.err.println("GM shutdown received. Shutting down NOW!");
				break;

			case GM_RESTART:
				System.err.println("GM restart received. Restarting NOW!");
				break;

			case TASK_SHUTDOWN:
				System.err.println("Auto task shutdown received. Shutting down NOW!");
				break;

			case TASK_RESTART:
				System.err.println("Auto task restart received. Restarting NOW!");
				break;

			case TELL_SHUTDOWN:
				System.err.println("Telnet shutdown received. Shutting down NOW!");
				break;

			case TELL_RESTART:
				System.err.println("Telnet restart received. Restarting NOW!");
				break;

		}
		try
		{
			_an.announceToAll("Server " + Config.ABORT_RR + " is " + MODE_TEXT[_shutdownMode] + " NOW!");
			_an = null;
		}
		catch(Throwable t)
		{
			_log.warn("" + t);
		}

		// Seven Signs data is now saved along with Festival data.
		if(!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
		}

		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData(null, true);

		// Save all raidboss status ^_^
		RaidBossSpawnManager.getInstance().cleanUp();
		System.err.println("RaidBossSpawnManager: All raidboss info saved!!");
		GrandBossManager.getInstance().cleanUp();
		System.err.println("GrandBossManager: All Grand Boss info saved!!");
		TradeController.getInstance().dataCountStore();
		System.err.println("TradeController: All count Item Saved");
		try
		{
			Olympiad.getInstance().save();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.err.println("Olympiad System: Data saved!!");

		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();

		// Save all manor data
		CastleManorManager.getInstance().save();

		// Save all global (non-player specific) Quest data that needs to persist after reboot
		QuestManager.getInstance().save();

		//Save items on ground before closing
		if(Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			System.err.println("ItemsOnGroundManager: All items on ground saved!!");
		}
		System.err.println("Data saved. All players disconnected, shutting down.");

		try
		{
			Thread.sleep(5000);
		}
		catch(InterruptedException e)
		{
			//never happens :p
		}
	}

	/**
	 * this disconnects all clients from the server
	 */
	private void disconnectAllCharacters()
	{
		_log.info("Players: All players save to disk"); 
		
		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			try
			{
				player.store();
				player.sendPacket(ServerClose.STATIC_PACKET);
			}
			catch(Throwable t)
			{}
		}

		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			_log.warn("Error: " + e);
		}

		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			try
			{
				player.closeNetConnection();
			}
			catch(Throwable t)
			{}
		}
	}

}
