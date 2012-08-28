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
import com.l2scoria.L2Scoria;
import com.l2scoria.ServerType;
import com.l2scoria.crypt.nProtect;
import com.l2scoria.gameserver.ai.special.AILoader;
import com.l2scoria.gameserver.cache.CrestCache;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.datatables.HeroSkillTable;
import com.l2scoria.gameserver.datatables.NobleSkillTable;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.csv.*;
import com.l2scoria.gameserver.datatables.sql.*;
import com.l2scoria.gameserver.datatables.xml.AugmentationData;
import com.l2scoria.gameserver.datatables.xml.ZoneData;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.handler.*;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.managers.*;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.Hero;
import com.l2scoria.gameserver.model.entity.MonsterRace;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2scoria.gameserver.model.entity.siege.BanditStrongholdSiege;
import com.l2scoria.gameserver.model.entity.siege.DevastatedCastle;
import com.l2scoria.gameserver.model.entity.siege.FortressOfResistance;
import com.l2scoria.gameserver.model.spawn.AutoSpawn;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.L2GamePacketHandler;
import com.l2scoria.gameserver.powerpak.PowerPak;
import com.l2scoria.gameserver.script.EventDroplist;
import com.l2scoria.gameserver.script.faenor.FaenorScriptEngine;
import com.l2scoria.gameserver.scripting.CompiledScriptCache;
import com.l2scoria.gameserver.scripting.L2ScriptEngineManager;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import com.l2scoria.gameserver.thread.LoginServerThread;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.thread.daemons.DeadlockDetector;
import com.l2scoria.gameserver.thread.daemons.PcPoint;
import com.l2scoria.gameserver.thread.daemons.ServerOnline;
import com.l2scoria.gameserver.thread.webdaemon.SWebDaemon;
import com.l2scoria.gameserver.util.DynamicExtension;
import com.l2scoria.gameserver.util.FloodProtector;
import com.l2scoria.gameserver.util.sql.SQLQueue;
import com.l2scoria.telnet.Status;
import com.l2scoria.util.Memory;
import com.l2scoria.util.Util;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.database.LoginRemoteDbFactory;
import com.lameguard.LameGuard;
import mmo.SelectorServerConfig;
import mmo.SelectorThread;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Calendar;

/**
 * 
 * @version 1.3
 */

public class GameServer
{
	private static Logger _log = Logger.getLogger(GameServer.class);
	private static SelectorThread<L2GameClient> _selectorThread;
	private static LoginServerThread _loginThread;

	public static final Calendar dateTimeServerStarted = Calendar.getInstance();

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void main(String[] args) throws Throwable
	{
		ServerType.serverMode = ServerType.MODE_GAMESERVER;

		// Create log folder
		new File(Config.DATAPACK_ROOT, "log").mkdir();

		long serverLoadStart = System.currentTimeMillis();

		Util.printSection("Configs");
		Config.load();

		try
		{
			Util.printSection("Database");
			L2DatabaseFactory.getInstance();
			if(Config.USE_RL_DATABSE)
			{
				LoginRemoteDbFactory.getInstance();
			}
			if(Config.IS_TELNET_ENABLED)
			{
				Status statusServer = new Status(ServerType.serverMode);
				statusServer.start();
			}
			else
			{
				System.out.println("Telnet server is currently disabled.");
			}
			Util.printSection("Logo");
			L2Scoria.infoGS();

			Util.printSection("Threads");
			ThreadPoolManager.getInstance();
			if(Config.DEADLOCKCHECK_INTIAL_TIME > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(DeadlockDetector.getInstance(), Config.DEADLOCKCHECK_INTIAL_TIME, Config.DEADLOCKCHECK_DELAY_TIME);
			}
                        
			if (!Arrays.equals(Util.securityCrypt(Config.USER_NAME), Util.getHash()))
			{
				System.out.println("UserName is wrong.");
				throw new Exception("UserName is wrong.");
			}

			new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
			new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
			new File(Config.DATAPACK_ROOT, "data/pathnode").mkdirs();
			new File(Config.DATAPACK_ROOT, "data/geodata").mkdirs();
			if(Config.USE_SAY_FILTER)
			{
				new File(Config.DATAPACK_ROOT, "config/chatfilter.txt").createNewFile();
			}
			HtmCache.getInstance();
			CrestCache.getInstance();
			L2ScriptEngineManager.getInstance();
			nProtect.getInstance();

			Util.printSection("World");
			L2World.getInstance();
			MapRegionTable.getInstance();
			// start game time control early
			GameTimeController.getInstance();
			Announcements.getInstance();
			AutoAnnouncementHandler.getInstance();
			if(!IdFactory.getInstance().isInitialized())
			{
				System.out.println("Could not read object IDs from DB. Please Check Your Data.");
				throw new Exception("Could not initialize the ID factory");
			}
			StaticObjects.getInstance();
			TeleportLocationTable.getInstance();
			// keep the references of Singletons to prevent garbage collection
			CharNameTable.getInstance();
			DatatablesManager.LoadSTS();

			Util.printSection("Skills");
			if(!SkillTable.getInstance().isInitialized())
			{
				System.out.println("Could not find the extraced files. Please Check Your Data.");
				throw new Exception("Could not initialize the skill table");
			}
			SkillTreeTable.getInstance();
			SkillSpellbookTable.getInstance();
			NobleSkillTable.getInstance();
			HeroSkillTable.getInstance();

			Util.printSection("Items");
			if(!ItemTable.getInstance().isInitialized())
			{
				System.out.println("Could not find the extraced files. Please Check Your Data.");
				throw new Exception("Could not initialize the item table");
			}
			ArmorSetsTable.getInstance();
			if(Config.CUSTOM_ARMORSETS_TABLE)
			{
				CustomArmorSetsTable.getInstance();
			}
			ExtractableItemsData.getInstance();
			SummonItemsData.getInstance();
			if(Config.ALLOWFISHING)
			{
				FishTable.getInstance();
			}

			Util.printSection("Npc");
			NpcWalkerRoutesTable.getInstance().load();
			if(!NpcTable.getInstance().isInitialized())
			{
				System.out.println("Could not find the extraced files. Please Check Your Data.");
				throw new Exception("Could not initialize the npc table");
			}

			Util.printSection("Characters");
			if(Config.COMMUNITY_TYPE.equals("full"))
			{
				ForumsBBSManager.getInstance().initRoot();
			}
			ClanTable.getInstance();
			CharTemplateTable.getInstance();
			LevelUpData.getInstance();
			if(!HennaTable.getInstance().isInitialized())
			{
				throw new Exception("Could not initialize the Henna Table");
			}
			if(!HennaTreeTable.getInstance().isInitialized())
			{
				throw new Exception("Could not initialize the Henna Tree Table");
			}
			if(!HelperBuffTable.getInstance().isInitialized())
			{
				throw new Exception("Could not initialize the Helper Buff Table");
			}

			Util.printSection("GeoEngine");
			if (Config.GEODATA)
				GeoEngine.loadGeo();

			Util.printSection("Economy");
			TradeController.getInstance();

			// Load clan hall data before zone data
			Util.printSection("Clan Halls");
			ClanHallManager.getInstance();
			FortressOfResistance.getInstance();
			DevastatedCastle.getInstance();
			BanditStrongholdSiege.getInstance();
			AuctionManager.getInstance();

			Util.printSection("Zone");
			ZoneData.getInstance();

			Util.printSection("Spawnlist");
			if(!Config.ALT_DEV_NO_SPAWNS)
			{
				SpawnTable.getInstance();
			}
			else
			{
				System.out.println("Spawn: disable load.");
			}
			if(!Config.ALT_DEV_NO_RB)
			{
				RaidBossSpawnManager.getInstance();
				GrandBossManager.getInstance();
				RaidBossPointsManager.init();
			}
			else
			{
				System.out.println("RaidBoss: disable load.");
			}
			DayNightSpawnManager.getInstance().notifyChangeMode();

			Util.printSection("Dimensional Rift");
			DimensionalRiftManager.getInstance();

			Util.printSection("Misc");
			RecipeTable.getInstance();
			RecipeController.getInstance();
			EventDroplist.getInstance();
			PartyMatchWaitingList.getInstance();
			PartyMatchRoomList.getInstance();
			AugmentationData.getInstance();
			MonsterRace.getInstance();
			FloodProtector.getInstance();
			MercTicketManager.getInstance();
			//PartyCommandManager.getInstance();
			PetitionManager.getInstance();
			// Init of a cursed weapon manager
			CursedWeaponsManager.getInstance();
			TaskManager.getInstance();
			// read pet stats from db
			L2PetDataTable.getInstance().loadPetsData();
			SQLQueue.getInstance();
			if(Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance();
			}
			if(Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			{
				ItemsAutoDestroy.getInstance();
			}

			Util.printSection("Scoria Mods");
			if(Config.L2JMOD_ALLOW_WEDDING)
			{
				CoupleManager.getInstance();
			}
			else
			{
				System.out.println("Wedding manager is currently Disabled");
			}
			if(Config.SCORIA_ALLOW_AWAY_STATUS)
			{
				AwayManager.getInstance();
			}

			/** Load Manor data */
			Util.printSection("Manor");
			L2Manor.getInstance();
			CastleManorManager.getInstance();

			/** Load Manager */
			Util.printSection("Castles");
			CastleManager.getInstance();
			SiegeManager.getInstance();
			FortManager.getInstance();
			FortSiegeManager.getInstance();
			CrownManager.getInstance();

			Util.printSection("Boat");
			BoatManager.getInstance();

			Util.printSection("Doors");
			DoorTable.getInstance().parseData();

			Util.printSection("Foure sepulches");
			FourSepulchersManager.getInstance().init();

			Util.printSection("Seven Signs");
			SevenSigns.getInstance();
			SevenSignsFestival.getInstance();
			AutoSpawn.getInstance();
			AutoChatHandler.getInstance();

			Util.printSection("Hero System");
			Olympiad.getInstance().load();
			Hero.getInstance();

			Util.printSection("L2Scoria Event Manager");
			// TODO

			if(Config.PCB_ENABLE)
			{
				ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(PcPoint.getInstance(), Config.PCB_INTERVAL * 1000, Config.PCB_INTERVAL * 1000);
			}
			else if(Config.PCB_WINDOW_ONLINE)
			{
				ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ServerOnline.getInstance(), Config.PCB_LIKE_WINDOW_ONLINE_RATE * 1000, Config.PCB_LIKE_WINDOW_ONLINE_RATE * 1000);
			}

			Util.printSection("Access Levels");
			AccessLevels.getInstance();
			AdminCommandAccessRights.getInstance();
			GmListTable.getInstance();

			Util.printSection("Handlers");
			ItemHandler.getInstance();
			SkillHandler.getInstance();
			AdminCommandHandler.getInstance();
			UserCommandHandler.getInstance();
			VoicedCommandHandler.getInstance();

			System.out.println("AutoChatHandler : Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
			System.out.println("AutoSpawnHandler : Loaded " + AutoSpawn.getInstance().size() + " handlers in total.");

			Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

			try
			{
				DoorTable doorTable = DoorTable.getInstance();
				doorTable.getDoor(24190001).openMe();
				doorTable.getDoor(24190002).openMe();
				doorTable.getDoor(24190003).openMe();
				doorTable.getDoor(24190004).openMe();
				doorTable.getDoor(23180001).openMe();
				doorTable.getDoor(23180002).openMe();
				doorTable.getDoor(23180003).openMe();
				doorTable.getDoor(23180004).openMe();
				doorTable.getDoor(23180005).openMe();
				doorTable.getDoor(23180006).openMe();
				doorTable.checkAutoOpen();
				doorTable = null;
			}
			catch(NullPointerException e)
			{
				System.out.println("There is errors in your Door.csv file. Update door.csv");

				if(Config.DEBUG)
				{
					e.printStackTrace();
				}
			}

			Util.printSection("Quests");
			if(!Config.ALT_DEV_NO_QUESTS)
			{
				QuestManager.getInstance();
			}
			else
			{
				System.out.println("Quest: disable load.");
			}

			Util.printSection("AI");
			if(!Config.ALT_DEV_NO_AI)
			{
				AILoader.init();
			}
			else
			{
				System.out.println("AI: disable load.");
			}

			Util.printSection("Scripts");
			try
			{
				File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
				L2ScriptEngineManager.getInstance().executeScriptsList(scripts);
			}
			catch(IOException ioe)
			{
				System.out.println("Failed loading scripts.cfg, no script going to be loaded");
			}

			try
			{
				CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
				if(compiledScriptCache == null)
				{
					System.out.println("Compiled Scripts Cache is disabled.");
				}
				else
				{
					compiledScriptCache.purge();

					if(compiledScriptCache.isModified())
					{
						compiledScriptCache.save();
						System.out.println("Compiled Scripts Cache was saved.");
					}
					else
					{
						System.out.println("Compiled Scripts Cache is up-to-date.");
					}
				}
			}
			catch(IOException e)
			{
				System.out.println("Failed to store Compiled Scripts Cache." + e);
			}

			QuestManager.getInstance().report();
			if(!Config.ALT_DEV_NO_SCRIPT)
			{
				FaenorScriptEngine.getInstance();
			}
			else
			{
				System.out.println("Script: disable load.");
			}

			Util.printSection("Web Daemons");
			SWebDaemon.getInstance();

			Util.printSection("Game Server");
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
				OfflineTradersTable.restoreOfflineTraders();
			System.out.println("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
			// initialize the dynamic extension loader
			try
			{
				DynamicExtension.getInstance();
			}
			catch(Exception ex)
			{
				System.out.println("DynamicExtension could not be loaded and initialized" + ex);
			}

			if(Config.LAME)
			{
				try
				{
					Class<?> clazz = Class.forName("com.lameguard.LameGuard");
					if(clazz!=null)
					{
						Util.printSection("LameGuard");
						LameGuard.main(new String []{"ru.catssoftware.protection.LameStub"});
					}
				} catch(Exception e) { }
			}

			PowerPak.getInstance();
			System.gc();

		}
		catch(Exception e)
		{
			System.exit(0);
		}
		/* ****************************
		 * ****************************
		 * ****************************/

		// maxMemory is the upper limit the jvm can use, totalMemory the size of the current allocation pool,
		// freeMemory the unused memory in the allocation pool
		_log.info("GameServer Started, free memory " + Memory.getFreeMemory() + " Mb of " + Memory.getTotalMemory() + " Mb");
		_log.info("Used memory:" + Memory.getUsedMemory() + " MB");

		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		_loginThread = null;

		SelectorServerConfig ssc = new SelectorServerConfig(Config.PORT_GAME); 
		L2GamePacketHandler gph = new L2GamePacketHandler(); 
		
		_selectorThread = new SelectorThread<L2GameClient>(ssc, gph, gph, gph); 
		
		try
		{
			_selectorThread.openServerSocket();
		}
		catch (IOException e)
		{
			_log.error("FATAL: Failed to open server socket. Reason: " + e.getMessage());
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		Util.printSection("L2Scoria Version");
		_log.info("Operating System: " + Util.getOSName() + " " + Util.getOSVersion() + " " + Util.getOSArch());
		_log.info("Available CPUs: " + Util.getAvailableProcessors());
		//Print general infos related to GS
		_log.info("Core Revision: " + Config.SERVER_REVISION);
		// print general infos related to DP
		_log.info("Datapack Revision: " + Config.DATAPACK_VERSION);
		_log.info("Version: " + Config.SERVER_VERSION);

		_log.info("Server Loaded in " + (System.currentTimeMillis() - serverLoadStart) / 1000 + " seconds");
		saveStartTime("GameServer", "GameServerStart", Long.toString(System.currentTimeMillis()));
		Util.printSection("Status");
	}

	public static SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public static void saveStartTime(String var1, String var2, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
			statement.setString(1, var1);
			statement.setString(2, var2);
			statement.setString(3, value);
			statement.executeUpdate();
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.info("could not insert server start time.");
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}
}
