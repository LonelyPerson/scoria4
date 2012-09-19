package com.l2scoria.gameserver.model.entity.event.DeathMatch;

import com.l2scoria.Config;
import com.l2scoria.L2Properties;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.handler.VoicedCommandHandler;
import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.model.entity.event.Language;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2scoria.gameserver.taskmanager.ExclusiveTask;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import com.l2scoria.gameserver.templates.L2EtcItemType;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.lang.ArrayUtils;
import com.l2scoria.util.random.Rnd;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TShortStack;

import java.util.HashMap;

/**
 * @author m095
 * @version 1.0
 */

public class DeathMatch extends GameEvent
{
	private TIntArrayList _players = new TIntArrayList();
	private TIntObjectHashMap<Location> _playerLoc = new TIntObjectHashMap<Location>();

	private HashMap<L2PcInstance, DeathMatchPlayer> _playersKills = new HashMap<L2PcInstance, DeathMatchPlayer>();

	class DeathMatchPlayer
	{
		int kills = 0;

		public void addKill()
		{
			kills++;
		}

		public int getKills()
		{
			return kills;
		}
	}

	private int _state = GameEvent.STATE_INACTIVE;
	private static DeathMatch _instance = null;
	//public long _eventDate = 0;
	private int _minLvl = 0;
	private int _maxLvl = 0;
	private int _maxPlayers = 60;
	private int _minPlayers = 0;
	private int _instanceId = 0;
	private int _regTime = 0;
	private int _eventTime = 0;
	private int[] _rewardId = null;
	private int[] _rewardAmount = null;
	private int _reviveDelay = 0;
	private int _remaining;

	private boolean ON_START_REMOVE_ALL_EFFECTS;
	private boolean ON_START_UNSUMMON_PET;
	private Location EVENT_LOCATION;
	private boolean RESORE_HP_MP_CP;
	private boolean ALLOW_POTIONS;
	private boolean ALLOW_SUMMON;
	private boolean JOIN_CURSED;
	private boolean ALLOW_INTERFERENCE;
	private boolean RESET_SKILL_REUSE;
	private boolean DM_RETURNORIGINAL;


	public static DeathMatch getInstance()
	{
		if (_instance == null)
		{
			new DeathMatch();
		}
		return _instance;
	}

	public String getStatus()
	{
		int free = (_maxPlayers - _players.size());
		if (free < 0)
		{
			free = 0;
		}

		return free + Language.LANG_STATUS + _maxPlayers;
	}

	public DeathMatch()
	{
		_instance = this;
	}

	@Override
	public boolean finish()
	{
		_eventTask.cancel();
		_registrationTask.cancel();
		L2PcInstance player;
		for (Integer playerId : _players.toArray())
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				remove(player);
			}
		}
		if (_eventScript != null)
		{
			_eventScript.onFinish(_instanceId);
		}

		if (_instanceId != 0)
		{
			InstanceManager.getInstance().destroyInstance(_instanceId);
			_instanceId = 0;
		}
		_players.clear();
		_state = GameEvent.STATE_INACTIVE;
		return true;
	}

	@Override
	public String getName()
	{
		return "DeathMatch";
	}

	@Override
	public int getState()
	{
		return _state;
	}

	@Override
	public boolean isParticipant(L2PcInstance player)
	{
		return _players.contains(player.getObjectId());
	}

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@Override
	public boolean load()
	{
		try
		{
			/* ----- Файл с параметрами -----*/
			L2Properties Setting = new L2Properties("./config/events/DM.properties");

			/* ----- Чтение параметров ------*/
			if (!Boolean.parseBoolean(Setting.getProperty("DMEnabled", "true")))
			{
				_instance = null;
				return false;
			}

			ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(Setting.getProperty("OnStartRemoveAllEffects", "true"));
			ON_START_UNSUMMON_PET = Boolean.parseBoolean(Setting.getProperty("OnStartUnsummonPet", "true"));
			DM_RETURNORIGINAL = Boolean.parseBoolean(Setting.getProperty("OriginalPosition", "false"));
			RESORE_HP_MP_CP = Boolean.parseBoolean(Setting.getProperty("OnStartRestoreHpMpCp", "false"));
			ALLOW_POTIONS = Boolean.parseBoolean(Setting.getProperty("AllowPotion", "false"));
			ALLOW_SUMMON = Boolean.parseBoolean(Setting.getProperty("AllowSummon", "false"));
			JOIN_CURSED = Boolean.parseBoolean(Setting.getProperty("CursedWeapon", "false"));
			ALLOW_INTERFERENCE = Boolean.parseBoolean(Setting.getProperty("AllowInterference", "false"));
			RESET_SKILL_REUSE = Boolean.parseBoolean(Setting.getProperty("ResetAllSkill", "false"));
			EVENT_LOCATION = new Location(Setting.getProperty("EventLocation", "149800 46800 -3412"));

			_reviveDelay = Integer.parseInt(Setting.getProperty("ReviveDelay", "10"));
			_regTime = Integer.parseInt(Setting.getProperty("RegTime", "10"));
			_eventTime = Integer.parseInt(Setting.getProperty("EventTime", "10"));
			_rewardId = null;
			_rewardAmount = null;

			for (String s : Setting.getProperty("RewardItem", "57").split(","))
			{
				_rewardId = ArrayUtils.add(_rewardId, Integer.parseInt(s));
			}

			for (String s : Setting.getProperty("RewardItemCount", "50000").split(","))
			{
				_rewardAmount = ArrayUtils.add(_rewardAmount, Integer.parseInt(s));
			}

			_minPlayers = Integer.parseInt(Setting.getProperty("MinPlayers", "2"));
			_maxPlayers = Integer.parseInt(Setting.getProperty("MaxPlayers", "60"));
			_minLvl = Integer.parseInt(Setting.getProperty("MinLevel", "1"));
			_maxLvl = Integer.parseInt(Setting.getProperty("MaxLevel", "90"));
		} catch (Exception e)
		{
			_log.warn("DeathMatch: Error reading config ", e);
			return false;
		}

		TaskManager.getInstance().registerTask(new TaskStartDM());
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new VoiceDeathMatch());
		return true;
	}

	@Override
	public void onCommand(L2PcInstance actor, String command, String params)
	{
		if (_state == GameEvent.STATE_ACTIVE)
		{
			if (command.equals("join"))
			{
				if (!register(actor))
				{
					actor.sendMessage(Language.LANG_REGISTER_ERROR);
				}
                                else 
                                {
                                    actor.sendMessage(Language.LANG_PLAYER_REGISTER);
                                }
			}
			else if (command.equals("leave"))
			{
				remove(actor);
			}
		}
	}

	@Override
	public void onKill(L2Character killer, L2Character victim)
	{
		if (killer == null || victim == null)
		{
			return;
		}

		if (killer.isPlayer && victim.isPlayer)
		{
			L2PcInstance plk = (L2PcInstance) killer;
			L2PcInstance pld = (L2PcInstance) victim;

			if (plk._event == this && pld._event == this)
			{
				if (!_playersKills.containsKey(plk))
				{
					_playersKills.put(plk, new DeathMatchPlayer());
				}

				DeathMatchPlayer dmp = _playersKills.get(plk);
				dmp.addKill();

				plk.setTitle("Kills: " + dmp.getKills());
				pld.sendMessage(Language.LANG_KILLED_MSG);
				ThreadPoolManager.getInstance().scheduleGeneral(new revivePlayer(victim), _reviveDelay * 1000);
			}
		}

	}

	@Override
	public boolean onNPCTalk(L2NpcInstance npc, L2PcInstance talker)
	{
		return false;
	}

	@Override
	public boolean register(L2PcInstance player)
	{
		if (!canRegister(player))
		{
			return false;
		}

		_players.add(player.getObjectId());
		player._event = this;
		return true;
	}

	@Override
	public void remove(L2PcInstance player)
	{
		if (isParticipant(player))
		{
			_players.remove(player.getObjectId());

			if (_state == GameEvent.STATE_RUNNING)
			{
				if (player.isDead())
				{
					player.doRevive();
				}

				player.setInstanceId(0);
				_playersKills.remove(player);

				if (!DM_RETURNORIGINAL)
				{
					randomTeleport(player);
				}
				else
				{
					player.teleToLocation(_playerLoc.get(player.getObjectId()), false);
				}
			}

			player._event = null;
		}
	}

	@Override
	public boolean canRegister(L2PcInstance player)
	{
		if (getState() != STATE_ACTIVE)
		{
			player.sendMessage(Language.LANG_EVEN_UNAVAILABLE);
			return false;
		}

		if (isParticipant(player))
		{
			player.sendMessage(Language.LANG_ALWAYS_REGISTER);
			return false;
		}

		if (!Config.Allow_Same_HWID_On_Events && player.getClient().getHWId() != null && player.getClient().getHWId().length() != 0)
		{
			L2PcInstance pc;
			for (int charId : _players.toArray())
			{
				pc = L2World.getInstance().getPlayer(charId);
				if (pc != null && player.getClient().getHWId().equals(pc.getClient().getHWId()))
				{
					player.sendMessage(Language.LANG_DUPLICATE_HWID);
					return false;
				}
			}
		}

		if (!Config.Allow_Same_IP_On_Events)
		{
			L2PcInstance pc;
			for (int charId : _players.toArray())
			{
				pc = L2World.getInstance().getPlayer(charId);
				if (pc != null && pc.getClient() != null && player.getClient().getHostAddress().equals(pc.getClient().getHostAddress()))
				{
					player.sendMessage(Language.LANG_DUPLICATE_IP);
					return false;
				}
			}
		}

		if (_players.size() >= _maxPlayers)
		{
			player.sendMessage(Language.LANG_MAX_PLAYERS);
			return false;
		}

		if (player.isCursedWeaponEquiped() && !JOIN_CURSED)
		{
			player.sendMessage(Language.LANG_CURSED_WEAPON);
			return false;
		}

		if (player.getLevel() > _maxLvl || player.getLevel() < _minLvl)
		{
			player.sendMessage(Language.LANG_NON_ENOUGH_LEVEL);
			return false;
		}

		return player.canRegisterToEvents();
	}

	@Override
	public boolean start()
	{
		_players.clear();

		AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_1);
		AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_2 + ": " + _minLvl + "-" + _maxLvl + ".");
		AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_3);

		for (int i = 0; i < _rewardId.length; i++)
		{
			AnnounceToPlayers(true, " - " + _rewardAmount[i] + " " + ItemTable.getInstance().getTemplate(_rewardId[i]).getName());
		}

		AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_4.replace("{$time}", String.valueOf(_regTime)));

		_state = GameEvent.STATE_ACTIVE;
		_remaining = (_regTime * 60000) / 2;
		_registrationTask.schedule(_remaining);
		return true;
	}

	@Override
	public boolean canInteract(L2Character actor, L2Character target)
	{
		return _state != GameEvent.STATE_RUNNING || (actor._event == target._event && actor._event == this) || ALLOW_INTERFERENCE;
	}

	@Override
	public boolean canAttack(L2Character attacker, L2Character target)
	{
		return _state == GameEvent.STATE_RUNNING && attacker._event == target._event && attacker._event == this;
	}

	@Override
	public boolean canBeSkillTarget(L2Character caster, L2Character target, L2Skill skill)
	{
		return _state != GameEvent.STATE_RUNNING;
	}

	@Override
	public boolean canUseItem(L2Character actor, L2ItemInstance item)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (item.getItem().getItemType() == L2EtcItemType.POTION)
			{
				return ALLOW_POTIONS;
			}
			else
			{
				int itemId = item.getItemId();
				return !((itemId == 3936 || itemId == 3959 || itemId == 737 || itemId == 9157 || itemId == 10150 || itemId == 13259));
			}

		}
		return true;
	}

	@Override
	public boolean canUseSkill(L2Character caster, L2Skill skill)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
			{
				return true;
			}
			else if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
			{
				return ALLOW_SUMMON;
			}
			else if (skill.getSkillType() == L2Skill.SkillType.HEAL || skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.MANAHEAL)
			{
				return caster.getTarget() == caster;
			}
		}
		return true;
	}

	@Override
	public void onRevive(L2Character actor)
	{
		if (RESORE_HP_MP_CP && _state == GameEvent.STATE_RUNNING)
		{
			actor.getStatus().setCurrentCp(actor.getMaxCp());
			actor.getStatus().setCurrentHp(actor.getMaxHp());
			actor.getStatus().setCurrentMp(actor.getMaxMp());
		}
	}

	@Override
	public void onLogin(L2PcInstance player)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			remove(player);
		}
	}

	/* Приватные методы эвента */
	public void AnnounceToPlayers(Boolean toall, String announce)
	{
		if (toall)
		{
			Announcements.getInstance().criticalAnnounceToAll(announce);
		}
		else
		{
			CreatureSay cs = new CreatureSay(0, CreatureSay.SystemChatChannelId.Chat_Critical_Announce, "", announce);
			L2PcInstance player;
			if (_players != null && !_players.isEmpty())
			{
				for (Integer playerid : _players.toArray())
				{
					player = L2World.getInstance().getPlayer(playerid);
					if (player != null && player.isOnline() != 0)
					{
						player.sendPacket(cs);
					}
				}
			}
		}
	}

	private final ExclusiveTask _registrationTask = new ExclusiveTask()
	{
		private boolean showed;

		@Override
		protected void onElapsed()
		{
			if (_remaining < 1000)
			{
				run();
			}
			else
			{
				if (_remaining >= 60000)
				{
					AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_5 + " " + _remaining / 60000 + " min");
				}
				else if (!showed)
				{
					AnnounceToPlayers(true, getName() + ": " + Language.LANG_ANNOUNCE_6);
					showed = true;
				}
				_remaining /= 2;
				schedule(_remaining);
			}
		}
	};

	private Runnable TeleportTask = new Runnable()
	{
		@Override
		public void run()
		{
			L2PcInstance player;
			int[] par = {-1, 1};
			int Radius = 500;

			for (Integer playerId : _players.toArray())
			{
				player = L2World.getInstance().getPlayer(playerId);
				if (player != null)
				{
					player.abortAttack();
					player.abortCast();
					player.setTarget(null);
					if (RESET_SKILL_REUSE)
					{
						player.resetSkillTime(true);
					}
					if (ON_START_REMOVE_ALL_EFFECTS)
					{
						player.stopAllEffects();
					}
					if (player.getPet() != null)
					{
						player.getPet().abortAttack();
						player.getPet().abortCast();
						player.getPet().setTarget(null);
						if (ON_START_REMOVE_ALL_EFFECTS)
						{
							player.getPet().stopAllEffects();
						}
						if (ON_START_UNSUMMON_PET)
						{
							player.getPet().unSummon(player);
						}
					}
					if (player.getParty() != null)
					{
						player.getParty().removePartyMember(player);
					}
					player.setInstanceId(_instanceId);

					player.teleToLocation(EVENT_LOCATION.getX() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getY() + (par[Rnd.get(2)] * Rnd.get(Radius)), EVENT_LOCATION.getZ());
					_playersKills.put(player, new DeathMatchPlayer());
					player.setTitle("Kills: 0");
					SkillTable.getInstance().getInfo(4515, 1).getEffects(player, player);
					player.sendPacket(new ExShowScreenMessage("1 minutes until event start, wait", 10000));
				}
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					L2PcInstance player;
					for (Integer playerId : _players.toArray())
					{
						player = L2World.getInstance().getPlayer(playerId);
						if (player != null)
						{
							player.stopAllEffects();
						}
					}
					AnnounceToPlayers(false, "DeathMatch: " + Language.LANG_EVENT_START);
					_remaining = _eventTime * 60000;
					_eventTask.schedule(10000);
				}
			}, 60000);
		}
	};

	private final ExclusiveTask _eventTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			_remaining -= 10000;
			if (_remaining <= 0)
			{
				rewardPlayers();
				return;
			}
			_eventTask.schedule(10000);
		}
	};


	private class revivePlayer implements Runnable
	{
		L2Character _player;

		public revivePlayer(L2Character player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player != null)
			{
				int[] par = {-1, 1};
				int Radius = 500;

				_player.teleToLocation(149800 + (par[Rnd.get(2)] * Rnd.get(Radius)), 46800 + (par[Rnd.get(2)] * Rnd.get(Radius)), -3412);
				_player.doRevive();
			}
		}
	}

	private void rewardPlayers()
	{
		L2PcInstance player;
		L2PcInstance winner = null;
		int top_score = 0;

		for (Integer playerId : _players.toArray())
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				player.abortAttack();
				player.abortCast();
				player.setTarget(null);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

				DeathMatchPlayer dmp = _playersKills.get(player);
				if (dmp.getKills() == top_score && top_score > 0)
				{
					winner = null;
				}

				if (dmp.getKills() > top_score)
				{
					winner = player;
					top_score = dmp.getKills();
				}
			}
		}

		if (winner != null && _playersKills.get(winner).getKills() > 0)
		{
			AnnounceToPlayers(true, getName() + ": " + Language.LANG_WINNER + winner.getName());

			for (int i = 0; i < _rewardId.length; i++)
			{
				winner.addItem("DM Reward", _rewardId[i], _rewardAmount[i], null, true);
			}
		}
		else
		{
			AnnounceToPlayers(true, getName() + ": " + Language.LANG_NO_WINNER);
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				finish();
			}
		}, 10000);
	}

	private void run()
	{
		int realPlayers = 0;
		_playerLoc.clear();
		L2PcInstance player;
		for (Integer playerId : _players.toArray())
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null && player.getLevel() >= _minLvl && player.getLevel() <= _maxLvl && player.getInstanceId() == 0)
			{
				if (!DM_RETURNORIGINAL)
				{
					player.setIsIn7sDungeon(false);
				}
				else
				{
					_playerLoc.put(playerId, player.getLoc());
				}
				realPlayers++;
			}
			else
			{
				if (player != null)
				{
					player._event = null;
				}
				_players.remove(playerId);
			}
		}
		if (realPlayers < _minPlayers)
		{
			AnnounceToPlayers(true, getName() + ": " + Language.LANG_EVENT_ABORT);
			finish();
			return;
		}

		_instanceId = InstanceManager.getInstance().createDynamicInstance(null);
		Instance eventInst = InstanceManager.getInstance().getInstance(_instanceId);
		eventInst.setReturnTeleport(146353, 46709, -3435);
		eventInst.addDoor(24190001, false);
		eventInst.addDoor(24190002, false);
		eventInst.addDoor(24190003, false);
		eventInst.addDoor(24190004, false);
		ThreadPoolManager.getInstance().scheduleGeneral(TeleportTask, 10000);
		_state = GameEvent.STATE_RUNNING;
		if (_eventScript != null)
		{
			_eventScript.onStart(_instanceId);
		}

	}

	/**
	 * Метод рандомного возврата игроков в города
	 * Выбор состоит из 5 городов
	 */
	private void randomTeleport(L2PcInstance player)
	{
		int _locX, _locY, _locZ;
		int _Rnd = Rnd.get(100);

		if (_Rnd < 20) // Giran
		{
			_locX = 81260;
			_locY = 148607;
			_locZ = -3471;
		}
		else if (_Rnd < 40) // Goddart
		{
			_locX = 147709;
			_locY = -53231;
			_locZ = -2732;
		}
		else if (_Rnd < 60) // Rune
		{
			_locX = 43429;
			_locY = -50913;
			_locZ = -796;
		}
		else if (_Rnd < 80) // Oren
		{
			_locX = 80523;
			_locY = 54741;
			_locZ = -1563;
		}
		else // Hein
		{
			_locX = 110745;
			_locY = 220618;
			_locZ = -3671;
		}
		player.teleToLocation(_locX, _locY, _locZ, false);
	}

	public int getRegistredPlayersCount()
	{
		return _players.size();
	}
}
