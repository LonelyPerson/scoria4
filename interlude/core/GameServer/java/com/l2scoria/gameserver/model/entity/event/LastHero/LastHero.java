package com.l2scoria.gameserver.model.entity.event.LastHero;

import com.l2scoria.Config;
import com.l2scoria.L2Properties;
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
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.taskmanager.ExclusiveTask;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import com.l2scoria.gameserver.templates.L2EtcItemType;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.L2Utils;
import com.l2scoria.util.random.Rnd;
import javolution.util.FastList;
import org.apache.commons.lang.ArrayUtils;


/**
 * @author vgy766
 * @version 2.0
 */

public class LastHero extends GameEvent
{
	private FastList<Integer> _players = new FastList<Integer>();
	private FastList<Integer> _winners = new FastList<Integer>();
	private int _state = GameEvent.STATE_INACTIVE;
	private static LastHero _instance = null;
	public long _eventDate = 0;
	private int _superPrizeId = 0;
	private int _superPrizeCount = 0;
	private int _minLvl = 0;
	private int _maxLvl = 0;
	private int _maxPlayers = 60;
	private int _minPlayers = 0;
	private int _instanceId = 0;
	private int _regTime = 0;
	private int _eventTime = 0;
	private int[] _rewardId = null;
	private int[] _rewardAmount = null;
	private int _remaining;

	private boolean LH_ON_START_REMOVE_ALL_EFFECTS;
	private Location LH_LOC;
	private boolean LH_ON_START_UNSUMMON_PET;
	private boolean LH_RESORE_HP_MP_CP;
	private boolean LH_ALLOW_POTIONS;
	private boolean LH_ALLOW_SUMMON;
	private boolean LH_JOIN_CURSED;
	private boolean LH_ALLOW_INTERFERENCE;
	private boolean LH_REWARD_KILLS;
	private boolean LH_GIVE_HERO;
	private int HERO_DAYS;

	public static LastHero getInstance()
	{
		if (_instance == null)
		{
			new LastHero();
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

	public LastHero()
	{
		_instance = this;
	}

	@Override
	public boolean finish()
	{
		_eventTask.cancel();
		_registrationTask.cancel();
		L2PcInstance player;
		for (Integer playerId : _players)
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
		_winners.clear();
		_state = GameEvent.STATE_INACTIVE;
		return true;
	}

	@Override
	public String getName()
	{
		return "LastHero";
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
			L2Properties Setting = new L2Properties("./config/events/LastHero.properties");

			/* ----- Чтение параметров ------*/
			if (!Boolean.parseBoolean(Setting.getProperty("LHEnabled", "true")))
			{
				_instance = null;
				return false;
			}

			LH_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(Setting.getProperty("LHOnStartRemoveAllEffects", "true"));
			LH_ON_START_UNSUMMON_PET = Boolean.parseBoolean(Setting.getProperty("LHOnStartUnsummonPet", "true"));
			LH_RESORE_HP_MP_CP = Boolean.parseBoolean(Setting.getProperty("LHOnStartRestoreHpMpCp", "false"));
			LH_ALLOW_POTIONS = Boolean.parseBoolean(Setting.getProperty("LHAllowPotion", "false"));
			LH_ALLOW_SUMMON = Boolean.parseBoolean(Setting.getProperty("LHAllowSummon", "false"));
			LH_JOIN_CURSED = Boolean.parseBoolean(Setting.getProperty("LHCursedWeapon", "false"));
			LH_ALLOW_INTERFERENCE = Boolean.parseBoolean(Setting.getProperty("LHTAllowInterference", "false"));
			LH_REWARD_KILLS = Boolean.parseBoolean(Setting.getProperty("LHRewardKills", "true"));
			LH_GIVE_HERO = Boolean.parseBoolean(Setting.getProperty("LHGiveHero", "true"));

			LH_LOC = new Location(Setting.getProperty("LHLocation", "149800 46800 -3412"));
			HERO_DAYS = Integer.parseInt(Setting.getProperty("LHHeroDays", "0"));
			_regTime = Integer.parseInt(Setting.getProperty("LHRegTime", "10"));
			_eventTime = Integer.parseInt(Setting.getProperty("LHEventTime", "10"));

			_rewardId = null;
			_rewardAmount = null;
			for (String s : Setting.getProperty("LHRewardItem", "57").split(","))
			{
				_rewardId = ArrayUtils.add(_rewardId, Integer.parseInt(s.trim()));
			}
			for (String s : Setting.getProperty("LHRewardItemCount", "100000").split(","))
			{
				_rewardAmount = ArrayUtils.add(_rewardAmount, Integer.parseInt(s.trim()));
			}

			_minPlayers = Integer.parseInt(Setting.getProperty("LHMinPlayers", "2"));
			_maxPlayers = Integer.parseInt(Setting.getProperty("LHMaxPlayers", "60"));
			_minLvl = Integer.parseInt(Setting.getProperty("LHMinLevel", "1"));
			_maxLvl = Integer.parseInt(Setting.getProperty("LHMaxLevel", "90"));

			_superPrizeId = Integer.parseInt(Setting.getProperty("LHPrizeId", "6392"));
			_superPrizeCount = Integer.parseInt(Setting.getProperty("LHPrizeCount", "5"));
		} catch (Exception e)
		{
			_log.warn("LastHero: Error reading config ", e);
			return false;
		}

		TaskManager.getInstance().registerTask(new TaskStartLH());
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new VoiceLastHero());
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
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (victim._event == this && killer._event == this && victim.isPlayer)
			{
				if (LH_REWARD_KILLS && killer.isPlayer)
				{
					((L2PcInstance) killer).addItem("LastHero Kill", _rewardId, _rewardAmount, null, true);
					((L2PcInstance) killer).sendMessage(Language.LANG_KILL_BONUS);
				}

				if (!_winners.contains(killer.getObjectId()))
				{
					_winners.add(killer.getObjectId());
				}

				if (_winners.contains(victim.getObjectId()))
				{
					_winners.remove(victim.getObjectId());
				}

				remove((L2PcInstance) victim);
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
			if (_state == GameEvent.STATE_RUNNING)
			{
				if (player.isDead())
				{
					player.doRevive();
				}
				player.setInstanceId(0);
				randomTeleport(player);
			}
			player._event = null;
			_players.remove(player.getObjectId());
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
			L2PcInstance pc = null;
			for (int charId : _players)
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
			L2PcInstance pc = null;
			for (int charId : _players)
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

		if (player.isCursedWeaponEquiped() && !LH_JOIN_CURSED)
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

		AnnounceToPlayers(true, "LastHero: " + Language.LANG_ANNOUNCE_1);
		AnnounceToPlayers(true, "LastHero: " + Language.LANG_ANNOUNCE_2 + ": " + _minLvl + "-" + _maxLvl + ".");
		AnnounceToPlayers(true, " - " + _superPrizeCount + " " + ItemTable.getInstance().getTemplate(_superPrizeId).getName());
		AnnounceToPlayers(true, "LastHero: " +Language.LANG_ANNOUNCE_3.replace("{$time}", String.valueOf(_regTime)));

		_state = GameEvent.STATE_ACTIVE;
		_remaining = (_regTime * 60000) / 2;
		_registrationTask.schedule(_remaining);
		return true;
	}

	@Override
	public boolean canInteract(L2Character actor, L2Character target)
	{
		return _state != GameEvent.STATE_RUNNING || (actor._event == target._event && actor._event == this) || LH_ALLOW_INTERFERENCE;
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
				return LH_ALLOW_POTIONS;
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
			if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
			{
				return LH_ALLOW_SUMMON;
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
		if (LH_RESORE_HP_MP_CP && _state == GameEvent.STATE_RUNNING)
		{
			actor.getStatus().setCurrentCp(actor.getMaxCp());
			actor.getStatus().setCurrentHp(actor.getMaxHp());
			actor.getStatus().setCurrentMp(actor.getMaxMp());
			if (actor.isPlayer)
			{
				remove((L2PcInstance) actor);
			}
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

	// -====================================================== ВНУТРЕНИЕ МЕТОД ЭВЕНТА ====================================
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
				for (Integer playerid : _players)
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

	// Контролирует время регистрации, через половину оставшегося времени извещая. Т.о.
	// Если время на регистрацию 10 мин то извещения будут через 10, 5, 2.5...
	private final ExclusiveTask _registrationTask = new ExclusiveTask()
	{
		private int announces = 0;
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
					if (announces == 0)
					{
						AnnounceToPlayers(true, "LastHero: " + Language.LANG_ANNOUNCE_5 + " " + _remaining / 60000 + " min");
						announces++;
					}
				}
				else if (announces == 1 && _remaining <= 30000 && !showed)
				{
					AnnounceToPlayers(true, "LastHero: " + Language.LANG_ANNOUNCE_6);
					showed = true;
					announces++;
				}
				_remaining /= 2;
				schedule(_remaining);
			}
		}
	};

	private void rewardPlayers()
	{
		L2PcInstance player;
		for (Integer playerId : _players)
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				player.abortAttack();
				player.abortCast();
				player.setTarget(null);
			}
		}

		if (_winners.size() == 1)
		{
			for (Integer playerId : _winners)
			{
				player = L2World.getInstance().getPlayer(playerId);
				if (player != null && !player.isAlikeDead())
				{
					AnnounceToPlayers(true, "LastHero: " + Language.LANG_WINNER + player.getName());
					player.broadcastPacket(new SocialAction(playerId, 16));
					if (LH_GIVE_HERO)
					{
						if (HERO_DAYS > 0)
						{
							L2Utils.addHeroStatus(player, HERO_DAYS);
						}
						else
						{
							player.setIsHero(true);
						}
					}
					player.addItem("LastHero reward", _superPrizeId, _superPrizeCount, null, true);
				}
			}
		}
		else
		{
			AnnounceToPlayers(true, "LastHero: " + Language.LANG_NO_WINNER);
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				finish();
			}
		}, 10000);
	}

	// Собственно сам эвент. Вызывается каждые 10 секунд времени эвента 
	private final ExclusiveTask _eventTask = new ExclusiveTask()
	{

		@Override
		protected void onElapsed()
		{
			_remaining -= 10000;
			// Время вышло, пора раздать пирожки 
			if (_remaining <= 0)
			{
				rewardPlayers();
				return;
			}

			int alivePlayers = 0;
			L2PcInstance player;
			for (Integer playerId : _players)
			{
				player = L2World.getInstance().getPlayer(playerId);

				if (player != null && !player.isDead())
				{
					alivePlayers++;
				}
			}

			if (alivePlayers <= 1)
			{
				rewardPlayers();
				return;
			}
			_eventTask.schedule(10000);
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
			for (Integer playerId : _players)
			{
				player = L2World.getInstance().getPlayer(playerId);
				if (player != null)
				{
					player.abortAttack();
					player.abortCast();
					player.setTarget(null);
					if (LH_ON_START_REMOVE_ALL_EFFECTS)
					{
						player.stopAllEffects();
					}
					if (player.getPet() != null)
					{
						player.getPet().abortAttack();
						player.getPet().abortCast();
						player.getPet().setTarget(null);
						if (LH_ON_START_REMOVE_ALL_EFFECTS)
						{
							player.getPet().stopAllEffects();
						}
						if (LH_ON_START_UNSUMMON_PET)
						{
							player.getPet().unSummon(player);
						}
					}
					if (player.getParty() != null)
					{
						player.getParty().removePartyMember(player);
					}
					player.setInstanceId(_instanceId);
					player.teleToLocation(LH_LOC.getX() + (par[Rnd.get(2)] * Rnd.get(Radius)), LH_LOC.getY() + (par[Rnd.get(2)] * Rnd.get(Radius)), LH_LOC.getZ()); // отправляем чара на арену
					// Кидаем рейд курс
					SkillTable.getInstance().getInfo(4515, 1).getEffects(player, player);
					player.sendPacket(new ExShowScreenMessage(Language.LANG_FIGHT_1_MIN, 10000));
				}
			}
			// Стартовая подготовка
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					L2PcInstance player;
					for (Integer playerId : _players)
					{
						player = L2World.getInstance().getPlayer(playerId);
						if (player != null)
						{
							player.stopAllEffects();
						}
					}
					AnnounceToPlayers(false, "LastHero: " + Language.LANG_EVENT_START);
					//AnnounceToPlayers(false, "LastHero: Матч продлится " + _eventTime + " минут(ы).");
					_remaining = _eventTime * 60000;
					_eventTask.schedule(10000);
				}
			}, 60000);
		}
	};

	// Запускаем сам эвент.
	private void run()
	{
		int realPlayers = 0;
		L2PcInstance player;
		for (Integer playerId : _players)
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null && player.getLevel() >= _minLvl && player.getLevel() <= _maxLvl)
			{
				realPlayers++;
			}
			else
			{
				_players.remove(playerId);
			}
		}
		if (realPlayers < _minPlayers)
		{
			AnnounceToPlayers(true, "LastHero: " + Language.LANG_EVENT_ABORT);
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