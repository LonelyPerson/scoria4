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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.crypt.nProtect;
import com.l2scoria.crypt.nProtect.RestrictionType;
import com.l2scoria.gameserver.GameServer;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2scoria.gameserver.datatables.GmListTable;
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2scoria.gameserver.handler.custom.CustomWorldHandler;
import com.l2scoria.gameserver.managers.*;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.base.ClassLevel;
import com.l2scoria.gameserver.model.base.PlayerClass;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.ClanHall;
import com.l2scoria.gameserver.model.entity.Hero;
import com.l2scoria.gameserver.model.entity.Wedding;
import com.l2scoria.gameserver.model.entity.event.TvTEvent;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.entity.siege.FortSiege;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.model.quest.QuestState;
import com.l2scoria.gameserver.network.Disconnection;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.thread.TaskPriority;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.FloodProtector;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev656 cbdddd
 * <p>
 *
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}

	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}

	@Override
	protected void runImpl()
	{
		//FIXME: What the fuck comment?

		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}

		// Register in flood protector
		FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());

		if (L2World.getInstance().getPlayer(activeChar.getName()) != null)
		{
			if (Config.DEBUG)
			{
				_log.warning("User already exist in OID map! User " + activeChar.getName() + " is cheater!");
				//activeChar.closeNetConnection();
			}
			//getClient().closeNow();
			//return;
		}

		activeChar.setOnlineStatus(true);

		// engage and notify Partner
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar.getPartnerId());
		}

		EnterGM(activeChar);

		ColorSystem(activeChar);

		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		activeChar.getKnownList().updateKnownObjects();

		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			sendPacket(new SignsSky());
		}

		// buff and status icons
		if (Config.STORE_SKILL_COOLTIME)
		{
			activeChar.restoreEffects();
			activeChar.restoreHpMpOnLoad();
		}

		activeChar.sendPacket(new EtcStatusUpdate(activeChar));

		if (activeChar.getAllEffects() != null)
		{
			for (L2Effect e : activeChar.getAllEffects())
			{
				if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}

				if (e.getEffectType() == L2Effect.EffectType.MANA_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.MANA_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}

				if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					activeChar.removeEffect(e);
				}
                                if (e.getSkill().getSkillType() == SkillType.FORCE_BUFF) 
                                {
                                    activeChar.removeEffect(e);
                                }
			}
		}

		// apply augmentation boni for equipped items
		for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().applyBoni(activeChar);
			}
		}

		//restores custom status
		activeChar.restoreCustomStatus();
                
                // restore some data from login data, like donator status account or hwid
                activeChar.restoreLoginCustomData();

		//Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);

		activeChar.getMacroses().sendUpdate();

		sendPacket(new ClientSetTime()); // SetClientTime

		sendPacket(new UserInfo(activeChar));

		sendPacket(new HennaInfo(activeChar));

		sendPacket(new FriendList(activeChar));

		sendPacket(new ItemList(activeChar, false));

		sendPacket(new ShortCutInit(activeChar));

		sendPacket(new SkillCoolTime());

		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			if (i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}

		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);

		loadTutorial(activeChar);

		// check for crowns
		CrownManager.getInstance().checkCrowns(activeChar);

		// check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			activeChar.checkAllowedSkills();
		}

		PetitionManager.getInstance().checkPetitionMessages(activeChar);

		// send user info again .. just like the real client
		//sendPacket(ui);

		if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
		}

		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		if (Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getConfirmed(activeChar.getObjectId()) == 1)
		{
			activeChar.setIsHero(true);
		}

		setPledgeClass(activeChar);

		notifyFriends(activeChar);
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);

		activeChar.onPlayerEnter();

		if (Config.PCB_ENABLE)
		{
			activeChar.showPcBangWindow();
		}

		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
		}

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}

		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));

			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}

				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}

			for (FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fortsiege.getIsInProgress())
				{
					continue;
				}

				if (fortsiege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if (fortsiege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}

			// Add message at connexion if clanHall not paid.
			// Possibly this is custom...
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());

			if (clanHall != null)
			{
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
				}
			}
		}

		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && activeChar.isInsideZone(L2Character.ZONE_SIEGE) && !activeChar.isDead())
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}

		RegionBBSManager.getInstance().changeCommunityBoard();
		CustomWorldHandler.getInstance().enterWorld(activeChar);

		TvTEvent.onLogin(activeChar);

		if (Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			ClassLevel lvlnow = PlayerClass.values()[activeChar.getClassId().getId()].getLevel();

			if (activeChar.getLevel() >= 20 && lvlnow == ClassLevel.First)
			{
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
			}
			else if (activeChar.getLevel() >= 40 && lvlnow == ClassLevel.Second)
			{
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
			}
			else if (activeChar.getLevel() >= 76 && lvlnow == ClassLevel.Third)
			{
				L2ClassMasterInstance.ClassMaster.onAction(activeChar);
			}
		}

		if (!Config.ALLOW_DUALBOX)
		{
			String thisip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
			Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
			L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

			for (L2PcInstance player : players)
			{
				if (player != null)
				{
					String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
					if (thisip.equals(ip) && activeChar != player && player != null)
					{
						activeChar.sendMessage("I'm sorry, but multibox is not allowed here.");
						activeChar.logout();
					}
				}
			}
		}

		Hellows(activeChar);
		activeChar.restoreProfileBuffs();

		if (!nProtect.getInstance().checkRestriction(activeChar, RestrictionType.RESTRICT_ENTER))
		{
			activeChar.setIsImobilised(true);
			activeChar.disableAllSkills();
			ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), 20000);
		}

		if (Config.ALLOW_BIND_HWID)
		{
			String _storeHwid = activeChar.loadHwid();
                        String hwid = null;
                        if(Config.SERVER_PROTECTION_TYPE.equals("CATS")) {
                            hwid = activeChar.getClient().getHWId();
                        }
                        if(Config.SERVER_PROTECTION_TYPE.equals("LAME")) {
                            hwid = activeChar.getClient().getHWID();
                        }

			if(_storeHwid != null && hwid != null && hwid.length() > 0)
			{
				if(!_storeHwid.equals("*") && !hwid.equalsIgnoreCase(_storeHwid))
				{
					activeChar.setIsImobilised(true);
                                        activeChar.setWrongHwid(true);
					activeChar.disableAllSkills();
                                        activeChar.sendMessage("This HWID not allowed in this account!\n Please, use valid PC!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), Config.WRONG_HWID_DISSCONNECT_TIME);
				}
			}
		}
	}

	private void EnterGM(L2PcInstance activeChar)
	{
		if (activeChar.isGM())
		{
			if (Config.SHOW_GM_LOGIN)
			{
				String gmname = activeChar.getName();
				String text = "GM " + gmname + " has logged on.";
				Announcements.getInstance().announceToAll(text);
			}

			if (Config.GM_STARTUP_INVULNERABLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}

			if (Config.GM_STARTUP_INVISIBLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.getAppearance().setInvisible();
			}

			if (Config.GM_STARTUP_SILENCE && AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setSilenceMode(true);
			}

			if (Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}

			activeChar.updateGmNameTitleColor();
		}
	}

	private void Hellows(L2PcInstance activeChar)
	{
		SystemMessage sm;

		if (Config.ALT_Server_Name_Enabled)
		{
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Welcome to " + Config.ALT_Server_Name);
			sendPacket(sm);
		}

		if (Config.ONLINE_PLAYERS_ON_LOGIN)
		{
			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("There are " + L2World.getInstance().getAllPlayers().size() + " players online.");
			sendPacket(sm);
		}

		if (Config.ONLINE_CLAN_ON_LOGIN)
		{
			L2Clan clan = activeChar.getClan();
			if (clan != null)
			{
				sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("There are " + clan.getOnlineMembers(activeChar.getObjectId()).length + " clan members online.");
				sendPacket(sm);
			}
		}

		if (Config.UPTIME_ON_LOGIN)
		{
			String time;
			long uptime = (System.currentTimeMillis() - GameServer.dateTimeServerStarted.getTimeInMillis()) / 1000L;

			if (uptime < 60)
			{
				time = uptime + " s.";
			}
			else if (uptime < 3600)
			{
				time = uptime / 60 + " m. " + uptime % 60 + " s.";
			}
			else if (uptime < 86400)
			{
				time = uptime / 60 / 60 + " h. " + uptime / 60 % 60 + " m. " + uptime % 60 + " s.";
			}
			else
			{
				time = uptime / 24 / 60 / 60 + " d. " + uptime / 60 / 60 % 24 + " h. " + uptime / 60 % 60 + " m. " + uptime % 60 + " s.";
			}

			sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Server uptime is: " + time);
			sendPacket(sm);
		}

		if (Config.WELCOME_HTM)
		{
			String welcome = HtmCache.getInstance().getHtm("data/html/welcome.htm");

			if (welcome != null)
			{
				welcome = welcome.replaceAll("%playername%", activeChar.getName());
				sendPacket(new NpcHtmlMessage(1, welcome));
			}
		}

		if (Config.SHOW_CASTLE_LORD_LOGIN)
		{
			L2Clan clan = activeChar.getClan();
			if (clan != null)
			{
				if (CastleManager.getInstance().getCastleByOwner(clan) != null)
				{
					Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (activeChar.isCastleLord(castle.getCastleId()))
					{
						String name = activeChar.getName();
						String text = "Behold!! " + name + ", castle lord of " + castle.getName() + ", has logged on.";
						Announcements.getInstance().announceToAll(text);
					}
					castle = null;
				}
				clan = null;
			}
		}

		if (Config.SHOW_HERO_LOGIN)
		{
			if (activeChar.isHero())
			{
				String name = activeChar.getName();
				String text = "Great hero " + name + " has logged on.";
				Announcements.getInstance().announceToAll(text);
			}
		}
	}

	private void ColorSystem(L2PcInstance activeChar)
	{
		// =================================================================================
		// Color System checks - Start =====================================================
		// Check if the custom PvP and PK color systems are enabled and if so ==============
		// check the character's counters and apply any color changes that must be done. ===
		//thank Kidzor
		/** KidZor: Ammount 1 **/
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT1 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if (activeChar.getPkKills() >= Config.PK_AMOUNT1 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 2 **/
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT2 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if (activeChar.getPkKills() >= Config.PK_AMOUNT2 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 3 **/
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT3 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if (activeChar.getPkKills() >= Config.PK_AMOUNT3 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 4 **/
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT4 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if (activeChar.getPkKills() >= Config.PK_AMOUNT4 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
		}

		/** KidZor: Ammount 5 **/
		if (activeChar.getPvpKills() >= Config.PVP_AMOUNT5 && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePvPColor(activeChar.getPvpKills());
		}

		if (activeChar.getPkKills() >= Config.PK_AMOUNT5 && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			activeChar.updatePkColor(activeChar.getPkKills());
			// Color System checks - End =======================================================
			// =================================================================================
		}

		// Apply color settings to clan leader when entering  
		if (activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == 1)
			{
				activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR, false);
			}
			else
			{
				activeChar.getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR, false);
			}
		}

		activeChar.updateNameTitleColor();
	}

	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		for (Wedding cl : CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
					cha.setmarriedType(cl.getType());
				}

				cha.setCoupleId(cl.getId());

				if (cl.getPlayer1Id() == _chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}

	/**
	 * @param activeChar partnerid
	 */
	private void notifyPartner(int partnerId)
	{
		if(partnerId == 0)
		{
			return;
		}

		L2PcInstance partner = (L2PcInstance) L2World.getInstance().findObject(partnerId);
		if (partner != null)
		{
			partner.sendMessage("Your Partner has logged in");
		}
	}

	private void notifyFriends(L2PcInstance cha)
	{
		cha.notifyFriends();

		SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(cha.getName());

		for (int id : cha.getFriendList())
		{
			L2PcInstance obj = L2World.getInstance().getPlayer(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;

			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");

		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}

	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;

		if (activeChar.getClan() != null)
		{
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
		}

		if (activeChar.isNoble() && pledgeClass < 5)
		{
			pledgeClass = 5;
		}

		if (activeChar.isHero())
		{
			pledgeClass = 8;
		}

		activeChar.setPledgeClass(pledgeClass);
	}
}
