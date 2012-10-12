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
package com.l2scoria.gameserver.network;

import com.l2scoria.Config;
import com.l2scoria.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2scoria.gameserver.datatables.sql.ClanTable;
import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.managers.AwayManager;
import com.l2scoria.gameserver.model.CharSelectInfoPackage;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2scoria.gameserver.network.serverpackets.LeaveWorld;
import com.l2scoria.gameserver.network.serverpackets.ServerClose;
import com.l2scoria.gameserver.thread.LoginServerThread;
import com.l2scoria.gameserver.thread.LoginServerThread.SessionKey;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.FloodProtector;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.lameguard.session.LameClientV195;
import javolution.util.FastList;
import mmo.MMOClient;
import mmo.MMOConnection;
import org.apache.log4j.Logger;
import ru.catssoftware.protection.CatsGuard;
import ru.catssoftware.protection.Restrictions;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author l2scoria dev
 */

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements LameClientV195
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());

	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesnt has character attached to it yet
	 * IN_GAME - client has selected a char and is in game
	 *
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}

	public GameClientState state;

	// Info
	public String accountName;
	public SessionKey sessionId;
	public L2PcInstance activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();
	private String _hostAddress = null;

	private boolean _isAuthedGG;
	private long _connectionStartTime;
	private List<Integer> _charSlotMapping = new FastList<Integer>();

	// Task
	protected ScheduledFuture<?> _autoSaveInDB;


	// Crypt
	private GameCrypt _crypt;

	// Flood protection
	public byte packetsSentInSec = 0;
	public int packetsSentStartTick = 0;
	//public long packetsNextSendTick = 0;

	//unknownPacket protection  
	private int unknownPacketCount = 0;

	private boolean _closenow = true;
	//public  String lastPacketSent;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();

		if (Config.AUTOSAVE_INITIAL_TIME > 0)
		{
			_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), Config.AUTOSAVE_INITIAL_TIME, Config.AUTOSAVE_DELAY_TIME);
		}
		else
		{
			_autoSaveInDB = null;
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				if (_closenow)
				{
					close(new LeaveWorld());
				}
			}
		}, 4000);
	}

	private GameCrypt getCrypt()
	{
		if (_crypt == null)
		{
			_crypt = new GameCrypt();
		}
		return _crypt;
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		getCrypt().setKey(key);
		return key;
	}

	public GameClientState getState()
	{
		return state;
	}

	public void setState(GameClientState pState)
	{
		state = pState;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_closenow = false;
		getCrypt().decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		getCrypt().encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	public L2PcInstance getActiveChar()
	{
		return activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		if (pActiveChar != null && !SPSEnabled)
		{
			Restrictions.check(this);
		}

		activeChar = pActiveChar;

		if (activeChar != null)
		{
			if (_reader != null)
			{
				_reader.checkChar(activeChar);
			}
			// why not?
			//L2World.storeObject(getActiveChar());
		}
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public void setAccountName(String pAccountName)
	{
		accountName = pAccountName;
		if (_reader == null)
		{
			CatsGuard.getInstance().initSession(this);
		}
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setSessionId(SessionKey sk)
	{
		sessionId = sk;
	}

	public SessionKey getSessionId()
	{
		return sessionId;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if (getConnection() == null)
		{
			return;
		}
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	/**
	 * Method to handle character deletion
	 *
	 * @return a byte: <li>-1: Error: No char was found for such charslot, caught exception, etc... <li>0: character is
	 *         not member of any clan, proceed with deletion <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{

		int objid = getObjectIdForSlot(charslot);

		if (objid < 0)
		{
			return -1;
		}

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();

			rs.next();

			int clanId = rs.getInt(1);

			byte answer = 0;

			if (clanId != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanId);

				if (clan == null)
				{
					answer = 0; // jeezes!
				}
				else if (clan.getLeaderId() == objid)
				{
					answer = 2;
				}
				else
				{
					answer = 1;
				}

				clan = null;
			}

			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objid);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_Id=?");
					statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
					statement.setInt(2, objid);
					statement.execute();
					statement.close();
					rs.close();
					statement = null;
					rs = null;
				}
			}
			else
			{
				statement.close();
				rs.close();
				statement = null;
				rs = null;
			}

			return answer;
		} catch (Exception e)
		{
			_log.warn("Data error on update delete time of char: " + e);
			return -1;
		} finally
		{
			try
			{
				con.close();
			} catch (Exception e)
			{
			}
			con = null;
		}
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		/*if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.info("active Char saved");
			this.setActiveChar(null);
		}*/

		int objid = getObjectIdForSlot(charslot);

		if (objid < 0)
		{
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;
		} catch (Exception e)
		{
			_log.fatal("Data error on restoring char: " + e);
		} finally
		{
			try
			{
				con.close();
			} catch (Exception e)
			{
			}
			con = null;
		}
	}

	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;

			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			statement = null;
		} catch (Exception e)
		{
			_log.warn("Data error on deleting char: " + e);
		} finally
		{
			try
			{
				con.close();
			} catch (Exception e)
			{
			}
			con = null;
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

		if (character != null)
		{
			//restoreInventory(character);
			//restoreSkills(character);
			//character.restoreSkills();
			//restoreShortCuts(character);
			//restoreWarehouse(character);

			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			//character.sendPacket(new UserInfo(character));
			//character.broadcastKarma();
			//character.setOnlineStatus(true);
		}
		else
		{
			_log.fatal("could not restore in slot: " + charslot);
		}

		//setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for (CharSelectInfoPackage c : chars)
		{
			int objectId = c.getObjectId();

			_charSlotMapping.add(objectId);
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		if (getConnection() != null)
		{
			getConnection().close(gsp);
		}
	}

	public void closeNow()
	{
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			Cleanup();
		}
	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if (charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warn(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}

		return _charSlotMapping.get(charslot);
	}

	@Override
	protected void onForcedDisconnection()
	{
		_log.info("Client " + toString() + " disconnected abnormally.");
	}

	public void stopGuardTask()
	{
	}

	@Override
	protected void onDisconection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
		} catch (RejectedExecutionException e)
		{
			// server is closing
		}
		if (_reader != null)
		{
			CatsGuard.getInstance().doneSession(this);
		}
	}

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
			String ip;

			if (address == null)
			{
				ip = "disconnected";
			}
			else
			{
				ip = address.getHostAddress();
			}

			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + ip + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + ip + "]";
				case IN_GAME:
					address = null;
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + ip + "]";
				default:
					address = null;
					throw new IllegalStateException("Missing state on switch");
			}
		} catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	class DisconnectTask implements Runnable
	{
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			try
			{
				// Update BBS
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				// we are going to mannually save the char bellow thus we can force the cancel
				if (_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
				}

				L2PcInstance player = getActiveChar();
				if (player != null) // this should only happen on connection loss
				{
					if (player.isOffline())
					{
						return;
					}
				}

				Cleanup();

				player = null;
			} catch (Exception e1)
			{
				_log.warn("error while disconnecting client", e1);
			} finally
			{
				Restrictions.onDisconnect(L2GameClient.this);
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}

	public void Cleanup()
	{
		try
		{
			L2PcInstance player = getActiveChar();
			if (player != null) // this should only happen on connection loss
			{
				if (player.isLocked())
				{
					_log.warn("Player " + player.getName() + " still performing subclass actions during disconnect.");
				}

				if (player.getRespawnTask() != null)
				{
					ThreadPoolManager.getInstance().removeGeneral(player.getRespawnTask());
				}

				if (player.getInstanceId() != 0)
				{
					Instance instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId());
					if (instanceObj != null)
					{
						Location tpLoc = instanceObj.getTpLoc(player);
						player.getPosition().setXYZ(tpLoc.getX(), tpLoc.getY(), tpLoc.getZ());
					}
				}

				if(player.inObserverMode())
				{
					player.leaveObserverMode();
				}

				if(player._event != null)
				{
					player._event.onLogout(player);
				}

				if (player.isAway())
				{
					AwayManager.getInstance().extraBack(player);
				}

				try
				{
					player.store();
				} catch (Exception e2)
				{
					/* ignore any problems here */
				}

				// notify the world about our disconnect
				player.deleteMe();
			}
			// from here
			setActiveChar(null);

			player = null;
		} catch (Exception e1)
		{
			_log.warn("Error while cleanup client.", e1);
		} finally
		{
			Restrictions.onDisconnect(L2GameClient.this);
			LoginServerThread.getInstance().sendLogout(getAccountName());
		}
	}

	//TODO
	public boolean checkUnknownPackets()
	{
		if (getActiveChar() != null && !FloodProtector.getInstance().tryPerformAction(getActiveChar().getObjectId(), FloodProtector.PROTECTED_UNKNOWNPACKET))
		{
			unknownPacketCount++;

			return unknownPacketCount >= Config.MAX_UNKNOWN_PACKETS;
		}
		else
		{
			unknownPacketCount = 0;
			return false;
		}
	}

	public interface IExReader
	{
		public int read(ByteBuffer buf);

		public void checkChar(L2PcInstance cha);
	}

	public IExReader _reader;
	public boolean SPSEnabled = false;

	class AutoSaveTask implements Runnable
	{
		public void run()
		{
			try
			{
				L2PcInstance player = getActiveChar();
				if (player != null && player.isOnline() == 1)
				{
					player.store();
				}
			} catch (Exception e)
			{
				_log.fatal("Error on AutoSaveTask.", e);
			}
		}
	}

	private String _hwid = null;

	/**
	 * Данный метод используется для эксплуатирования данных об "железе" пользователя.
	 * Здесь обрабатывается лишь отсутствие защиты на клиент.
	 *
	 * @return String - client's WHID
	 */
	public String getHWId()
	{
		return _hwid;
	}

	@Override
	public String getHWID()
	{
		return _hwid;
	}

	public void setHWID(String value)
	{
		_hwid = value;
	}

	@Override
	public void setInstanceCount(int i)
	{
	}

	@Override
	public int getInstanceCount()
	{
		return -1;
	}

	@Override
	public void setProtected(boolean b)
	{
	}

	@Override
	public boolean isProtected()
	{
		return false;
	}

	@Override
	public void setPatchVersion(int i)
	{
	}

	@Override
	public int getPatchVersion()
	{
		return -1;
	}

	public String getHostAddress()
	{
		String host;
		try
		{
			if (_hostAddress == null || _hostAddress.isEmpty())
			{
				if (getConnection() != null && getConnection().getInetAddress() != null)
				{
					host = getConnection().getInetAddress().getHostAddress();
				}
				else
				{
					host = "";
				}
			}
			else
			{
				host = _hostAddress;
			}
		} catch (Exception e)
		{
			host = "";
			if (Config.DEBUG)
			{
				_log.info("Could't get client host address. Return empty. Error: " + e);
			}
		}
		return host;
	}
}
