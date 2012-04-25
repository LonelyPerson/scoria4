package webservice;

/**
 *
 * @author zenn
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


public class ClientSocket extends Thread {
	private BufferedInputStream _bis;
	private BufferedOutputStream _bos;
	private Socket _sock;
        private String _login;
        private String _shapwd;
        private String _email;
        private String _action;
        private String _char;
        private String _x,_y,_z;
        private String _security_cookie;
        private String _remote_ip, _init_time;
        private String _r_hash;
        private String _item_id;
        private String _item_count;
        private String _price;
   //     Connection con = null;
        Connection loginconnect = null;
        Connection gameconnect = null;
        public static InetAddress adr;

	public ClientSocket(Socket s) {
		try {
			_sock  = s;
			_sock.setKeepAlive(Config.USE_KEEP_ALIVE);
			_bis = new BufferedInputStream(s.getInputStream());
			_bos = new BufferedOutputStream(s.getOutputStream());
			start();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}
	@Override
	public void run() {
		while(_sock.isConnected()) try {
                            String ip = _sock.getInetAddress().getHostAddress();
                                if(_bis.available() > 0 && (ip.equals(Config.WEBISTE_REMOTE_IP) || Config.WEBISTE_REMOTE_IP.equals("IGNORE_ALL"))) {
				byte []b = new byte[_bis.available()];
				_bis.read(b);
                                String s  = new String(b);
				System.out.println("QUERY: WebSite do query to you: " +s);
                                if(TrySerialize(s)) {
                                    ConnectToLoginDb();
                                    ConnectToGameDb();
                                    _bos.write(selection().getBytes());
                                    _bos.flush();
                                    CloseLoginConnDb();
                                    CloseGameConnDb();
                                }
		}
                try { 
                    Thread.sleep(20);
                } catch(InterruptedException ie) { break; }
                } catch(IOException e) {
			break;
		}
                try {
                    _sock.close();
                    System.out.println("System: Socket closed successfull...");
                } catch(Exception z) {
                   System.out.println("Err closing socket ..." +z);
                }
	}

        public boolean TrySerialize(String value) {
            // String value = "key;action;login:password:mail;charname:security_cookie:remote_ip:init_time:r_hash:x:y:z"
            if(value == null) {
                return false;
            }
            String[] arr = value.split(";");
            if(arr[0].equals(Config.INIT_KEY)) {
                _action = arr[1];
                String[] cv = arr[2].split(":");
                String[] sv = arr[3].split(":");
                if(cv.length != 3) { return false; }
                _login = cv[0] == null ? "empty" : cv[0];
                _shapwd = cv[1] == null ? "empty" : cv[1];
                _email = cv[2] == null ? "empty" : cv[2];
                _char = sv[0] == null ? "empty" : sv[0];
                _security_cookie = sv[1] == null ? "empty" : sv[1];
                _remote_ip = sv[2] == null ? "0.0.0.0" : sv[2];
                _init_time = sv[3] == null ? "9238234" : sv[3];
                _r_hash = sv[4] == null ? "empty" : sv[4];
                _x = sv[5] == null ? "0" : sv[5];
                _y = sv[6] == null ? "0" : sv[6];
                _z = sv[7] == null ? "0" : sv[7];
                _item_id = sv[8] == null ? "0" : sv[8];
                _item_count = sv[9] == null ? "0" : sv[9];
                _price = sv[10] == null ? "0" : sv[10];
                return true;
            }
            return false;
        }

        public String selection() {
            if(_action == null) {
                return "unknown";
            }
            if(_action.equals("gamestatus")) {
                return GameStatus() ? "online" : "offline";
            } else if(_action.equals("loginstatus")) {
                return LoginStatus() ? "online" : "offline";
            } else if (_action.equals("online")) {
                return Online();
            } else if (_action.equals("accounts")) {
                return AccountNum();
            } else if (_action.equals("charnum")) {
                return CharNum();
            } else if (_action.equals("register")) {
                return Register();
            } else if (_action.equals("toppvp")) {
                return PvpTop(1);
            } else if (_action.equals("toppk")) {
                return PvpTop(2);
            } else if (_action.equals("toplvl")) {
                return PvpTop(3);
            } else if (_action.equals("topclan")) {
                return ClanTop();
            } else if (_action.equals("castle")) {
                return Castle();
            } else if (_action.equals("requestauth")) {
                return RequestAuth();
            } else if (_action.equals("requestpwdsq")) {
                return RequestPwdSQ();
            } else if (_action.equals("setsecuritycookie")) {
                return UpdateSecurityCookie();
            } else if (_action.equals("getallowedip")) {
                return GetAllowedIp();
            } else if (_action.equals("getiplast")) {
                return GetLastIps();
            } else if (_action.equals("getaccemail")) {
                return RecoveryCheckLoginEmail();
            } else if (_action.equals("checkiponrecovery")) {
                return RecoveryCheckRemoteIp();
            } else if (_action.equals("checkloginrecoverydaily")) {
                return RecoveryCheckLoginDaily();
            } else if (_action.equals("recoveryinsertassigned")) {
                return RecoveryAssignedInsert();
            } else if (_action.equals("recoverygetpwdbyhash")) {
                return RecoveryGetValueByHash();
            } else if (_action.equals("recoveryupdatepasswordsuccess")) {
                return RecoveryUpdatePassword(true);
            } else if (_action.equals("updatepasswordcp")) {
                return RecoveryUpdatePassword(false);
            } else if (_action.equals("updateemail")) {
                return UpdateEmail();
            } else if (_action.equals("getcharlistinfo")) {
                return GetCharList();
            } else if (_action.equals("getbannedcharlist")) {
                return GetModifiedCharList(true); // banned list
            } else if (_action.equals("getclearcharlist")) {
                return GetModifiedCharList(false); // non-banned list
            } else if (_action.equals("getaccountbalance")) {
                return GetAccountBalance();
            } else if (_action.equals("getlastcpinfo")) {
                return GetLastTpCharInfo();
            } else if (_action.equals("updatexyz")) {
                return UpdateXYZCords();
            } else if (_action.equals("getpaymentlog")) {
                return GetCpLogPayment();
            } else if (_action.equals("checkonlinechar")) {
                return CheckCharOnlineStatus();
            } else if (_action.equals("takeitemtochar")) {
                TakeItemToDatabase();
                return "ok";
            } else {
                return "Unknown";
            }
        }
        
        public void ConnectToLoginDb() {
            try {
                Class.forName(Config.DATABASE_DRIVER_LS).newInstance();
                loginconnect = DriverManager.getConnection(Config.DATABASE_URL_LS, Config.DATABASE_LOGIN_LS, Config.DATABASE_PASSWORD_LS);   
            } catch(Exception e) {
                System.out.println("Connection to login database is failed. Please, check they in config files.");
                System.exit(1);
            }
        }
        
        public void ConnectToGameDb() {
            try {
                Class.forName(Config.DATABASE_DRIVER_GS).newInstance();
                gameconnect = DriverManager.getConnection(Config.DATABASE_URL_GS, Config.DATABASE_LOGIN_GS, Config.DATABASE_PASSWORD_GS);
            } catch(Exception e) {
                System.out.println("Connection to game database is failed. Please, check they in config files.");
            }
        }
        
        public void CloseLoginConnDb() {
            try {
                if(loginconnect != null) {
                    loginconnect.close();
                    loginconnect = null;
                }
            } catch (Exception e) {
                // nuff
            }
        }
        
        public void CloseGameConnDb() {
            try {
                if(gameconnect != null) {
                    gameconnect.close();
                    gameconnect = null;
                }
            } catch (Exception e) {
                // nuff
            }
        }

        public String Online() {
            String count = "0";
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                rs = stmt.executeQuery(Config.GS_QUERY_ONLINE_COUNT);
                rs.next();
                count = rs.getString(1);
                rs.close();
                stmt.close();
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return count;
        }

        public String Register() {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
            if(CheckReg()) {
                try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_QUERY_REG);
		statement.setString(1, _login);
		statement.setString(2, _shapwd);
                statement.setString(3, _email);
		statement.executeUpdate();
		statement.close();
                PreparedStatement cpquery = loginconnect.prepareStatement(Config.LS_CP_QUERY);
                cpquery.setString(1, _login);
                cpquery.executeUpdate();
                cpquery.close();
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
                return "ok";
            }
            return "error";
        }

        public boolean CheckReg() {
            String query = Config.LS_QUERY_CHECK_REG.replace("<LOGIN>", _login.toLowerCase());
            int sin = 1;
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                rs = stmt.executeQuery(query);
                rs.next();
                sin = rs.getInt(1);
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return sin==0 ? true : false;
        }

        public static boolean GameStatus() {
            try {
                ServerSocket s = new ServerSocket(Config.GAMEPORT);
                s.close();
                s = null;
                return false;
            } catch(Exception e) {
                return true;
            }
        }

        public static boolean LoginStatus() {
            try {
                ServerSocket s = new ServerSocket(Config.LOGINPORT);
                s.close();
                s = null;
                return false;
            } catch(Exception e) {
                return true;
            }
        }

        public String AccountNum() {
            String count = "0";
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                rs = stmt.executeQuery(Config.LS_QUERY_ACC_NUM);
                if(rs.next()) {
                    count = rs.getString(1);
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return count;

        }

        public String CharNum() {
            String count = "0";
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                rs = stmt.executeQuery(Config.GS_QUERY_CHAR_NUM);
                if(rs.next()) {
                    count = rs.getString(1);
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return count;

        }

        public String PvpTop(int init) {
            String result = "";
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                switch(init) {
                    case 1: rs = stmt.executeQuery(Config.GS_QUERY_TOP_PVP);
                    break;
                    case 2: rs = stmt.executeQuery(Config.GS_QUERY_TOP_PK);
                    break;
                    case 3: rs = stmt.executeQuery(Config.GS_QUERY_TOP_LEVEL);
                    break;
                    default: rs = stmt.executeQuery(Config.GS_QUERY_TOP_PVP);
                    break;
                }
                while(rs.next()) {
                    String clanname;
                    if(rs.getString(6) == null) { clanname = "no"; } else { clanname = rs.getString(6); }
                    result += rs.getString(1)+":"+rs.getString(2)+":"+rs.getString(3)+":"+rs.getString(4)+":"+rs.getString(5)+":"+clanname+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return result == null ? "unknown" : result;
        }

        public String ClanTop() {
            String result = "";
                try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                rs = stmt.executeQuery(Config.GS_QUERY_TOP_CLAN);
                while(rs.next()) {
                    result += rs.getString(2)+":"+rs.getString(1)+":"+rs.getString(3)+":"+rs.getString(4)+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return result == null ? "unknown" : result;
        }

        public String Castle() {
            String result = "";
                try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                rs = stmt.executeQuery(Config.GS_QUERY_CASTLE);
                while(rs.next()) {
                    String own_clan;
                    if(rs.getString(6) == null) { own_clan = "no"; } else { own_clan = rs.getString(6); }
                    result += rs.getString(2)+":"+rs.getString(3)+":"+rs.getString(4)+":"+rs.getString(5)+":"+own_clan+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
            return result == null ? "unknown" : result;
        }

        public String RequestAuth() {
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_CP_AUTH_REQUEST.replace("<LOGIN>", _login);
                query = query.replace("<PWD>", _shapwd);
                query = query.replace("<EMAIL>", _email);
                rs = stmt.executeQuery(query);
                rs.next();
                if(rs.getInt(1) == 1) {
                    return "ok";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return "error";
        }

        public String RequestPwdSQ() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_CP_GET_PASS_SQ.replace("<LOGIN>", _login);
                query = query.replace("<COOKIE>", _security_cookie);
                query = query.replace("<EMAIL>", _email);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                    pss = rs.getString(1);
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }

        public String UpdateSecurityCookie() {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
                try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_CP_UPDATE_SQ);
		statement.setString(1, _security_cookie);
		statement.setString(2, _login);
                statement.setString(3, _shapwd);
		statement.executeUpdate();
		statement.close();
                return "ok";
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
        }

       public String GetAllowedIp() {
           String result = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_CP_GET_ALLOWED_IP.replace("<LOGIN>", _login);
                rs = stmt.executeQuery(query);
                rs.next();
                result = rs.getString(1);
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return result;
        }

        public String GetLastIps() {
           String result = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_CP_GET_LAST_IP.replace("<LOGIN>", _login);
                rs = stmt.executeQuery(query);
                rs.next();
                result = rs.getString(1)+":"+rs.getString(2);
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return result;
        }

        public String RecoveryCheckLoginEmail() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_RECOVERY_CHECK_LOGIN_MAIL.replace("<LOGIN>", _login);
                query = query.replace("<EMAIL>", _email);
                rs = stmt.executeQuery(query);
                rs.next();
                if(rs.getInt(1) == 1) {
                    pss = "ok";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }

        public String RecoveryCheckRemoteIp() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_RECOVERY_CHECK_REMOTE_IP.replace("<IP>", _remote_ip);
                query = query.replace("<INIT_TIME>", _init_time);
                rs = stmt.executeQuery(query);
                rs.next();
                if(rs.getInt(1) <= 0) {
                    pss = "ok";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }

        public String RecoveryCheckLoginDaily() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_RECOVERY_CHECK_LOGIN_DAILY.replace("<LOGIN>", _login);
                query = query.replace("<INIT_TIME>", _init_time);
                rs = stmt.executeQuery(query);
                rs.next();
                if(rs.getInt(1) <= 0) {
                    pss = "ok";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }

        public String RecoveryAssignedInsert() {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
            try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_RECOVERY_ASSIGNED_INSERT);
		statement.setString(1, _login);
		statement.setString(2, _shapwd);
                statement.setString(3, _email);
                statement.setString(4, _r_hash);
                statement.setString(5, _remote_ip);
                statement.setString(6, _init_time);
		statement.executeUpdate();
		statement.close();
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
                return "ok";
        }

        public String RecoveryGetValueByHash() {
           String pss = "error:error:error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_RECOVERY_GET_VALUE_BY_HASH.replace("<R_HASH>", _r_hash);
                query = query.replace("<INIT_TIME>", _init_time);
                rs = stmt.executeQuery(query);
                rs.next();
                pss = rs.getString(1)+":"+rs.getString(2)+":"+rs.getString(3);
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error:error:error";
            }
            return pss;
        }

        public String RecoveryUpdatePassword(boolean clear) {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
                try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_RECOVERY_UPDATE_PASSWORD);
		statement.setString(1, _shapwd);
		statement.setString(2, _login);
                statement.setString(3, _email);
		statement.executeUpdate();
		statement.close();
                if(clear) {
                PreparedStatement cpquery = loginconnect.prepareStatement(Config.LS_RECOVERY_CLEAR_LOGIN_VALUE);
                cpquery.setString(1, _login);
                cpquery.executeUpdate();
                cpquery.close();
                }
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
                return "ok";
        }

        public String UpdateEmail() {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
                try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_UPDATE_EMAIL_CP);
		statement.setString(1, _shapwd);
		statement.setString(2, _email);
                statement.setString(3, _login);
		statement.executeUpdate();
		statement.close();
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
                return "ok";
        }

        public String GetCharList() {
            String result = "";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query = Config.GS_GET_CHAR_LIST.replace("<LOGIN>", _login);
                rs = stmt.executeQuery(query);
                while(rs.next()) {
                    result += rs.getString(1)+":"+rs.getString(2)+":"+rs.getString(3)+":"+rs.getString(4)+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return result;
        }

        public String GetModifiedCharList(boolean isBanned) {
            String result = "";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query;
                if(isBanned) {
                    query = Config.GS_GET_BANNED_CHAR_LIST.replace("<LOGIN>", _login);
                } else {
                    query = Config.GS_GET_CLEAR_CHAR_LIST.replace("<LOGIN>", _login);
                }
                query = query.replace("<ACC_LEVEL>", _shapwd);
                rs = stmt.executeQuery(query);
                while(rs.next()) {
                    result += rs.getString(1)+":"+rs.getString(2)+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return result;
        }

        public String GetAccountBalance() {
           String pss = "0";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_GET_ACCOUNT_CP_BALANCE.replace("<LOGIN>", _login);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                pss = rs.getString(1) == null ? "0" : rs.getString(1);
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }
        
        public void ChangeBalance(int newval) {
                try {
                PreparedStatement statement = loginconnect.prepareStatement(Config.LS_LOOSE_ACC_MONEY);
		statement.setInt(1, newval);
		statement.setString(2, _login);
		statement.executeUpdate();
		statement.close();
            } catch(Exception e) {
                Main.log("Exception: "+e);
            }
        }
        
        public String TakeItemToChar() {
            String results = "error";
            if(ValidateAccountCharObject()) {
                int nowBalance = Integer.parseInt(GetAccountBalance());
                int newBalance = nowBalance - Integer.parseInt(_price);
                if(newBalance < 0) {
                    results = "lowbalance";
                } else {
                    // take item to char and low balance count
                    this.ChangeBalance(newBalance);
                    this.TakeItemToDatabase();
                    results = "success";
                }
            } else {
                results = "hackposible";
            }
            return results;
        }
        
        public void TakeItemToDatabase() {
            String charid;
            int lastid = 268477526;
            try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query = Config.GS_GET_CHAR_OBJ_ID.replace("<CHAR>", _char);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                    charid = rs.getString(1);
                    ResultSet rs2 = null;
                    String query2 = Config.GS_CHECK_CHAR_HAS_ITEM.replace("<CHARID>", charid);
                    query2 = query2.replace("<ITEMID>", _item_id);
                    rs2 = stmt.executeQuery(query2);
                    if(rs2.next()) {
                        // item has in inventory
                         try {
                         PreparedStatement statement = gameconnect.prepareStatement(Config.GS_UPDATE_ITEM_COUNT);
                         int preCount = Integer.parseInt(_item_count);
                         statement.setInt(1, preCount);
                         statement.setString(2, charid);
                         statement.setString(3, _item_id);
                         statement.executeUpdate();
                         } catch(Exception z) {
                             //nuff
                         }
                    } else {
                        // doesn`t has item in inventory, create new
                        try {
                        ResultSet rs3 = null;
                        rs3 = stmt.executeQuery(Config.GS_GET_LAST_ITEM_ID);
                        if(rs3.next()) {
                            lastid = rs3.getInt(1)+1;
                        }
                        String newquery = Config.GS_INSERT_ITEM_BY_ID.replace("<CHARID>", charid).replace("<OBJECT>", Integer.toString(lastid)).replace("<ITEMID>", _item_id).replace("<COUNT>", _item_count);
                        stmt.executeUpdate(newquery);
                        } catch(Exception z) {
                            System.out.println("Stage 7");
                        }
                    }
                }
            } catch(Exception e) {
                   //nuff
            }
            
        }

        public String GetLastTpCharInfo() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query = Config.GS_GET_TP_TIME_CHAR.replace("<LOGIN>", _login);
                query = query.replace("<CHAR>", _char);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                    pss = rs.getString(1) == null ? "0" : rs.getString(1);
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }
        
        public String CheckCharOnlineStatus() {
            String result = "error";
                try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query = Config.GS_CHECK_CHAR_STATUS.replace("<CHAR>", _char);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                    if(rs.getInt(1) == 1) {
                        result = "online";
                    } else {
                        result = "offline";
                    }
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
          return result;
        }

        public String UpdateXYZCords() {
            if(_login == null || _shapwd == null || _email == null) {
                return "error";
            }
            try {
                PreparedStatement statement = gameconnect.prepareStatement(Config.GS_UPDATE_XYZ_CORDS);
		statement.setString(1, _x);
		statement.setString(2, _y);
                statement.setString(3, _z);
                statement.setString(4, _shapwd);
                statement.setString(5, _char);
                statement.setString(6, _login);
		statement.executeUpdate();
		statement.close();
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
                return "ok";
        }
        
        public boolean ValidateAccountCharObject() {
          boolean pss = false;
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = gameconnect.createStatement();
                String query = Config.GS_VALIDATE_OBJECT_CHAR.replace("<LOGIN>", _login);
                query = query.replace("<OBJECTID>", _char);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                    pss = true;
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return false;
            }
            return pss;
        }

        public String GetCpLogPayment() {
           String pss = "error";
           try {
                Statement stmt = null;
                ResultSet rs = null;
                stmt = loginconnect.createStatement();
                String query = Config.LS_GET_CP_PAYMENT_LOG.replace("<LOGIN>", _login);
                rs = stmt.executeQuery(query);
                if(rs.next()) {
                pss = "";
                pss += rs.getString(1)+":"+rs.getString(2)+":"+rs.getString(3)+":"+rs.getString(4)+";";
                }
                rs = null;
                stmt = null;
            } catch(Exception e) {
                Main.log("Exception: "+e);
                return "error";
            }
            return pss;
        }

}