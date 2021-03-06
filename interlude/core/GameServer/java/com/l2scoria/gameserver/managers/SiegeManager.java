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
package com.l2scoria.gameserver.managers;

import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.services.FService;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class SiegeManager
{
	private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());

	// =========================================================
	private static SiegeManager _instance;

	public static final SiegeManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing SiegeManager");
			_instance = new SiegeManager();
			_instance.load();
		}
		return _instance;
	}

	// =========================================================
	// Data Field
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config

	// Siege settings
	private boolean _siegeEveryWeek = false;
	
	private FastMap<Integer, FastList<SiegeSpawn>> _artefactSpawnList;
	private FastMap<Integer, FastList<SiegeSpawn>> _controlTowerSpawnList;

	private int _controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 4; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
        
        public int _gludiomerc = 100;
        public int _dionmerc = 150;
        public int _giranmerc = 200;
        public int _orenmerc = 300;
        public int _adenmerc = 400;
        public int _innadrilmerc = 400;
        public int _goddardmerc = 400;
        public int _runemerc = 400;
        public int _schuttgartmerc = 400;
        
        public int _gludiotakecastlez = 30;
        public int _diontakecastlez = 30;
        public int _girantakecastlez = 30;
        public int _orentakecastlez = 50;
        public int _adentakecastlez = 30;
        public int _innadriltakecastlez = 50;
        public int _goddardtakecastlez = 30;
        public int _runetakecastlez = 50;
        public int _schutgardtakecastlez = 30;

	//private List<Siege> _sieges;

	// =========================================================
	// Constructor
	private SiegeManager()
	{}

	// =========================================================
	// Method - Public
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}

	/**
	 * Return true if character summon<BR>
	 * <BR>
	 * 
	 * @param activeChar The L2Character of the character can summon
	 */
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if(activeChar == null || !(activeChar.isPlayer))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		Castle castle = CastleManager.getInstance().getCastle(player);

		if(castle == null || castle.getCastleId() <= 0)
		{
			sm.addString("You must be on castle ground to summon this");
		}
		else if(!castle.getSiege().getIsInProgress())
		{
			sm.addString("You can only summon this during a siege.");
		}
		else if(player.getClanId() != 0 && castle.getSiege().getAttackerClan(player.getClanId()) == null)
		{
			sm.addString("You can only summon this as a registered attacker.");
		}
		else
			return true;

		if(!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		sm = null;
		player = null;
		castle = null;

		return false;
	}

	/**
	 * Return true if the clan is registered or owner of a castle<BR>
	 * <BR>
	 * 
	 * @param clan The L2Clan of the player
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if(clan == null)
			return false;

		if(clan.getHasCastle() > 0)
			return true;

		Connection con = null;
		boolean register = false;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
			ResultSet rs = statement.executeQuery();

			while(rs.next())
			{
				register = true;
				break;
			}

			rs.close();
			statement.close();
			statement = null;
			rs = null;
		}
		catch(Exception e)
		{
			_log.info("Exception: checkIsRegistered(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		return register;
	}

	public final void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
        
        public int getCastleMonumentZmaxdiff(Castle castle)
        {
            if(castle == null)
                return 0;
           switch(castle.getCastleId())
           {
               case 1:
                   return _gludiotakecastlez;
               case 2:
                   return _diontakecastlez;
               case 3:
                   return _girantakecastlez;
               case 4:
                   return _orentakecastlez;
               case 5:
                   return _adentakecastlez;
               case 6:
                   return _innadriltakecastlez;
               case 7:
                   return _goddardtakecastlez;
               case 8:
                   return _runetakecastlez;
               case 9:
                   return _schutgardtakecastlez;
               default:
                   return 0;    
           }
        }

	// =========================================================
	// Method - Private
	private final void load()
	{
		try
		{
			InputStream is = new FileInputStream(new File(FService.SIEGE_CONFIGURATION_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			is.close();
			is = null;

			// Siege setting
			_siegeEveryWeek = Boolean.parseBoolean(siegeSettings.getProperty("SiegeEveryWeek", "false"));
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000"));
			_controlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000"));
			_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			_defenderRespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
                        
                        _gludiomerc = Integer.decode(siegeSettings.getProperty("GludioLimitMerc", "100"));
                        _dionmerc = Integer.decode(siegeSettings.getProperty("DionLimitMerc", "150"));
                        _giranmerc = Integer.decode(siegeSettings.getProperty("GiranLimitMerc", "200"));
                        _orenmerc = Integer.decode(siegeSettings.getProperty("OrenLimitMerc", "300"));
                        _adenmerc = Integer.decode(siegeSettings.getProperty("AdenLimitMerc", "400"));
                        _innadrilmerc = Integer.decode(siegeSettings.getProperty("InnadrilLimitMerc", "400"));
                        _goddardmerc = Integer.decode(siegeSettings.getProperty("GoddardLimitMerc", "400"));
                        _runemerc = Integer.decode(siegeSettings.getProperty("RuneLimitMerc", "400"));
                        _schuttgartmerc = Integer.decode(siegeSettings.getProperty("SchuttgartLimitMerc", "400"));
                        
                        _gludiotakecastlez = Integer.decode(siegeSettings.getProperty("GludioTakeCastleZdiff", "30"));
                        _diontakecastlez = Integer.decode(siegeSettings.getProperty("DionTakeCastleZdiff", "30"));
                        _girantakecastlez = Integer.decode(siegeSettings.getProperty("GiranTakeCastleZdiff", "30"));
                        _orentakecastlez = Integer.decode(siegeSettings.getProperty("OrenTakeCastleZdiff", "50"));
                        _adentakecastlez = Integer.decode(siegeSettings.getProperty("AdenTakeCastleZdiff", "30"));
                        _innadriltakecastlez = Integer.decode(siegeSettings.getProperty("InnadrilTakeCastleZdiff", "50"));
                        _goddardtakecastlez = Integer.decode(siegeSettings.getProperty("GoddardTakeCastleZdiff", "30"));
                        _runetakecastlez = Integer.decode(siegeSettings.getProperty("RuneTakeCastleZdiff", "50"));
                        _schutgardtakecastlez = Integer.decode(siegeSettings.getProperty("SchuttgartTakeCastleZdiff", "30"));
                        
			// Siege spawns settings
			_controlTowerSpawnList = new FastMap<Integer, FastList<SiegeSpawn>>();
			_artefactSpawnList = new FastMap<Integer, FastList<SiegeSpawn>>();

			for(Castle castle : CastleManager.getInstance().getCastles())
			{
				FastList<SiegeSpawn> _controlTowersSpawns = new FastList<SiegeSpawn>();

				for(int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");

					if(_spawnParams.length() == 0)
					{
						break;
					}

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					_spawnParams = null;

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						int hp = Integer.parseInt(st.nextToken());

						_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));

						st = null;
					}
					catch(Exception e)
					{
						_log.warn("Error while loading control tower(s) for " + castle.getName() + " castle.");
					}
				}

				FastList<SiegeSpawn> _artefactSpawns = new FastList<SiegeSpawn>();

				for(int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");

					if(_spawnParams.length() == 0)
					{
						break;
					}

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					_spawnParams = null;

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());

						st = null;
						_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch(Exception e)
					{
						_log.warn("Error while loading artefact(s) for " + castle.getName() + " castle.");
					}
				}

				_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
				_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);

				_artefactSpawns = null;
				_controlTowersSpawns = null;
			}

			siegeSettings = null;

		}
		catch(Exception e)
		{
			//_initialized = false;
			_log.error("Error while loading siege data.");
			e.printStackTrace();
		}
	}

	// =========================================================
	// Property - Public
	public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		if(_artefactSpawnList.containsKey(_castleId))
                {
			return _artefactSpawnList.get(_castleId);
                }
		else
                {
                 	return null;   
                }
	}

	public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		if(_controlTowerSpawnList.containsKey(_castleId))
			return _controlTowerSpawnList.get(_castleId);
		else
			return null;
	}

	public final boolean getEveryWeek()
	{
		return _siegeEveryWeek;
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}

	public final int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}

	public final int getControlTowerLosePenalty()
	{
		return _controlTowerLosePenalty;
	}

	public final int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}

	public final int getDefenderRespawnDelay()
	{
		return _defenderRespawnDelay;
	}

	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}

	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Siege getSiege(int x, int y, int z)
	{
		for(Castle castle : CastleManager.getInstance().getCastles())
			if(castle.getSiege().checkIfInZone(x, y, z))
				return castle.getSiege();
		return null;
	}

	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}

	public final int getSiegeLength()
	{
		return _siegeLength;
	}

	public final List<Siege> getSieges()
	{
		FastList<Siege> _sieges = new FastList<Siege>();
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			_sieges.add(castle.getSiege());
		}
		return _sieges;
	}

	public class SiegeSpawn
	{
		Location _location;
		private int _npcId;
		private int _heading;
		private int _castleId;
		private int _hp;

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}

		public int getCastleId()
		{
			return _castleId;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getHp()
		{
			return _hp;
		}

		public Location getLocation()
		{
			return _location;
		}
	}
}
