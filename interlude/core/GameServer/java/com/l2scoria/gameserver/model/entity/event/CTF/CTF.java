package com.l2scoria.gameserver.model.entity.event.CTF;

import com.l2scoria.Config;
import com.l2scoria.L2Properties;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.sql.DoorTable;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.handler.VoicedCommandHandler;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.network.clientpackets.RequestActionUse;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2scoria.gameserver.network.serverpackets.RadarControl;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.taskmanager.ExclusiveTask;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import com.l2scoria.gameserver.templates.L2EtcItemType;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.lang.ArrayUtils;
import com.l2scoria.util.random.Rnd;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;


public class CTF extends GameEvent
{
	private static CTF _instance = null;
	private int _state = GameEvent.STATE_INACTIVE;
	private Map<Integer, Team> _participicants = new FastMap<Integer, Team>();
	private Map<L2PcInstance, Object[]> _flagowners = new FastMap<L2PcInstance, Object[]>();
	private Map<Integer, Location> _playerPositions = new FastMap<Integer, Location>();
	private List<Team> _teams = new FastList<Team>();
	private boolean _canStand = true;

	/* ------ КОНФИГУРАЦИЯ ЭВЕНТА ------- */
	private boolean CTF_IN_INSTANCE;
	private boolean CTF_AURA;
	private boolean CTF_ALLOW_SUMMON;
	private boolean CTF_ALLOW_POTIONS;
	private boolean CTF_REVIVE_RECOVERY;
	private boolean CTF_ALLOW_INTERFERENCE;
	private int CTF_REVIVE_DELAY;
	private boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	private boolean CTF_ON_START_UNSUMMON_PET;
	private boolean CTF_JOIN_CURSED;
	private boolean CTF_CLOSE_COLISEUM_DOORS;
	private boolean CTF_ALLOW_TEAM_CASTING;
	private boolean CTF_RETURNORIGINAL;

	/* -------- ПАРАМЕТРЫ ЭВЕНТА -------- */
	private int _minlvl;
	private int _maxlvl;
	private int _eventTime;
	private int _joinTime;
	private int[] _rewardId = null;
	private int[] _rewardAmount = null;
	private int _minPlayers;
	private int _maxPlayers = 60;
	private int _instanceId = 0;
	private int _remain;

	private class Team
	{
		String name;
		int index;
		int color;
		L2NpcInstance flag = null;
		L2NpcInstance throne = null;
		Location loc;
		Location flagloc;

		List<Integer> members = new FastList<Integer>();
		int flags = 0;

		public void takeFlag(L2PcInstance player)
		{
			Team team = getPlayerTeam(player);
			if (team != null)
			{
				flag.deleteMe();
				L2ItemInstance wep = player.getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_RHAND);
				player.addItem("Take flag", 6718, 1, null, false);
				L2ItemInstance fl = player.getInventory().getItemByItemId(6718);
				player.getInventory().equipItem(fl);
				player.sendMessage("Отнесите флаг на свою базу.");
				player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				player.sendPacket(new RadarControl(0, 2, team.flagloc.getX(), team.flagloc.getY(), team.flagloc.getZ()));
				_flagowners.put(player, new Object[]{this, wep});
			}
		}

		public void init()
		{
			L2NpcTemplate template;
			template = NpcTable.getInstance().getTemplate(32027);
			throne = new L2NpcInstance(IdFactory.getInstance().getNextId(), template);
			throne.setInstanceId(_instanceId);
			throne.setName(name);
			throne._event = CTF.this;
			throne.setIsInvul(true);
			throne.spawnMe(flagloc.getX(), flagloc.getY(), flagloc.getZ());

			template = NpcTable.getInstance().getTemplate(35062);
			flag = new L2NpcInstance(IdFactory.getInstance().getNextId(), template);
			flag.setInstanceId(_instanceId);
			flag.setName(name);
			flag._event = CTF.this;
			flag.setIsInvul(true);
			flag.spawnMe(flagloc.getX(), flagloc.getY(), flagloc.getZ());
		}

		public void despawn()
		{
			if (flag != null)
			{
				flag.deleteMe();
			}
			if (throne != null)
			{
				throne.deleteMe();
			}
		}
	}

	public CTF()
	{
		_instance = this;
	}

	public String getStatus()
	{
		int free = (_maxPlayers - _participicants.size());
		if (free < 0)
		{
			free = 0;
		}

		return free + " из " + _maxPlayers;
	}

	public static CTF getInstance()
	{
		if (_instance == null)
		{
			new CTF();
		}
		return _instance;
	}

	@Override
	public boolean finish()
	{
		_eventTask.cancel();
		_registrationTask.cancel();

		for (Team team : _teams)
		{
			team.despawn();
		}

		L2PcInstance player;
		for (Integer playerId : _participicants.keySet())
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

		if (CTF_IN_INSTANCE && _instanceId != 0)
		{
			InstanceManager.getInstance().destroyInstance(_instanceId);
			_instanceId = 0;
		}
		else
		{
			if (CTF_CLOSE_COLISEUM_DOORS)
			{
				DoorTable.getInstance().getDoor(24190001).openMe();
				DoorTable.getInstance().getDoor(24190002).openMe();
				DoorTable.getInstance().getDoor(24190003).openMe();
				DoorTable.getInstance().getDoor(24190004).openMe();
			}
		}

		_participicants.clear();
		_state = GameEvent.STATE_INACTIVE;
		return true;
	}

	@Override
	public String getName()
	{
		return "CTF";
	}

	@Override
	public int getState()
	{
		return _state;
	}

	@Override
	public boolean isParticipant(L2PcInstance player)
	{
		return _participicants.containsKey(player.getObjectId());
	}

	@Override
	public boolean load()
	{
		readConfig();
		if (_instance == null)
		{
			return false;
		}

		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new VoiceCtfEngie());
		TaskManager.getInstance().registerTask(new TaskCtfStart());
		return true;
	}

	@Override
	public void onCommand(L2PcInstance actor, String command, String params)
	{
		if (actor != null)
		{
			if (command.equals("join"))
			{
				if (register(actor))
				{
					actor.sendMessage("Вы зарегистрированы на эвенте CTF");
				}
				else
				{
					actor.sendMessage("Ваше участие на эвенте невозможно");
				}
			}
			else if (command.equals("leave"))
			{
				if (isParticipant(actor))
				{
					remove(actor);
					actor.sendMessage("Ваше участие на эвенте CTF отменено.");
				}
				else
				{
					actor.sendMessage("Вы не участник эвента.");
				}
			}
		}
	}

	@Override
	public void onKill(L2Character killer, L2Character victim)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			L2PcInstance pv = victim.getActingPlayer();

			if (hasFlag(pv))
			{
				removeFlag(pv);
			}

			if (killer == null)
			{
				return;
			}

			L2PcInstance pk = killer.getActingPlayer();

			if (victim._event == this)
			{
				pv.sendMessage("Вы убиты, дождитесь воскрешения.");
				ThreadPoolManager.getInstance().scheduleGeneral(new ReviveTask(victim), CTF_REVIVE_DELAY * 1000);
			}
			if (getPlayerTeam(victim) == getPlayerTeam(killer))
			{
				pk.sendMessage("Вы убили члена своей команды.");
				if (pk.getDeathPenaltyBuffLevel() < 10)
				{
					pk.setDeathPenaltyBuffLevel(pk.getDeathPenaltyBuffLevel() + 5);
				}
			}
			if (killer._event != this || victim._event != this)
			{
				pk.sendMessage("Вы убили простого игрока.");
				if (pk.getDeathPenaltyBuffLevel() < 10)
				{
					pk.setDeathPenaltyBuffLevel(pk.getDeathPenaltyBuffLevel() + 5);
				}
			}
		}
	}

	@Override
	public boolean onNPCTalk(L2NpcInstance npc, L2PcInstance talker)
	{
		if (hasFlag(talker))
		{
			Team team = getPlayerTeam(talker);
			if (team != null && (npc == team.flag || npc == team.throne))
			{
				team.flags++;
				removeFlag(talker);

				Team talkerTeam = getPlayerTeam(talker);
				String talkerName = talker.getName();
				L2PcInstance player;
				for (Integer playerId : talkerTeam.members)
				{
					player = L2World.getInstance().getPlayer(playerId);
					if (player != null)
					{
						player.sendPacket(new ExShowScreenMessage("Игрок " + talkerName + " приносит 1 очко вашей команде", 5000));
					}
				}
			}
		}
		else
		{
			L2PcInstance player;
			for (Team team : _teams)
			{
				if (team.flag == npc && team != getPlayerTeam(talker) && getPlayerTeam(talker) != null)
				{
					if (!hasFlag(talker))
					{
						team.takeFlag(talker);

						String talkerName = talker.getName();
						for (Integer playerId : team.members)
						{
							player = L2World.getInstance().getPlayer(playerId);
							if (player != null)
							{
								player.sendPacket(new ExShowScreenMessage("Игрок " + talkerName + " захватил флаг вашей команды", 5000));
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean register(L2PcInstance player)
	{
		if (!canRegister(player))
		{
			return false;
		}
		_participicants.put(player.getObjectId(), null);
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
				if (hasFlag(player))
				{
					removeFlag(player);
				}
				Team playerTeam = _participicants.get(player.getObjectId());
				if (playerTeam != null)
				{
					playerTeam.members.remove(player.getObjectId());
				}
				player.setInstanceId(0);
				if (!CTF_RETURNORIGINAL)
				{
					randomTeleport(player);
				}
				else
				{
					player.teleToLocation(_playerPositions.get(player.getObjectId()), false);
				}
				player.setTeam(0);
				if (player.isDead())
				{
					player.doRevive();
				}
			}
			_participicants.remove(player.getObjectId());
			player._event = null;
		}
	}

	@Override
	public boolean start()
	{
		_participicants.clear();
		_teams.clear();
		_flagowners.clear();

		readConfig();


		_remain = _joinTime * 60000 / 2;
		AnnounceToPlayers(true, getName() + ": Открыта регистрация.");
		AnnounceToPlayers(true, getName() + ": Разница уровней " + _minlvl + "-" + _maxlvl + ".");
		AnnounceToPlayers(true, getName() + ": Награды:");

		for (int i = 0; i < _rewardId.length; i++)
		{
			AnnounceToPlayers(true, " - " + _rewardAmount[i] + " " + ItemTable.getInstance().getTemplate(_rewardId[i]).getName());
		}

		AnnounceToPlayers(true, getName() + ": Начало через " + _joinTime + " минут.");

		_registrationTask.schedule(_remain);
		_state = GameEvent.STATE_ACTIVE;
		return true;
	}

	@Override
	public void onLogout(L2PcInstance player)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (hasFlag(player))
			{
				removeFlag(player);
			}
		}
	}

	@Override
	public void onLogin(L2PcInstance player)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			player.abortAttack();
			player.abortCast();
			player.setTarget(null);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if (player.isMounted())
			{
				player.dismount();
			}
			if (CTF_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
			}
			if (player.getPet() != null)
			{
				if (CTF_ON_START_UNSUMMON_PET)
				{
					player.getPet().unSummon(player);
				}
				else if (CTF_ON_START_REMOVE_ALL_EFFECTS)
				{
					player.getPet().stopAllEffects();
				}
			}
			player.setInstanceId(_instanceId);
			player.teleToLocation(getPlayerTeam(player).loc, false);
			if (CTF_AURA)
			{
				player.setTeam(getPlayerTeam(player).index);
			}
		}
	}

	@Override
	public boolean canInteract(L2Character actor, L2Character target)
	{
		return _state != GameEvent.STATE_RUNNING || (actor._event == target._event && target._event == this) || CTF_ALLOW_INTERFERENCE;
	}

	@Override
	public boolean canBeSkillTarget(L2Character caster, L2Character target, L2Skill skill)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
			{
				return getPlayerTeam(caster) == getPlayerTeam(target);
			}

		}
		return true;
	}

	@Override
	public boolean canUseItem(L2Character actor, L2ItemInstance item)
	{
		boolean result = true;
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (item.isEquipable() && hasFlag(actor.getActingPlayer()))
			{
				result = false;
			}

			if (item.getItem().getItemType() == L2EtcItemType.POTION)
			{
				result = CTF_ALLOW_POTIONS;
			}
			else if (item.getItem().getItemType() == L2EtcItemType.PET_COLLAR)
			{
				result = CTF_ALLOW_SUMMON;
			}
			else
			{
				int itemId = item.getItemId();
				result = !((itemId == 3936 || itemId == 3959 || itemId == 737 || itemId == 9157 || itemId == 10150 || itemId == 13259));
			}
		}
		L2PcInstance ap = actor.getActingPlayer();
		if (!result)
		{
			ap.sendMessage("Вы не можете использовать этот предемет на эвенте.");
		}
		return result;
	}

	@Override
	public boolean canUseSkill(L2Character caster, L2Skill skill)
	{
		boolean result = true;
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
			{
				result = CTF_ALLOW_SUMMON;
			}
			else if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
			{
				return true;
			}
			else if (skill.getSkillType() == L2Skill.SkillType.HEAL || skill.getSkillType() == L2Skill.SkillType.REFLECT || skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.MANAHEAL)
			{
				result = getPlayerTeam(caster) == getPlayerTeam((L2Character) caster.getTarget());
			}
			else if (getPlayerTeam(caster) == getPlayerTeam((L2Character) caster.getTarget()))
			{
				result = CTF_ALLOW_TEAM_CASTING;
			}
		}
		L2PcInstance cp = caster.getActingPlayer();
		if (!result)
		{
			cp.sendMessage("Это умение запрещено на эвенте.");
		}

		return result;
	}

	@Override
	public void onRevive(L2Character actor)
	{
		if (_state == GameEvent.STATE_RUNNING && CTF_REVIVE_RECOVERY)
		{
			actor.getStatus().setCurrentCp(actor.getMaxCp());
			actor.getStatus().setCurrentHp(actor.getMaxHp());
			actor.getStatus().setCurrentMp(actor.getMaxMp());
		}
	}

	@Override
	public boolean canRegister(L2PcInstance player)
	{
		if (getState() != STATE_ACTIVE)
		{
			player.sendMessage("Извините, эвент не доступен.");
			return false;
		}

		if (isParticipant(player))
		{
			player.sendMessage("Вы уже зарегистрированы на эвент.");
			return false;
		}

		if (!Config.Allow_Same_HWID_On_Events && player.getClient().getHWId() != null && player.getClient().getHWId().length() != 0)
		{
			L2PcInstance pc = null;
			for (int charId : _participicants.keySet())
			{
				pc = L2World.getInstance().getPlayer(charId);
				if (pc != null && player.getClient().getHWId().equals(pc.getClient().getHWId()))
				{
					player.sendMessage("Игрок с вашего компьютера уже зарегистрирован.");
					return false;
				}
			}
		}

		if (!Config.Allow_Same_IP_On_Events)
		{
			L2PcInstance pc = null;
			for (int charId : _participicants.keySet())
			{
				pc = L2World.getInstance().getPlayer(charId);
				if (pc != null && pc.getClient() != null && player.getClient().getHostAddress().equals(pc.getClient().getHostAddress()))
				{
					player.sendMessage("Игрок с вашего компьютера уже зарегистрирован.");
					return false;
				}
			}
		}

		if (_participicants.size() >= _maxPlayers)
		{
			player.sendMessage("Все места на эвент заняты.");
			return false;
		}

		if (player.isCursedWeaponEquiped() && !CTF_JOIN_CURSED)
		{
			player.sendMessage("Запрещено с проклятым оружием.");
			return false;
		}

		if (player.getLevel() > _maxlvl || player.getLevel() < _minlvl)
		{
			player.sendMessage("Ваш уровень не подходит для участия.");
			return false;
		}

		return player.canRegisterToEvents();
	}

	@Override
	public boolean canDoAction(L2PcInstance player, int action)
	{
		if (_state == GameEvent.STATE_RUNNING)
		{
			if (action == RequestActionUse.ACTION_SIT_STAND)
			{
				return _canStand;
			}
			else
			{
				return !hasFlag(player) && action != RequestActionUse.ACTION_MOUNT;
			}
		}
		return true;
	}

	// ============================= МЕТОДЫ ЭВЕНТА ==============================================

	private final ExclusiveTask _registrationTask = new ExclusiveTask()
	{
		private int announces = 0;
		private boolean showed;

		@Override
		protected void onElapsed()
		{
			if (_remain < 1000)
			{
				run();
			}
			else
			{
				if (_remain > 60000)
				{
					if (announces == 0)
					{
						AnnounceToPlayers(true, getName() + ": До конца регистрации " + _remain / 60000 + " минут");
						announces++;
					}
				}
				else
				{
					if (!showed && announces == 1 && _remain <= 30000)
					{
						AnnounceToPlayers(true, getName() + ": До конца регистрации меньше минуты");
						showed = true;
						announces++;
					}

				}
				_remain /= 2;
				schedule(_remain);
			}
		}
	};

	private final ExclusiveTask _eventTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (_remain > 0)
			{
				schedule(_remain);
				_remain = 0;
			}
			else
			{
				rewardPlayers();
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					public void run()
					{
						finish();
					}
				}, 10000);
			}
		}
	};

	private void rewardPlayers()
	{
		L2PcInstance player;
		for (Integer playerId : _participicants.keySet())
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				player.abortAttack();
				player.abortCast();
				player.setTarget(null);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if (hasFlag(player))
				{
					removeFlag(player);
				}
			}

		}

		Team winner = null;
		int top_score = 0;

		for (Team team : _teams)
		{
			if (team.flags == top_score && top_score > 0)
			{
				winner = null;
			}
			if (team.flags > top_score)
			{
				winner = team;
				top_score = team.flags;
			}
		}

		if (winner != null && winner.flags > 0)
		{
			AnnounceToPlayers(true, getName() + ": Победитель - команда " + winner.name);

			for (Integer playerId : winner.members)
			{
				player = L2World.getInstance().getPlayer(playerId);
				if (player != null)
				{
					for (int i = 0; i < _rewardId.length; i++)
					{
						player.addItem("CTF Reward", _rewardId[i], _rewardAmount[i], null, true);
					}
				}
			}
		}
		else
		{
			AnnounceToPlayers(true, getName() + ": Победитель не определен");
		}
	}

	private void run()
	{
		_playerPositions.clear();
		L2PcInstance player;
		_registrationTask.cancel();
		for (Integer playerId : _participicants.keySet())
		{
			player = L2World.getInstance().getPlayer(playerId);
			if (player != null)
			{
				if (player.getInstanceId() != 0 || player.getLevel() < _minlvl || player.getLevel() > _maxlvl || player.isInStoreMode() || player.isDead())
				{
					player.sendMessage("Ваше участие на эвенте невозможно");
					player._event = null;
					_participicants.remove(playerId);
				}
				if (!CTF_RETURNORIGINAL)
				{
					player.setIsIn7sDungeon(false);
				}
				else
				{
					_playerPositions.put(player.getObjectId(), player.getLoc());
				}
			}
			else
			{
				_participicants.remove(playerId);
			}
		}
		if (_participicants.size() < _minPlayers)
		{
			AnnounceToPlayers(true, getName() + ": Недостаточно игроков");
			finish();
			return;
		}
		int delta = _participicants.size() % 2 == 0 ? 0 : 1;
		for (; ; )
		{
			boolean allShuffled = true;
			for (Integer playerId : _participicants.keySet())
			{
				if (_participicants.get(playerId) == null)
				{
					Team team;
					for (; ; )
					{
						team = _teams.get(Rnd.get(_teams.size()));
						if (team.members.size() < _participicants.size() / _teams.size() + delta)
						{
							break;
						}
					}
					team.members.add(playerId);
					_participicants.put(playerId, team);
					allShuffled = false;
				}
			}
			if (allShuffled)
			{
				break;
			}
		}

		if (CTF_IN_INSTANCE)
		{
			_instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			Instance inst = InstanceManager.getInstance().getInstance(_instanceId);
			inst.setReturnTeleport(82688, 148344, -3469);
			if (CTF_CLOSE_COLISEUM_DOORS)
			{
				inst.addDoor(24190001, false);
				inst.addDoor(24190002, false);
				inst.addDoor(24190003, false);
				inst.addDoor(24190004, false);
			}
		}
		else
		{
			if (CTF_CLOSE_COLISEUM_DOORS)
			{
				DoorTable.getInstance().getDoor(24190001).closeMe();
				DoorTable.getInstance().getDoor(24190002).closeMe();
				DoorTable.getInstance().getDoor(24190003).closeMe();
				DoorTable.getInstance().getDoor(24190004).closeMe();
			}
		}
		_state = GameEvent.STATE_RUNNING;
		for (Team team : _teams)
		{
			if (team.members.size() == 0)
			{
				_teams.remove(team);
			}
			else
			{
				team.init();
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				L2PcInstance player;
				_canStand = false;
				for (Integer playerId : _participicants.keySet())
				{
					player = L2World.getInstance().getPlayer(playerId);
					if (player != null)
					{
						onLogin(player);
						player.sitDown();
					}
				}
			}
		}, 10000);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				L2PcInstance player;
				_canStand = true;
				for (Integer playerId : _participicants.keySet())
				{
					player = L2World.getInstance().getPlayer(playerId);
					if (player != null)
					{
						player.standUp();
					}
				}
				AnnounceToPlayers(false, getName() + ": Игра началась!");
				_remain = _eventTime * 60000 / 2;
				_eventTask.schedule(_remain);
			}
		}, 20000);
		if (_eventScript != null)
		{
			_eventScript.onStart(_instanceId);
		}

	}

	private Team getPlayerTeam(L2Character player)
	{
		if (player == null)
		{
			return null;
		}
		return _participicants.get(player.getObjectId());
	}

	public static boolean hasFlag(L2PcInstance player)
	{
		return player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null && player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItemId() == 6718;
	}

	private void removeFlag(L2PcInstance player)
	{
		player.destroyItemByItemId("Drop Flag", 6718, 1, null, false);
		player.sendPacket(new RadarControl(1, 2, 0, 0, 0));
		Team team = (Team) _flagowners.get(player)[0];
		L2ItemInstance wep = (L2ItemInstance) _flagowners.get(player)[1];
		_flagowners.remove(player);
		if (team != null)
		{
			team.flag.spawnMe(team.flagloc.getX(), team.flagloc.getY(), team.flagloc.getZ());
		}
		if (wep != null)
		{
			player.getInventory().equipItem(wep);
		}

	}

	private void AnnounceToPlayers(Boolean toall, String announce)
	{
		if (toall)
		{
			Announcements.getInstance().criticalAnnounceToAll(announce);
		}
		else
		{
			L2PcInstance player;
			CreatureSay cs = new CreatureSay(0, CreatureSay.SystemChatChannelId.Chat_Critical_Announce, "", announce);
			for (Integer charId : _participicants.keySet())
			{
				player = L2World.getInstance().getPlayer(charId);
				if (player != null)
				{
					player.sendPacket(cs);
				}
			}
		}
	}

	private class ReviveTask implements Runnable
	{
		private L2Character _player;

		public ReviveTask(L2Character victim)
		{
			_player = victim;
		}

		public void run()
		{
			if (_player != null)
			{
				if (getPlayerTeam(_player) != null)
				{
					_player.teleToLocation(getPlayerTeam(_player).loc, false);
					_player.doRevive();
				}
				else
				{
					remove((L2PcInstance) _player);
				}
			}
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

	private void readConfig()
	{
		try
		{
			/* ----- Файл с параметрами -----*/
			L2Properties Setting = new L2Properties("./config/events/CTF.properties");

			/* ----- Чтение параметров ------*/
			if (!Boolean.parseBoolean(Setting.getProperty("CTFEnabled", "true")))
			{
				_instance = null;
				return;
			}

			CTF_AURA = Boolean.parseBoolean(Setting.getProperty("CTFAura", "true"));
			CTF_IN_INSTANCE = Boolean.parseBoolean(Setting.getProperty("CTFInInstance", "true"));
			CTF_REVIVE_RECOVERY = Boolean.parseBoolean(Setting.getProperty("CTFReviveRecovery", "true"));
			CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(Setting.getProperty("CTFAllowInterference", "false"));
			CTF_ALLOW_POTIONS = Boolean.parseBoolean(Setting.getProperty("CTFAllowPotions", "false"));
			CTF_ALLOW_SUMMON = Boolean.parseBoolean(Setting.getProperty("CTFAllowSummon", "true"));
			CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(Setting.getProperty("CTFOnStartRemoveAllEffects", "true"));
			CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(Setting.getProperty("CTFOnStartUnsummonPet", "true"));
			CTF_JOIN_CURSED = Boolean.parseBoolean(Setting.getProperty("CTFJoinWithCursedWeapon", "false"));
			CTF_REVIVE_DELAY = Integer.parseInt(Setting.getProperty("CTFReviveDelay", "10"));
			CTF_CLOSE_COLISEUM_DOORS = Boolean.parseBoolean(Setting.getProperty("CTFCloseColiseumDoors", "true"));
			CTF_ALLOW_TEAM_CASTING = Boolean.parseBoolean(Setting.getProperty("CTFAllowTeamCasting", "false"));
			CTF_RETURNORIGINAL = Boolean.parseBoolean(Setting.getProperty("CTFOriginalPosition", "false"));
			_minlvl = Integer.parseInt(Setting.getProperty("CTFMinLevel", "1"));
			_maxlvl = Integer.parseInt(Setting.getProperty("CTFMaxLevel", "85"));
			_rewardId = null;
			_rewardAmount = null;

			for (String s : Setting.getProperty("CTFRewardId", "57").split(","))
			{
				_rewardId = ArrayUtils.add(_rewardId, Integer.parseInt(s));
			}

			for (String s : Setting.getProperty("CTFRewardAmount", "100000").split(","))
			{
				_rewardAmount = ArrayUtils.add(_rewardAmount, Integer.parseInt(s));
			}

			_joinTime = Integer.parseInt(Setting.getProperty("CTFJoinTime", "5"));
			_eventTime = Integer.parseInt(Setting.getProperty("CTFEventTime", "15"));
			_minPlayers = Integer.parseInt(Setting.getProperty("CTFMinPlayers", "8"));
			_maxPlayers = Integer.parseInt(Setting.getProperty("CTFMaxPlayers", "60"));

			/* ---- Blue Team ------ */
			Team teamBlue = new Team();
			String[] teamLocBlue = Setting.getProperty("BlueTeamLoc", "150545,46734,-3415").split(",");
			String[] flagLocBlue = Setting.getProperty("BlueFlagLoc", "150399,46732,-3390").split(",");

			teamBlue.index = _teams.size() + 1;
			teamBlue.name = "Blue";
			teamBlue.color = 16711680;
			teamBlue.loc = new Location(Integer.parseInt(teamLocBlue[0]), Integer.parseInt(teamLocBlue[1]), Integer.parseInt(teamLocBlue[2]));
			teamBlue.flagloc = new Location(Integer.parseInt(flagLocBlue[0]), Integer.parseInt(flagLocBlue[1]), Integer.parseInt(flagLocBlue[2]));
			_teams.add(teamBlue);

			/* ---- Red Team ------ */
			Team teamRed = new Team();
			String[] teamLocRed = Setting.getProperty("RedTeamLoc", "148386,46747,-3415").split(",");
			String[] flagLocRed = Setting.getProperty("RedFlagLoc", "148501,46738,-3390").split(",");

			teamRed.index = _teams.size() + 1;
			teamRed.name = "Red";
			teamRed.color = 255;
			teamRed.loc = new Location(Integer.parseInt(teamLocRed[0]), Integer.parseInt(teamLocRed[1]), Integer.parseInt(teamLocRed[2]));
			teamRed.flagloc = new Location(Integer.parseInt(flagLocRed[0]), Integer.parseInt(flagLocRed[1]), Integer.parseInt(flagLocRed[2]));
			_teams.add(teamRed);
		} catch (Exception e)
		{
			_log.warn("CTF Engine: Error reading config", e);
			_instance = null;
		}
	}

	public int getRegistredPlayersCount()
	{
		return _participicants.size();
	}

	public int getCharTitleColor(L2PcInstance cha, L2PcInstance other)
	{
		return getPlayerTeam(cha).color;
	}

	public String getTitle(L2PcInstance cha, L2PcInstance other)
	{
		return getPlayerTeam(cha).name;

	}

}