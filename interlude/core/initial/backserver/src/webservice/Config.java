package webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author zenn
 * 
 */
public final class Config
{
    public static final String  CONFIGURATION_FILE    = "./config/config.properties";
    public static String DATABASE_DRIVER_LS;
    public static String DATABASE_URL_LS;
    public static String DATABASE_LOGIN_LS;
    public static String DATABASE_PASSWORD_LS;
    public static int DATABASE_MAX_CONNECTIONS_LS;
    public static String DATABASE_DRIVER_GS;
    public static String DATABASE_URL_GS;
    public static String DATABASE_LOGIN_GS;
    public static String DATABASE_PASSWORD_GS;
    public static int DATABASE_MAX_CONNECTIONS_GS;
    public static int PORT;
    public static String WEBISTE_REMOTE_IP;
    public static boolean USE_KEEP_ALIVE;
    public static String GS_QUERY_ONLINE_COUNT;
    public static int GAMEPORT;
    public static int LOGINPORT;
    public static String INIT_KEY;
    public static String LS_QUERY_CHECK_REG;
    public static String LS_QUERY_REG;
    public static String LS_QUERY_ACC_NUM;
    public static String GS_QUERY_CHAR_NUM;
    public static String GS_QUERY_TOP_PVP;
    public static String GS_QUERY_TOP_PK;
    public static String GS_QUERY_TOP_LEVEL;
    public static String LS_CP_QUERY;
    public static String GS_QUERY_TOP_CLAN;
    public static String GS_QUERY_CASTLE;
    public static String LS_CP_AUTH_REQUEST;
    public static String LS_CP_GET_PASS_SQ;
    public static String LS_CP_UPDATE_SQ;
    public static String LS_CP_GET_ALLOWED_IP;
    public static String LS_CP_GET_LAST_IP;
    public static String LS_RECOVERY_CHECK_LOGIN_MAIL;
    public static String LS_RECOVERY_CHECK_REMOTE_IP;
    public static String LS_RECOVERY_CHECK_LOGIN_DAILY;
    public static String LS_RECOVERY_ASSIGNED_INSERT;
    public static String LS_RECOVERY_GET_VALUE_BY_HASH;
    public static String LS_RECOVERY_UPDATE_PASSWORD;
    public static String BACKSERVER_IP;
    public static String LS_RECOVERY_CLEAR_LOGIN_VALUE;
    public static String LS_UPDATE_EMAIL_CP;
    public static String GS_GET_CHAR_LIST;
    public static String GS_GET_BANNED_CHAR_LIST;
    public static String GS_GET_CLEAR_CHAR_LIST;
    public static String LS_GET_ACCOUNT_CP_BALANCE;
    public static String GS_GET_TP_TIME_CHAR;
    public static String GS_CHECK_CHAR_STATUS;
    public static String GS_UPDATE_XYZ_CORDS;
    public static String LS_GET_CP_PAYMENT_LOG;
    public static String GS_VALIDATE_OBJECT_CHAR;
    public static String LS_LOOSE_ACC_MONEY;
    // todo
    public static String GS_GET_CHAR_OBJ_ID;
    public static String GS_CHECK_CHAR_HAS_ITEM;
    public static String GS_UPDATE_ITEM_COUNT;
    public static String GS_GET_LAST_ITEM_ID;
    public static String GS_INSERT_ITEM_BY_ID;

	public static void load()
	{
			try
			{
                                Properties serverSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(CONFIGURATION_FILE));
				serverSettings.load(is);
				is.close();

	            DATABASE_DRIVER_LS             = serverSettings.getProperty("Driver_ls", "com.mysql.jdbc.Driver");
	            DATABASE_URL_LS                = serverSettings.getProperty("URL_ls", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN_LS              = serverSettings.getProperty("Login_ls", "root");
	            DATABASE_PASSWORD_LS           = serverSettings.getProperty("Password_ls", "");
	            DATABASE_MAX_CONNECTIONS_LS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections_ls", "10"));
                    PORT                           = Integer.parseInt(serverSettings.getProperty("BACK_PORT", "5555"));
                    BACKSERVER_IP                  = serverSettings.getProperty("BackServerIP", "127.0.0.1");
                    DATABASE_DRIVER_GS             = serverSettings.getProperty("Driver_gs", "com.mysql.jdbc.Driver");
	            DATABASE_URL_GS                = serverSettings.getProperty("URL_gs", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN_GS              = serverSettings.getProperty("Login_gs", "root");
	            DATABASE_PASSWORD_GS           = serverSettings.getProperty("Password_gs", "");
	            DATABASE_MAX_CONNECTIONS_GS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections_gs", "10"));
                    GS_QUERY_ONLINE_COUNT          = serverSettings.getProperty("OnlineQueryGS", "SELECT COUNT(*) FROM characters WHERE online != '0'");
                    GAMEPORT                       = Integer.parseInt(serverSettings.getProperty("GAME_PORT", "7777"));
                    LOGINPORT                      = Integer.parseInt(serverSettings.getProperty("LOGIN_PORT", "2106"));
                    WEBISTE_REMOTE_IP              = serverSettings.getProperty("WebsiteIp", "127.0.0.1");
                    USE_KEEP_ALIVE                 = Boolean.parseBoolean(serverSettings.getProperty("UseKeepAlivePinging", "false"));
                    INIT_KEY                       = serverSettings.getProperty("INIT_KEY", "abcd");
                    LS_QUERY_CHECK_REG             = serverSettings.getProperty("ValidateReg", "SELECT COUNT(*) FROM accounts WHERE login = '<LOGIN>'");
                    LS_QUERY_REG                   = serverSettings.getProperty("InsertReg", "INSERT INTO accounts (login, password, email) VALUES (?,?,?)");
                    LS_QUERY_ACC_NUM               = serverSettings.getProperty("AccountNum", "SELECT COUNT(*) FROM accounts");
                    GS_QUERY_CHAR_NUM              = serverSettings.getProperty("CharNum", "SELECT COUNT(*) FROM characters");
                    GS_QUERY_TOP_PVP               = serverSettings.getProperty("TopPvpChars", "SELECT a.char_name,a.level,a.pvpkills,a.pkkills,a.online,b.clan_name,a.clanid,b.clan_id FROM characters a left outer join clan_data b on b.clan_id = a.clanid ORDER by pvpkills DESC LIMIT 25");
                    GS_QUERY_TOP_PK                = serverSettings.getProperty("TopPkChars", "SELECT a.char_name,a.level,a.pvpkills,a.pkkills,a.online,b.clan_name,a.clanid,b.clan_id FROM characters a left outer join clan_data b on b.clan_id = a.clanid ORDER by pkkills DESC LIMIT 25");
                    GS_QUERY_TOP_LEVEL             = serverSettings.getProperty("TopLvlChars", "SELECT a.char_name,a.level,a.pvpkills,a.pkkills,a.online,b.clan_name,a.clanid,b.clan_id FROM characters a left outer join clan_data b on b.clan_id = a.clanid ORDER by level DESC LIMIT 25");
                    GS_CHECK_CHAR_STATUS           = serverSettings.getProperty("CheckCharOnlineStatus", "SELECT online FROM characters WHERE char_name = '<CHAR>'");
                    LS_RECOVERY_CHECK_LOGIN_MAIL   = serverSettings.getProperty("RecoveryCheckLoginMail", "SELECT COUNT(*) FROM accounts WHERE login = '<LOGIN>' AND email = '<EMAIL>'");
                    LS_CP_QUERY                    = serverSettings.getProperty("RegCpValues", "INSERT INTO scoria_cp (account, balance) VALUES (?, 0)");
                    GS_QUERY_TOP_CLAN              = serverSettings.getProperty("TopClan", "SELECT b.char_name,a.clan_name,a.clan_level,a.reputation_score,b.obj_Id,a.leader_id FROM clan_data a, characters b WHERE a.leader_id = b.obj_Id ORDER by a.clan_level DESC LIMIT 25");
                    GS_QUERY_CASTLE                = serverSettings.getProperty("CastleInfo", "SELECT a.id,a.name,a.siegeDayOfWeek,a.siegeHourOfDay,a.taxPercent,b.clan_name,b.hasCastle FROM castle a left outer join clan_data b on b.hasCastle = a.id");
                    LS_CP_AUTH_REQUEST             = serverSettings.getProperty("CPRequestAuth", "SELECT COUNT(*) FROM accounts WHERE login = '<LOGIN>' AND password = '<PWD>' AND email = '<EMAIL>' AND accesslevel >= 0");
                    LS_CP_GET_PASS_SQ              = serverSettings.getProperty("CpRequestShaPwd", "SELECT password FROM accounts WHERE login = '<LOGIN>' AND email = '<EMAIL>' AND security_cookie = '<COOKIE>' LIMIT 1");
                    LS_CP_UPDATE_SQ                = serverSettings.getProperty("CpUpdateSecurityCookie", "UPDATE accounts SET security_cookie = ? WHERE login = ? AND password = ? LIMIT 1");
                    LS_CP_GET_ALLOWED_IP           = serverSettings.getProperty("CpGetIpAllowed", "SELECT allowed_ip FROM accounts WHERE login = '<LOGIN>' LIMIT 1");
                    LS_CP_GET_LAST_IP              = serverSettings.getProperty("CpGetLastIp", "SELECT allowed_ip,lastIP FROM accounts WHERE login = '<LOGIN>' LIMIT 1");
                    LS_RECOVERY_CHECK_REMOTE_IP    = serverSettings.getProperty("RecoveryCheckByRemoteIp", "SELECT COUNT(*) FROM scoria_recovery WHERE ip = '<IP>' AND time > '<INIT_TIME>'");
                    LS_RECOVERY_CHECK_LOGIN_DAILY  = serverSettings.getProperty("RecoveryCheckByLoginDaily", "SELECT COUNT(*) FROM scoria_recovery WHERE login = '<LOGIN>' AND time > '<INIT_TIME>'");
                    LS_RECOVERY_ASSIGNED_INSERT    = serverSettings.getProperty("RecoveryInsertAssignedValue", "INSERT INTO scoria_recovery (`login`, `password`, `email`, `hash`, `ip`, `time`) VALUES (?, ?, ?, ?, ?, ?)");
                    LS_RECOVERY_GET_VALUE_BY_HASH  = serverSettings.getProperty("RecoveryGetLoginPwdByHash", "SELECT login,password FROM scoria_recovery WHERE hash = '<R_HASH>' AND time > '<INIT_TIME>'");
                    LS_RECOVERY_UPDATE_PASSWORD    = serverSettings.getProperty("RecoveryUpdateValuesByValidate", "UPDATE accounts SET password = ? WHERE login = ? AND email = ?");
                    LS_RECOVERY_CLEAR_LOGIN_VALUE  = serverSettings.getProperty("RecoveryRemoveOldValue", "DELETE FROM scoria_recovery WHERE login = ?");
                    LS_UPDATE_EMAIL_CP             = serverSettings.getProperty("CpUpdateEmail", "UPDATE accounts SET email = ? WHERE email = ? AND login = ?");
                    GS_GET_CHAR_LIST               = serverSettings.getProperty("CpGetCharList", "SELECT char_name, level, pvpkills, pkkills FROM characters WHERE account_name = '<LOGIN>'");
                    GS_GET_BANNED_CHAR_LIST        = serverSettings.getProperty("CpGetBannedCharList", "SELECT obj_Id,char_name FROM characters WHERE account_name = '<LOGIN>' AND accesslevel <= '<ACC_LEVEL>'");
                    GS_GET_CLEAR_CHAR_LIST         = serverSettings.getProperty("CpGetClearCharList", "SELECT obj_Id,char_name FROM characters WHERE account_name = '<LOGIN>' AND accesslevel = '<ACC_LEVEL>'");
                    LS_GET_ACCOUNT_CP_BALANCE      = serverSettings.getProperty("CpGetAccountBalance", "SELECT balance FROM scoria_cp WHERE account = '<LOGIN>'");
                    GS_GET_TP_TIME_CHAR            = serverSettings.getProperty("CpGetLastTpTime", "SELECT lastcptp FROM characters WHERE account_name = '<LOGIN>' AND obj_Id = '<CHAR>' AND accesslevel = '0' AND online = '0'");
                    GS_UPDATE_XYZ_CORDS            = serverSettings.getProperty("CpUpdateXYZCords", "UPDATE characters SET x = ?, y = ?, z = ?, lastcptp = ? WHERE obj_Id = ? AND account_name = ?");
                    LS_GET_CP_PAYMENT_LOG          = serverSettings.getProperty("CpGetLoggByPayment", "SELECT date,note,count,method FROM scoria_cp_log WHERE account = '<LOGIN>' ORDER by id DESC LIMIT 5");
                    GS_VALIDATE_OBJECT_CHAR        = serverSettings.getProperty("CpCheckObjForAcc", "SELECT char_name FROM characters WHERE account_name = '<LOGIN>' AND obj_Id = '<OBJECTID>' AND accesslevel = '0' AND online = '0'");
                    LS_LOOSE_ACC_MONEY             = serverSettings.getProperty("CpLooseAccountMoney", "UPDATE scoria_cp SET balance = ? WHERE account = ?");
                    GS_GET_CHAR_OBJ_ID             = serverSettings.getProperty("GetObjIdCharacter", "SELECT obj_Id from characters WHERE char_name = '<CHAR>'");
                    GS_CHECK_CHAR_HAS_ITEM         = serverSettings.getProperty("CheckCharHasItemById", "SELECT * FROM items WHERE owner_id = '<CHARID>' AND item_id = '<ITEMID>'");
                    GS_UPDATE_ITEM_COUNT           = serverSettings.getProperty("UpdateExistItemById", "UPDATE `items` SET `count` = count+? WHERE `owner_id` = ? AND `item_id` = ?");
                    GS_GET_LAST_ITEM_ID            = serverSettings.getProperty("GetLastItemId", "SELECT `object_id` FROM items ORDER BY `object_id` DESC LIMIT 1");
                    GS_INSERT_ITEM_BY_ID           = serverSettings.getProperty("InsertItemById", "INSERT INTO items (owner_id, object_id, item_id, count, loc, loc_data) values('<CHARID>', '<OBJECT>', '<ITEMID>', '<COUNT>', 'INVENTORY', '0')");
                }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }

	}
}