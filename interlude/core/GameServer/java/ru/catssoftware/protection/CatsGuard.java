package ru.catssoftware.protection;

import com.l2scoria.util.Util;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;



import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2scoria.L2Properties;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.L2GameClient.IExReader;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.network.serverpackets.GameGuardQuery;

import org.apache.log4j.Logger;

public class CatsGuard {
	private static Logger _log = Logger.getLogger(CatsGuard.class.getName());
	private class CatsGuardReader implements IExReader {
		private RC4 _crypt;
		private L2GameClient _client;
		private int _prevcode = 0;
		private byte []buffer = new byte[4];
		private int _state;
		private boolean _checkChar;
		private CatsGuardReader(L2GameClient cl) {
			_state = 0;
			_client = cl;
		}

		private void setKey(int data[]) {
			String key = "";
			for(int i=0;i<10;i++)
				key+=String.format("%X%X", data[1],_SERVER_KEY);
			_crypt = new RC4(key,false);
			_state = 1;
		}
		public int read(ByteBuffer buf) {
			int opcode = 0;
			if(_state==0) {
				opcode = buf.get() & 0xff;
				if(opcode!=0xca) {
					illegalAction(_client,"Invalid opcode on pre-auth state");
					return 0;
				}
			} else {
				if(buf.remaining()<4)
					illegalAction(_client,"Invalid block size on authed state");
				else {
					buf.get(buffer);
					opcode = decryptPacket(buffer) & 0xff;
				}
			}
			return opcode;
		}
		private int decryptPacket(byte [] packet) {
			packet = _crypt.rc4(packet);
			int crc = CRC16.calc(new byte[] { (byte)(_prevcode & 0xff),packet[1]});
			int read_crc = (((packet[3] & 0xff) << 8) & 0xff00) | (packet[2] & 0xff); 
			if(crc!= read_crc ) {
				illegalAction(_client,"CRC error");
				return 0;
			}
			_prevcode = packet[1] & 0xff;
			return _prevcode;
		}

		@Override
		public void checkChar(L2PcInstance cha) {
			if(!_checkChar || cha == null)
                        {
				return;
                        }
			if(ALLOW_GM_FROM_BANNED_HWID && cha.isGM())
                        {
				return;
                        }
			if(LOG_OPTION.contains("BANNED"))
                        {
				_log.info("CatsGuard: Client "+cha.getClient()+" try to log with banned hwid");
                                cha.logout();
                        }
		}
		
	}
	private static CatsGuard _instance;
	public static CatsGuard getInstance() {
		if(_instance==null)
			_instance = new CatsGuard();
		return _instance;
	}
	private Map<String, Integer> _connections;
	private List<String> _premium = new FastList<String>(); 
	private List<String> _bannedhwid;
	private static boolean ENABLED = false;
	private static int _SERVER_KEY;
        private static boolean _FREE_LIB;
	private int MAX_SESSIONS;
	private int MAX_PREMIUM_SESSIONS;
	private String LOG_OPTION;
	private boolean ANNOUNCE_HACK;
	private String ON_HACK_ATTEMP;
	private boolean ALLOW_GM_FROM_BANNED_HWID;
	private boolean LOG_SESSIONS;

	private CatsGuard() {
		Util.printSection("CatsGuard");
		try {
			L2Properties p = new L2Properties("./config/protected/catsguard.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("Enabled","false"));
			if(!ENABLED) {
				_log.info("CatsGuard: disabled");
				return;
			}
                        _FREE_LIB = Boolean.parseBoolean(p.getProperty("CryptForFree", "true"));
                        _SERVER_KEY = Integer.parseInt(p.getProperty("CryptKeyInt", "0"));
			LOG_OPTION = p.getProperty("LogOption","NOSPS HACK");
			MAX_SESSIONS = Integer.parseInt(p.getProperty("MaxSessionsFromHWID","-1"));
			MAX_PREMIUM_SESSIONS = Integer.parseInt(p.getProperty("MaxSessionsForPremium","-1"));
			ANNOUNCE_HACK = Boolean.parseBoolean(p.getProperty("AnnounceHackAttempt","true"));
			ON_HACK_ATTEMP = p.getProperty("OnHackAttempt","kick");
			ALLOW_GM_FROM_BANNED_HWID = Boolean.parseBoolean(p.getProperty("AllowGMFromBannedHWID","false"));
			_connections = new FastMap<String, Integer>().setShared(true);
			LOG_SESSIONS = Boolean.parseBoolean(p.getProperty("LogSessions","false"));
                        
                        if(_SERVER_KEY == 0 && !_FREE_LIB) {
                            _log.error("CryptKeyInt is 0 on default inset");
                            return;
                        }

			_bannedhwid = new FastList<String>();
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("select * from banned_hwid");
			try {
				ResultSet rs = stm.executeQuery();
				while(rs.next()) 
					_bannedhwid.add(rs.getString(1));
				rs.close();
			} catch(Exception e) {
				if (e.getClass().getSimpleName().equals("MySQLSyntaxErrorException")) {
					stm.close();
					stm = con.prepareStatement("create table `banned_hwid` (`hwid` varchar(64) not null primary key)");
					stm.execute();
				}
			}
			stm.close();
			con.close();
			//gmController.getInstance().regCommand(new GatsGuardHandler());
			_log.info("CatsGuard: Loaded "+_bannedhwid.size()+" banned hwid(s)");
			_log.info("CatsGuard: Ready");
		} catch(Exception e) {
			_log.warn("CatsGuard: Error while loading ./config/main/catsguard.properties",e);
			ENABLED = false;
		}
	}
	
	public boolean isEnabled() {
		return ENABLED;
	}
	public void ban(L2PcInstance player) {
		ban(player.getHWid());
	}
	public void ban(String hwid) {
		if(!ENABLED)
			return;
		synchronized(_bannedhwid) {
			if(_bannedhwid.contains(hwid))
				return;
			_bannedhwid.add(hwid);
			try {
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("insert into banned_hwid values(?)");
				stm.setString(1, hwid);
				stm.execute();
				stm.close();
				con.close();
			} catch(SQLException e) {
				_log.error("CatsGuard: Unable to store banned hwid",e);
			}
		}
	}
	private void illegalAction(L2GameClient cl, String reason) 
        {
            L2PcInstance playerobject = cl.getActiveChar();
		if(cl.getActiveChar()!=null && ANNOUNCE_HACK) 
			Announcements.getInstance().announceToAll("Player "+playerobject.getName()+" using cheats! Kicked.");
		if(ON_HACK_ATTEMP.equals("hwidban") && cl.getHWId()!=null)
			ban(cl.getHWId());
		else if(ON_HACK_ATTEMP.equals("jail") && cl.getActiveChar()!=null)
			playerobject.setInJail(true, -1);
		else if(ON_HACK_ATTEMP.equals("ban") && cl.getActiveChar()!=null)
			playerobject.setAccountAccesslevel(-100);
		_log.info("CatsGuard: Client "+cl+" use illegal software and will "+ON_HACK_ATTEMP+"ed. Reason: "+reason);
                if(playerobject != null) {
                    try {
                    playerobject.logout();
                    } catch(Exception s) {}
                }
	}
        
	public void unban(String hwid) {
		if(!ENABLED)
			return;
		synchronized(_bannedhwid) {
			_bannedhwid.remove(hwid);
		}
		try {
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("delete from banned_hwid where hwid=?");
			stm.setString(1, hwid);
			stm.execute();
			stm.close();
			con.close();
		} catch(SQLException e) {
			_log.error("CatsGuard: Unable to clear banned hwid",e);
		}
		
	}
	public void initSession(L2GameClient cl) {
		if(!ENABLED ) {
                    return;
                }
		cl.sendPacket(GameGuardQuery.STATIC_PACKET);
		cl._reader = new CatsGuardReader(cl); 			
	}
	
	public void doneSession(L2GameClient cl) {
		if(!ENABLED) {
                     return;
                }
		if(cl.getHWId()!=null) {
			_premium.remove(cl.getHWId());
			if(_connections.containsKey(cl.getHWId())) {
				int nwnd = _connections.get(cl.getHWId());
				if(nwnd==0)
					_connections.remove(cl.getHWId());
				else 
					_connections.put(cl.getHWId(),--nwnd);
			}
		}
		cl._reader = null;
	}

	public void initSession(L2GameClient cl, int [] data) {
		if(!ENABLED)
                {
			return;
                }
                if(_FREE_LIB)
                {
                    _SERVER_KEY = 7958915;
                }
		if(data[0]!=_SERVER_KEY) {
			if(LOG_OPTION.contains("NOPROTECT")) {
                            if(data[0] == 7958915)
                            {
                                _log.info("CatsGuard: Client "+cl+" try to log with no CatsGuard. CryptKeyInt: SCORIA_FREE");
                            } else 
                            {
                                _log.info("CatsGuard: Client "+cl+" try to log with no CatsGuard. CryptKeyInt " +data[0]);
                            }
                        }
                        cl.closeNow();
			return;
		}
		String hwid = String.format("%x", data[3]);
		if(cl._reader==null) {
			if(LOG_OPTION.contains("HACK")) 
				_log.info("CatsGuard: Client "+cl+" has no pre-authed state");
			cl.closeNow();
			return;
		}
		if(_bannedhwid.contains(hwid)) {
			((CatsGuardReader) cl._reader)._checkChar = true;
		}
		if(!_connections.containsKey(hwid))
			_connections.put(hwid,0);
		int nwindow = _connections.get(hwid);
		int max = MAX_SESSIONS; 
		if (_premium.contains(hwid))
			max = MAX_PREMIUM_SESSIONS;
		if(max > 0 && ++nwindow>max) {
			if(LOG_OPTION.contains("SESSIONS"))
				_log.info("CatsGuard: To many sessions from hwid "+hwid);
			cl.closeNow();
			return;
		}
		_connections.put(hwid, nwindow);
		cl.setHWID(hwid);
		((CatsGuardReader) cl._reader).setKey(data);
		if(LOG_SESSIONS)
			_log.info("Client "+cl.getAccountName()+" ["+cl.getHostAddress()+"] connected with hwid "+cl.getHWId());
					
	}
	
}
