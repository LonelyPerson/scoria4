/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import mmo.SelectorServerConfig;
import mmo.SelectorThread;

import com.l2scoria.Config;
import com.l2scoria.L2Scoria;
import com.l2scoria.ServerType;
import com.l2scoria.gameserver.datatables.GameServerTable;
import com.l2scoria.telnet.Status;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.database.SqlUtils;

public class L2LoginServer
{
	public static final int PROTOCOL_REV = 0x0102;

	private static L2LoginServer _instance;
	private Logger _log = Logger.getLogger(L2LoginServer.class.getName());
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;

	public static void main(String[] args)
	{
		_instance = new L2LoginServer();
	}

	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	public L2LoginServer()
	{
		ServerType.serverMode = ServerType.MODE_LOGINSERVER;
		//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file

		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(LOG_NAME));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(is != null)
				{
					is.close();
				}

				is = null;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		//view info
		L2Scoria.infoLS();

		// Load Config
		Config.load();

		// Prepare Database
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch(SQLException e)
		{
			_log.severe("FATAL: Failed initializing database. Reason: " + e.getMessage());

			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		try
		{
			LoginController.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed initializing LoginController. Reason: " + e.getMessage());
			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		try
		{
			GameServerTable.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());

			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}
		catch(SQLException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());

			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		loadBanFile();

		InetAddress bindAddress = null;
		if(!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch(UnknownHostException e1)
			{
				_log.severe("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage());

				if(Config.DEVELOPER)
				{
					e1.printStackTrace();
				}
			}
		}

		SelectorServerConfig ssc = new SelectorServerConfig(bindAddress, Config.PORT_LOGIN);
		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<L2LoginClient>(ssc, loginPacketHandler, sh, sh);
			_selectorThread.setAcceptFilter(sh);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open Selector. Reason: " + e.getMessage());

			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage());

			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		if(Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new Status(ServerType.serverMode);
				_statusServer.start();
			}
			catch(IOException e)
			{
				_log.severe("Failed to start the Telnet Server. Reason: " + e.getMessage());
				if(Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}

		try
		{
			_selectorThread.openServerSocket();
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open server socket. Reason: " + e.getMessage());
			if(Config.DEVELOPER)
			{
				e.printStackTrace();
			}

			System.exit(1);
		}

		_selectorThread.start();

		if(Config.USE_AUTO_REBOOT)
		{
			try
			{
				 ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
				 scheduler.schedule(new CloseSocketSchedule(), Config.AUTO_REBOOT_TIMER_TASK*60000L, TimeUnit.MILLISECONDS);

				_log.severe("+==> Auto reboot login initialized in " +Config.AUTO_REBOOT_TIMER_TASK + " minute(s).");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		long mems = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		System.out.println("+=>...Free memory: "+mems);
		_log.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);

		bindAddress = null;
	}

	public Status getStatusServer()
	{
		return _statusServer;
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}

	class CloseSocketSchedule implements Runnable
	{
		public void run() 
		{ 
			System.out.println("Server now restarting by schedule task");
			shutdown(true);
		}
	}

	private void loadBanFile()
	{
		Iterator<String> ban_ip = Config.BANS.iterator();
		long duration = 0;

		do
		{
			if(!ban_ip.hasNext())
			{
				break;
			}

			String pattern = ban_ip.next();
			duration++;

			try
			{
				LoginController.getInstance().addBanForAddress(pattern, duration);
			}
			catch(UnknownHostException e)
			{
				_log.warning("Skipped: Invalid address \"" + pattern + "\" on \"" + Config.BANS + "\"");
			}
		} while(true);

		/*
		File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(bannedFile);
			}
			catch (FileNotFoundException e)
			{
				_log.warning("Failed to load banned IPs file ("+bannedFile.getName()+") for reading. Reason: "+e.getMessage());
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
				return;
			}

			LineNumberReader reader = new LineNumberReader(new InputStreamReader(fis));

			String line;
			String[] parts;
			try
			{

				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					// check if this line isnt a comment line
					if (line.length() > 0 && line.charAt(0) != '#')
					{
						// split comments if any
						parts = line.split("#");

						// discard comments in the line, if any
						line = parts[0];

						parts = line.split(" ");

						String address = parts[0];

						long duration = 0;

						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException e)
							{
								_log.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
								continue;
							}
						}

						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch (UnknownHostException e)
						{
							_log.warning("Skipped: Invalid address \"" + parts[0] + "\" on \"" + bannedFile.getName() + "\". Line: " + reader.getLineNumber() + "\"");
						}
						
						address = null;
					}
				}
			}
			catch (IOException e)
			{
				_log.warning("Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage());

				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
			_log.config("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
			
			line = null;
			parts = null;
			fis = null;
			reader = null;
		}
		else
		{
			_log.config("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
		
		bannedFile = null;
		*/
	}

	public void shutdown(boolean restart)
	{
		LoginController.getInstance().shutdown();
		SqlUtils.OpzLogin();
		System.gc();
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
