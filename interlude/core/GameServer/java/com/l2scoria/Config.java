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
package com.l2scoria;

import com.l2scoria.gameserver.services.FService;
import com.l2scoria.gameserver.services.Instruments;
import gnu.trove.TIntIntHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * This class contains global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize<br>
 * away debug/unused code.<br>
 * <br>
 * <li>ArrayList & List РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р… РїС—Р…РїС—Р… РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р… РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р…РїС—Р… FastList
 */
public final class Config
{
	private static final Logger _log = Logger.getLogger(Config.class);

	//============================================================
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_ANNOUNCER_NAME;
	public static int MASTERACCESS_LEVEL;
	public static int MASTERACCESS_NAME_COLOR;
	public static int MASTERACCESS_TITLE_COLOR;
	public static boolean GM_MODIFY_LIST_ENABLED;
	public static String GM_MODIFY_LIST_ENTETY;
	public static FastList<Integer> GM_REALY_MODIFY_LIST = new FastList<Integer>();
	//============================================================
	public static void loadAccessConfig()
	{
		final String ACCESS = FService.ACCESS_CONFIGURATION_FILE;

		_log.info("Loading: " + ACCESS + ".");
		try
		{
			Properties AccessSettings = new Properties();
			InputStream is = new FileInputStream(new File(ACCESS));
			AccessSettings.load(is);
			is.close();
			//============================================================
			EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(AccessSettings.getProperty("EverybodyHasAdminRights", "false"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(AccessSettings.getProperty("GMStartupAutoList", "true"));
			GM_ADMIN_MENU_STYLE = AccessSettings.getProperty("GMAdminMenuStyle", "modern");
			GM_HERO_AURA = Boolean.parseBoolean(AccessSettings.getProperty("GMHeroAura", "false"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(AccessSettings.getProperty("GMStartupInvulnerable", "true"));
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(AccessSettings.getProperty("AnnounceGmName", "false"));
			SHOW_GM_LOGIN = Boolean.parseBoolean(AccessSettings.getProperty("ShowGMLogin", "false"));
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(AccessSettings.getProperty("GMStartupInvisible", "true"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(AccessSettings.getProperty("GMStartupSilence", "true"));
			MASTERACCESS_LEVEL = Integer.parseInt(AccessSettings.getProperty("MasterAccessLevel", "1"));
			MASTERACCESS_NAME_COLOR = Integer.decode("0x" + AccessSettings.getProperty("MasterNameColor", "00FF00"));
			MASTERACCESS_TITLE_COLOR = Integer.decode("0x" + AccessSettings.getProperty("MasterTitleColor", "00FF00"));
			GM_MODIFY_LIST_ENABLED = Boolean.parseBoolean(AccessSettings.getProperty("GMModifyListIsEnabled", "false"));
			if(GM_MODIFY_LIST_ENABLED)
			{
					GM_MODIFY_LIST_ENTETY = AccessSettings.getProperty("GmModifyListEntity", "");
					GM_REALY_MODIFY_LIST = new FastList<Integer>();
					for(String preParse : GM_MODIFY_LIST_ENTETY.trim().split(","))
					{
						GM_REALY_MODIFY_LIST.add(Integer.parseInt(preParse.trim()));
					}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ACCESS + " File.");
		}
	}

	public static enum ChatMode
	{
		GLOBAL, REGION, GM, OFF
	}

	//============================================================
	public static boolean CHECK_KNOWN;

	public static ChatMode DEFAULT_GLOBAL_CHAT;
	public static ChatMode DEFAULT_TRADE_CHAT;
	public static int MAX_CHAT_LENGTH;
	public static boolean TRADE_CHAT_IS_NOOBLE;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static int COORD_SYNCHRONIZE;
	public static int DELETE_DAYS;
	public static int MAX_DRIFT_RANGE;
    public static int MAX_FOLLOW_DRIFT_RANGE;
    public static boolean ON_DRIFT_MAX_RANGE_TELEPORT;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_MANOR;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static String PROTECTED_ITEMS;
	public static FastList<Integer> LIST_PROTECTED_ITEMS = new FastList<Integer>();
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_NPC_WALKERS;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean ALLOW_USE_CURSOR_FOR_WALK;
	public static boolean USE_3D_MAP;
	public static String COMMUNITY_TYPE;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static int PATH_NODE_RADIUS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static boolean SHOW_NPC_LVL;
	public static int ZONE_TOWN;
	public static boolean COUNT_PACKETS = false;
	public static boolean DUMP_PACKET_COUNTS = false;
	public static int DUMP_INTERVAL_SECONDS = 60;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int MINIMUM_UPDATE_DISTANCE;
	public static int KNOWNLIST_FORGET_DELAY;
	public static int MINIMUN_UPDATE_TIME;
	public static boolean BYPASS_VALIDATION;

	//============================================================
	public static void loadOptionsConfig()
	{
		final String OPTIONS = FService.OPTIONS_FILE;

		_log.info("Loading: " + OPTIONS + ".");
		try
		{
			Properties optionsSettings = new Properties();
			InputStream is = new FileInputStream(new File(OPTIONS));
			optionsSettings.load(is);
			is.close();

			AUTODESTROY_ITEM_AFTER = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
			HERB_AUTO_DESTROY_TIME = Integer.parseInt(optionsSettings.getProperty("AutoDestroyHerbTime", "15")) * 1000;
			PROTECTED_ITEMS = optionsSettings.getProperty("ListOfProtectedItems");
			LIST_PROTECTED_ITEMS = new FastList<Integer>();
			for(String id : PROTECTED_ITEMS.split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			}
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyPlayerDroppedItem", "false"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyEquipableItem", "false"));
			SAVE_DROPPED_ITEM = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "false"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(optionsSettings.getProperty("ClearDroppedItemTable", "false"));

			PRECISE_DROP_CALCULATION = Boolean.valueOf(optionsSettings.getProperty("PreciseDropCalculation", "True"));
			MULTIPLE_ITEM_DROP = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True"));

			COORD_SYNCHRONIZE = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));

			ALLOW_WAREHOUSE = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
			ALLOW_FREIGHT = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
			ALLOW_WEAR = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
			WEAR_DELAY = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
			ALLOW_LOTTERY = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
			ALLOW_RACE = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
			ALLOW_WATER = Boolean.valueOf(optionsSettings.getProperty("AllowWater", "False"));
			ALLOW_RENTPET = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
			ALLOW_DISCARDITEM = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
			ALLOWFISHING = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "False"));
			ALLOW_MANOR = Boolean.parseBoolean(optionsSettings.getProperty("AllowManor", "False"));
			ALLOW_BOAT = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
			ALLOW_NPC_WALKERS = Boolean.valueOf(optionsSettings.getProperty("AllowNpcWalkers", "true"));
			ALLOW_CURSED_WEAPONS = Boolean.valueOf(optionsSettings.getProperty("AllowCursedWeapons", "False"));

			ALLOW_USE_CURSOR_FOR_WALK = Boolean.valueOf(optionsSettings.getProperty("AllowUseCursorForWalk", "False"));

			DEFAULT_GLOBAL_CHAT = ChatMode.valueOf(optionsSettings.getProperty("GlobalChat", "REGION").toUpperCase());
			DEFAULT_TRADE_CHAT = ChatMode.valueOf(optionsSettings.getProperty("TradeChat", "REGION").toUpperCase());

			MAX_CHAT_LENGTH = Integer.parseInt(optionsSettings.getProperty("MaxChatLength", "100"));
			TRADE_CHAT_IS_NOOBLE = Boolean.valueOf(optionsSettings.getProperty("TradeChatIsNooble", "false"));

			COMMUNITY_TYPE = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
			BBS_DEFAULT = optionsSettings.getProperty("BBSDefault", "_bbshome");
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
			SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));

			ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));

			MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));
                        MAX_FOLLOW_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxFollowDriftRage", "2000"));
                        ON_DRIFT_MAX_RANGE_TELEPORT = Boolean.parseBoolean(optionsSettings.getProperty("OnFreeWallDriftMonsterTeleport", "false"));

			MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
			MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
			MIN_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxMonsterAnimation", "20"));

			SHOW_NPC_LVL = Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False"));

			FORCE_INVENTORY_UPDATE = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));

			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));

			DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));

			DEFAULT_PUNISH = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
			DEFAULT_PUNISH_PARAM = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));

			GRIDS_ALWAYS_ON = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "30"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "300"));

			// ---------------------------------------------------
			// Configuration values not found in config files its hide options not for users
			// ---------------------------------------------------

			USE_3D_MAP = Boolean.valueOf(optionsSettings.getProperty("Use3DMap", "False"));

			PATH_NODE_RADIUS = Integer.parseInt(optionsSettings.getProperty("PathNodeRadius", "50"));
			NEW_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			SELECTED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			LINKED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			NEW_NODE_TYPE = optionsSettings.getProperty("NewNodeType", "npc");

			COUNT_PACKETS = Boolean.valueOf(optionsSettings.getProperty("CountPacket", "false"));
			DUMP_PACKET_COUNTS = Boolean.valueOf(optionsSettings.getProperty("DumpPacketCounts", "false"));
			DUMP_INTERVAL_SECONDS = Integer.parseInt(optionsSettings.getProperty("PacketDumpInterval", "60"));

			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
			MINIMUN_UPDATE_TIME = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
			CHECK_KNOWN = Boolean.valueOf(optionsSettings.getProperty("CheckKnownList", "false"));
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OPTIONS + " File.");
		}
	}
        
    //================================================================
	/**
	 * Fun_event configuration load
	 */
	//================================================================

	public static boolean		Allow_Same_HWID_On_Events;
	public static boolean		Allow_Same_IP_On_Events;

	public static void loadFunEvents()
	{
		final String FUNEVENT_FILE = FService.EVENTS;
		_log.info("Loading: " + FUNEVENT_FILE + ".");
		try
		{
			Properties funevSettings = new Properties();
			InputStream is = new FileInputStream(new File(FUNEVENT_FILE));
			funevSettings.load(is);
			is.close();

			Allow_Same_HWID_On_Events = Boolean.parseBoolean(funevSettings.getProperty("SameHWIDOnEvents", "false"));
			Allow_Same_IP_On_Events = Boolean.parseBoolean(funevSettings.getProperty("SameIPOnEvents", "true"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + FUNEVENT_FILE + " File.");
		}
	}

	//============================================================
	public static int PORT_GAME;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_TIMEOUT;
	public static int DATABASE_STATEMENT;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static boolean RWHO_LOG;
	public static int RWHO_FORCE_INC;
	public static int RWHO_KEEP_STAT;
	public static int RWHO_MAX_ONLINE;
	public static boolean RWHO_SEND_TRASH;
	public static int RWHO_ONLINE_INCREMENT;
	public static float RWHO_PRIV_STORE_FACTOR;
	public static int RWHO_ARRAY[] = new int[13];
	public static boolean LAME;
	public static String USER_NAME;
	public static String ON_SUCCESS_LOGIN_COMMAND_GS;

	//============================================================
	public static void loadServerConfig()
	{
		final String GAMESERVER = FService.CONFIGURATION_FILE;

		_log.info("Loading: " + GAMESERVER + ".");
		try
		{
			Properties serverSettings = new Properties();
			InputStream is = new FileInputStream(new File(GAMESERVER));
			serverSettings.load(is);
			is.close();
			GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
			PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
			
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");

			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");

			DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "100"));

			DATABASE_TIMEOUT = Integer.parseInt(serverSettings.getProperty("TimeOutConDb", "0"));
			DATABASE_STATEMENT = Integer.parseInt(serverSettings.getProperty("MaximumDbStatement", "100"));

			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();

			Random ppc = new Random();
			int z = ppc.nextInt(6);
			if(z == 0)
			{
				z += 2;
			}
			for(int x = 0; x < 8; x++)
			{
				if(x == 4)
				{
					RWHO_ARRAY[x] = 44;
				}
				else
				{
					RWHO_ARRAY[x] = 51 + ppc.nextInt(z);
				}
			}
			RWHO_ARRAY[11] = 37265 + ppc.nextInt(z * 2 + 3);
			RWHO_ARRAY[8] = 51 + ppc.nextInt(z);
			z = 36224 + ppc.nextInt(z * 2);
			RWHO_ARRAY[9] = z;
			RWHO_ARRAY[10] = z;
			RWHO_ARRAY[12] = 1;
			RWHO_LOG = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoLog", "False"));
			RWHO_SEND_TRASH = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoSendTrash", "False"));
			RWHO_MAX_ONLINE = Integer.parseInt(serverSettings.getProperty("RemoteWhoMaxOnline", "0"));
			RWHO_KEEP_STAT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineKeepStat", "5"));
			RWHO_ONLINE_INCREMENT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineIncrement", "0"));
			RWHO_PRIV_STORE_FACTOR = Float.parseFloat(serverSettings.getProperty("RemotePrivStoreFactor", "0"));
			RWHO_FORCE_INC = Integer.parseInt(serverSettings.getProperty("RemoteWhoForceInc", "0"));

			LAME = Boolean.parseBoolean(serverSettings.getProperty("Lame", "False"));
			USER_NAME = serverSettings.getProperty("UserName", "");
			ON_SUCCESS_LOGIN_COMMAND_GS = serverSettings.getProperty("OnSelectServerCommandGS","");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + GAMESERVER + " File.");
		}
	}

	//============================================================
	public static String SERVER_REVISION;
	public static String SERVER_BUILD_DATE;
	public static String SERVER_VERSION;

	//============================================================
	public static void loadServerVersionConfig()
	{
		final String SV = FService.SERVER_VERSION_FILE;

		_log.info("Loading: " + SV + ".");
		try
		{
			Properties serverVersion = new Properties();
			InputStream is = new FileInputStream(new File(SV));
			serverVersion.load(is);
			is.close();
			SERVER_REVISION = serverVersion.getProperty("revision", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = serverVersion.getProperty("builddate", "Undefined Date.");
			SERVER_VERSION = serverVersion.getProperty("revision", "null");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SV + " File.");
		}
	}

	//============================================================
	public static String DATAPACK_VERSION;
	public static String DATAPACK_BUILD_DATE;

	//============================================================
	public static void loadDPVersionConfig()
	{
		final String DP = FService.DATAPACK_VERSION_FILE;

		_log.info("Loading: " + DP + ".");
		try
		{
			Properties dpVersion = new Properties();
			InputStream is = new FileInputStream(new File(DP));
			dpVersion.load(is);
			is.close();
			DATAPACK_VERSION = dpVersion.getProperty("version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = dpVersion.getProperty("builddate", "Undefined Date.");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + DP + " File.");
		}
	}

	//============================================================
	public static boolean IS_TELNET_ENABLED;

	//============================================================
	public static void loadTellConfig()
	{
		final String TELL_NET = FService.TELNET_FILE;

		_log.info("Loading: " + TELL_NET + ".");
		try
		{
			Properties telnetSettings = new Properties();
			InputStream is = new FileInputStream(new File(TELL_NET));
			telnetSettings.load(is);
			is.close();

			IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + TELL_NET + " File.");
		}
	}

	//============================================================
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;

	//============================================================
	public static void loadIdFactoryConfig()
	{
		final String ID = FService.ID_CONFIG_FILE;

		_log.info("Loading: " + ID + ".");
		try
		{
			Properties idSettings = new Properties();
			InputStream is = new FileInputStream(new File(ID));
			idSettings.load(is);
			is.close();

			MAP_TYPE = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
			SET_TYPE = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
			IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
			BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ID + " File.");
		}
	}

	//============================================================
	public static int MAX_ITEM_IN_PACKET;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static int WYVERN_SPEED;
	public static int STRIDER_SPEED;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static String NONDROPPABLE_ITEMS;
	public static FastList<Integer> LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	public static String PET_RENT_NPC;
	public static FastList<Integer> LIST_PET_RENT_NPC = new FastList<Integer>();
	public static boolean EFFECT_CANCELING;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_P_DEFENCE_MULTIPLIER;
	public static double RAID_M_DEFENCE_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static int STARTING_ADENA;
	public static int STARTING_AA;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int UNSTUCK_INTERVAL;
	public static int DEATH_PENALTY_CHANCE;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
        public static boolean ENABLE_POTION_SKILL_ATTACH;
	public static TIntIntHashMap SKILL_DURATION_LIST;
        public static TIntIntHashMap POTION_SKILL_ATTACH;
	/** Chat Filter **/
	public static int CHAT_FILTER_PUNISHMENT_PARAM1;
	public static int CHAT_FILTER_PUNISHMENT_PARAM2;
	public static int CHAT_FILTER_PUNISHMENT_PARAM3;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static String CHAT_FILTER_PUNISHMENT;
	public static ArrayList<String> FILTER_LIST = new ArrayList<String>();
        
        public static boolean USE_TRADE_WORDS_FILTER;
        public static String TRADE_WORD_FILTER_TEXT;
        public static ArrayList<String> FILTER_TRADE_LIST = new ArrayList<String>();

	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static boolean ALLOW_QUAKE_SYSTEM;

	public static int CHRISTMAS_TREE_LIVE_TIME;
	public static boolean CHRISTMAS_TREE_PRESENTS;
	public static boolean NOT_DESTROY_ARROW;
	public static boolean SHOW_ONLINE_IN_COMMUNITY;
	public static boolean CANNOT_HEAL_RBGB;
        public static boolean CANNOT_BUFF_RBGB;
        public static boolean ANTI_HEAVY_SYSTEM;
        public static boolean MOUNT_PROHIBIT;
	public static int CHRISTMAS_TREE_PRESENTS_TIME;
	public static TIntIntHashMap CHRISTMAS_PRESENTS_LIST;

	//============================================================
	public static void loadOtherConfig()
	{
		final String OTHER = FService.OTHER_CONFIG_FILE;

		_log.info("Loading: " + OTHER + ".");
		try
		{
			Properties otherSettings = new Properties();
			InputStream is = new FileInputStream(new File(OTHER));
			otherSettings.load(is);
			is.close();
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
			ALLOW_GUARDS = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False"));
                        ATTACK_AGRESSIVE_MOBS = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "True"));
			EFFECT_CANCELING = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
			WYVERN_SPEED = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));
			STRIDER_SPEED = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
			ALLOW_WYVERN_UPGRADER = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False"));

			/* Inventory slots limits */
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));

			/* Inventory slots limits */
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
			FREIGHT_SLOTS = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));

			GM_OVER_ENCHANT = Integer.parseInt(otherSettings.getProperty("GMOverEnchant", "0"));

			/* if different from 100 (ie 100%) heal rate is modified acordingly */
			HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100")) / 100;
			MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100")) / 100;
			CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100")) / 100;

			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
			RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidPhysicalDefenceMultiplier", "100")) / 100;
			RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMagicalDefenceMultiplier", "100")) / 100;
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));

			STARTING_ADENA = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
			STARTING_AA = Integer.parseInt(otherSettings.getProperty("StartingAncientAdena", "0"));
			UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

			/* Player protection after teleport or login */
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));

			/* Player protection after recovering from fake death (works against mobs only) */
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));

			/* Defines some Party XP related values */
			PARTY_XP_CUTOFF_METHOD = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));

			/* Amount of HP, MP, and CP is restored */
			RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;

			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));

			/* Maximum number of available slots for pvt stores */
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));

			STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));

			PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "30827");
			LIST_PET_RENT_NPC = new FastList<Integer>();
			for(String id : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			}
			NONDROPPABLE_ITEMS = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");

			LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
			for(String id : NONDROPPABLE_ITEMS.split(","))
			{
				LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}

			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
			PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));
			JAIL_IS_PVP = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True"));
			JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));
			DEATH_PENALTY_CHANCE = Integer.parseInt(otherSettings.getProperty("DeathPenaltyChance", "20"));
			//////////////
			ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(otherSettings.getProperty("EnableModifySkillDuration", "false"));
			if(ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST = new TIntIntHashMap();

				String[] propertySplit;
				propertySplit = otherSettings.getProperty("SkillDurationList", "").split(";");

				for(String skill : propertySplit)
				{
					String[] skillSplit = skill.split(",");
					if(skillSplit.length != 2)
					{
						_log.info("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!skill.equals(""))
							{
								_log.info("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}

                        ENABLE_POTION_SKILL_ATTACH = Boolean.parseBoolean(otherSettings.getProperty("EnablePotionSkillAttach", "false"));
                        if(ENABLE_POTION_SKILL_ATTACH) {
                            POTION_SKILL_ATTACH = new TIntIntHashMap();
                            String[] spliter;
                            spliter = otherSettings.getProperty("PotionSkillAttach", "").split(";");

                                for(String spliter2 : spliter) {
                                    String[] potionskill = spliter2.split(",");
                                    if(potionskill.length != 2) {
                                        _log.info("[PotionSkillAttach]: bad protecties -> \"" + spliter2 + "\"");
                                    } else {
                                        try {
                                            POTION_SKILL_ATTACH.put(Integer.parseInt(potionskill[0]), Integer.parseInt(potionskill[1]));
                                        } catch(NumberFormatException e) {
                                            if(!spliter2.equals("")) {
                                                _log.info("[PotionSkillAttachException]: invalid config props -> \"" + potionskill[0] + "\"" + potionskill[1]);
                                            }
                                        }
                                    }

                                }
                        }

			USE_SAY_FILTER = Boolean.parseBoolean(otherSettings.getProperty("UseChatFilter", "false"));
			CHAT_FILTER_CHARS = otherSettings.getProperty("ChatFilterChars", "[I love Scoria]");
			CHAT_FILTER_PUNISHMENT = otherSettings.getProperty("ChatFilterPunishment", "off");
			CHAT_FILTER_PUNISHMENT_PARAM1 = Integer.parseInt(otherSettings.getProperty("ChatFilterPunishmentParam1", "1"));
			CHAT_FILTER_PUNISHMENT_PARAM2 = Integer.parseInt(otherSettings.getProperty("ChatFilterPunishmentParam2", "1000"));
                        
                        USE_TRADE_WORDS_FILTER = Boolean.parseBoolean(otherSettings.getProperty("UseTradeWordsFilter", "false"));
                        TRADE_WORD_FILTER_TEXT = otherSettings.getProperty("TradeBadWordPunishment", "I`m just a spamer");

			FS_TIME_ATTACK = Integer.parseInt(otherSettings.getProperty("TimeOfAttack", "50"));
			FS_TIME_COOLDOWN = Integer.parseInt(otherSettings.getProperty("TimeOfCoolDown", "5"));
			FS_TIME_ENTRY = Integer.parseInt(otherSettings.getProperty("TimeOfEntry", "3"));
			FS_TIME_WARMUP = Integer.parseInt(otherSettings.getProperty("TimeOfWarmUp", "2"));
			FS_PARTY_MEMBER_COUNT = Integer.parseInt(otherSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));

			if(FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if(FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if(FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if(FS_TIME_WARMUP <= 0)
			{
				FS_TIME_WARMUP = 2;
			}
			if(FS_PARTY_MEMBER_COUNT <= 0)
			{
				FS_PARTY_MEMBER_COUNT = 4;
			}

			ALLOW_QUAKE_SYSTEM = Boolean.parseBoolean(otherSettings.getProperty("AllowQuakeSystem", "False"));
			
			CHRISTMAS_TREE_LIVE_TIME = Integer.parseInt(otherSettings.getProperty("ChristmasTreeTime", "600")) * 1000;
			CHRISTMAS_TREE_PRESENTS = Boolean.parseBoolean(otherSettings.getProperty("AllowChristmasPresents", "false"));
			CHRISTMAS_TREE_PRESENTS_TIME = Integer.parseInt(otherSettings.getProperty("ChristmassPresentsTime", "100"));
			NOT_DESTROY_ARROW = Boolean.parseBoolean(otherSettings.getProperty("NotDestroyArrow", "false"));
			SHOW_ONLINE_IN_COMMUNITY = Boolean.parseBoolean(otherSettings.getProperty("ComunityShowOnline", "true"));
			CANNOT_HEAL_RBGB = Boolean.parseBoolean(otherSettings.getProperty("NotHealRbGb", "false"));
                        CANNOT_BUFF_RBGB = Boolean.parseBoolean(otherSettings.getProperty("NotBuffRbGb", "false"));
                        ANTI_HEAVY_SYSTEM = Boolean.parseBoolean(otherSettings.getProperty("AntiHeavySystem", "false"));
                        MOUNT_PROHIBIT = Boolean.parseBoolean(otherSettings.getProperty("AllowUseItemOnMount", "false"));
			if(CHRISTMAS_TREE_PRESENTS)
			{
				CHRISTMAS_PRESENTS_LIST = new TIntIntHashMap();

				String[] propertySplit;
				propertySplit = otherSettings.getProperty("ChristmassPresents", "").split(";");

				for(String present : propertySplit)
				{
					String[] presentSplit = present.split(",");
					if(presentSplit.length != 2)
					{
						_log.info("[ChristmassPresents]: invalid config property -> ChristmassPresents \"" + present + "\"");
					}
					else
					{
						try
						{
							CHRISTMAS_PRESENTS_LIST.put(Integer.parseInt(presentSplit[0]), Integer.parseInt(presentSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!present.equals(""))
							{
								_log.info("[ChristmassPresents]: invalid config property -> SkillList \"" + presentSplit[0] + "\"" + presentSplit[1]);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OTHER + " File.");
		}
	}

	//============================================================
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SEAL_STONES;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_DROP_QUEST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	/** RB rate **/
	public static float ADENA_BOSS;
	public static float ADENA_RAID;
	public static float ADENA_MINON;
	public static float ITEMS_BOSS;
	public static float ITEMS_RAID;
	public static float ITEMS_MINON;
	public static float SPOIL_BOSS;
	public static float SPOIL_RAID;
	public static float SPOIL_MINON;

	//============================================================
	public static void loadRatesConfig()
	{
		final String RATES = FService.RATES_CONFIG_FILE;

		_log.info("Loading: " + RATES + ".");
		try
		{
			Properties ratesSettings = new Properties();
			InputStream is = new FileInputStream(new File(RATES));
			ratesSettings.load(is);
			is.close();

			RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1.00"));
			RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1.00"));
			RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1.00"));
			RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1.00"));
			RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1.00"));
			RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1.00"));
			RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1.00"));
			RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1.00"));
			RATE_DROP_SEAL_STONES = Float.parseFloat(ratesSettings.getProperty("RateDropSealStones", "1.00"));
			RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1.00"));
			RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1.00"));
			RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1.00"));
			RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1.00"));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1.00"));
			RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15.00"));
			RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10.00"));
			RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4.00"));
			RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.80")) * 10;
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.20")) * 10;

			PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
			PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));

			PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1.00"));
			PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
			SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1.00"));

			KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));

			/** RB rate **/
			ADENA_BOSS = Float.parseFloat(ratesSettings.getProperty("AdenaBoss", "1.00"));
			ADENA_RAID = Float.parseFloat(ratesSettings.getProperty("AdenaRaid", "1.00"));
			ADENA_MINON = Float.parseFloat(ratesSettings.getProperty("AdenaMinon", "1.00"));
			ITEMS_BOSS = Float.parseFloat(ratesSettings.getProperty("ItemsBoss", "1.00"));
			ITEMS_RAID = Float.parseFloat(ratesSettings.getProperty("ItemsRaid", "1.00"));
			ITEMS_MINON = Float.parseFloat(ratesSettings.getProperty("ItemsMinon", "1.00"));
			SPOIL_BOSS = Float.parseFloat(ratesSettings.getProperty("SpoilBoss", "1.00"));
			SPOIL_RAID = Float.parseFloat(ratesSettings.getProperty("SpoilRaid", "1.00"));
			SPOIL_MINON = Float.parseFloat(ratesSettings.getProperty("SpoilMinon", "1.00"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + RATES + " File.");
		}
	}

	//============================================================
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_BOSS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static double ALT_WEIGHT_LIMIT;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_TIREDNESS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALT_GAME_MOB_ATTACK_AI;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static float ALT_GAME_SKILL_HIT_RATE;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static boolean ALT_RECOMMEND;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static int ALT_MAX_SUBCLASS_COUNT;
	public static int ALT_SUBCLASS_LVL;
	public static boolean ALT_GAME_VIEWNPC;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean ALT_PRIVILEGES_SECURE_CHECK;
	public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
        public static int ALT_LOTTERY_CONSUME_ITEM_ID;
        public static int ALT_LOTTERY_PRISE_ITEM_ID;
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean ALLOW_GUARDS;
	public static boolean ATTACK_AGRESSIVE_MOBS;
	public static boolean ALLOW_CLASS_MASTERS;
	public static boolean CLASS_MASTER_STRIDER_UPDATE;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte DEBUFFS_MAX_AMOUNT;
	public static boolean DONT_STOP_BUFFS;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALLOW_REMOTE_CLASS_MASTERS;
	public static boolean DONT_DESTROY_SS;
	public static boolean SHOW_CASTLE_LORD_LOGIN;
	public static boolean SHOW_HERO_LOGIN;
	public static int MAX_LEVEL_NEWBIE;
	public static int ALT_RECOMMENDATIONS_NUMBER;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int ALT_PRIVATE_STORE_DISTANCE;
	public static boolean ALT_MINION_REMOVE;
	public static boolean DISABLE_SKILLS_ON_LEVEL_LOST;
	public static int DISABLE_SKILLS_LEVEL_DIF;
	public static boolean ALT_ALLOW_ATTACK_NPC;
	public static boolean ALT_CAN_TRADE_AUGMENT, ALT_CAN_DROP_AUGMENT;

	//============================================================
	public static void loadAltConfig()
	{
		final String ALT = FService.ALT_SETTINGS_FILE;

		_log.info("Loading: " + ALT + ".");
		try
		{
			Properties altSettings = new Properties();
			InputStream is = new FileInputStream(new File(ALT));
			altSettings.load(is);
			is.close();
			/* General Information */
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
			ALT_WEIGHT_LIMIT = Double.parseDouble(altSettings.getProperty("AltWeightLimit", "1"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
			AUTO_LEARN_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "false"));
			DISABLE_SKILLS_ON_LEVEL_LOST = Boolean.parseBoolean(altSettings.getProperty("AltDisableSkillsOnLevelLost", "true"));
			DISABLE_SKILLS_LEVEL_DIF = Integer.parseInt(altSettings.getProperty("AltSkillDecreaseDif", "9"));
			ALT_GAME_CANCEL_BOW = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "false"));
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "10"));
			ALT_GAME_DELEVEL = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
			ALT_GAME_MOB_ATTACK_AI = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "false"));
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "true"));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
			DONT_STOP_BUFFS = Boolean.parseBoolean(altSettings.getProperty("DontStopBuffs", "false"));
			AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(altSettings.getProperty("AutoLearnDivineInspiration", "false"));
			DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("DivineInspirationSpBookNeeded", "true"));
			ALLOW_CLASS_MASTERS = Boolean.valueOf(altSettings.getProperty("AllowClassMasters", "False"));
			CLASS_MASTER_STRIDER_UPDATE = Boolean.valueOf(altSettings.getProperty("AllowClassMastersStriderUpdate", "False"));
			CLASS_MASTER_SETTINGS = new ClassMasterSettings(altSettings.getProperty("ConfigClassMaster"));
			ALLOW_REMOTE_CLASS_MASTERS = Boolean.valueOf(altSettings.getProperty("AllowRemoteClassMasters", "False"));
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
			ALT_PARTY_RANGE = Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
			ALT_PARTY_RANGE2 = Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
			REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("RemoveCastleCirclets", "true"));
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(altSettings.getProperty("LifeCrystalNeeded", "true"));
			SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("EnchantSkillSpBookNeeded", "true"));
			AUTO_LOOT = altSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
			AUTO_LOOT_BOSS = altSettings.getProperty("AutoLootBoss").equalsIgnoreCase("True");
			LOOT_RAIDS_PRIVILEGE_INTERVAL = Integer.parseInt(altSettings.getProperty("RaidLootRightsInterval", "900")) * 1000;
			LOOT_RAIDS_PRIVILEGE_CC_SIZE = Integer.parseInt(altSettings.getProperty("RaidLootRightsCCSize", "45"));
			AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs").equalsIgnoreCase("True");
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
			ALT_RECOMMEND = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
			ALT_MAX_SUBCLASS_COUNT = Integer.parseInt(altSettings.getProperty("AltMaxSubclassCount", "3"));
			ALT_SUBCLASS_LVL = Integer.parseInt(altSettings.getProperty("AltSubclassLvl", "40"));
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(altSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(altSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(altSettings.getProperty("AltMaxNumOfClansInAlly", "3"));

			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(altSettings.getProperty("AltClanMembersForWar", "15"));
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAClan", "5"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateAClan", "10"));
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(altSettings.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));

			ALT_MANOR_REFRESH_TIME = Integer.parseInt(altSettings.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(altSettings.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(altSettings.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(altSettings.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(altSettings.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(altSettings.getProperty("AltManorSaveAllActions", "false"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(altSettings.getProperty("AltManorSavePeriodRate", "2"));
                        
                        ALT_LOTTERY_CONSUME_ITEM_ID = Integer.parseInt(altSettings.getProperty("AltLotteryConsumeItemId", "57"));
                        ALT_LOTTERY_PRISE_ITEM_ID = Integer.parseInt(altSettings.getProperty("AltLotteryWinItemId", "57"));
			ALT_LOTTERY_PRIZE = Integer.parseInt(altSettings.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize", "200"));
			BUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("MaxBuffAmount", "24"));
			DEBUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("MaxDebuffAmount", "6"));

			// Dimensional Rift Config
			RIFT_MIN_PARTY_SIZE = Integer.parseInt(altSettings.getProperty("RiftMinPartySize", "5"));
			RIFT_MAX_JUMPS = Integer.parseInt(altSettings.getProperty("MaxRiftJumps", "4"));
			RIFT_SPAWN_DELAY = Integer.parseInt(altSettings.getProperty("RiftSpawnDelay", "10000"));
			RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMin", "480"));
			RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMax", "600"));
			RIFT_ENTER_COST_RECRUIT = Integer.parseInt(altSettings.getProperty("RecruitCost", "18"));
			RIFT_ENTER_COST_SOLDIER = Integer.parseInt(altSettings.getProperty("SoldierCost", "21"));
			RIFT_ENTER_COST_OFFICER = Integer.parseInt(altSettings.getProperty("OfficerCost", "24"));
			RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(altSettings.getProperty("CaptainCost", "27"));
			RIFT_ENTER_COST_COMMANDER = Integer.parseInt(altSettings.getProperty("CommanderCost", "30"));
			RIFT_ENTER_COST_HERO = Integer.parseInt(altSettings.getProperty("HeroCost", "33"));
			RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(altSettings.getProperty("BossRoomTimeMultiply", "1.5"));

			//destroy ss
			DONT_DESTROY_SS = Boolean.parseBoolean(altSettings.getProperty("DontDestroySS", "false"));

			SHOW_CASTLE_LORD_LOGIN = Boolean.parseBoolean(altSettings.getProperty("ShowCastleLordLgin", "false"));
			SHOW_HERO_LOGIN = Boolean.parseBoolean(altSettings.getProperty("ShowHeroLogin", "false"));

			//max level newbie
			MAX_LEVEL_NEWBIE = Integer.parseInt(altSettings.getProperty("MaxLevelNewbie", "20"));

			ALT_RECOMMENDATIONS_NUMBER = Integer.parseInt(altSettings.getProperty("AltMaxRecommendationNumber", "255"));

			RAID_RANKING_1ST = Integer.parseInt(altSettings.getProperty("1stRaidRankingPoints", "1250"));
			RAID_RANKING_2ND = Integer.parseInt(altSettings.getProperty("2ndRaidRankingPoints", "900"));
			RAID_RANKING_3RD = Integer.parseInt(altSettings.getProperty("3rdRaidRankingPoints", "700"));
			RAID_RANKING_4TH = Integer.parseInt(altSettings.getProperty("4thRaidRankingPoints", "600"));
			RAID_RANKING_5TH = Integer.parseInt(altSettings.getProperty("5thRaidRankingPoints", "450"));
			RAID_RANKING_6TH = Integer.parseInt(altSettings.getProperty("6thRaidRankingPoints", "350"));
			RAID_RANKING_7TH = Integer.parseInt(altSettings.getProperty("7thRaidRankingPoints", "300"));
			RAID_RANKING_8TH = Integer.parseInt(altSettings.getProperty("8thRaidRankingPoints", "200"));
			RAID_RANKING_9TH = Integer.parseInt(altSettings.getProperty("9thRaidRankingPoints", "150"));
			RAID_RANKING_10TH = Integer.parseInt(altSettings.getProperty("10thRaidRankingPoints", "100"));
			RAID_RANKING_UP_TO_50TH = Integer.parseInt(altSettings.getProperty("UpTo50thRaidRankingPoints", "25"));
			RAID_RANKING_UP_TO_100TH = Integer.parseInt(altSettings.getProperty("UpTo100thRaidRankingPoints", "12"));

			ALT_PRIVATE_STORE_DISTANCE = Integer.parseInt(altSettings.getProperty("AltPrivateStoreDist", "10"));
			ALT_MINION_REMOVE = Boolean.parseBoolean(altSettings.getProperty("AltMinionRemove", "true"));
			ALT_ALLOW_ATTACK_NPC = Boolean.parseBoolean(altSettings.getProperty("AltAllowAttackNPC", "true"));
			ALT_CAN_TRADE_AUGMENT = Boolean.parseBoolean(altSettings.getProperty("AltCanTradeAugment", "false"));
			ALT_CAN_DROP_AUGMENT = Boolean.parseBoolean(altSettings.getProperty("AltCanDropAugment", "false"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ALT + " File.");
		}
	}

	//============================================================
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_REQUIRE_WIN_7S;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;

	//============================================================
	public static void load7sConfig()
	{
		final String SEVENSIGNS = FService.SEVENSIGNS_FILE;

		_log.info("Loading: " + SEVENSIGNS + ".");
		try
		{
			Properties SevenSettings = new Properties();
			InputStream is = new FileInputStream(new File(SEVENSIGNS));
			SevenSettings.load(is);
			is.close();

			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
			ALT_REQUIRE_WIN_7S = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireWin7s", "True"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SEVENSIGNS + " File.");
		}
	}

	//============================================================
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;

	//============================================================
	public static void loadCHConfig()
	{
		final String CLANHALL = FService.CLANHALL_CONFIG_FILE;

		_log.info("Loading: " + CLANHALL + ".");
		try
		{
			Properties clanhallSettings = new Properties();
			InputStream is = new FileInputStream(new File(CLANHALL));
			clanhallSettings.load(is);
			is.close();
			CH_TELE_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
			CH_TELE1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
			CH_TELE2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
			CH_SUPPORT_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
			CH_SUPPORT1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
			CH_SUPPORT2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
			CH_SUPPORT3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
			CH_SUPPORT4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
			CH_SUPPORT5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
			CH_SUPPORT6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
			CH_SUPPORT7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
			CH_SUPPORT8_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
			CH_MPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
			CH_MPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
			CH_MPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
			CH_MPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
			CH_MPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
			CH_MPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
			CH_HPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
			CH_HPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
			CH_HPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
			CH_HPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
			CH_HPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
			CH_HPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
			CH_HPREG6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
			CH_HPREG7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
			CH_HPREG8_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
			CH_HPREG9_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
			CH_HPREG10_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
			CH_HPREG11_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
			CH_HPREG12_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
			CH_HPREG13_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
			CH_EXPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
			CH_EXPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
			CH_EXPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
			CH_EXPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
			CH_EXPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
			CH_EXPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
			CH_EXPREG6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
			CH_EXPREG7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
			CH_ITEM_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
			CH_ITEM1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
			CH_ITEM2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
			CH_ITEM3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
			CH_CURTAIN_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
			CH_CURTAIN1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
			CH_CURTAIN2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
			CH_FRONT_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
			CH_FRONT1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
			CH_FRONT2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CLANHALL + " File.");
		}
	}

	//============================================================
	public static int DEVASTATED_DAY;
	public static int DEVASTATED_HOUR;
	public static int DEVASTATED_MINUTES;
	public static int PARTISAN_DAY;
	public static int PARTISAN_HOUR;
	public static int PARTISAN_MINUTES;

	//============================================================
	public static void loadElitCHConfig()
	{
		final String ELIT_CH = FService.ELIT_CLANHALL_CONFIG_FILE;

		_log.info("Loading: " + ELIT_CH + ".");
		try
		{
			Properties elitchSettings = new Properties();
			InputStream is = new FileInputStream(new File(ELIT_CH));
			elitchSettings.load(is);
			is.close();

			DEVASTATED_DAY = Integer.valueOf(elitchSettings.getProperty("DevastatedDay", "1"));
			DEVASTATED_HOUR = Integer.valueOf(elitchSettings.getProperty("DevastatedHour", "18"));
			DEVASTATED_MINUTES = Integer.valueOf(elitchSettings.getProperty("DevastatedMinutes", "0"));
			PARTISAN_DAY = Integer.valueOf(elitchSettings.getProperty("PartisanDay", "5"));
			PARTISAN_HOUR = Integer.valueOf(elitchSettings.getProperty("PartisanHour", "21"));
			PARTISAN_MINUTES = Integer.valueOf(elitchSettings.getProperty("PartisanMinutes", "0"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ELIT_CH + " File.");
		}
	}

	//============================================================
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static int L2JMOD_CHAMPION_MAX_LVL_DIFF;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static int L2JMOD_CHAMPION_REWARDS;
	public static int L2JMOD_CHAMPION_ADENAS_REWARDS;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static String L2JMOD_CHAMP_TITLE;

	//============================================================
	public static void loadChampionConfig()
	{
		final String EVENT_CHAMPION = FService.EVENT_CHAMPION_FILE;

		_log.info("Loading: " + EVENT_CHAMPION + ".");
		try
		{
			Properties ChampionSettings = new Properties();
			InputStream is = new FileInputStream(new File(EVENT_CHAMPION));
			ChampionSettings.load(is);
			is.close();

			L2JMOD_CHAMPION_ENABLE = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionEnable", "false"));
			L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(ChampionSettings.getProperty("ChampionFrequency", "0"));
			L2JMOD_CHAMPION_MAX_LVL_DIFF = Integer.parseInt(ChampionSettings.getProperty("ChampionMaxLvlDiff", "9"));
			L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMinLevel", "20"));
			L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMaxLevel", "60"));
			L2JMOD_CHAMPION_HP = Integer.parseInt(ChampionSettings.getProperty("ChampionHp", "7"));
			L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(ChampionSettings.getProperty("ChampionHpRegen", "1.0"));
			L2JMOD_CHAMPION_REWARDS = Integer.parseInt(ChampionSettings.getProperty("ChampionRewards", "8"));
			L2JMOD_CHAMPION_ADENAS_REWARDS = Integer.parseInt(ChampionSettings.getProperty("ChampionAdenasRewards", "1"));
			L2JMOD_CHAMPION_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionAtk", "1.0"));
			L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionSpdAtk", "1.0"));
			L2JMOD_CHAMPION_REWARD = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItem", "0"));
			L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemID", "6393"));
			L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemQty", "1"));
			L2JMOD_CHAMP_TITLE = ChampionSettings.getProperty("ChampionTitle", "Champion");

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_CHAMPION + " File.");
		}
	}

	//============================================================
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static int L2JMOD_WEDDING_NAME_COLOR_NORMAL;
	public static int L2JMOD_WEDDING_NAME_COLOR_GEY;
	public static int L2JMOD_WEDDING_NAME_COLOR_LESBO;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	/** cupidon bow giving **/
	public static boolean WEDDING_GIVE_CUPID_BOW;
	/** announce wedding **/
	public static boolean ANNOUNCE_WEDDING;

	//============================================================
	public static void loadWeddingConfig()
	{
		final String EVENT_WEDDING = FService.EVENT_WEDDING_FILE;

		_log.info("Loading: " + EVENT_WEDDING + ".");
		try
		{
			Properties WeddingSettings = new Properties();
			InputStream is = new FileInputStream(new File(EVENT_WEDDING));
			WeddingSettings.load(is);
			is.close();

			L2JMOD_ALLOW_WEDDING = Boolean.valueOf(WeddingSettings.getProperty("AllowWedding", "False"));
			L2JMOD_WEDDING_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingPrice", "250000000"));
			L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingPunishInfidelity", "True"));
			L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingTeleport", "True"));
			L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportPrice", "50000"));
			L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportDuration", "60"));
			L2JMOD_WEDDING_NAME_COLOR_NORMAL = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorN", "FFFFFF"));
			L2JMOD_WEDDING_NAME_COLOR_GEY = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorB", "FFFFFF"));
			L2JMOD_WEDDING_NAME_COLOR_LESBO = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorL", "FFFFFF"));
			L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingAllowSameSex", "False"));
			L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingFormalWear", "True"));
			L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(WeddingSettings.getProperty("WeddingDivorceCosts", "20"));
			WEDDING_GIVE_CUPID_BOW = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingGiveBow", "False"));
			ANNOUNCE_WEDDING = Boolean.parseBoolean(WeddingSettings.getProperty("AnnounceWedding", "True"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_WEDDING + " File.");
		}
	}

	//============================================================
	public static int REBIRTH_MAGE_SKILL1_ID;
	public static int REBIRTH_MAGE_SKILL1_LEVEL;
	public static int REBIRTH_MAGE_SKILL2_ID;
	public static int REBIRTH_MAGE_SKILL2_LEVEL;
	public static int REBIRTH_MAGE_SKILL3_ID;
	public static int REBIRTH_MAGE_SKILL3_LEVEL;
	public static int REBIRTH_FIGHTER_SKILL1_ID;
	public static int REBIRTH_FIGHTER_SKILL1_LEVEL;
	public static int REBIRTH_FIGHTER_SKILL2_ID;
	public static int REBIRTH_FIGHTER_SKILL2_LEVEL;
	public static int REBIRTH_FIGHTER_SKILL3_ID;
	public static int REBIRTH_FIGHTER_SKILL3_LEVEL;
	public static int REBIRTH_MIN_LEVEL;
	public static int REBIRTH_ITEM1_NEEDED;
	public static int REBIRTH_ITEM1_AMOUNT;
	public static int REBIRTH_ITEM2_NEEDED;
	public static int REBIRTH_ITEM2_AMOUNT;
	public static int REBIRTH_ITEM3_NEEDED;
	public static int REBIRTH_ITEM3_AMOUNT;

	//============================================================
	public static void loadREBIRTHConfig()
	{
		final String EVENT_REBIRTH = FService.EVENT_REBIRTH_FILE;

		_log.info("Loading: " + EVENT_REBIRTH + ".");
		try
		{
			Properties REBIRTHSettings = new Properties();
			InputStream is = new FileInputStream(new File(EVENT_REBIRTH));
			REBIRTHSettings.load(is);
			is.close();

			REBIRTH_MIN_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MIN_LEVEL", "80"));
			REBIRTH_ITEM1_NEEDED = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM1_NEEDED", "0"));
			REBIRTH_ITEM1_AMOUNT = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM1_AMOUNT", "0"));
			REBIRTH_ITEM2_NEEDED = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM2_NEEDED", "0"));
			REBIRTH_ITEM2_AMOUNT = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM2_AMOUNT", "0"));
			REBIRTH_ITEM3_NEEDED = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM3_NEEDED", "0"));
			REBIRTH_ITEM3_AMOUNT = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_ITEM3_AMOUNT", "0"));

			REBIRTH_MAGE_SKILL1_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL1_ID", "0"));
			REBIRTH_MAGE_SKILL1_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL1_LEVEL", "0"));
			REBIRTH_MAGE_SKILL2_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL2_ID", "0"));
			REBIRTH_MAGE_SKILL2_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL2_LEVEL", "0"));
			REBIRTH_MAGE_SKILL3_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL3_ID", "0"));
			REBIRTH_MAGE_SKILL3_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL3_LEVEL", "0"));
			REBIRTH_FIGHTER_SKILL1_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL1_ID", "0"));
			REBIRTH_FIGHTER_SKILL1_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL1_LEVEL", "0"));
			REBIRTH_FIGHTER_SKILL2_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL2_ID", "0"));
			REBIRTH_FIGHTER_SKILL2_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL2_LEVEL", "0"));
			REBIRTH_FIGHTER_SKILL3_ID = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL3_ID", "0"));
			REBIRTH_FIGHTER_SKILL3_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL3_LEVEL", "0"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_REBIRTH + " File.");
		}
	}

	//============================================================
	public static boolean PCB_ENABLE;
        public static boolean PCB_WINDOW_ONLINE;
	public static int PCB_MIN_LEVEL;
	public static int PCB_POINT_MIN;
	public static int PCB_POINT_MAX;
	public static int PCB_CHANCE_DUAL_POINT;
	public static int PCB_INTERVAL;
    public static int PCB_LIKE_WINDOW_ONLINE_RATE;
    public static int PCB_LIKE_WINDOW_INCREASE_RATE; 
    
	//============================================================
	public static void loadPCBPointConfig()
	{
		final String PCB_POINT = FService.EVENT_PC_BANG_POINT_FILE;

		_log.info("Loading: " + PCB_POINT + ".");
		try
		{
			Properties pcbpSettings = new Properties();
			InputStream is = new FileInputStream(new File(PCB_POINT));
			pcbpSettings.load(is);
			is.close();

			PCB_ENABLE = Boolean.parseBoolean(pcbpSettings.getProperty("PcBangPointEnable", "true"));
			PCB_MIN_LEVEL = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMinLevel", "20"));
			PCB_POINT_MIN = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMinCount", "20"));
			PCB_POINT_MAX = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMaxCount", "1000000"));

			if(PCB_POINT_MAX < 1)
			{
				PCB_POINT_MAX = Integer.MAX_VALUE;
			}

			PCB_CHANCE_DUAL_POINT = Integer.parseInt(pcbpSettings.getProperty("PcBangPointDualChance", "20"));
			PCB_INTERVAL = Integer.parseInt(pcbpSettings.getProperty("PcBangPointTimeStamp", "900"));
			PCB_WINDOW_ONLINE = Boolean.parseBoolean(pcbpSettings.getProperty("PcBangShowOnlineForm", "false"));
			PCB_LIKE_WINDOW_ONLINE_RATE = Integer.parseInt(pcbpSettings.getProperty("PcBangShowOnlineFormInterval", "300"));
			PCB_LIKE_WINDOW_INCREASE_RATE = Integer.parseInt(pcbpSettings.getProperty("PcBangShiwOnlineIncriment", "10"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PCB_POINT + " File.");
		}

	}

	//============================================================
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_NO_SCRIPT;
	public static boolean ALT_DEV_NO_RB;
	public static boolean ALT_DEV_NO_AI;
	public static boolean DEBUG;
	public static boolean ASSERT;
	public static boolean DEVELOPER;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static boolean SERVER_GMONLY;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	public static boolean GMAUDIT;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static String LOG_ITEMS_EXC_PROC;
	public static String LOG_ITEMS_EXC_ITEM;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static int GAMEGUARD_KEY;
	public static int GAMEGUARD_NUM_WINDOW;
	public static boolean GAMEGUARD_ANNOUNCE_ABOUT_HACK;
	public static int GAMEGUARD_CHECK_INTERVAL;
	//thread effects
	public static int THREAD_P_EFFECTS;
	//thread general
	public static int THREAD_P_GENERAL;
	//thread packet core size
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	//thread io packet core size
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	//thread AI
	public static int AI_MAX_THREAD;
	public static boolean LAZY_CACHE;
	public static boolean ENABLE_CACHE_INFO = false;
	public static boolean ENABLE_DDOS_PROTECTION;

	//============================================================
	public static void loadDevConfig()
	{
		final String DEV = FService.DEVELOPER;

		_log.info("Loading: " + DEV + ".");
		try
		{
			Properties devSettings = new Properties();
			InputStream is = new FileInputStream(new File(DEV));
			devSettings.load(is);
			is.close();

			DEBUG = Boolean.parseBoolean(devSettings.getProperty("Debug", "false"));
			ASSERT = Boolean.parseBoolean(devSettings.getProperty("Assert", "false"));
			DEVELOPER = Boolean.parseBoolean(devSettings.getProperty("Developer", "false"));
			SERVER_LIST_TESTSERVER = Boolean.parseBoolean(devSettings.getProperty("TestServer", "false"));
			SERVER_LIST_BRACKET = Boolean.valueOf(devSettings.getProperty("ServerListBrackets", "false"));
			SERVER_LIST_CLOCK = Boolean.valueOf(devSettings.getProperty("ServerListClock", "false"));
			SERVER_GMONLY = Boolean.valueOf(devSettings.getProperty("ServerGMOnly", "false"));
			ALT_DEV_NO_QUESTS = Boolean.parseBoolean(devSettings.getProperty("AltDevNoQuests", "False"));
			ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(devSettings.getProperty("AltDevNoSpawns", "False"));
			ALT_DEV_NO_SCRIPT = Boolean.parseBoolean(devSettings.getProperty("AltDevNoScript", "False"));
			ALT_DEV_NO_AI = Boolean.parseBoolean(devSettings.getProperty("AltDevNoAI", "False"));
			ALT_DEV_NO_RB = Boolean.parseBoolean(devSettings.getProperty("AltDevNoRB", "False"));

			/////////////////////
			REQUEST_ID = Integer.parseInt(devSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(devSettings.getProperty("AcceptAlternateID", "True"));
			/////////////////////
			CNAME_TEMPLATE = devSettings.getProperty("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = devSettings.getProperty("PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = devSettings.getProperty("ClanNameTemplate", ".*");
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(devSettings.getProperty("CharMaxNumber", "0"));
			/////////////////////
			MAXIMUM_ONLINE_USERS = Integer.parseInt(devSettings.getProperty("MaximumOnlineUsers", "100"));
			/////////////////////
			MIN_PROTOCOL_REVISION = Integer.parseInt(devSettings.getProperty("MinProtocolRevision", "660"));
			MAX_PROTOCOL_REVISION = Integer.parseInt(devSettings.getProperty("MaxProtocolRevision", "665"));
			if(MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
				throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
			/////////////////////
			GMAUDIT = Boolean.valueOf(devSettings.getProperty("GMAudit", "False"));
			LOG_CHAT = Boolean.valueOf(devSettings.getProperty("LogChat", "false"));
			LOG_ITEMS = Boolean.valueOf(devSettings.getProperty("LogItems", "false"));
			LOG_ITEMS_EXC_PROC = devSettings.getProperty("LogItems", "Consume");
			LOG_ITEMS_EXC_ITEM = devSettings.getProperty("LogItems", "Arrow;Shot;Herb");
			/////////////////////
			GAMEGUARD_ENFORCE = Boolean.valueOf(devSettings.getProperty("GameGuardEnforce", "False"));
			GAMEGUARD_PROHIBITACTION = Boolean.valueOf(devSettings.getProperty("GameGuardProhibitAction", "False"));
			GAMEGUARD_KEY = Integer.parseInt(devSettings.getProperty("GameGuardKey", "0"));
			GAMEGUARD_NUM_WINDOW = Integer.parseInt(devSettings.getProperty("GameGuardNumWindowFromSamePC", "0"));
			GAMEGUARD_ANNOUNCE_ABOUT_HACK = Boolean.valueOf(devSettings.getProperty("GameGuardAnnounceAllAboutHack", "true"));
			GAMEGUARD_CHECK_INTERVAL = Integer.parseInt(devSettings.getProperty("GameGuardCheckInterval", "180"));
			/////////////////////
			THREAD_P_EFFECTS = Integer.parseInt(devSettings.getProperty("ThreadPoolSizeEffects", "6"));
			THREAD_P_GENERAL = Integer.parseInt(devSettings.getProperty("ThreadPoolSizeGeneral", "15"));
			GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(devSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
			IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(devSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
			AI_MAX_THREAD = Integer.parseInt(devSettings.getProperty("AiMaxThread", "10"));
			GENERAL_THREAD_CORE_SIZE = Integer.parseInt(devSettings.getProperty("GeneralThreadCoreSize", "4"));
			/////////////////////
			LAZY_CACHE = Boolean.valueOf(devSettings.getProperty("LazyCache", "False"));

			ENABLE_DDOS_PROTECTION = Boolean.valueOf(devSettings.getProperty("EnableDdosProtection", "True"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + DEV + " File.");
		}

	}

	//============================================================
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static boolean ALT_STOP_CRAFT_ON_ADENA_LIMIT;

	//============================================================
	public static void loadCraftConfig()
	{
		final String CRAFT = FService.CRAFTING;

		_log.info("Loading: " + CRAFT + ".");
		try
		{
			Properties craftSettings = new Properties();
			InputStream is = new FileInputStream(new File(CRAFT));
			craftSettings.load(is);
			is.close();

			DWARF_RECIPE_LIMIT = Integer.parseInt(craftSettings.getProperty("DwarfRecipeLimit", "50"));
			COMMON_RECIPE_LIMIT = Integer.parseInt(craftSettings.getProperty("CommonRecipeLimit", "50"));
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(craftSettings.getProperty("CraftingEnabled", "True"));
			ALT_GAME_CREATION = Boolean.parseBoolean(craftSettings.getProperty("AltGameCreation", "False"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(craftSettings.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(craftSettings.getProperty("AltGameCreationRateXp", "1"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(craftSettings.getProperty("AltGameCreationRateSp", "1"));
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(craftSettings.getProperty("AltBlacksmithUseRecipes", "True"));
			ALT_STOP_CRAFT_ON_ADENA_LIMIT = Boolean.parseBoolean(craftSettings.getProperty("AltStopCraftOnAdenaLimit", "True"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CRAFT + " File.");
		}

	}

	//============================================================
	public static boolean SCORIA_ALLOW_AWAY_STATUS;
	public static int SCORIA_AWAY_TIMER;
	public static int SCORIA_BACK_TIMER;
	public static int SCORIA_AWAY_TITLE_COLOR;
	public static boolean SCORIA_AWAY_PLAYER_TAKE_AGGRO;
	public static boolean SCORIA_AWAY_PEACE_ZONE;

	//============================================================
	public static void loadAWAYConfig()
	{
		final String AWAY_SYSTEM = FService.AWAY_FILE;

		_log.info("Loading: " + AWAY_SYSTEM + ".");
		try
		{
			Properties AWAYSettings = new Properties();
			InputStream is = new FileInputStream(new File(AWAY_SYSTEM));
			AWAYSettings.load(is);
			is.close();

			/** Away System **/
			SCORIA_ALLOW_AWAY_STATUS = Boolean.parseBoolean(AWAYSettings.getProperty("AllowAwayStatus", "False"));
			SCORIA_AWAY_PLAYER_TAKE_AGGRO = Boolean.parseBoolean(AWAYSettings.getProperty("AwayPlayerTakeAggro", "False"));
			SCORIA_AWAY_TITLE_COLOR = Integer.decode("0x" + AWAYSettings.getProperty("AwayTitleColor", "0000FF"));
			SCORIA_AWAY_TIMER = Integer.parseInt(AWAYSettings.getProperty("AwayTimer", "30"));
			SCORIA_BACK_TIMER = Integer.parseInt(AWAYSettings.getProperty("BackTimer", "30"));
			SCORIA_AWAY_PEACE_ZONE = Boolean.parseBoolean(AWAYSettings.getProperty("AwayOnlyInPeaceZone", "False"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + AWAY_SYSTEM + " File.");
		}
	}

	//============================================================
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;

	//============================================================
	public static void loadBankingConfig()
	{
		final String BANK = FService.BANK_FILE;

		_log.info("Loading: " + BANK + ".");
		try
		{
			Properties BANKSettings = new Properties();
			InputStream is = new FileInputStream(new File(BANK));
			BANKSettings.load(is);
			is.close();

			BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(BANKSettings.getProperty("BankingEnabled", "false"));
			BANKING_SYSTEM_GOLDBARS = Integer.parseInt(BANKSettings.getProperty("BankingGoldbarCount", "1"));
			BANKING_SYSTEM_ADENA = Integer.parseInt(BANKSettings.getProperty("BankingAdenaCount", "500000000"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + BANK + " File.");
		}

	}

	//============================================================
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;

	public static boolean RESTORE_OFFLINERS;
 	public static int OFFLINE_MAX_DAYS;
 	public static boolean OFFLINE_DISCONNECT_FINISHED;
        public static int OFFLINE_MIN_LEVEL;
	//============================================================
	public static void loadOfflineConfig()
	{
		final String OFFLINE = FService.OFFLINE_FILE;

		_log.info("Loading: " + OFFLINE + ".");
		try
		{
			Properties OfflineSettings = new Properties();
			InputStream is = new FileInputStream(new File(OFFLINE));
			OfflineSettings.load(is);
			is.close();

			OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineTradeEnable", "false"));
			OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineCraftEnable", "false"));
			OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineNameColorEnable", "false"));
			OFFLINE_NAME_COLOR = Integer.decode("0x" + OfflineSettings.getProperty("OfflineNameColor", "ff00ff"));

			RESTORE_OFFLINERS = Boolean.parseBoolean(OfflineSettings.getProperty("RestoreOffliners", "false")); 
			OFFLINE_MAX_DAYS = Integer.parseInt(OfflineSettings.getProperty("OfflineMaxDays", "10")); 
			OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineDisconnectFinished", "true"));
                        OFFLINE_MIN_LEVEL = Integer.parseInt(OfflineSettings.getProperty("OfflineMinLevel", "1"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OFFLINE + " File.");
		}

	}

	//============================================================
	public static boolean ONLINE_PLAYERS_ON_LOGIN;
	public static boolean ONLINE_CLAN_ON_LOGIN;
	public static boolean UPTIME_ON_LOGIN;
	public static boolean SHOW_SERVER_VERSION;
	public static boolean SUBSTUCK_SKILLS;
	public static boolean ALT_Server_Name_Enabled;
	public static boolean ANNOUNCE_TO_ALL_SPAWN_RB;
	public static boolean ANNOUNCE_BAN;
	public static String ALT_Server_Name;
	public static int GM_OVER_ENCHANT;
	public static boolean DONATOR_NAME_COLOR_ENABLED;
        public static boolean DONATOR_PARTY_REWARD;
	public static int DONATOR_NAME_COLOR;
	public static int DONATOR_TITLE_COLOR;
	public static float DONATOR_XPSP_RATE;
	public static float DONATOR_ADENA_RATE;
	public static float DONATOR_DROP_RATE;
	public static float DONATOR_SPOIL_RATE;
	public static boolean ALLOW_GRADE_PENALTY;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE = true;
	public static boolean CUSTOM_ITEM_TABLES = true;
	public static boolean CUSTOM_ARMORSETS_TABLE = true;
	public static boolean CUSTOM_TELEPORT_TABLE = true;
	public static boolean CUSTOM_DROPLIST_TABLE = true;
	public static boolean CUSTOM_MERCHANT_TABLES = true;
	public static boolean ALLOW_STAT_VIEW;
	public static boolean ALLOW_ONLINE_VIEW;
	public static boolean WELCOME_HTM;
	public static String ALLOWED_SKILLS; // List of Skills that are allowed for all Classes if CHECK_SKILLS_ON_ENTER = true
	public static FastList<Integer> ALLOWED_SKILLS_LIST = new FastList<Integer>();
	public static boolean PROTECTOR_PLAYER_PK;
	public static boolean PROTECTOR_PLAYER_PVP;
	public static int PROTECTOR_RADIUS_ACTION;
	public static int PROTECTOR_SKILLID;
	public static int PROTECTOR_SKILLLEVEL;
	public static int PROTECTOR_SKILLTIME;
	public static String PROTECTOR_MESSAGE;
	public static boolean CASTLE_SHIELD; // Alternative gaming - Castle Shield can be equiped by all clan members if they own a castle. - default true
	public static boolean CLANHALL_SHIELD; // Alternative gaming - Clan Hall Shield can be equiped by all clan members if they own a clan hall. - default true
	public static boolean APELLA_ARMORS; // Alternative gaming - Apella armors can be equiped only by clan members if their class is Baron or higher - default true
	public static boolean OATH_ARMORS; // Alternative gaming - Clan Oath Armors can be equiped only by clan members - default true
	public static boolean CASTLE_CROWN; // Alternative gaming - Castle Crown can be equiped only by castle lord - default true
	public static boolean CASTLE_CIRCLETS; // Alternative gaming - Castle Circlets can be equiped only by clan members if they own a castle - default true
	public static boolean KEEP_SUBCLASS_SKILLS;
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static boolean NOBLE_CUSTOM_ITEMS;
	public static boolean HERO_CUSTOM_ITEMS;
	public static boolean ALLOW_CREATE_LVL;
	public static int CHAR_CREATE_LVL;
	public static String ABORT_RR;
	public static boolean SPAWN_CHAR;
	/** X Coordinate of the SPAWN_CHAR setting. */
	public static int SPAWN_X;
	/** Y Coordinate of the SPAWN_CHAR setting. */
	public static int SPAWN_Y;
	/** Z Coordinate of the SPAWN_CHAR setting. */
	public static int SPAWN_Z;
	public static boolean ALLOW_HERO_SUBSKILL;
	public static int HERO_COUNT;
	public static int CRUMA_TOWER_LEVEL_RESTRICT;
	/** Allow RaidBoss Petrified if player have +9 lvl to RB */
	public static boolean ALLOW_RAID_BOSS_PUT;
	/** Allow Players Level Difference Protection ? */
	public static int ALT_PLAYER_PROTECTION_LEVEL;
	public static boolean ALLOW_LOW_LEVEL_TRADE;
	/** Chat filter */
	public static boolean USE_CHAT_FILTER;

	public static boolean ALLOW_VERSION_COMMAND;
	public static int CLAN_LEADER_COLOR;
	public static int CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int CLAN_LEADER_COLORED;
	public static boolean SAVE_RAIDBOSS_STATUS_INTO_DB;
	public static boolean DISABLE_WEIGHT_PENALTY;
	public static int DIFFERENT_Z_CHANGE_OBJECT;
	public static int DIFFERENT_Z_NEW_MOVIE;
	//////////////////////////////
	public static int HERO_CUSTOM_ITEM_ID;
	public static int NOOBLE_CUSTOM_ITEM_ID;
	public static int HERO_CUSTOM_DAY;
	public static boolean ALLOW_FARM1_COMMAND;
	public static boolean ALLOW_FARM2_COMMAND;
	public static boolean ALLOW_PVP1_COMMAND;
	public static boolean ALLOW_PVP2_COMMAND;
	public static int FARM1_X;
	public static int FARM1_Y;
	public static int FARM1_Z;
	public static int PVP1_X;
	public static int PVP1_Y;
	public static int PVP1_Z;
	public static int FARM2_X;
	public static int FARM2_Y;
	public static int FARM2_Z;
	public static int PVP2_X;
	public static int PVP2_Y;
	public static int PVP2_Z;
	public static String FARM1_CUSTOM_MESSAGE;
	public static String FARM2_CUSTOM_MESSAGE;
	public static String PVP1_CUSTOM_MESSAGE;
	public static String PVP2_CUSTOM_MESSAGE;

	//============================================================
	public static void loadL2SCORIAConfig()
	{
		final String SCORIA = FService.l2scoria_CONFIG_FILE;

		_log.info("Loading: " + SCORIA + ".");
		try
		{
			Properties L2ScoriaSettings = new Properties();
			InputStream is = new FileInputStream(new File(SCORIA));
			L2ScoriaSettings.load(is);
			is.close();

			/** KidZor Custom Tables **/
			CUSTOM_SPAWNLIST_TABLE = Boolean.valueOf(L2ScoriaSettings.getProperty("CustomSpawnlistTable", "True"));
			SAVE_GMSPAWN_ON_CUSTOM = Boolean.valueOf(L2ScoriaSettings.getProperty("SaveGmSpawnOnCustom", "True"));

			ONLINE_PLAYERS_ON_LOGIN = Boolean.valueOf(L2ScoriaSettings.getProperty("OnlineOnLogin", "False"));
			ONLINE_CLAN_ON_LOGIN = Boolean.valueOf(L2ScoriaSettings.getProperty("OnlineClanOnLogin", "False"));
			UPTIME_ON_LOGIN = Boolean.valueOf(L2ScoriaSettings.getProperty("UptimeOnLogin", "False"));
			SHOW_SERVER_VERSION = Boolean.valueOf(L2ScoriaSettings.getProperty("ShowServerVersion", "False"));

			/** Emu Project Protector =) **/
			PROTECTOR_PLAYER_PK = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ProtectorPlayerPK", "false"));
			PROTECTOR_PLAYER_PVP = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ProtectorPlayerPVP", "false"));
			PROTECTOR_RADIUS_ACTION = Integer.parseInt(L2ScoriaSettings.getProperty("ProtectorRadiusAction", "500"));
			PROTECTOR_SKILLID = Integer.parseInt(L2ScoriaSettings.getProperty("ProtectorSkillId", "1069"));
			PROTECTOR_SKILLLEVEL = Integer.parseInt(L2ScoriaSettings.getProperty("ProtectorSkillLevel", "42"));
			PROTECTOR_SKILLTIME = Integer.parseInt(L2ScoriaSettings.getProperty("ProtectorSkillTime", "800"));
			PROTECTOR_MESSAGE = L2ScoriaSettings.getProperty("ProtectorMessage", "Protector, not spawnkilling here, go read the rules !!!");

			/** Donator color name **/
			DONATOR_NAME_COLOR_ENABLED = Boolean.parseBoolean(L2ScoriaSettings.getProperty("DonatorNameColorEnabled", "False"));
                        DONATOR_PARTY_REWARD = Boolean.parseBoolean(L2ScoriaSettings.getProperty("DonatorPartyRewards", "False"));
			DONATOR_NAME_COLOR = Integer.decode("0x" + L2ScoriaSettings.getProperty("DonatorColorName", "00FFFF"));
			DONATOR_TITLE_COLOR = Integer.decode("0x" + L2ScoriaSettings.getProperty("DonatorTitleColor", "00FF00"));
			DONATOR_XPSP_RATE = Float.parseFloat(L2ScoriaSettings.getProperty("DonatorXpSpRate", "1.5"));
			DONATOR_ADENA_RATE = Float.parseFloat(L2ScoriaSettings.getProperty("DonatorAdenaRate", "1.5"));
			DONATOR_DROP_RATE = Float.parseFloat(L2ScoriaSettings.getProperty("DonatorDropRate", "1.5"));
			DONATOR_SPOIL_RATE = Float.parseFloat(L2ScoriaSettings.getProperty("DonatorSpoilRate", "1.5"));

			ALLOW_GRADE_PENALTY = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowGradePenalty", "False"));
			/** Welcome Htm **/
			WELCOME_HTM = Boolean.parseBoolean(L2ScoriaSettings.getProperty("WelcomeHtm", "False"));

			/** Server Name **/
			ALT_Server_Name_Enabled = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ServerNameEnabled", "false"));
			ANNOUNCE_TO_ALL_SPAWN_RB = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AnnounceToAllSpawnRb", "false"));
			ANNOUNCE_BAN = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AnnounceTryBannedAccount", "false"));
			ALT_Server_Name = String.valueOf(L2ScoriaSettings.getProperty("ServerName"));
			DIFFERENT_Z_CHANGE_OBJECT = Integer.parseInt(L2ScoriaSettings.getProperty("DifferentZchangeObject", "650"));
			DIFFERENT_Z_NEW_MOVIE = Integer.parseInt(L2ScoriaSettings.getProperty("DifferentZnewmovie", "1000"));

			ALLOW_STAT_VIEW = Boolean.valueOf(L2ScoriaSettings.getProperty("AllowStatView", "False"));
			ALLOW_ONLINE_VIEW = Boolean.valueOf(L2ScoriaSettings.getProperty("AllowOnlineView", "False"));

			KEEP_SUBCLASS_SKILLS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("KeepSubClassSkills", "False"));

			ALLOWED_SKILLS = L2ScoriaSettings.getProperty("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619");
			ALLOWED_SKILLS_LIST = new FastList<Integer>();
			for(String id : ALLOWED_SKILLS.trim().split(","))
			{
				ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			}
			CASTLE_SHIELD = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CastleShieldRestriction", "true"));
			CLANHALL_SHIELD = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ClanHallShieldRestriction", "true"));
			APELLA_ARMORS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ApellaArmorsRestriction", "true"));
			OATH_ARMORS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("OathArmorsRestriction", "true"));
			CASTLE_CROWN = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CastleLordsCrownRestriction", "true"));
			CASTLE_CIRCLETS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CastleCircletsRestriction", "true"));
			CHAR_TITLE = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CharTitle", "false"));
			ADD_CHAR_TITLE = L2ScoriaSettings.getProperty("CharAddTitle", "Welcome");
			/////////////////////////
			NOBLE_CUSTOM_ITEMS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("EnableNobleCustomItem", "true"));
			NOOBLE_CUSTOM_ITEM_ID = Integer.parseInt(L2ScoriaSettings.getProperty("NoobleCustomItemId", "6673"));
			HERO_CUSTOM_ITEMS = Boolean.parseBoolean(L2ScoriaSettings.getProperty("EnableHeroCustomItem", "true"));
			HERO_CUSTOM_ITEM_ID = Integer.parseInt(L2ScoriaSettings.getProperty("HeroCustomItemId", "3481"));
			HERO_CUSTOM_DAY = Integer.parseInt(L2ScoriaSettings.getProperty("HeroCustomDay", "0"));
			/////////////////////////
			ALLOW_CREATE_LVL = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CustomStartingLvl", "False"));
			CHAR_CREATE_LVL = Integer.parseInt(L2ScoriaSettings.getProperty("CharLvl", "80"));
			ABORT_RR = L2ScoriaSettings.getProperty("AbortRestart", "L2J-Scoria");
			SPAWN_CHAR = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CustomSpawn", "false"));
			SPAWN_X = Integer.parseInt(L2ScoriaSettings.getProperty("SpawnX", ""));
			SPAWN_Y = Integer.parseInt(L2ScoriaSettings.getProperty("SpawnY", ""));
			SPAWN_Z = Integer.parseInt(L2ScoriaSettings.getProperty("SpawnZ", ""));
			ALLOW_LOW_LEVEL_TRADE = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowLowLevelTrade", "True"));
			ALLOW_HERO_SUBSKILL = Boolean.parseBoolean(L2ScoriaSettings.getProperty("CustomHeroSubSkill", "False"));
			HERO_COUNT = Integer.parseInt(L2ScoriaSettings.getProperty("HeroCount", "1"));
			CRUMA_TOWER_LEVEL_RESTRICT = Integer.parseInt(L2ScoriaSettings.getProperty("CrumaTowerLevelRestrict", "56"));
			ALLOW_RAID_BOSS_PUT = Boolean.valueOf(L2ScoriaSettings.getProperty("AllowRaidBossPetrified", "True"));
			ALT_PLAYER_PROTECTION_LEVEL = Integer.parseInt(L2ScoriaSettings.getProperty("AltPlayerProtectionLevel", "0"));
			CLAN_LEADER_COLOR_ENABLED = Boolean.parseBoolean(L2ScoriaSettings.getProperty("ClanLeaderNameColorEnabled", "true"));
			CLAN_LEADER_COLORED = Integer.parseInt(L2ScoriaSettings.getProperty("ClanLeaderColored", "1"));
			CLAN_LEADER_COLOR = Integer.decode("0x" + L2ScoriaSettings.getProperty("ClanLeaderColor", "00FFFF"));
			CLAN_LEADER_COLOR_CLAN_LEVEL = Integer.parseInt(L2ScoriaSettings.getProperty("ClanLeaderColorAtClanLevel", "1"));
			ALLOW_VERSION_COMMAND = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowVersionCommand", "False"));
			SAVE_RAIDBOSS_STATUS_INTO_DB = Boolean.parseBoolean(L2ScoriaSettings.getProperty("SaveRBStatusIntoDB", "False"));
			DISABLE_WEIGHT_PENALTY = Boolean.parseBoolean(L2ScoriaSettings.getProperty("DisableWeightPenalty", "False"));
			ALLOW_FARM1_COMMAND = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowFarm1Command", "false"));
			ALLOW_FARM2_COMMAND = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowFarm2Command", "false"));
			ALLOW_PVP1_COMMAND = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowPvP1Command", "false"));
			ALLOW_PVP2_COMMAND = Boolean.parseBoolean(L2ScoriaSettings.getProperty("AllowPvP2Command", "false"));
			FARM1_X = Integer.parseInt(L2ScoriaSettings.getProperty("farm1_X", "81304"));
			FARM1_Y = Integer.parseInt(L2ScoriaSettings.getProperty("farm1_Y", "14589"));
			FARM1_Z = Integer.parseInt(L2ScoriaSettings.getProperty("farm1_Z", "-3469"));
			PVP1_X = Integer.parseInt(L2ScoriaSettings.getProperty("pvp1_X", "81304"));
			PVP1_Y = Integer.parseInt(L2ScoriaSettings.getProperty("pvp1_Y", "14589"));
			PVP1_Z = Integer.parseInt(L2ScoriaSettings.getProperty("pvp1_Z", "-3469"));
			FARM2_X = Integer.parseInt(L2ScoriaSettings.getProperty("farm2_X", "81304"));
			FARM2_Y = Integer.parseInt(L2ScoriaSettings.getProperty("farm2_Y", "14589"));
			FARM2_Z = Integer.parseInt(L2ScoriaSettings.getProperty("farm2_Z", "-3469"));
			PVP2_X = Integer.parseInt(L2ScoriaSettings.getProperty("pvp2_X", "81304"));
			PVP2_Y = Integer.parseInt(L2ScoriaSettings.getProperty("pvp2_Y", "14589"));
			PVP2_Z = Integer.parseInt(L2ScoriaSettings.getProperty("pvp2_Z", "-3469"));
			FARM1_CUSTOM_MESSAGE = L2ScoriaSettings.getProperty("Farm1CustomMeesage", "You have been teleported to Farm Zone 1!");
			FARM2_CUSTOM_MESSAGE = L2ScoriaSettings.getProperty("Farm2CustomMeesage", "You have been teleported to Farm Zone 2!");
			PVP1_CUSTOM_MESSAGE = L2ScoriaSettings.getProperty("PvP1CustomMeesage", "You have been teleported to PvP Zone 1!");
			PVP2_CUSTOM_MESSAGE = L2ScoriaSettings.getProperty("PvP2CustomMeesage", "You have been teleported to PvP Zone 2!");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SCORIA + " File.");
		}
	}

	//============================================================
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
        public static boolean REMOVE_PVP_FLAG_ON_LOST_KARMA;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static FastList<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
	public static FastList<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;
	public static boolean PVP_REWARD_ENABLED;
	public static int PVP_REWORD_ID;
	public static int PVP_REWORD_AMOUNT;
	public static boolean PK_REWARD_ENABLED;
	public static int PK_REWORD_ID;
	public static int PK_REWORD_AMOUNT;
	public static int REWORD_PROTECT;
	public static boolean ENABLE_PK_INFO;
	public static boolean FLAGED_PLAYER_USE_BUFFER;
	public static boolean FLAGED_PLAYER_CAN_USE_GK;
	public static boolean PVPEXPSP_SYSTEM;
	/** Add Exp At Pvp! */
	public static int ADD_EXP;
	/** Add Sp At Pvp! */
	public static int ADD_SP;
	public static boolean ALLOW_POTS_IN_PVP;
	public static boolean ALLOW_SOE_IN_PVP;
	/** Announce PvP */
	public static boolean ANNOUNCE_PVP_KILL;
	/** Announce PK */
	public static boolean ANNOUNCE_PK_KILL;
	/** Announce Kill */
	public static boolean ANNOUNCE_ALL_KILL;

	public static int DUEL_SPAWN_X;
	public static int DUEL_SPAWN_Y;
	public static int DUEL_SPAWN_Z;

        public static boolean DUEL_WITH_ITEM_REQUIREMENT;
        public static int DUEL_CONSUM_ITEM_ID;
        public static int DUEL_CONSUM_ITEM_COST;

	public static boolean PVP_PK_TITLE;
	public static String PVP_TITLE_PREFIX;
	public static String PK_TITLE_PREFIX;

	//============================================================
	public static void loadPvpConfig()
	{
		final String PVP = FService.PVP_CONFIG_FILE;

		_log.info("Loading: " + PVP + ".");
		try
		{
			Properties pvpSettings = new Properties();
			InputStream is = new FileInputStream(new File(PVP));
			pvpSettings.load(is);
			is.close();

			/* KARMA SYSTEM */
			KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
			KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
			KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
			KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));

			KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));

                        REMOVE_PVP_FLAG_ON_LOST_KARMA = Boolean.parseBoolean(pvpSettings.getProperty("RemovePvpFlagsOnLostKarma", "true"));

			KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));

			KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
			KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");

			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
			for(String id : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
			}

			KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
			for(String id : KARMA_NONDROPPABLE_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}

			PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
			PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanShop", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pvpSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));

			/** Custom Reword **/
			PVP_REWARD_ENABLED = Boolean.valueOf(pvpSettings.getProperty("PvpRewardEnabled", "false"));
			PVP_REWORD_ID = Integer.parseInt(pvpSettings.getProperty("PvpRewardItemId", "6392"));
			PVP_REWORD_AMOUNT = Integer.parseInt(pvpSettings.getProperty("PvpRewardAmmount", "1"));
			///////
			PK_REWARD_ENABLED = Boolean.valueOf(pvpSettings.getProperty("PKRewardEnabled", "false"));
			PK_REWORD_ID = Integer.parseInt(pvpSettings.getProperty("PKRewardItemId", "6392"));
			PK_REWORD_AMOUNT = Integer.parseInt(pvpSettings.getProperty("PKRewardAmmount", "1"));
			///////
			REWORD_PROTECT = Integer.parseInt(pvpSettings.getProperty("RewardProtect", "1"));

			// PVP Name Color System configs - Start
			PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("EnablePvPColorSystem", "false"));
			PVP_AMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PvpAmount1", "500"));
			PVP_AMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PvpAmount2", "1000"));
			PVP_AMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PvpAmount3", "1500"));
			PVP_AMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PvpAmount4", "2500"));
			PVP_AMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PvpAmount5", "5000"));
			NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount1", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount2", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount3", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount4", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount5", "00FF00"));

			// PK Title Color System configs - Start
			PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("EnablePkColorSystem", "false"));
			PK_AMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PkAmount1", "500"));
			PK_AMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PkAmount2", "1000"));
			PK_AMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PkAmount3", "1500"));
			PK_AMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PkAmount4", "2500"));
			PK_AMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PkAmount5", "5000"));
			TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount1", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount2", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount3", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount4", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount5", "00FF00"));

			FLAGED_PLAYER_USE_BUFFER = Boolean.valueOf(pvpSettings.getProperty("AltKarmaFlagPlayerCanUseBuffer", "false"));

			FLAGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pvpSettings.getProperty("FlaggedPlayerCanUseGK", "false"));
			PVPEXPSP_SYSTEM = Boolean.parseBoolean(pvpSettings.getProperty("AllowAddExpSpAtPvP", "False"));
			ADD_EXP = Integer.parseInt(pvpSettings.getProperty("AddExpAtPvp", "0"));
			ADD_SP = Integer.parseInt(pvpSettings.getProperty("AddSpAtPvp", "0"));
			ALLOW_SOE_IN_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowSoEInPvP", "True"));
			ALLOW_POTS_IN_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowPotsInPvP", "True"));
			/** Enable Pk Info mod. Displays number of times player has killed other */
			ENABLE_PK_INFO = Boolean.valueOf(pvpSettings.getProperty("EnablePkInfo", "false"));
			// Get the AnnounceAllKill, AnnouncePvpKill and AnnouncePkKill values 
			ANNOUNCE_ALL_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnounceAllKill", "False"));
			if(ANNOUNCE_ALL_KILL)
			{
				ANNOUNCE_PVP_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePvPKill", "False"));
				ANNOUNCE_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePkKill", "False"));
			}
			else
			{
				ANNOUNCE_PVP_KILL = false;
				ANNOUNCE_PK_KILL = false;
			}

			DUEL_SPAWN_X = Integer.parseInt(pvpSettings.getProperty("DuelSpawnX", "-102495"));
			DUEL_SPAWN_Y = Integer.parseInt(pvpSettings.getProperty("DuelSpawnY", "-209023"));
			DUEL_SPAWN_Z = Integer.parseInt(pvpSettings.getProperty("DuelSpawnZ", "-3326"));
                        DUEL_WITH_ITEM_REQUIREMENT = Boolean.parseBoolean(pvpSettings.getProperty("DuelWithItem", "False"));
                        DUEL_CONSUM_ITEM_ID = Integer.parseInt(pvpSettings.getProperty("DuelWithItemId", "0"));
                        DUEL_CONSUM_ITEM_COST = Integer.parseInt(pvpSettings.getProperty("DuelWithItemCount", "0"));
			PVP_PK_TITLE = Boolean.parseBoolean(pvpSettings.getProperty("PvpPkTitle", "False"));
			PVP_TITLE_PREFIX = pvpSettings.getProperty("PvPTitlePrefix", " ");
			PK_TITLE_PREFIX = pvpSettings.getProperty("PkTitlePrefix", " | ");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PVP + " File.");
		}
	}

	public static int DEAMON_MAX_VOTES;
	public static boolean L2TOPDAEMON_ENABLED, MMOTOPDAEMON_ENABLED, HOPZONEDAEMON_ENABLED;

	public static void loadWebDaemonsConfig()
	{
		try
		{
			Properties daemonConfig = new Properties();
			InputStream is = new FileInputStream(new File(FService.DAEMON_CONFIG_FILE));
			daemonConfig.load(is);
			is.close();

			DEAMON_MAX_VOTES = Integer.parseInt(daemonConfig.getProperty("DeamonMaxVotesPerSession","20"));

			L2TOPDAEMON_ENABLED = Boolean.parseBoolean(daemonConfig.getProperty("L2TopEnabled", "false"));
			if(L2TOPDAEMON_ENABLED)
			{
				loadL2topConfig();
			}

			MMOTOPDAEMON_ENABLED = Boolean.parseBoolean(daemonConfig.getProperty("MMOTopEnabled", "false"));
			if(MMOTOPDAEMON_ENABLED)
			{
				loadMMOTopConfig();
			}

			HOPZONEDAEMON_ENABLED = Boolean.parseBoolean(daemonConfig.getProperty("HopZoneEnabled", "false"));
			if(HOPZONEDAEMON_ENABLED)
			{
				loadHopZoneConfig();
			}
		}
		catch (Exception e){}
	}


	///////////////////////////////////////////////////
	//		L  2  T  O  P
	///////////////////////////////////////////////////

	public static int			L2TOPDEMON_POLLINTERVAL;
	public static int			L2TOPDEMON_SERVERID;
	public static String		L2TOPDEMON_KEY;
	public static String 		L2TOPDEMON_NAMEALLOWED;
	public static int			L2TOPDEMON_MIN;
	public static int			L2TOPDEMON_MAX;
	public static int			L2TOPDEMON_ITEM;
	public static int			L2TOPDEMON_POLL_INVERVAL;
	public static String		L2TOPDEMON_PREFIX;
	public static boolean		L2TOPDEMON_IGNOREFIRST;
	public static RewardMode	L2TOP_REW_MODE;

	public static enum			RewardMode
	{
		ALL, SMS, WEB
	}

	//**********************************************************************************************
	public static void loadL2topConfig()
	{
		_log.info("Loading: " + FService.L2TOP_DAEMON_CONFIG_FILE);
		try
		{
			Properties l2topSettings = new Properties();
			InputStream is = new FileInputStream(new File(FService.L2TOP_DAEMON_CONFIG_FILE));
			l2topSettings.load(is);
			is.close();

			L2TOPDEMON_SERVERID = Integer.parseInt(l2topSettings.getProperty("ServerID","0"));
			L2TOPDEMON_KEY = l2topSettings.getProperty("ServerKey","");
			L2TOPDEMON_POLLINTERVAL = Integer.parseInt(l2topSettings.getProperty("PollInterval","10"));
			L2TOPDEMON_PREFIX = l2topSettings.getProperty("Prefix","");
			L2TOPDEMON_ITEM = Integer.parseInt(l2topSettings.getProperty("RewardItem","0"));
			L2TOPDEMON_MIN = Integer.parseInt(l2topSettings.getProperty("Min","1"));
			L2TOPDEMON_MAX = Integer.parseInt(l2topSettings.getProperty("Max","1"));
			L2TOPDEMON_POLL_INVERVAL = Integer.parseInt(l2topSettings.getProperty("PollInterval","10"));
			L2TOPDEMON_NAMEALLOWED = l2topSettings.getProperty("AllowedNames",".+");
			L2TOPDEMON_IGNOREFIRST = Boolean.parseBoolean(l2topSettings.getProperty("DoNotRewardAtFirstTime","false"));
			L2TOP_REW_MODE = RewardMode.valueOf(l2topSettings.getProperty("Mode", "ALL"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + FService.L2TOP_DAEMON_CONFIG_FILE + " File.");
		}
	}

	///////////////////////////////////////////////////
	//		M  M  O  T  O  P
	///////////////////////////////////////////////////

	public static int[] MMOTOPDAEMON_REWARD;
	public static int MMOTOPDAEMON_ITEM_ID;
	public static String MMOTOPDAEMON_URL;
	public static boolean MMOTOPDAEMON_REWARD_FIRST;
	public static int MMOTOPDAEMON_POLL_INVERVAL;

	//**********************************************************************************************
	public static void loadMMOTopConfig()
	{
		_log.info("Loading: " + FService.MMOTOP_DAEMON_CONFIG_FILE);
		try
		{
			Properties mmotopSettings = new Properties();
			InputStream is = new FileInputStream(new File(FService.MMOTOP_DAEMON_CONFIG_FILE));
			mmotopSettings.load(is);
			is.close();

			MMOTOPDAEMON_REWARD = new int[2];
			MMOTOPDAEMON_REWARD[0] = Integer.parseInt(mmotopSettings.getProperty("RewardMin","1"));
			MMOTOPDAEMON_REWARD[1] = Integer.parseInt(mmotopSettings.getProperty("RewardMax","10"));
			MMOTOPDAEMON_ITEM_ID = Integer.parseInt(mmotopSettings.getProperty("ItemID", "4037"));
			MMOTOPDAEMON_URL = mmotopSettings.getProperty("SiteURL", "");
			MMOTOPDAEMON_REWARD_FIRST = Boolean.parseBoolean(mmotopSettings.getProperty("RewardAtFirst", "false"));
			MMOTOPDAEMON_POLL_INVERVAL = Integer.parseInt(mmotopSettings.getProperty("PollInterval","10"));
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + FService.MMOTOP_DAEMON_CONFIG_FILE + " File.");
		}
	}

	public static int HOPZONEDAEMON_INTERVAL;
	public static int HOPZONEDAEMON_VOTES_FOR_REWARD;
	public static String HOPZONEDAEMON_URL;
	public static int[][] HOPZONEDAEMON_REWARDS;
	public static boolean HOPZONEDAEMON_REWARD_PRIVATE_STORE;
	public static String HOPZONEDAEMON_MESSAGE;

	public static void loadHopZoneConfig()
	{
		_log.info("Loading: " + FService.HOPZONE_DAEMON_CONFIG_FILE);
		try
		{
			Properties p = new L2Properties(FService.HOPZONE_DAEMON_CONFIG_FILE);

			HOPZONEDAEMON_INTERVAL = Integer.parseInt(p.getProperty("PollInterval","15"));
			HOPZONEDAEMON_VOTES_FOR_REWARD = Integer.parseInt(p.getProperty("VotesForReward","10"));
			HOPZONEDAEMON_URL = p.getProperty("SiteURL","localhost");
			HOPZONEDAEMON_REWARD_PRIVATE_STORE = Boolean.parseBoolean(p.getProperty("RewardPrivateStore","false"));
			HOPZONEDAEMON_MESSAGE = p.getProperty("DeamonMessage","");

			String rewards = p.getProperty("Reward", null);
			if(rewards != null)
			{
				String[] entries = rewards.split(";");
				HOPZONEDAEMON_REWARDS = new int[entries.length][];

				int i = 0;
				for(String enty : entries)
				{
					String[] data = enty.split(",");
					try
					{
						int itemId = Integer.parseInt(data[0]);
						int itemCount = Integer.parseInt(data[1]);
						HOPZONEDAEMON_REWARDS[i++] = new int[]{itemId, itemCount};
					}
					catch (NumberFormatException nfe)
					{
						nfe.printStackTrace();
					}
				}
			}
			else
			{
				_log.error("HopZone rewards are not defined!");
				HOPZONEDAEMON_ENABLED = false;
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			throw new Error("Failed to Load " + FService.HOPZONE_DAEMON_CONFIG_FILE + " File.");
		}
	}


	//============================================================
	public static boolean ALT_OLY_WEEK;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_DAY;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_BWAIT;
	public static long ALT_OLY_IWAIT;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static boolean ALT_OLY_PORT_RANDOM;
	public static boolean ALT_OLY_RESET_SKILL_REUSE;
	public static boolean ALT_OLY_ALLOW_SAME_IP;
	public static boolean ALT_OLY_ALLOW_SAME_HWID;
        public static boolean ALT_OLY_DENY_LS_SKILLS;
	public static String ALT_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_MAX_NOBLE_LIST;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new FastList<Integer>();
	public static String ALT_OLY_RESTRICTED_SKILLS;
	public static List<Integer> LIST_OLY_RESTRICTED_SKILLS = new FastList<Integer>();

	//============================================================
	public static void loadOlympConfig()
	{
		final String OLYMPC = FService.OLYMP_CONFIG_FILE;

		_log.info("Loading: " + OLYMPC + ".");
		try
		{
			Properties OLYMPSetting = new Properties();
			InputStream is = new FileInputStream(new File(OLYMPC));
			OLYMPSetting.load(is);
			is.close();
			LIST_OLY_RESTRICTED_ITEMS.clear();
			LIST_OLY_RESTRICTED_SKILLS.clear();
			ALT_OLY_WEEK = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyWeek", "false"));
			ALT_OLY_DAY = Integer.parseInt(OLYMPSetting.getProperty("AltDayHero", "1"));
			ALT_OLY_START_TIME = Integer.parseInt(OLYMPSetting.getProperty("AltOlyStartTime", "18"));
			ALT_OLY_MIN = Integer.parseInt(OLYMPSetting.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyCPeriod", "21600000"));
			ALT_OLY_BATTLE = Long.parseLong(OLYMPSetting.getProperty("AltOlyBattle", "360000"));
			ALT_OLY_BWAIT = Long.parseLong(OLYMPSetting.getProperty("AltOlyBWait", "600000"));
			ALT_OLY_IWAIT = Long.parseLong(OLYMPSetting.getProperty("AltOlyIWait", "300000"));
			ALT_OLY_WPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyWPeriod", "604800000"));
			ALT_OLY_VPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyVPeriod", "86400000"));
			ALT_OLY_CLASSED = Integer.parseInt(OLYMPSetting.getProperty("AltOlyClassedParticipants", "5"));
			ALT_OLY_NONCLASSED = Integer.parseInt(OLYMPSetting.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_PORT_RANDOM = Boolean.parseBoolean(OLYMPSetting.getProperty("AltolyPortRandom", "True"));
			ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(OLYMPSetting.getProperty("AltOlyBattleRewItem", "6651"));
			ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(OLYMPSetting.getProperty("AltOlyClassedRewItemCount", "50"));
			ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(OLYMPSetting.getProperty("AltOlyNonClassedRewItemCount", "30"));
			ALT_OLY_COMP_RITEM = Integer.parseInt(OLYMPSetting.getProperty("AltOlyCompRewItem", "6651"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(OLYMPSetting.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_MIN_POINT_FOR_EXCH = Integer.parseInt(OLYMPSetting.getProperty("AltOlyMinPointForExchange", "50"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(OLYMPSetting.getProperty("AltOlyHeroPoints", "300"));
			ALT_OLY_RESTRICTED_ITEMS = OLYMPSetting.getProperty("AltOlyRestrictedItems", "0");
			ALT_OLY_RESET_SKILL_REUSE = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyRestSkillReuse", "true"));
			ALT_OLY_ALLOW_SAME_IP = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlySameIP", "false"));
			ALT_OLY_ALLOW_SAME_HWID = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlySameHwid", "false"));
                        ALT_OLY_DENY_LS_SKILLS = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyDenyLifeStoneSkill", "false"));
			ALT_OLY_MAX_NOBLE_LIST = Integer.parseInt(OLYMPSetting.getProperty("AltOlyMaxNobleInList", "100"));
			LIST_OLY_RESTRICTED_ITEMS = new FastList<Integer>();
			for(String id : ALT_OLY_RESTRICTED_ITEMS.split(","))
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			ALT_OLY_RESTRICTED_SKILLS = OLYMPSetting.getProperty("AltOlyRestrictedSkills", "0");
			LIST_OLY_RESTRICTED_SKILLS = new FastList<Integer>();
			for(String id : ALT_OLY_RESTRICTED_SKILLS.split(","))
			{
				LIST_OLY_RESTRICTED_SKILLS.add(Integer.parseInt(id));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OLYMPC + " File.");
		}
	}

	//============================================================
	//enchant map
	public static TIntIntHashMap NORMAL_WEAPON_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap BLESS_WEAPON_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap CRYTAL_WEAPON_ENCHANT_LEVEL = new TIntIntHashMap();

	public static TIntIntHashMap NORMAL_ARMOR_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap BLESS_ARMOR_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap CRYSTAL_ARMOR_ENCHANT_LEVEL = new TIntIntHashMap();

	public static TIntIntHashMap NORMAL_JEWELRY_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap BLESS_JEWELRY_ENCHANT_LEVEL = new TIntIntHashMap();
	public static TIntIntHashMap CRYSTAL_JEWELRY_ENCHANT_LEVEL = new TIntIntHashMap();

	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int ENCHANT_WEAPON_MAX;
	public static int ENCHANT_ARMOR_MAX;
	public static int ENCHANT_JEWELRY_MAX;

	//dwarf bonus
	public static boolean ENABLE_DWARF_ENCHANT_BONUS;
	public static int DWARF_ENCHANT_MIN_LEVEL;
	public static int DWARF_ENCHANT_BONUS;
	//augment chance
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	//augment glow
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	//enchant hero weapon
	public static boolean ENCHANT_HERO_WEAPON;
	//soul crystal
	public static int SOUL_CRYSTAL_BREAK_CHANCE;
	public static int SOUL_CRYSTAL_LEVEL_CHANCE;
	public static int SOUL_CRYSTAL_MAX_LEVEL;
	//count enchant
	public static int CUSTOM_ENCHANT_VALUE;
	/** Olympiad max enchant limitation */
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int BREAK_ENCHANT;
	public static boolean STACKABLE_ENCHANTS;

	//============================================================
	public static void loadEnchantConfig()
	{
		final String ENCHANTC = FService.ENCHANT_CONFIG_FILE;

		_log.info("Loading: " + ENCHANTC + ".");
		try
		{
			Properties ENCHANTSetting = new Properties();
			InputStream is = new FileInputStream(new File(ENCHANTC));
			ENCHANTSetting.load(is);
			is.close();

			String[] propertySplit = ENCHANTSetting.getProperty("NormalWeaponEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("BlessWeaponEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("CrystalWeaponEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						CRYTAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			////

			propertySplit = ENCHANTSetting.getProperty("NormalArmorEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("BlessArmorEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("CrystalArmorEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			////

			propertySplit = ENCHANTSetting.getProperty("NormalJewelryEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("BlessJewelryEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			propertySplit = ENCHANTSetting.getProperty("CrystalJewelryEnchantLevel", "").split(";");
			for(String readData : propertySplit)
			{
				String[] writeData = readData.split(",");
				if(writeData.length != 2)
				{
					_log.info("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch(NumberFormatException nfe)
					{
						if(!readData.equals(""))
						{
							_log.info("invalid config property");
						}
					}
				}
			}

			/** limit of safe enchant normal **/
			ENCHANT_SAFE_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantSafeMax", "3"));

			/** limit of safe enchant full **/
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(ENCHANTSetting.getProperty("EnchantSafeMaxFull", "4"));

			/** limit of max enchant **/
			ENCHANT_WEAPON_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantWeaponMax", "25"));
			ENCHANT_ARMOR_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantArmorMax", "25"));
			ENCHANT_JEWELRY_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantJewelryMax", "25"));

			/** bonus for dwarf **/
			ENABLE_DWARF_ENCHANT_BONUS = Boolean.parseBoolean(ENCHANTSetting.getProperty("EnableDwarfEnchantBonus", "False"));
			DWARF_ENCHANT_MIN_LEVEL = Integer.parseInt(ENCHANTSetting.getProperty("DwarfEnchantMinLevel", "80"));
			DWARF_ENCHANT_BONUS = Integer.parseInt(ENCHANTSetting.getProperty("DwarfEncahntBonus", "15"));

			/** augmentation chance **/
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationNGSkillChance", "15"));
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationMidSkillChance", "30"));
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationHighSkillChance", "45"));
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationTopSkillChance", "60"));
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationBaseStatChance", "1"));

			/** augmentation glow **/
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationNGGlowChance", "0"));
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationMidGlowChance", "40"));
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationHighGlowChance", "70"));
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationTopGlowChance", "100"));

			/** enchant hero weapon **/
			ENCHANT_HERO_WEAPON = Boolean.parseBoolean(ENCHANTSetting.getProperty("EnableEnchantHeroWeapons", "False"));

			/** soul crystal **/
			SOUL_CRYSTAL_BREAK_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalBreakChance", "10"));
			SOUL_CRYSTAL_LEVEL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalLevelChance", "32"));
			SOUL_CRYSTAL_MAX_LEVEL = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalMaxLevel", "13"));

			/** count enchant **/
			CUSTOM_ENCHANT_VALUE = Integer.parseInt(ENCHANTSetting.getProperty("CustomEnchantValue", "1"));
			ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(ENCHANTSetting.getProperty("AltOlyMaxEnchant", "-1"));
			BREAK_ENCHANT = Integer.valueOf(ENCHANTSetting.getProperty("BreakEnchant", "0"));
			STACKABLE_ENCHANTS = Boolean.parseBoolean(ENCHANTSetting.getProperty("StackableEnchantment", "False"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ENCHANTC + " File.");
		}
	}

	//============================================================
	public static int FLOODPROTECTOR_INITIALSIZE;
	public static int PROTECTED_BYPASS_C;
	public static int PROTECTED_HEROVOICE_C;
	public static int PROTECTED_MULTISELL_C;
	public static int PROTECTED_SUBCLASS_C;
	public static int PROTECTED_CHAT_C;
	public static int PROTECTED_PARTY_ADD_MEMBER_C;
	public static int PROTECTED_DROP_C;
	public static int PROTECTED_ENCHANT_C;
	public static int PROTECTED_BANKING_SYSTEM_C;
	public static int PROTECTED_WEREHOUSE_C;
	public static int PROTECTED_CRAFT_C;
	public static int PROTECTED_USE_ITEM_C;
	public static int PROTECTED_UNPACK_ITEM_C;
	public static int PROTECTED_ITEM_COUNT;

	//============================================================
	public static void loadFloodConfig()
	{
		final String PROTECT_FLOOD_CONFIG = FService.PROTECT_FLOOD_CONFIG_FILE;

		_log.info("Loading: " + PROTECT_FLOOD_CONFIG + ".");
		try
		{
			Properties FloodSetting = new Properties();
			InputStream is = new FileInputStream(new File(PROTECT_FLOOD_CONFIG));
			FloodSetting.load(is);
			is.close();

			FLOODPROTECTOR_INITIALSIZE = Integer.parseInt(FloodSetting.getProperty("FloodProtectorInitialSize", "50"));
			PROTECTED_BYPASS_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorByPass", "4"));
			PROTECTED_HEROVOICE_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorHeroVoice", "100"));
			PROTECTED_MULTISELL_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorMultisell", "100"));
			PROTECTED_SUBCLASS_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorSubclass", "100"));
			PROTECTED_CHAT_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorChatDelay", "5"));
			PROTECTED_PARTY_ADD_MEMBER_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorPartyAddMember", "80"));
			PROTECTED_DROP_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorDrop", "50"));
			PROTECTED_ENCHANT_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorEnchant", "50"));
			PROTECTED_BANKING_SYSTEM_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorBankingSystem", "50"));
			PROTECTED_WEREHOUSE_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorWerehouse", "50"));
			PROTECTED_CRAFT_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorCraft", "50"));
			PROTECTED_ITEM_COUNT = Integer.parseInt(FloodSetting.getProperty("FloodProtectorItemCount", "2"));
			PROTECTED_USE_ITEM_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorUseItem", "10"));
			PROTECTED_UNPACK_ITEM_C = Integer.parseInt(FloodSetting.getProperty("FloodProtectorUnpackItem", "100"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PROTECT_FLOOD_CONFIG + " File.");
		}
	}

	//============================================================
	public static boolean ENABLE_UNK_PACKET_PROTECTION;
	public static int MAX_UNKNOWN_PACKETS;
	public static int UNKNOWN_PACKETS_PUNiSHMENT;
	public static boolean DEBUG_UNKNOWN_PACKETS;
	public static int PROTECTED_UNKNOWNPACKET_C;

	public static int PROTECTED_ACTIVE_PACK_RETURN;
	public static int PROTECTED_ACTIVE_PACK_FAILED;

	/** packet life time **/
	public static int PACKET_LIFETIME;

	//============================================================
	public static void loadPacketConfig()
	{
		final String PROTECT_PACKET_CONFIG = FService.PROTECT_PACKET_CONFIG_FILE;

		_log.info("Loading: " + PROTECT_PACKET_CONFIG + ".");
		try
		{
			Properties PacketSetting = new Properties();
			InputStream is = new FileInputStream(new File(PROTECT_PACKET_CONFIG));
			PacketSetting.load(is);
			is.close();

			ENABLE_UNK_PACKET_PROTECTION = Boolean.parseBoolean(PacketSetting.getProperty("UnknownPacketProtection", "true"));
			MAX_UNKNOWN_PACKETS = Integer.parseInt(PacketSetting.getProperty("UnknownPacketsBeforeBan", "5"));
			UNKNOWN_PACKETS_PUNiSHMENT = Integer.parseInt(PacketSetting.getProperty("UnknownPacketsPunishment", "2"));
			DEBUG_UNKNOWN_PACKETS = Boolean.parseBoolean(PacketSetting.getProperty("UnknownDebugPackets", "false"));
			PROTECTED_UNKNOWNPACKET_C = Integer.parseInt(PacketSetting.getProperty("UnknownFloodProtectorPacket", "50"));

			PROTECTED_ACTIVE_PACK_RETURN = Integer.parseInt(PacketSetting.getProperty("ActivePacketReturn", "12"));
			PROTECTED_ACTIVE_PACK_FAILED = Integer.parseInt(PacketSetting.getProperty("ActivePacketAF", "100"));

			/** packet life time **/
			PACKET_LIFETIME = Integer.parseInt(PacketSetting.getProperty("PacketLifeTime", "0"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PROTECT_PACKET_CONFIG + " File.");
		}
	}

	//============================================================
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static boolean L2WALKER_PROTEC;
	public static boolean PROTECTED_ENCHANT;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean ONLY_GM_TELEPORT_FREE;
	public static boolean ALLOW_DUALBOX;
	public static boolean BOT_PROTECTOR;
	public static int BOT_PROTECTOR_FIRST_CHECK;
	public static int BOT_PROTECTOR_NEXT_CHECK;
	public static int BOT_PROTECTOR_WAIT_ANSVER;
        public static boolean ENABLE_VISIBLE_OBJECT_OPTIMIZATION;
        public static int VISIBLE_DEECREASE_VALUE_ONLINE;
        public static int RADIUS_VISIBLE_DEECREASE_VALUE;

	//============================================================
	public static void loadPOtherConfig()
	{
		final String PROTECT_OTHER_CONFIG = FService.PROTECT_OTHER_CONFIG_FILE;

		_log.info("Loading: " + PROTECT_OTHER_CONFIG + ".");
		try
		{
			Properties POtherSetting = new Properties();
			InputStream is = new FileInputStream(new File(PROTECT_OTHER_CONFIG));
			POtherSetting.load(is);
			is.close();

			CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(POtherSetting.getProperty("CheckSkillsOnEnter", "True"));

			/** l2walker protection **/
			L2WALKER_PROTEC = Boolean.parseBoolean(POtherSetting.getProperty("L2WalkerProtection", "False"));

			/** enchant protected **/
			PROTECTED_ENCHANT = Boolean.parseBoolean(POtherSetting.getProperty("ProtectorEnchant", "false"));

			ONLY_GM_TELEPORT_FREE = Boolean.parseBoolean(POtherSetting.getProperty("OnlyGMTeleportFree", "false"));
			ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(POtherSetting.getProperty("OnlyGMItemsFree", "false"));

			BYPASS_VALIDATION = Boolean.parseBoolean(POtherSetting.getProperty("BypassValidation", "True"));

			ALLOW_DUALBOX = Boolean.parseBoolean(POtherSetting.getProperty("AllowDualBox", "True"));

			BOT_PROTECTOR = Boolean.parseBoolean(POtherSetting.getProperty("BotProtect", "False"));
			BOT_PROTECTOR_FIRST_CHECK = Integer.parseInt(POtherSetting.getProperty("BotProtectFirstCheck", "15"));
			BOT_PROTECTOR_NEXT_CHECK = Integer.parseInt(POtherSetting.getProperty("BotProtectNextCheck", "60"));
			BOT_PROTECTOR_WAIT_ANSVER = Integer.parseInt(POtherSetting.getProperty("BotProtectAnsver", "180"));

                        ENABLE_VISIBLE_OBJECT_OPTIMIZATION = Boolean.parseBoolean(POtherSetting.getProperty("EnableVisibleObjectOptimization", "false"));
                        VISIBLE_DEECREASE_VALUE_ONLINE = Integer.parseInt(POtherSetting.getProperty("ValuesOfOnline", "1500"));
                        RADIUS_VISIBLE_DEECREASE_VALUE = Integer.parseInt(POtherSetting.getProperty("ValueToVisibleObjects", "1500"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PROTECT_OTHER_CONFIG + " File.");
		}
	}

	//============================================================

	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;

	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MCRIT_RATE_MUL;
	public static int BOSS_LIMIT_RADIUS;

	public static int RUN_SPD_BOOST;
	public static int MAX_RUN_SPEED;
	public static int MAX_HIT_TIME;

	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	// Alternative damage for dagger skills VS heavy
	public static float ALT_DAGGER_DMG_VS_HEAVY;
	// Alternative damage for dagger skills VS robe
	public static float ALT_DAGGER_DMG_VS_ROBE;
	// Alternative damage for dagger skills VS light
	public static float ALT_DAGGER_DMG_VS_LIGHT;
	// Take dameg from fall
	public static boolean FALL_DAMAGE;
	public static boolean REMOVE_LS_BUFF;
	public static boolean MOVE_SIT_LIKE_PTS;

	//============================================================
	public static void loadPHYSICSConfig()
	{
		final String PHYSICS_CONFIG = FService.PHYSICS_CONFIGURATION_FILE;

		_log.info("Loading: " + PHYSICS_CONFIG + ".");
		try
		{
			Properties PHYSICSSetting = new Properties();
			InputStream is = new FileInputStream(new File(PHYSICS_CONFIG));
			PHYSICSSetting.load(is);
			is.close();

			//Max patk speed and matk speed
			MAX_PATK_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxPAtkSpeed", "1500"));
			MAX_MATK_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxMAtkSpeed", "1999"));

			if(MAX_PATK_SPEED < 1)
			{
				MAX_PATK_SPEED = Integer.MAX_VALUE;
			}

			if(MAX_MATK_SPEED < 1)
			{
				MAX_MATK_SPEED = Integer.MAX_VALUE;
			}

			MAX_PCRIT_RATE = Integer.parseInt(PHYSICSSetting.getProperty("MaxPCritRate", "500"));
			MAX_MCRIT_RATE = Integer.parseInt(PHYSICSSetting.getProperty("MaxMCritRate", "300"));
			MCRIT_RATE_MUL = Integer.parseInt(PHYSICSSetting.getProperty("McritMulDif", "8"));
			BOSS_LIMIT_RADIUS = Integer.parseInt(PHYSICSSetting.getProperty("BossRadiusProtection", "5000"));

			ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageMages", "1.00"));
			ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageMages", "1.00"));
			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageFighters", "1.00"));
			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageFighters", "1.00"));
			ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamagePets", "1.00"));
			ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamagePets", "1.00"));
			ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageNpc", "1.00"));
			ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageNpc", "1.00"));
			ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSHeavy", "2.50"));
			ALT_DAGGER_DMG_VS_ROBE = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSRobe", "1.80"));
			ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSLight", "2.00"));
			RUN_SPD_BOOST = Integer.parseInt(PHYSICSSetting.getProperty("RunSpeedBoost", "0"));
			MAX_RUN_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxRunSpeed", "250"));
			MAX_HIT_TIME = Integer.parseInt(PHYSICSSetting.getProperty("MaxCastHitTime", "300"));
			FALL_DAMAGE = Boolean.parseBoolean(PHYSICSSetting.getProperty("FallDamage", "False"));
			REMOVE_LS_BUFF = Boolean.parseBoolean(PHYSICSSetting.getProperty("RemoveLSBuff", "True"));
			MOVE_SIT_LIKE_PTS = Boolean.parseBoolean(PHYSICSSetting.getProperty("UseMoveSitLikePts", "False"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PHYSICS_CONFIG + " File.");
		}
	}

	//============================================================
	public static boolean GEODATA;
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;

	public static boolean CONTROL_HEIGHT_DAMAGE;

	public static boolean ALLOW_KEYBOARD_MOVE;

	public static boolean GEO_AR;
	public static boolean GEO_AR_PRELOAD_NEIBOURS;

	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;

	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static double WEIGHT0;
	public static double WEIGHT1;
	public static double WEIGHT2;
	public static boolean COMPACT_GEO;

	//============================================================
	public static void loadgeodataConfig()
	{
		final String GEODATA_CONFIG_FILE = FService.GEODATA_CONFIG_FILE;

		_log.info("Loading: " + GEODATA_CONFIG_FILE + ".");

		try
		{
			Properties geodataSetting = new Properties();
			InputStream is = new FileInputStream(new File(GEODATA_CONFIG_FILE));
			geodataSetting.load(is);
			is.close();

			GEODATA 					= Boolean.parseBoolean(geodataSetting.getProperty("GeoData", "false"));
			GEO_X_FIRST					= Integer.parseInt(geodataSetting.getProperty("GeoFirstX", "15"));
			GEO_Y_FIRST 				= Integer.parseInt(geodataSetting.getProperty("GeoFirstY", "10"));
			GEO_X_LAST 					= Integer.parseInt(geodataSetting.getProperty("GeoLastX", "26"));
			GEO_Y_LAST 					= Integer.parseInt(geodataSetting.getProperty("GeoLastY", "26"));
			CONTROL_HEIGHT_DAMAGE       = Boolean.parseBoolean(geodataSetting.getProperty("ControlHeightDamage", "true"));
			PATH_CLEAN 					= Boolean.parseBoolean(geodataSetting.getProperty("PathClean", "true"));
			ALLOW_KEYBOARD_MOVE 		= Boolean.parseBoolean(geodataSetting.getProperty("AllowMoveWithKeyboard", "true"));
			PATHFIND_BOOST 				= Integer.parseInt(geodataSetting.getProperty("PathFindBoost", "2"));
			PATHFIND_DIAGONAL 			= Boolean.parseBoolean(geodataSetting.getProperty("PathFindDiagonal", "true"));
			PATHFIND_MAX_Z_DIFF			= Integer.parseInt(geodataSetting.getProperty("PathFindMaxZDiff", "32"));
			MAX_Z_DIFF 					= Integer.parseInt(geodataSetting.getProperty("MaxZDiff", "64"));
			MIN_LAYER_HEIGHT 			= Integer.parseInt(geodataSetting.getProperty("MinLayerHeight", "64"));
			WEIGHT0 					= Double.parseDouble(geodataSetting.getProperty("Weight0", "0.5"));
			WEIGHT1 					= Double.parseDouble(geodataSetting.getProperty("Weight1", "2.0"));
			WEIGHT2 					= Double.parseDouble(geodataSetting.getProperty("Weight2", "1.0"));

			GEO_AR 						= Boolean.parseBoolean(geodataSetting.getProperty("GeoAR", "false"));
			GEO_AR_PRELOAD_NEIBOURS		= Boolean.parseBoolean(geodataSetting.getProperty("GeoARPreloadNeibours", "true"));

			PATHFIND_MAX_TIME 			= Long.parseLong(geodataSetting.getProperty("PathFindMaxTime", "10000000"));
			PATHFIND_BUFFERS 			= geodataSetting.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");

			COMPACT_GEO 				= Boolean.parseBoolean(geodataSetting.getProperty("CompactGeoData", "false"));
			GRIDS_ALWAYS_ON             = Boolean.parseBoolean(geodataSetting.getProperty("GridsAlwaysOn", "false"));
			GRID_NEIGHBOR_TURNON_TIME   = Integer.parseInt(geodataSetting.getProperty("GridNeighborTurnOnTime", "30"));
			GRID_NEIGHBOR_TURNOFF_TIME  = Integer.parseInt(geodataSetting.getProperty("GridNeighborTurnOffTime", "300"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + GEODATA_CONFIG_FILE + " File.");
		}

	}

	public static boolean USE_RL_DATABSE;
	public static String RL_DATABASE_DRIVER;
	public static String RL_DATABASE_URL;
	public static String RL_DATABASE_LOGIN;
	public static String RL_DATABASE_PASSWORD;
	public static int RL_DATABASE_MAX_CONNECTIONS;
	public static int RL_DATABASE_TIMEOUT;
	public static int RL_DATABASE_STATEMENT;
	public static boolean ALLOW_PERSONAL;
	public static boolean ALLOW_SHOW_MENU;
	public static boolean ALLOW_SET_AUTOLOOT;
	public static boolean ALLOW_SET_AUTOLEARNSKILL;
	public static boolean ALLOW_SET_XP_RATE;
	public static boolean ALLOW_CHANGE_PASS;
	public static int PERSONAL_PASS_ITEM;
	public static int PERSONAL_PASS_COUNT;
	public static boolean ALLOW_CHANGE_IP;
	public static int PERSONAL_IP_ITEM;
	public static int PERSONAL_IP_COUNT;
	public static boolean ALLOW_PREMIUM;
	public static int PERSONAL_PREMIUM_ITEM;
	public static int PERSONAL_PREMIUM_COUNT;
	public static int PERSONAL_PREMIUM_TIME;
	public static boolean CHAR_MOVE;
	public static boolean ALLOW_SCRIPT;
        public static boolean ALLOW_BIND_HWID;
        public static String SERVER_PROTECTION_TYPE;
        public static int WRONG_HWID_DISSCONNECT_TIME;
	public static String PERSONAL_SCRIPTS;
	public static FastList<Integer> PERSONAL_SCRIPTS_ID;
	public static String NETMASK_FIST_RULLE;
	public static String NETMASK_SECOND_RULLE;

	//============================================================
	public static void loadPersonalConfig()
	{
		final String PERSONAL = FService.PERSONAL_CONFIG_FILE;

		_log.info("Loading: " + PERSONAL + ".");
		try
		{
			Properties PersonalSettings = new Properties();
			InputStream is = new FileInputStream(new File(PERSONAL));
			PersonalSettings.load(is);
			is.close();
			//============================================================
			USE_RL_DATABSE = Boolean.parseBoolean(PersonalSettings.getProperty("UseRemoteLoginDatabase", "False"));
			ALLOW_PERSONAL = Boolean.parseBoolean(PersonalSettings.getProperty("AllowPersonal", "False"));
			ALLOW_SHOW_MENU = Boolean.parseBoolean(PersonalSettings.getProperty("AllowShowMenu", "False"));
			ALLOW_SET_AUTOLOOT = Boolean.parseBoolean(PersonalSettings.getProperty("AllowSetAutoloot", "False"));
			ALLOW_SET_AUTOLEARNSKILL  = Boolean.parseBoolean(PersonalSettings.getProperty("AllowSetAutolearnSkill", "False"));
			ALLOW_SET_XP_RATE = Boolean.parseBoolean(PersonalSettings.getProperty("AllowSetXpRate", "False"));
			ALLOW_CHANGE_PASS = Boolean.parseBoolean(PersonalSettings.getProperty("AllowChangePass", "False"));
			PERSONAL_PASS_ITEM = Integer.parseInt(PersonalSettings.getProperty("PersonalPassItem", "5575"));
			PERSONAL_PASS_COUNT = Integer.parseInt(PersonalSettings.getProperty("PersonalPassCount", "1000000"));
			ALLOW_CHANGE_IP = Boolean.parseBoolean(PersonalSettings.getProperty("AllowChangeIP", "False"));
			PERSONAL_IP_ITEM = Integer.parseInt(PersonalSettings.getProperty("PersonalIPItem", "5575"));
			PERSONAL_IP_COUNT = Integer.parseInt(PersonalSettings.getProperty("PersonalIPCount", "1000000"));
			ALLOW_PREMIUM = Boolean.parseBoolean(PersonalSettings.getProperty("AllowPremium", "False"));
			PERSONAL_PREMIUM_ITEM = Integer.parseInt(PersonalSettings.getProperty("PersonalPremiumItem", "5575"));
			PERSONAL_PREMIUM_COUNT = Integer.parseInt(PersonalSettings.getProperty("PersonalPremiumCount", "1000000"));
			PERSONAL_PREMIUM_TIME = Integer.parseInt(PersonalSettings.getProperty("PersonalPremiumTime", "7"));
			CHAR_MOVE = Boolean.parseBoolean(PersonalSettings.getProperty("AllowCharMove", "False"));
			NETMASK_FIST_RULLE = PersonalSettings.getProperty("DynamoIpFistRulle", "0/24");
			NETMASK_SECOND_RULLE = PersonalSettings.getProperty("DynamoIpSecondRulle", "0/16");
			ALLOW_SCRIPT = Boolean.parseBoolean(PersonalSettings.getProperty("AllowScripts", "false"));

			ALLOW_BIND_HWID = Boolean.parseBoolean(PersonalSettings.getProperty("AllowHwidBind", "false"));
			SERVER_PROTECTION_TYPE = PersonalSettings.getProperty("ProtectionTypeHwidBind", null);
                        WRONG_HWID_DISSCONNECT_TIME = Integer.parseInt(PersonalSettings.getProperty("WrongHwidDisconnect", "2000"));

			if(SERVER_PROTECTION_TYPE.equals(""))
			{
				SERVER_PROTECTION_TYPE = null;
			}


			if(ALLOW_SCRIPT)
			{
				PERSONAL_SCRIPTS = PersonalSettings.getProperty("PersonalScripts", "");
				if(PERSONAL_SCRIPTS != null && !PERSONAL_SCRIPTS.equals(""))
				{
					PERSONAL_SCRIPTS_ID = new FastList<Integer>();
					for(String id : PERSONAL_SCRIPTS.trim().split(","))
					{
						PERSONAL_SCRIPTS_ID.add(Integer.parseInt(id.trim()));
					}
				}
			}
			RL_DATABASE_DRIVER = PersonalSettings.getProperty("RLDriver", "com.mysql.jdbc.Driver");
			RL_DATABASE_URL = PersonalSettings.getProperty("RLURL", "jdbc:mysql://localhost/l2jdb");
			RL_DATABASE_LOGIN = PersonalSettings.getProperty("RLLogin", "root");
			RL_DATABASE_PASSWORD = PersonalSettings.getProperty("RLPassword", "");
			RL_DATABASE_MAX_CONNECTIONS = Integer.parseInt(PersonalSettings.getProperty("RLMaximumDbConnections", "10"));
			RL_DATABASE_TIMEOUT = Integer.parseInt(PersonalSettings.getProperty("RLTimeOutConDb", "0"));
			RL_DATABASE_STATEMENT = Integer.parseInt(PersonalSettings.getProperty("RLMaximumDbStatement", "100"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PERSONAL + " File.");
		}
	}
	
	//============================================================
	public static boolean ANTHARAS_OLD;
	public static int ANTHARAS_WEEK_LIMIT;
	public static int ANTHARAS_NORMAL_LIMIT;
	public static boolean ANTHARAS_MOVE;
	public static int ANTHARAS_MAX_MOBS;
	public static int ANTHARAS_CLOSE;
	public static int ANTHARAS_RESP_FIRST;
	public static int ANTHARAS_RESP_SECOND;
	public static boolean ANNOUNCE_SPAWN_ANTHARAS;
	public static boolean BAIUM_WALL_SMASH;
	public static int BAIUM_SLEEP;
	public static int BAIUM_RESP_FIRST;
	public static int BAIUM_RESP_SECOND;
	public static boolean ANNOUNCE_SPAWN_BAIUM;
	public static int CORE_RESP_MINION;
	public static int CORE_RESP_FIRST;
	public static int CORE_RESP_SECOND;
	public static int QA_RESP_NURSE;
	public static int QA_RESP_ROYAL;
	public static int QA_RESP_FIRST;
	public static int QA_RESP_SECOND;
	public static boolean ANNOUNCE_SPAWN_QA;
	public static boolean QA_TELEPORT_HIGHTERS;
	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	public static int FRINTEZZA_RESP_FIRST;
	public static int FRINTEZZA_RESP_SECOND;
	public static int ZAKEN_RESP_FIRST;
	public static int ZAKEN_RESP_SECOND;
	public static boolean ZAKEN_BANISH_CHEATERS;

	//============================================================
	public static void loadBossConfig()
	{
		final String BOSS = FService.BOSS_CONFIG_FILE;

		_log.info("Loading: " + BOSS + ".");
		try
		{
			Properties bossSettings = new Properties();
			InputStream is = new FileInputStream(new File(BOSS));
			bossSettings.load(is);
			is.close();
			//============================================================
			//Antharas
			ANTHARAS_OLD = Boolean.parseBoolean(bossSettings.getProperty("AntharasOld", "false"));
			ANTHARAS_WEEK_LIMIT = Integer.parseInt(bossSettings.getProperty("AntharasWeakLimit", "45"));
			ANTHARAS_NORMAL_LIMIT = Integer.parseInt(bossSettings.getProperty("AntharasNormalLimit", "60"));
			ANTHARAS_MOVE = Boolean.parseBoolean(bossSettings.getProperty("AntharasMove", "true"));
			ANTHARAS_MAX_MOBS = Integer.parseInt(bossSettings.getProperty("AntharasMaxMobs", "10"));
			ANTHARAS_CLOSE = Integer.parseInt(bossSettings.getProperty("AntharasClose", "1200"));
			ANTHARAS_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("AntharasRespFirst", "192"));
			ANTHARAS_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("AntharasRespSecond", "145"));
			ANNOUNCE_SPAWN_ANTHARAS = Boolean.parseBoolean(bossSettings.getProperty("AnnounceAntharasSpawn", "false"));
			//============================================================
			//Baium
			BAIUM_WALL_SMASH = Boolean.parseBoolean(bossSettings.getProperty("BaiumWallSmash", "true"));
			BAIUM_SLEEP = Integer.parseInt(bossSettings.getProperty("BaiumSleep", "1800"));
			BAIUM_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("BaiumRespFirst", "121"));
			BAIUM_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("BaiumRespSecond", "8"));
			ANNOUNCE_SPAWN_BAIUM = Boolean.parseBoolean(bossSettings.getProperty("AnnounceBaiumSpawn", "false"));
			//============================================================
			//Core
			CORE_RESP_MINION = Integer.parseInt(bossSettings.getProperty("CoreRespMinion", "60"));
			CORE_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("CoreRespFirst", "37"));
			CORE_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("CoreRespSecond", "42"));
			//============================================================
			//Queen Ant
			QA_RESP_NURSE = Integer.parseInt(bossSettings.getProperty("QueenAntRespNurse", "60"));
			QA_RESP_ROYAL = Integer.parseInt(bossSettings.getProperty("QueenAntRespRoyal", "120"));
			QA_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("QueenAntRespFirst", "19"));
			QA_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("QueenAntRespSecond", "35"));
			QA_TELEPORT_HIGHTERS = Boolean.parseBoolean(bossSettings.getProperty("QueenAntTeleportHigh", "false"));
			ANNOUNCE_SPAWN_QA = Boolean.parseBoolean(bossSettings.getProperty("AnnounceQueenAntSpawn", "false"));
			//============================================================
			//High Priestess van Halter
			HPH_FIXINTERVALOFHALTER = Integer.parseInt(bossSettings.getProperty("FixIntervalOfHalter", "172800"));
			if(HPH_FIXINTERVALOFHALTER < 300 || HPH_FIXINTERVALOFHALTER > 864000)
			{
				HPH_FIXINTERVALOFHALTER = 172800;
			}
			HPH_FIXINTERVALOFHALTER *= 6000;

			HPH_RANDOMINTERVALOFHALTER = Integer.parseInt(bossSettings.getProperty("RandomIntervalOfHalter", "86400"));
			if(HPH_RANDOMINTERVALOFHALTER < 300 || HPH_RANDOMINTERVALOFHALTER > 864000)
			{
				HPH_RANDOMINTERVALOFHALTER = 86400;
			}
			HPH_RANDOMINTERVALOFHALTER *= 6000;

			HPH_APPTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("AppTimeOfHalter", "20"));
			if(HPH_APPTIMEOFHALTER < 5 || HPH_APPTIMEOFHALTER > 60)
			{
				HPH_APPTIMEOFHALTER = 20;
			}
			HPH_APPTIMEOFHALTER *= 6000;

			HPH_ACTIVITYTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("ActivityTimeOfHalter", "21600"));
			if(HPH_ACTIVITYTIMEOFHALTER < 7200 || HPH_ACTIVITYTIMEOFHALTER > 86400)
			{
				HPH_ACTIVITYTIMEOFHALTER = 21600;
			}
			HPH_ACTIVITYTIMEOFHALTER *= 1000;

			HPH_FIGHTTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("FightTimeOfHalter", "7200"));
			if(HPH_FIGHTTIMEOFHALTER < 7200 || HPH_FIGHTTIMEOFHALTER > 21600)
			{
				HPH_FIGHTTIMEOFHALTER = 7200;
			}
			HPH_FIGHTTIMEOFHALTER *= 6000;

			HPH_CALLROYALGUARDHELPERCOUNT = Integer.parseInt(bossSettings.getProperty("CallRoyalGuardHelperCount", "6"));
			if(HPH_CALLROYALGUARDHELPERCOUNT < 1 || HPH_CALLROYALGUARDHELPERCOUNT > 6)
			{
				HPH_CALLROYALGUARDHELPERCOUNT = 6;
			}

			HPH_CALLROYALGUARDHELPERINTERVAL = Integer.parseInt(bossSettings.getProperty("CallRoyalGuardHelperInterval", "10"));
			if(HPH_CALLROYALGUARDHELPERINTERVAL < 1 || HPH_CALLROYALGUARDHELPERINTERVAL > 60)
			{
				HPH_CALLROYALGUARDHELPERINTERVAL = 10;
			}
			HPH_CALLROYALGUARDHELPERINTERVAL *= 6000;

			HPH_INTERVALOFDOOROFALTER = Integer.parseInt(bossSettings.getProperty("IntervalOfDoorOfAlter", "5400"));
			if(HPH_INTERVALOFDOOROFALTER < 60 || HPH_INTERVALOFDOOROFALTER > 5400)
			{
				HPH_INTERVALOFDOOROFALTER = 5400;
			}
			HPH_INTERVALOFDOOROFALTER *= 6000;

			HPH_TIMEOFLOCKUPDOOROFALTAR = Integer.parseInt(bossSettings.getProperty("TimeOfLockUpDoorOfAltar", "180"));
			if(HPH_TIMEOFLOCKUPDOOROFALTAR < 60 || HPH_TIMEOFLOCKUPDOOROFALTAR > 600)
			{
				HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
			}
			HPH_TIMEOFLOCKUPDOOROFALTAR *= 6000;
			//============================================================
			//Frintezza
			FRINTEZZA_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("FrintezzaSpawn", "121"));
			if (FRINTEZZA_RESP_FIRST < 1 || FRINTEZZA_RESP_FIRST > 480) {
				FRINTEZZA_RESP_FIRST = 121;
			}
			FRINTEZZA_RESP_FIRST = FRINTEZZA_RESP_FIRST * 3600000;

			FRINTEZZA_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("FrintezzaSpawnRandom", "8"));
			if (FRINTEZZA_RESP_SECOND < 1 || FRINTEZZA_RESP_SECOND > 192) {
				FRINTEZZA_RESP_SECOND = 8;
			}
			FRINTEZZA_RESP_SECOND = FRINTEZZA_RESP_SECOND * 3600000;
			//============================================================
			//Zaken
			ZAKEN_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("ZakenSpawn", "36"));
			if (ZAKEN_RESP_FIRST < 1 || ZAKEN_RESP_FIRST > 240) {
				ZAKEN_RESP_FIRST = 36;
			}
			ZAKEN_RESP_FIRST = ZAKEN_RESP_FIRST * 3600000;

			ZAKEN_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("ZakenSpawnRandom", "8"));
			if (ZAKEN_RESP_SECOND < 1 || ZAKEN_RESP_SECOND > 120) {
				ZAKEN_RESP_SECOND = 8;
			}
			ZAKEN_RESP_SECOND = ZAKEN_RESP_SECOND * 3600000;

			ZAKEN_BANISH_CHEATERS = Boolean.parseBoolean(bossSettings.getProperty("ZakenBanishCheaters", "true"));

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + BOSS + " File.");
		}
	}

	//============================================================
	public static boolean SCRIPT_DEBUG;
	public static boolean SCRIPT_ALLOW_COMPILATION;
	public static boolean SCRIPT_CACHE;
	public static boolean SCRIPT_ERROR_LOG;

	//============================================================
	public static void loadScriptConfig()
	{
		final String SCRIPT = FService.SCRIPT_FILE;

		_log.info("Loading: " + SCRIPT + ".");
		try
		{
			Properties scriptSetting = new Properties();
			InputStream is = new FileInputStream(new File(SCRIPT));
			scriptSetting.load(is);
			is.close();

			SCRIPT_DEBUG = Boolean.valueOf(scriptSetting.getProperty("EnableScriptDebug", "false"));
			SCRIPT_ALLOW_COMPILATION = Boolean.valueOf(scriptSetting.getProperty("AllowCompilation", "true"));
			SCRIPT_CACHE = Boolean.valueOf(scriptSetting.getProperty("UseCache", "true"));
			SCRIPT_ERROR_LOG = Boolean.valueOf(scriptSetting.getProperty("EnableScriptErrorLog", "true"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SCRIPT + " File.");
		}
	}

	//============================================================
	public static boolean POWERPAK_ENABLED;

	//============================================================
	public static void loadPowerPak()
	{
		final String POWERPAK = FService.POWERPAK_FILE;

		_log.info("Loading: " + POWERPAK + ".");
		try
		{
			L2Properties p = new L2Properties(POWERPAK);
			POWERPAK_ENABLED = Boolean.parseBoolean(p.getProperty("PowerPakEnabled", "true"));
		}
		catch(Exception e)
		{
			_log.error("Failed to load " + POWERPAK + " file");
		}
	}

	//============================================================
	
	public static Map<String, List<String>> EXTENDERS;

	//============================================================
	public static void loadExtendersConfig()
	{
		final String EXTENDER_FILE = FService.EXTENDER_FILE;

		_log.info("Loading: " + EXTENDER_FILE + ".");

		EXTENDERS = new FastMap<String, List<String>>();
		File f = new File(EXTENDER_FILE);
		if(f.exists())
		{
			try
			{
				LineNumberReader lineReader = new LineNumberReader(new BufferedReader(new FileReader(f)));
				String line;
				while((line = lineReader.readLine()) != null)
				{
					int iPos = line.indexOf("#");

					if(iPos != -1)
					{
						line = line.substring(0, iPos);
					}

					if(line.trim().length() == 0)
					{
						continue;
					}

					iPos = line.indexOf("=");
					if(iPos != -1)
					{
						String baseName = line.substring(0, iPos).trim();
						String className = line.substring(iPos + 1).trim();

						if(EXTENDERS.get(baseName) == null)
						{
							EXTENDERS.put(baseName, new FastList<String>());
						}

						EXTENDERS.get(baseName).add(className);
					}
				}
			}
			catch(Exception e)
			{
				_log.error("Failed to Load " + EXTENDER_FILE + " File.");
			}
		}
	}
	
	//============================================================
	public static long AUTOSAVE_INITIAL_TIME;
	public static long AUTOSAVE_DELAY_TIME;
	public static long DEADLOCKCHECK_INTIAL_TIME;
	public static long DEADLOCKCHECK_DELAY_TIME;
	//============================================================
	public static void loadDaemonsConf()
	{
		final String DAEMONS = FService.DAEMONS_FILE;
		
		_log.info("Loading: " + DAEMONS + ".");
		
		try
		{
			L2Properties p = new L2Properties(DAEMONS);
			
			AUTOSAVE_INITIAL_TIME = Long.parseLong(p.getProperty("AutoSaveInitial", "300000"));
			AUTOSAVE_DELAY_TIME = Long.parseLong(p.getProperty("AutoSaveDelay", "900000"));
			DEADLOCKCHECK_INTIAL_TIME = Long.parseLong(p.getProperty("DeadLockCheck", "0"));
			DEADLOCKCHECK_DELAY_TIME = Long.parseLong(p.getProperty("DeadLockDelay", "0"));
		}
		catch(Exception e)
		{
			_log.error("Failed to load " + DAEMONS + " file.");
		}
	}

	//==============================================================
	/**
	 * Loads all Filter Words
	 */
	//==============================================================
	public static void loadFilter()
	{
		final String FILTER_FILE = FService.FILTER_FILE;

		_log.info("Loading: " + FILTER_FILE + ".");
		try
		{
			LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(FILTER_FILE))));
			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				FILTER_LIST.add(line.trim());
			}
			_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + FILTER_FILE + " File.");
		}
	}
        
        public static void loadTradeFilter() 
        {
            	final String FILTER_TRADE_FILE = FService.FILTER_TRADE_FILE;

		_log.info("Loading: " + FILTER_TRADE_FILE + ".");
		try
		{
			LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(FILTER_TRADE_FILE))));
			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				FILTER_TRADE_LIST.add(line.trim());
			}
			_log.info("Loaded " + FILTER_TRADE_LIST.size() + " Filter Trade title Words.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + FILTER_TRADE_FILE + " File.");
		}
        }

	//============================================================
	public static ArrayList<String> QUESTION_LIST = new ArrayList<String>();
	public static void loadQuestion()
	{
		final String QUESTION_FILE = FService.QUESTION_FILE;

		QUESTION_LIST.clear();
		_log.info("Loading: " + QUESTION_FILE + ".");
		try
		{
			LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(QUESTION_FILE))));
			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() < 10 || line.trim().length() > 16 || line.startsWith("#"))
				{
					continue;
				}
				QUESTION_LIST.add(line.trim());
			}
			_log.info("Loaded " + QUESTION_LIST.size() + " Question Words.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + QUESTION_FILE + " File.");
		}
	}
	//============================================================


	//============================================================
	private static final String HEXID_FILE = FService.HEXID_FILE;
	//============================================================
	public static int SERVER_ID;
	public static byte[] HEX_ID;

	//============================================================
	public static void loadHexed()
	{
		_log.info("Loading: " + HEXID_FILE + ".");
		try
		{
			Properties Settings = new Properties();
			InputStream is = new FileInputStream(new File(HEXID_FILE));
			Settings.load(is);
			is.close();
			SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
			HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
		}
		catch(Exception e)
		{
			_log.error("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
		}

	}

	//============================================================
	public static enum OnSuccessLoginAction
	{
		COMMAND, NOTIFY
	};

	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static File DATAPACK_ROOT;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int IP_UPDATE_TIME;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SHOW_LICENCE;
	public static boolean ANTI_BRUT_PROTECTION;
	public static boolean USE_ACTIVATOR_PROTECTION;
	public static boolean USE_AUTO_REBOOT;
	public static int AUTO_REBOOT_TIMER_TASK;
	public static boolean FORCE_GGAUTH;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static String NETWORK_IP_LIST;
	public static long SESSION_TTL;
	public static int MAX_LOGINSESSIONS;
	public static int MIN_MEMORY_LEES;
	public static boolean DDOS_PROTECTION_ENABLED;
	public static OnSuccessLoginAction ON_SUCCESS_LOGIN_ACTION;
	public static String ON_SUCCESS_LOGIN_COMMAND_LS;
        public static boolean FAKE_SERVER_LIST;
        public static String FAKE_SERVER_LIST_TYPE;
        public static double FAKE_SERVER_LIST_PARAM1;
        public static int FAKE_SERVER_LIST_PARAM2;
        

	//============================================================
	public static void loadLoginStartConfig()
	{
		final String LOGIN = FService.LOGIN_CONFIGURATION_FILE;

		_log.info("Loading: " + LOGIN + ".");
		try
		{
			Properties serverSettings = new Properties();
			Instruments iset = new Instruments();
			InputStream is = new FileInputStream(new File(LOGIN));
			serverSettings.load(is);
			iset.load(is);
			is.close();

			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9013"));

			LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
			PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));

			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));

			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));

			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();

			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "localhost");
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "localhost");

			DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));

			DDOS_PROTECTION_ENABLED = Boolean.parseBoolean(serverSettings.getProperty("DDoSProtection","true"));
			ON_SUCCESS_LOGIN_ACTION = OnSuccessLoginAction.valueOf(serverSettings.getProperty("OnSelectServer","NOTIFY").toUpperCase());
			ON_SUCCESS_LOGIN_COMMAND_LS = serverSettings.getProperty("OnSelectServerCommandLS","");

			DATABASE_TIMEOUT = Integer.parseInt(serverSettings.getProperty("TimeOutConDb", "0"));
			DATABASE_STATEMENT = Integer.parseInt(serverSettings.getProperty("MaximumDbStatement", "100"));

			SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "false"));
			IP_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("IpUpdateTime", "15"));
			FORCE_GGAUTH = Boolean.parseBoolean(serverSettings.getProperty("ForceGGAuth", "false"));
			ANTI_BRUT_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("AntiBrut", "False"));
			USE_ACTIVATOR_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("UseActivatorProtection", "False"));
			USE_AUTO_REBOOT = Boolean.parseBoolean(serverSettings.getProperty("EnableAutoRebootLogin", "False"));
			AUTO_REBOOT_TIMER_TASK = Integer.parseInt(serverSettings.getProperty("AutoRebootLoginTimer", "60"));
                        
                        FAKE_SERVER_LIST = Boolean.parseBoolean(serverSettings.getProperty("FakeServerList", "False"));
                        FAKE_SERVER_LIST_TYPE = serverSettings.getProperty("FakeServerListType", "STATIC");
                        FAKE_SERVER_LIST_PARAM1 = Double.parseDouble(serverSettings.getProperty("FakeServerListParam1", "2000"));
                        FAKE_SERVER_LIST_PARAM2 = Integer.parseInt(serverSettings.getProperty("FakeServerListParam2", "5000"));
			
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "True"));

			FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
			DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
			DEVELOPER = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));

			NETWORK_IP_LIST = serverSettings.getProperty("NetworkList", "");
			SESSION_TTL = Long.parseLong(serverSettings.getProperty("SessionTTL", "25000"));
			MAX_LOGINSESSIONS = Integer.parseInt(serverSettings.getProperty("MaxSessions","200"));
                        MIN_MEMORY_LEES = Integer.parseInt(serverSettings.getProperty("MinMemoryFreeUnderCleanup","100"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + LOGIN + " File.");
		}

	}

	//============================================================
	public static List<String> BANS = new FastList<String>();

	//============================================================
	public static void loadBanIPConfig()
	{
		final String BAN_IP_FILE = FService.BANNED_IP;

		_log.info("Loading: " + BAN_IP_FILE + ".");
		try
		{
			Instruments banSettings = new Instruments();
			InputStream is = new FileInputStream(new File(BAN_IP_FILE));
			banSettings.load(is);
			is.close();

			BANS = banSettings.getStringList("IP", "", ",");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + BAN_IP_FILE + " File.");
		}

	}

	//============================================================
	public static void loadTellLoginConfig()
	{
		final String TELNET_FILE = FService.TELNET_FILE;

		_log.info("Loading: " + TELNET_FILE + ".");
		try
		{
			Properties telnetSettings = new Properties();
			InputStream is = new FileInputStream(new File(TELNET_FILE));
			telnetSettings.load(is);
			is.close();

			IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + TELNET_FILE + " File.");
		}

	}

	// Enums
	/** Enumeration for type of ID Factory */
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}

	/** Enumeration for type of maps object */
	public static enum ObjectMapType
	{
		WorldObjectTree,
		WorldObjectMap
	}

	/** Enumeration for type of set object */
	public static enum ObjectSetType
	{
		L2ObjectHashSet,
		WorldObjectSet
	}

	// load config
	public static void load()
	{
		if(ServerType.serverMode == ServerType.MODE_GAMESERVER)
		{
			_log.info("Loading Game Server config");
			loadHexed();

			//load network
			loadServerConfig();

			//load system
			loadIdFactoryConfig();

			//load developer parameters
			loadDevConfig();

			//head
			loadOptionsConfig();
			loadOtherConfig();
			loadRatesConfig();
			loadAltConfig();
			load7sConfig();
			loadCHConfig();
			loadElitCHConfig();
			loadOlympConfig();
			loadEnchantConfig();
			loadBossConfig();
			loadPersonalConfig();

			//head functions
			loadL2SCORIAConfig();
			loadPHYSICSConfig();
			loadAccessConfig();
			loadPvpConfig();
			loadCraftConfig();

			//protect
			loadFloodConfig();
			loadPacketConfig();
			loadPOtherConfig();

			//geo&path
			loadgeodataConfig();

			//fun
			loadChampionConfig();
			loadWeddingConfig();
			loadREBIRTHConfig();
			loadAWAYConfig();
			loadBankingConfig();
			loadPCBPointConfig();
			loadOfflineConfig();
			loadFunEvents();

			//other
			loadTellConfig();
			loadDPVersionConfig();
			loadServerVersionConfig();
			loadExtendersConfig();
			loadDaemonsConf();
			loadWebDaemonsConfig();

			if(Config.USE_SAY_FILTER)
			{
				loadFilter();
			}
                        if(Config.USE_TRADE_WORDS_FILTER) 
                        {
                                loadTradeFilter();
                        }
			if(Config.BOT_PROTECTOR)
			{
				loadQuestion();
			}
		}
		else if(ServerType.serverMode == ServerType.MODE_LOGINSERVER)
		{
			_log.info("Loading Login Server config");
			loadLoginStartConfig();
			loadBanIPConfig();
			loadTellLoginConfig();
		}
		else
		{
			_log.error("Could not Load Config: server mode was not set");
		}
	}

	/**
	 * Set a new value to a game parameter from the admin console.
	 * 
	 * @param pName (String) : name of the parameter to change
	 * @param pValue (String) : new value of the parameter
	 * @return boolean : true if modification has been made
	 * @link useAdminCommand
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		// Server settings
		if(pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateSp"))
		{
			RATE_SP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RatePartyXp"))
		{
			RATE_PARTY_XP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RatePartySp"))
		{
			RATE_PARTY_SP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateQuestsReward"))
		{
			RATE_QUESTS_REWARD = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropAdena"))
		{
			RATE_DROP_ADENA = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateConsumableCost"))
		{
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropItems"))
		{
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropSealStones"))
		{
			RATE_DROP_SEAL_STONES = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropSpoil"))
		{
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropManor"))
		{
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("RateDropQuest"))
		{
			RATE_DROP_QUEST = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateKarmaExpLost"))
		{
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
		{
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerDropLimit"))
		{
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerRateDrop"))
		{
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerRateDropItem"))
		{
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerRateDropEquip"))
		{
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
		{
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("KarmaDropLimit"))
		{
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("KarmaRateDrop"))
		{
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("KarmaRateDropItem"))
		{
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("KarmaRateDropEquip"))
		{
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
		{
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("DestroyPlayerDroppedItem"))
		{
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("DestroyEquipableItem"))
		{
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("SaveDroppedItem"))
		{
			SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad"))
		{
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("SaveDroppedItemInterval"))
		{
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ClearDroppedItemTable"))
		{
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("PreciseDropCalculation"))
		{
			PRECISE_DROP_CALCULATION = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("MultipleItemDrop"))
		{
			MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("CoordSynchronize"))
		{
			COORD_SYNCHRONIZE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("DeleteCharAfterDays"))
		{
			DELETE_DAYS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowDiscardItem"))
		{
			ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowFreight"))
		{
			ALLOW_FREIGHT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowWarehouse"))
		{
			ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowWear"))
		{
			ALLOW_WEAR = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("WearDelay"))
		{
			WEAR_DELAY = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("WearPrice"))
		{
			WEAR_PRICE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowWater"))
		{
			ALLOW_WATER = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowRentPet"))
		{
			ALLOW_RENTPET = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowBoat"))
		{
			ALLOW_BOAT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowCursedWeapons"))
		{
			ALLOW_CURSED_WEAPONS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowManor"))
		{
			ALLOW_MANOR = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("BypassValidation"))
		{
			BYPASS_VALIDATION = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("CommunityType"))
		{
			COMMUNITY_TYPE = pValue.toLowerCase();
		}
		else if(pName.equalsIgnoreCase("BBSDefault"))
		{
			BBS_DEFAULT = pValue;
		}
		else if(pName.equalsIgnoreCase("ShowLevelOnCommunityBoard"))
		{
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ShowStatusOnCommunityBoard"))
		{
			SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard"))
		{
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("NamePerRowOnCommunityBoard"))
		{
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ShowNpcLevel"))
		{
			SHOW_NPC_LVL = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ForceInventoryUpdate"))
		{
			FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
		{
			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumOnlineUsers"))
		{
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("UnknownPacketProtection"))
		{
			ENABLE_UNK_PACKET_PROTECTION = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("UnknownPacketsBeforeBan"))
		{
			MAX_UNKNOWN_PACKETS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("UnknownPacketsPunishment"))
		{
			UNKNOWN_PACKETS_PUNiSHMENT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ZoneTown"))
		{
			ZONE_TOWN = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumUpdateDistance"))
		{
			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MinimumUpdateTime"))
		{
			MINIMUN_UPDATE_TIME = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("CheckKnownList"))
		{
			CHECK_KNOWN = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("KnownListForgetDelay"))
		{
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("UseDeepBlueDropRules"))
		{
			DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowGuards"))
		{
			ALLOW_GUARDS = Boolean.valueOf(pValue);
		}
                else if(pName.equalsIgnoreCase("AttackAgressiveMonsters"))
                {
                        ATTACK_AGRESSIVE_MOBS = Boolean.valueOf(pValue);
                }
		else if(pName.equalsIgnoreCase("CancelLesserEffect"))
		{
			EFFECT_CANCELING = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("WyvernSpeed"))
		{
			WYVERN_SPEED = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("StriderSpeed"))
		{
			STRIDER_SPEED = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
		{
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
		{
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
		{
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
		{
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
		{
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
		{
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaximumFreightSlots"))
		{
			FREIGHT_SLOTS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationNGSkillChance"))
		{
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationMidSkillChance"))
		{
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationHighSkillChance"))
		{
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationTopSkillChance"))
		{
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationBaseStatChance"))
		{
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationNGGlowChance"))
		{
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationMidGlowChance"))
		{
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationHighGlowChance"))
		{
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AugmentationTopGlowChance"))
		{
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("EnchantSafeMax"))
		{
			ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("EnchantSafeMaxFull"))
		{
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("GMOverEnchant"))
		{
			GM_OVER_ENCHANT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("HpRegenMultiplier"))
		{
			HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("MpRegenMultiplier"))
		{
			MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("CpRegenMultiplier"))
		{
			CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
		{
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
		{
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("RaidPhysicalDefenceMultiplier"))
		{
			RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if(pName.equalsIgnoreCase("RaidMagicalDefenceMultiplier"))
		{
			RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if(pName.equalsIgnoreCase("RaidMinionRespawnTime"))
		{
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("StartingAdena"))
		{
			STARTING_ADENA = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("UnstuckInterval"))
		{
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerSpawnProtection"))
		{
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
		{
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PartyXpCutoffMethod"))
		{
			PARTY_XP_CUTOFF_METHOD = pValue;
		}
		else if(pName.equalsIgnoreCase("PartyXpCutoffPercent"))
		{
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("PartyXpCutoffLevel"))
		{
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("RespawnRestoreCP"))
		{
			RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		}
		else if(pName.equalsIgnoreCase("RespawnRestoreHP"))
		{
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		}
		else if(pName.equalsIgnoreCase("RespawnRestoreMP"))
		{
			RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
		}
		else if(pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf"))
		{
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaxPvtStoreSlotsOther"))
		{
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("StoreSkillCooltime"))
		{
			STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AnnounceMammonSpawn"))
		{
			ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameTiredness"))
		{
			ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameCreation"))
		{
			ALT_GAME_CREATION = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameCreationSpeed"))
		{
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameCreationXpRate"))
		{
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameCreationSpRate"))
		{
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("AltWeightLimit"))
		{
			ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		}
		else if(pName.equalsIgnoreCase("AltBlacksmithUseRecipes"))
		{
			ALT_BLACKSMITH_USE_RECIPES = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameSkillLearn"))
		{
			ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("RemoveCastleCirclets"))
		{
			REMOVE_CASTLE_CIRCLETS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		else if(pName.equalsIgnoreCase("AltShieldBlocks"))
		{
			ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
		{
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("Delevel"))
		{
			ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("MagicFailures"))
		{
			ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameMobAttackAI"))
		{
			ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltMobAgroInPeaceZone"))
		{
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameExponentXp"))
		{
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameExponentSp"))
		{
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowClassMasters"))
		{
			ALLOW_CLASS_MASTERS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameFreights"))
		{
			ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltGameFreightPrice"))
		{
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AltPartyRange"))
		{
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AltPartyRange2"))
		{
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("CraftingEnabled"))
		{
			IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("LifeCrystalNeeded"))
		{
			LIFE_CRYSTAL_NEEDED = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("SpBookNeeded"))
		{
			SP_BOOK_NEEDED = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AutoLoot"))
		{
			AUTO_LOOT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AutoLootHerbs"))
		{
			AUTO_LOOT_HERBS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaFlagPlayerCanUseBuffer"))
		{
			FLAGED_PLAYER_USE_BUFFER = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltRequireCastleForDawn"))
		{
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltRequireClanCastle"))
		{
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltFreeTeleporting"))
		{
			ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
		{
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie"))
		{
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH"))
		{
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("DwarfRecipeLimit"))
		{
			DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("CommonRecipeLimit"))
		{
			COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionEnable"))
		{
			L2JMOD_CHAMPION_ENABLE = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionFrequency"))
		{
			L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionMinLevel"))
		{
			L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionMaxLevel"))
		{
			L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionHp"))
		{
			L2JMOD_CHAMPION_HP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionHpRegen"))
		{
			L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionRewards"))
		{
			L2JMOD_CHAMPION_REWARDS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionAdenasRewards"))
		{
			L2JMOD_CHAMPION_ADENAS_REWARDS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionAtk"))
		{
			L2JMOD_CHAMPION_ATK = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionSpdAtk"))
		{
			L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionRewardItem"))
		{
			L2JMOD_CHAMPION_REWARD = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionRewardItemID"))
		{
			L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ChampionRewardItemQty"))
		{
			L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowWedding"))
		{
			L2JMOD_ALLOW_WEDDING = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingPrice"))
		{
			L2JMOD_WEDDING_PRICE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingPunishInfidelity"))
		{
			L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingTeleport"))
		{
			L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingTeleportPrice"))
		{
			L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingTeleportDuration"))
		{
			L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingAllowSameSex"))
		{
			L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingFormalWear"))
		{
			L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("WeddingDivorceCosts"))
		{
			L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MinKarma"))
		{
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaxKarma"))
		{
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("XPDivider"))
		{
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("BaseKarmaLost"))
		{
			KARMA_LOST_BASE = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("CanGMDropEquipment"))
		{
			KARMA_DROP_GM = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
		{
			KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
		}
                else if (pName.equalsIgnoreCase("RemovePvpFlagsOnLostKarma"))
                {
                        REMOVE_PVP_FLAG_ON_LOST_KARMA = Boolean.valueOf(pValue);
                }
		else if(pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
		{
			KARMA_PK_LIMIT = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PvPVsNormalTime"))
		{
			PVP_NORMAL_TIME = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("PvPVsPvPTime"))
		{
			PVP_PVP_TIME = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("GlobalChat"))
		{
			DEFAULT_GLOBAL_CHAT = ChatMode.valueOf(pValue.toUpperCase());
		}
		else if(pName.equalsIgnoreCase("TradeChat"))
		{
			DEFAULT_TRADE_CHAT = ChatMode.valueOf(pValue.toUpperCase());
		}
		else if(pName.equalsIgnoreCase("MenuStyle"))
		{
			GM_ADMIN_MENU_STYLE = pValue;
		}
		else if(pName.equalsIgnoreCase("AllowVersionCommand"))
		{
			ALLOW_VERSION_COMMAND = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("MaxPAtkSpeed"))
		{
			MAX_PATK_SPEED = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("MaxMAtkSpeed"))
		{
			MAX_MATK_SPEED = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("ServerNameEnabled"))
		{
			ALT_Server_Name_Enabled = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ServerName"))
		{
			ALT_Server_Name = String.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("FlagedPlayerCanUseGK"))
		{
			FLAGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("AddExpAtPvp"))
		{
			ADD_EXP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AddSpAtPvp"))
		{
			ADD_SP = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AbortRestart"))
		{
			ABORT_RR = pValue;
		}
		else if(pName.equalsIgnoreCase("CastleShieldRestriction"))
		{
			CASTLE_SHIELD = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ClanHallShieldRestriction"))
		{
			CLANHALL_SHIELD = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("ApellaArmorsRestriction"))
		{
			APELLA_ARMORS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("OathArmorsRestriction"))
		{
			OATH_ARMORS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("CastleLordsCrownRestriction"))
		{
			CASTLE_CROWN = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("CastleCircletsRestriction"))
		{
			CASTLE_CIRCLETS = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowRaidBossPetrified"))
		{
			ALLOW_RAID_BOSS_PUT = Boolean.valueOf(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowLowLevelTrade"))
		{
			ALLOW_LOW_LEVEL_TRADE = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("AllowPotsInPvP"))
		{
			ALLOW_POTS_IN_PVP = Boolean.parseBoolean(pValue);
		}
		else if(pName.equalsIgnoreCase("StartingAncientAdena"))
		{
			STARTING_AA = Integer.parseInt(pValue);
		}
		else if(pName.equalsIgnoreCase("AnnouncePvPKill") && !ANNOUNCE_ALL_KILL)
		{
			ANNOUNCE_PVP_KILL = Boolean.valueOf(pValue); // Set announce Pvp value 
		}
		else if(pName.equalsIgnoreCase("AnnouncePkKill") && !ANNOUNCE_ALL_KILL)
		{
			ANNOUNCE_PK_KILL = Boolean.valueOf(pValue); // Set announce Pk value 
		}
		else if(pName.equalsIgnoreCase("AnnounceAllKill") && !ANNOUNCE_PVP_KILL && !ANNOUNCE_PK_KILL)
		{
			ANNOUNCE_ALL_KILL = Boolean.valueOf(pValue); // Set announce kill value
		}
		else if(pName.equalsIgnoreCase("DisableWeightPenalty"))
		{
			DISABLE_WEIGHT_PENALTY = Boolean.valueOf(pValue);
		}
		else
			return false;
		return true;
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 * 
	 * @param string (String) : hexadecimal ID of the server to store
	 * @see HEXID_FILE
	 * @see saveHexid(String string, String fileName)
	 * @link LoginServerThread
	 */
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 * 
	 * @param hexId (String) : hexadecimal ID of the server to store
	 * @param fileName (String) : name of the properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(fileName);
			//Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch(Exception e)
		{
			_log.error("Failed to save hex id to " + fileName + " File.");
			e.printStackTrace();
		}
	}

	/**
	 * Clear all buffered filter words on memory.
	 */
	public static void unallocateFilterBuffer()
	{
		_log.info("Cleaning Chat Filter..");
		FILTER_LIST.clear();
	}
}
