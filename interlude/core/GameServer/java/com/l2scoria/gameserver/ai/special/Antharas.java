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
package com.l2scoria.gameserver.ai.special;

import com.l2scoria.Config;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.datatables.sql.SpawnTable;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.GrandBossManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2scoria.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.model.quest.State;
import com.l2scoria.gameserver.model.spawn.L2Spawn;
import com.l2scoria.gameserver.model.zone.type.L2BossZone;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2scoria.gameserver.network.serverpackets.PlaySound;
import com.l2scoria.gameserver.network.serverpackets.SpecialCamera;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.templates.StatsSet;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.random.Rnd;
import javolution.util.FastList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;


public class Antharas extends Quest
{
	// config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	private static final int FWA_INACTIVITYTIME = 900000;

	// Location of teleport cube.
	private final int _teleportCubeId = 31859;
	private final int _teleportCubeLocation[][] = { { 177615, 114941, -7709, 0 } };
	
	protected List<L2Spawn> _teleportCubeSpawn = new FastList<L2Spawn>();
	protected List<L2NpcInstance> _teleportCube = new FastList<L2NpcInstance>();
	
	// Spawn data of monsters.
	protected HashMap<Integer, L2Spawn> _monsterSpawn = new HashMap<Integer, L2Spawn>();
	
	// Instance of monsters.
	protected List<L2NpcInstance> _monsters = new FastList<L2NpcInstance>();
	protected L2GrandBossInstance _antharas = null;
	
	// monstersId
	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;
	
	private static final int HEART = 13001;	
	private static final int STONE = 3865;
	
	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _selfDestructionTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;
	
	//Antharas Status Tracking :
	private static final byte DORMANT = 0;		//Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1;		//Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	//before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2;		//Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3;			//Antharas has been killed. Entry is locked
	
	protected static long _LastAction = 0;
	
	protected static L2BossZone _Zone;
	
	public Antharas(int id,String name,String descr)
	{
		super(id,name,descr);
		setInitialState(new State("Start", this));

		int[] mobs = {ANTHARASOLDID,ANTHARASWEAKID,ANTHARASNORMALID,ANTHARASSTRONGID,29069,29070,29071,29072,29073,29074,29075,29076};

		for(int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
			addEventId(mob, Quest.QuestEventType.ON_SPELL_FINISHED);
			addEventId(mob, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
		
		addEventId(HEART, Quest.QuestEventType.QUEST_START);
		addEventId(HEART, Quest.QuestEventType.QUEST_TALK);
		
		init();
	}
	
	private void init()
	{
		try
		{
			_Zone = GrandBossManager.getInstance().getZone(179700,113800,-7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			
			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29019, tempSpawn);
			
			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29066, tempSpawn);
			
			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29067, tempSpawn);
			
			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}
		
		// Setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if (Config.ANTHARAS_OLD || status == WAITING)
		{
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			Long respawnTime = info.getLong("respawn_time");
			if (status == DEAD && respawnTime <= System.currentTimeMillis())
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID,loc_x,loc_y,loc_z,heading,false,0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp,mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			int statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			int statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			int statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if (statusWeak == FIGHTING || statusWeak == DEAD)
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if (statusNormal == FIGHTING || statusNormal == DEAD)
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if (statusStrong == FIGHTING || statusStrong == DEAD)
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if (antharasId != 0 && status == FIGHTING)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(antharasId,loc_x,loc_y,loc_z,heading,false,0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp,mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (antharasId != 0 && status == DEAD)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId,DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		if(player.getQuestState("antharas") == null)
		{
			newQuestState(player);
		}

		if(npc.getNpcId() == HEART)
		{
			int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
			int statusW = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			int statusN = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			int statusS = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);

			if (status == FIGHTING || statusW == FIGHTING || statusN == FIGHTING || statusS == FIGHTING)
			{
				return "<html><body>A voice whispers in your ear:<br><font color=\"LEVEL\">Some have already entered the Lair of Antharas. You may not enter until they are finished.</font></body></html>";
			}
			
			if (status == DEAD || statusW == DEAD || statusN == DEAD || statusS == DEAD)
			{
				return "<html><body>A voice whispers in your ear:<br><font color=\"LEVEL\">You may not confront Antharas now. Step back.</font></body></html>";
			}

			if (status == DORMANT || status == WAITING)
			{
				if(player.getInventory().getItemByItemId(STONE) != null && player.getInventory().getItemByItemId(STONE).getCount() >= 1) 
				{
					player.getInventory().destroyItemByItemId("Antarath", STONE, 1, player, player.getTarget());
					player.sendPacket(new ItemList(player, true));
					GrandBossManager.getInstance().getZone(177615, 114941, -7709).allowPlayerEntry(player, 30);
					player.teleToLocation(177615, 114941, -7709);
					if(status == DORMANT || statusW == DORMANT || statusN == DORMANT || statusS == DORMANT)
					{
						setAntharasSpawnTask();
					}
				}
				else
				{
					return "<html><body>A voice whispers in your ear:<br><font color=\"LEVEL\">Only those with a Portal Stone may meet Antharas. Step back.</font></body></html>";
				}
			}
		}
		return super.onTalk(npc, player);
	}
	
	// Do spawn teleport cube.
	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
	}
	
	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			synchronized(this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,WAITING);
					_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1),Config.ANTHARAS_CLOSE*1000);
				}
			}
		}
	}
	
	private void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;
		
		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch (antharasId)
		{
			case ANTHARASWEAKID:
				intervalOfMobs = 180000;
				break;
			case ANTHARASNORMALID:
				intervalOfMobs = 150000;
				break;
			default:
				intervalOfMobs = 120000;
				break;
		}
		
		// Spawn mobs.
		_mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}
	
	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private final Collection<L2Character> _players = _Zone.getCharactersInside().values();
		
		public AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;
			
			switch (_taskId)
			{
				case 1: // Spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (Config.ANTHARAS_OLD)
						npcId = 29019; // old
					else if (_players.size() <= Config.ANTHARAS_WEEK_LIMIT)
						npcId = 29066; // weak
					else if (_players.size() > Config.ANTHARAS_NORMAL_LIMIT)
						npcId = 29068; // strong
					else
						npcId = 29067; // normal
					
					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);
					
					_monsters.add(_antharas);
					_antharas.setIsImobilised(true);
					
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId,FIGHTING);
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
					
					// Setting 1st time of minions spawn task.
					if (!Config.ANTHARAS_OLD)
					{
						startMinionSpawns(npcId);
					}
					
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),700,13,-19,0,20000));
					
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;
					
				case 3:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),700,13,0,6000,20000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),3700,0,-3,0,10000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;
					
				case 5:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),1100,0,-3,22000,30000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;
					
				case 6:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),1100,0,-3,300,7000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;
					
				case 7:
					_antharas.abortCast();
					
					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);
					
					// Move at random.
					if (Config.ANTHARAS_MOVE)
					{
						L2CharPosition pos = new L2CharPosition(Rnd.get(175000,178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos),500);
					}
					
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
			}
		}
	}
	
	private void broadcastPacket(L2GameServerPacket mov)
	{
		if (_Zone != null)
		{
			for (L2Character characters : _Zone.getCharactersInside().values())
			{
				if (characters instanceof L2PcInstance)
					characters.sendPacket(mov);
			}
		}
	}
	
	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}
		
		@Override
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			boolean isBehemoth = Rnd.get(100) < 60;
			try
			{
				int mobNumber = (isBehemoth ? 2 : 3);
				// Set spawn.
				for(int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= Config.ANTHARAS_MAX_MOBS)
						break;
					int npcId;
					if (isBehemoth)
						npcId = 29069;
					else
						npcId = Rnd.get(29070, 29076);
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (_antharas.getX() - x) * (_antharas.getX() - x) + (_antharas.getY() - y) * (_antharas.getY() - y);
					while (tried++ < 25 && notFound)
					{
						int rx = Rnd.get(175000, 179900);
						int ry = Rnd.get(112400, 116000);
						int rdt = (_antharas.getX() - rx) * (_antharas.getX() - rx) + (_antharas.getY() - ry) * (_antharas.getY() - ry);
						if (GeoEngine.canSeeCoord(_antharas, rx, ry, -7704, false))
							if (rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if (rdt <= 900000)
									notFound = false;
							}
					}
					tempSpawn.setLocx(x);
					tempSpawn.setLocy(y);
					tempSpawn.setLocz(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (Exception e)
			{
				_log.warn(e.getMessage());
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter (L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
				if (_selfDestructionTask == null && !npc.isDead())
					_selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(npc), 15000);
				break;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	// Do self destruction.
	private class SelfDestructionOfBomber implements Runnable
	{
		private final L2NpcInstance _bomber;
		
		public SelfDestructionOfBomber(L2NpcInstance bomber)
		{
			_bomber = bomber;
		}
		
		@Override
		public void run()
		{
			L2Skill skill = null;
			switch (_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}
			
			_bomber.doCast(skill);
			if (_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}
	
	@Override
	public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if (skill != null && (skill.getId() == 5097 || skill.getId() == 5094))
		{
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
					npc.doDie(npc);
					break;
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	// At end of activity time.
	private class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			Long temp = (System.currentTimeMillis() - _LastAction);
			if (temp > FWA_INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getNpcId(),DORMANT);
				setUnspawn();
			}
		}
	}
	
	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		_Zone.oustAllPlayers();
		
		// Not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}
		
		// Delete monsters.
		for (L2NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();
		
		// Delete teleport cube.
		for (L2NpcInstance cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}
	
	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private final int _type;
		
		public CubeSpawn(int type)
		{
			_type = type;
		}
		
		@Override
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
				setUnspawn();
		}
	}
	
	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;
		
		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}
		
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId,DORMANT);
		}
	}
	
	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance _boss;
		
		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}
		
		@Override
		public void run()
		{
			_boss.setIsImobilised(false);
			
			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}
	
	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private final L2NpcInstance _npc;
		private final L2CharPosition _pos;
		
		public MoveAtRandom(L2NpcInstance npc, L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}
	
	@Override
	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			_LastAction = System.currentTimeMillis();
			if (GrandBossManager.getInstance().getBossStatus(_antharas.getNpcId()) != FIGHTING)
			{
				_Zone.oustAllPlayers();
			}
			else if (!Config.ANTHARAS_OLD && _mobsSpawnTask == null)
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		else if (npc.getNpcId() > 29069 && npc.getNpcId() < 29077 && npc.getCurrentHp() <= damage)
		{
			L2Skill skill = null;
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}
			
			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getNpcId(),DEAD);
			long respawnTime = (long)(Config.ANTHARAS_RESP_FIRST + Rnd.get(Config.ANTHARAS_RESP_SECOND)) * 3600000;
			ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getNpcId()), respawnTime);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getNpcId());
			info.set("respawn_time",(System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatsSet(npc.getNpcId(),info);
			
			// Delete monsters.
			for (L2NpcInstance mob : _monsters)
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			_monsters.clear();
		}
		else if (npc.getNpcId() == 29069)
		{
			int countHPHerb = Rnd.get(6, 18);
			int countMPHerb = Rnd.get(6, 18);
			for (int i = 0; i < countHPHerb; i++)
				((L2MonsterInstance)npc).DropItem(killer, 8602, 1);
			for (int i = 0; i < countMPHerb; i++)
				((L2MonsterInstance)npc).DropItem(killer, 8605, 1);
		}
		if (_monsters.contains(npc))
			_monsters.remove(npc);
		return super.onKill(npc,killer,isPet);
	}
}