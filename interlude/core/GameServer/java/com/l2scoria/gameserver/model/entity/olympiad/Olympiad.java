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

package com.l2scoria.gameserver.model.entity.olympiad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import com.l2scoria.Config;
import com.l2scoria.crypt.nProtect;
import com.l2scoria.crypt.nProtect.RestrictionType;
import com.l2scoria.gameserver.managers.OlympiadStadiaManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.entity.Hero;
import com.l2scoria.gameserver.model.entity.event.TvTEvent;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.StatsSet;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.L2FastList;
import com.l2scoria.util.database.L2DatabaseFactory;

public class Olympiad
{
	protected static final Logger _log = Logger.getLogger(Olympiad.class.getName());

	private static Olympiad _instance;

	protected static Map<Integer, StatsSet> _nobles;
	protected static L2FastList<StatsSet> _heroesToBe;
	protected static L2FastList<L2PcInstance> _nonClassBasedRegisters;
	protected static Map<Integer, L2FastList<L2PcInstance>> _classBasedRegisters;

	private static final String OLYMPIAD_DATA_FILE = "config/olympiad.info";
	public static final String OLYMPIAD_HTML_FILE = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT * from olympiad_nobles";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles values (?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles set olympiad_points = ?, competitions_done = ?, competitions_won = ? where char_id = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT char_id, char_name from olympiad_nobles where class_id = ? and competitions_done >= 5 and competitions_won >= 1 order by olympiad_points desc, competitions_done desc, competitions_won desc";
	private static final String GET_EACH_CLASS_LEADER = "SELECT char_name from olympiad_nobles where class_id = ? order by olympiad_points desc, competitions_done desc, competitions_won desc";
	private static final String OLYMPIAD_DELETE_ALL = "DELETE from olympiad_nobles";
	private static final int[] HERO_IDS =
	{
			88, 89, 90, 91, 92, 93, 94, 95, 96, 97,
			98, 99,	100, 101, 102, 103, 104, 105, 106, 107,
			108, 109, 110, 111, 112, 113, 114, 115, 116, 117,
			118, 131, 132, 133, 134
	};

	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 6 mins
	protected static final long BATTLE_WAIT = Config.ALT_OLY_BWAIT; // 10mins
	protected static final long INITIAL_WAIT = Config.ALT_OLY_IWAIT; // 5mins
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours

	private static final int DEFAULT_POINTS = 18;
	protected static final int WEEKLY_POINTS = 3;

	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";

	protected long _olympiadEnd;
	protected long _validationEnd;
	protected int _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected static boolean _inCompPeriod;
	protected static boolean _isOlympiadEnd;
	protected static boolean _compStarted = false;
	protected static boolean _battleStarted;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	protected ScheduledFuture<?> _gameManager = null;

	public static final OlympiadStadia[] STADIUMS =
	{
			new OlympiadStadia(-20814, -21189, -3000),
			new OlympiadStadia(-120324, -225077, -3331),
			new OlympiadStadia(-102495, -209023, -3331),
			new OlympiadStadia(-120156, -207378, -3331),
			new OlympiadStadia(-87628, -225021, -3331),
			new OlympiadStadia(-81705, -213209, -3331),
			new OlympiadStadia(-87593, -207339, -3331),
			new OlympiadStadia(-93709, -218304, -3331),
			new OlympiadStadia(-77157, -218608, -3331),
			new OlympiadStadia(-69682, -209027, -3331),
			new OlympiadStadia(-76887, -201256, -3331),
			new OlympiadStadia(-109985, -218701, -3331),
			new OlympiadStadia(-126367, -218228, -3331),
			new OlympiadStadia(-109629, -201292, -3331),
			new OlympiadStadia(-87523, -240169, -3331),
			new OlympiadStadia(-81748, -245950, -3331),
			new OlympiadStadia(-77123, -251473, -3331),
			new OlympiadStadia(-69778, -241801, -3331),
			new OlympiadStadia(-76754, -234014, -3331),
			new OlympiadStadia(-93742, -251032, -3331),
			new OlympiadStadia(-87466, -257752, -3331),
			new OlympiadStadia(-114413, -213241, -3331)
	};

	public static int getStadiaSize()
	{
		return STADIUMS.length;
	}

	public OlympiadStadia getFreeStadium()
	{
		for(OlympiadStadia stad: STADIUMS)
		{
			if(stad.isFreeToUse())
			{
				return stad;
			}
		}
		return null;
	}

	public static Olympiad getInstance()
	{
		if(_instance == null)
		{
			_instance = new Olympiad();
		}
		return _instance;
	}

	public Olympiad()
	{}

	public int getPeriod()
	{
		return _period;
	}

	public void load() throws IOException, SQLException
	{
		_nobles = new FastMap<Integer, StatsSet>();

		Properties OlympiadProperties = new Properties();
		InputStream is = new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));
		OlympiadProperties.load(is);
		is.close();

		_currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
		_period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
		_olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
		_validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValdationEnd", "0"));
		_nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));

		switch(_period)
		{
			case 0:
				if(_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
				{
					setNewOlympiadEnd();
				}
				else
				{
					_isOlympiadEnd = false;
				}
				break;
			case 1:
				if(_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					_isOlympiadEnd = true;

					if (_scheduledValdationTask != null)
					{
						_scheduledValdationTask.cancel(true);
					}
					_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new valdationTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			default:
				_log.warning("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}

		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet statDat = new StatsSet();
				int charId = rset.getInt(CHAR_ID);
				statDat.set(CLASS_ID, rset.getInt(CLASS_ID));
				statDat.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statDat.set(POINTS, rset.getInt(POINTS));
				statDat.set(COMP_DONE, rset.getInt(COMP_DONE));
				statDat.set(COMP_WON, rset.getInt(COMP_WON));
				statDat.set("to_save", false);
				_nobles.put(charId, statDat);
			}
			rset.close();
			statement.close();
			rset = null;
			statement = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		synchronized (this)
		{
			_log.info("Olympiad System: Loading Olympiad System....");
			if(_period == 0)
			{
				_log.info("Olympiad System: Currently in Olympiad Period");
			}
			else
			{
				_log.info("Olympiad System: Currently in Validation Period");
			}

			_log.info("Olympiad System: Period Ends....");

			long milliToEnd;
			if(_period == 0)
			{
				milliToEnd = getMillisToOlympiadEnd();
			}
			else
			{
				milliToEnd = getMillisToValidationEnd();
			}

			double numSecs = milliToEnd / 1000 % 60;
			double countDown = (milliToEnd / 1000 - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);

			_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

			if(_period == 0)
			{
				_log.info("Olympiad System: Next Weekly Change is in....");

				milliToEnd = getMillisToWeekChange();

				double numSecs2 = milliToEnd / 1000 % 60;
				double countDown2 = (milliToEnd / 1000 - numSecs2) / 60;
				int numMins2 = (int) Math.floor(countDown2 % 60);
				countDown2 = (countDown2 - numMins2) / 60;
				int numHours2 = (int) Math.floor(countDown2 % 24);
				int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24);

				_log.info("Olympiad System: " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
			}
		}
		_log.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");

		if(_period == 0)
		{
			init();
		}
	}

	protected void init()
	{
		if(_period == 1)
			return;

		_nonClassBasedRegisters = new L2FastList<L2PcInstance>();
		_classBasedRegisters = new FastMap<Integer, L2FastList<L2PcInstance>>();

		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;

		if (_compStart.getTimeInMillis() < _olympiadEnd && _compEnd > _olympiadEnd)
		{
			_compEnd = _olympiadEnd - 300000L;
		}

		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new olympiadEnd(), getMillisToOlympiadEnd());

		updateCompStatus();
		if (_scheduledWeeklyTask != null)
		{
			_scheduledWeeklyTask.cancel(true);
		}
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new weeklyTask(), getMillisToWeekChange(), WEEKLY_PERIOD);
	}
	
	class weeklyTask implements Runnable
	{
		public void run()
		{
			addWeeklyPoints();
			_log.info("Olympiad System: Added weekly points to nobles");

			Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}
	}

	class valdationTask implements Runnable
	{
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
			_period = 0;
			_currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}

	class olympiadEnd implements Runnable
	{
		public void run()
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
			sm.addNumber(_currentCycle);

			Announcements.getInstance().announceToAll(sm);
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

			_isOlympiadEnd = true;
			if(_scheduledWeeklyTask != null)
			{
				_scheduledWeeklyTask.cancel(true);
			}

			Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;

			saveNobleData();

			_period = 1;

			sortHerosToBe();

			giveHeroBonus();

			Hero.getInstance().computeNewHeroes(_heroesToBe);

			try
			{
				save();
			}
			catch(Exception e)
			{
				_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
			}

			if (_scheduledValdationTask != null)
			{
				_scheduledValdationTask.cancel(true);
			}
			_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new valdationTask(), getMillisToValidationEnd());
		}
	}

	public boolean registerNoble(L2PcInstance noble, boolean classBased)
	{
		SystemMessage sm;

		/*
		if (_compStarted)
		{
		    noble.sendMessage("Cant Register whilst competition is under way");
		    return false;
		}
		*/
		if(TvTEvent.isParticipating())
		{
			if(TvTEvent.getParticipantTeamId(noble.getObjectId()) != -1)
			{
				noble.sendMessage("You can't participate to Olympiad with TvT Event mode.");
				return false;
			}
		}

		if(noble.atEvent)
		{
			noble.sendMessage("You can't participate to Olympiad while in an event.");
			return false;
		}

		if(noble.getKarma() > 0)
		{
			noble.sendMessage("You can't participate to Olympiad with karma.");
			return false;
		}

		if(!_inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}

		if(getMillisToCompEnd() < 600000)
		{
			sm = new SystemMessage(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
			noble.sendPacket(sm);
			return false;
		}

		if(noble.isCursedWeaponEquiped())
		{
			noble.sendMessage("You can't participate to Olympiad while holding a cursed weapon.");
			return false;
		}

		if(!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}


		if(noble.getBaseClass() != noble.getClassId().getId())
		{
			sm = new SystemMessage(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
			noble.sendPacket(sm);
			return false;
		}

		if(!nProtect.getInstance().checkRestriction(noble, RestrictionType.RESTRICT_OLYMPIAD))
			return false;

		if(!_nobles.containsKey(noble.getObjectId()))
		{
			StatsSet statDat = new StatsSet();
			statDat.set(CLASS_ID, noble.getClassId().getId());
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WON, 0);
			statDat.set("to_save", true);

			_nobles.put(noble.getObjectId(), statDat);
		}

		if(_classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			for(L2PcInstance partecipant : classed)
			{
				if(partecipant!=null) {
					if(partecipant.getObjectId() == noble.getObjectId())
					{
						sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS);
						noble.sendPacket(sm);
						return false;
					}
				} else 
					classed.remove(partecipant);
			}
		}

		for(L2PcInstance partecipant : _nonClassBasedRegisters)
		{
			if(partecipant!=null) {
				if(partecipant.getObjectId() == noble.getObjectId())
				{
					sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
					noble.sendPacket(sm);
					return false;
				}
			} else 
				_nonClassBasedRegisters.remove(partecipant);
		}

		for(L2OlympiadGame g : OlympiadManager.getInstance().getOlympiadGames().values())
		{
			if (g == null)
				continue;
			if (g.getPlayers() == null)
				continue;

			for(L2PcInstance player : g.getPlayers())
			{
				if(player.getObjectId() == noble.getObjectId())
				{
					sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
					noble.sendPacket(sm);
					return false;
				}
			}
		}

		if(getNoblePoints(noble.getObjectId()) <= 0)
		{
			NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile("data/html/olympiad/noble_nopoints1.htm");
			noble.sendPacket(message);
			return false;
		}

		if(classBased)
		{
			if(_classBasedRegisters.containsKey(noble.getClassId().getId()))
			{
				L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
				classed.add(noble);

				_classBasedRegisters.remove(noble.getClassId().getId());
				_classBasedRegisters.put(noble.getClassId().getId(), classed);

				sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				noble.sendPacket(sm);
			}
			else
			{
				L2FastList<L2PcInstance> classed = new L2FastList<L2PcInstance>();
				classed.add(noble);

				_classBasedRegisters.put(noble.getClassId().getId(), classed);

				sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				noble.sendPacket(sm);
			}
		}
		else
		{
			_nonClassBasedRegisters.add(noble);
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
			noble.sendPacket(sm);
		}

		return true;
	}

	public boolean isRegistered(L2PcInstance noble)
	{
		if(_nonClassBasedRegisters == null)
			return false;
		if(_classBasedRegisters == null)
			return false;
		if(!_nonClassBasedRegisters.contains(noble))
		{
			if(!_classBasedRegisters.containsKey(noble.getClassId().getId()))
				return false;
			else
			{
				L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
				if(classed == null || !classed.contains(noble))
					return false;
			}
		}
		return true;
	}

	public boolean unRegisterNoble(L2PcInstance noble)
	{
		SystemMessage sm;
		/*
		if (_compStarted)
		{
			noble.sendMessage("Cant Unregister whilst competition is under way");
			return false;
		}
		*/

		if(!_inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}

		if(!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}

		if(!isRegistered(noble))
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			noble.sendPacket(sm);
			return false;
		}

		if(_nonClassBasedRegisters.contains(noble))
		{
			_nonClassBasedRegisters.remove(noble);
		}
		else
		{
			L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			classed.remove(noble);

			_classBasedRegisters.remove(noble.getClassId().getId());
			_classBasedRegisters.put(noble.getClassId().getId(), classed);
		}

		for(L2OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
		{
			if (game != null)
			{
				if(game._playerOne.getObjectId() == noble.getObjectId() || game._playerTwo.getObjectId() == noble.getObjectId())
				{
					noble.sendMessage("Cant Unregister whilst you are already selected for a game");
					return false;
				}
			}
		}

		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
		noble.sendPacket(sm);

		return true;
	}

	public void removeDisconnectedCompetitor(L2PcInstance player)
	{
		if (OlympiadManager.getInstance().getOlympiadInstance(player.getOlympiadGameId()) != null)
		{
			OlympiadManager.getInstance().getOlympiadInstance(player.getOlympiadGameId()).handleDisconnect(player);
			return;
		}

		if (_nonClassBasedRegisters.contains(player))
		{
			_nonClassBasedRegisters.remove(player);
			return;
		}

		L2FastList<L2PcInstance> classed = _classBasedRegisters.get(player.getClassId().getId());
		if (classed != null && classed.contains(player))
		{
			classed.remove(player);

			_classBasedRegisters.remove(player.getClassId().getId());
			_classBasedRegisters.put(player.getClassId().getId(), classed);
		}
	}

	private void updateCompStatus()
	{
		//_compStarted = false;

		synchronized (this)
		{
			long milliToStart = getMillisToCompBegin();

			double numSecs = milliToStart / 1000 % 60;
			double countDown = (milliToStart / 1000 - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);

			_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

			_log.info("Olympiad System: Event starts/started : " + _compStart.getTime());
		}

		if (_scheduledCompStart != null)
		{
			_scheduledCompStart.cancel(true);
		}
		_scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(new compStart(), getMillisToCompBegin());
	}
	
	class compStart implements Runnable
	{
		public void run()
		{
			if(isOlympiadEnd())
				return;

			_inCompPeriod = true;
			_gameManager = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(OlympiadManager.getInstance(), 30000, 30000);

			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
			_log.info("Olympiad System: Olympiad Game Started");

			if (_scheduledCompEnd != null)
			{
				_scheduledCompEnd.cancel(true);
			}
			_scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(new compEnd(), getMillisToCompEnd());
		}
	}
	
	class compEnd implements Runnable
	{
		public void run()
		{
			if(isOlympiadEnd())
				return;

			_inCompPeriod = false;
			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
			_log.info("Olympiad System: Olympiad Game Ended");

			try
			{
				while(_battleStarted)
				{
					try
					{
						//wait 1 minutes for end of pendings games
						Thread.sleep(60000);
					}
					catch(InterruptedException e)
					{
						//null
					}
				}
				if (_gameManager != null)
				{
					_gameManager.cancel(false);
					_gameManager = null;
				}
				save();
			}
			catch(Exception e)
			{
				_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
			}

			init();
		}
	}

	private long getMillisToOlympiadEnd()
	{
		//if (_olympiadEnd > Calendar.getInstance().getTimeInMillis())
		return _olympiadEnd - Calendar.getInstance().getTimeInMillis();
		//return 10L;
	}

	public void manualSelectHeroes()
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
		sm.addNumber(_currentCycle);

		Announcements.getInstance().announceToAll(sm);
		Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

		_isOlympiadEnd = true;
		if(_scheduledWeeklyTask != null)
		{
			_scheduledWeeklyTask.cancel(true);
		}
		if(_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}

		Calendar validationEnd = Calendar.getInstance();
		_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;

		saveNobleData();

		_period = 1;

		sortHerosToBe();

		giveHeroBonus();

		Hero.getInstance().computeNewHeroes(_heroesToBe);

		try
		{
			save();
		}
		catch(Exception e)
		{
			_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
		}

		if (_scheduledValdationTask != null)
		{
			_scheduledValdationTask.cancel(true);
		}
		_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new valdationTask(), getMillisToValidationEnd());
	}

	protected long getMillisToValidationEnd()
	{
		if(_validationEnd > Calendar.getInstance().getTimeInMillis())
			return _validationEnd - Calendar.getInstance().getTimeInMillis();
		return 10L;
	}

	public boolean isOlympiadEnd()
	{
		return _isOlympiadEnd;
	}

	protected void setNewOlympiadEnd()
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addNumber(_currentCycle);

		Announcements.getInstance().announceToAll(sm);

		Calendar currentTime = Calendar.getInstance();

		if(Config.ALT_OLY_WEEK)
		{
			currentTime.set(Calendar.DAY_OF_WEEK, Config.ALT_OLY_DAY);
			currentTime.set(Calendar.AM_PM, Calendar.AM);
			currentTime.set(Calendar.HOUR, 0);
			currentTime.set(Calendar.MINUTE, 0);
			currentTime.set(Calendar.SECOND, 0);
			while (currentTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				currentTime.add(Calendar.DATE, 7);
		}
		else
		{
			currentTime.add(Calendar.MONTH, 1);
			currentTime.set(Calendar.DAY_OF_MONTH, 1);
			currentTime.set(Calendar.AM_PM, Calendar.AM);
			currentTime.set(Calendar.HOUR, 0);
			currentTime.set(Calendar.MINUTE, 0);
			currentTime.set(Calendar.SECOND, 0);
		}

		_olympiadEnd = currentTime.getTimeInMillis();

		Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;

		_isOlympiadEnd = false;
	}

	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
			return 120000L;

		if(_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

		return setNewCompBegin();
	}

	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;

		if (_compStart.getTimeInMillis() < _olympiadEnd && _compEnd > _olympiadEnd)
		{
			_compEnd = _olympiadEnd - 300000L;
		}

		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	protected long getMillisToCompEnd()
	{
		//if (_compEnd > Calendar.getInstance().getTimeInMillis())
		return _compEnd - Calendar.getInstance().getTimeInMillis();
		//return 10L;
	}

	private long getMillisToWeekChange()
	{
		if(_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
			return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
		return 10L;
	}

	protected synchronized void addWeeklyPoints()
	{
		if(_period == 1)
			return;

		for(Integer nobleId : _nobles.keySet())
		{
			StatsSet nobleInfo = _nobles.get(nobleId);
			int currentPoints = nobleInfo.getInteger(POINTS);
			currentPoints += WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);

			_nobles.remove(nobleId);
			_nobles.put(nobleId, nobleInfo);
		}
	}

	public FastMap<Integer, String> getMatchList()
	{
		return OlympiadManager.getInstance().getAllTitles();
	}

	public L2PcInstance[] getPlayers(int Id)
	{
		if(OlympiadManager.getInstance().getOlympiadInstance(Id) == null)
			return null;
		L2PcInstance[] players = OlympiadManager.getInstance().getOlympiadInstance(Id).getPlayers();
		return players;
	}

	public int getCurrentCycle()
	{
		return _currentCycle;
	}

	public void addSpectator(int id, L2PcInstance spectator)
	{
		for(L2PcInstance player : _nonClassBasedRegisters)
		{
			if(spectator.getObjectId() == player.getObjectId())
			{
				spectator.sendMessage("You are already registered for a competition");
				return;
			}
		}
		for(L2FastList<L2PcInstance> list : _classBasedRegisters.values())
		{
			for(L2PcInstance player : list)
			{
				if(spectator.getObjectId() == player.getObjectId())
				{
					spectator.sendMessage("You are already registered for a competition");
					return;
				}
			}
		}
		if(spectator.getOlympiadGameId() != -1)
		{
			spectator.sendMessage("You are already registered for a competition");
			return;
		}
		if(TvTEvent.isParticipating())
		{
			if(TvTEvent.getParticipantTeamId(spectator.getObjectId()) != -1)
			{
				spectator.sendMessage("You can't spectate olympiad fight with TvT Event mode.");
				return;
			}
		}
		if(!_inCompPeriod)
		{
			spectator.sendPacket(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
			return;
		}

		spectator.enterOlympiadObserverMode(STADIUMS[id].getCoordinates()[0], STADIUMS[id].getCoordinates()[1], STADIUMS[id].getCoordinates()[2], id);

		STADIUMS[id].addSpectator(spectator);
	}

	public void changeSpectator(int id, L2PcInstance spectator)
	{
		for(L2PcInstance player : _nonClassBasedRegisters)
		{
			if(spectator.getObjectId() == player.getObjectId())
			{
				spectator.sendMessage("You are already registered for a competition");
				return;
			}
		}
		for(L2FastList<L2PcInstance> list : _classBasedRegisters.values())
		{
			for(L2PcInstance player : list)
			{
				if(spectator.getObjectId() == player.getObjectId())
				{
					spectator.sendMessage("You are already registered for a competition");
					return;
				}
			}
		}
		if(spectator.getOlympiadGameId() != -1)
		{
			spectator.sendMessage("You are already registered for a competition");
			return;
		}
		if(!_inCompPeriod)
		{
			spectator.sendPacket(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
			return;
		}

		spectator.changeOlympiadObserverMode(STADIUMS[id].getCoordinates()[0], STADIUMS[id].getCoordinates()[1], STADIUMS[id].getCoordinates()[2], id);

		STADIUMS[id].addSpectator(spectator);
	}

	public int getSpectatorArena(L2PcInstance player)
	{
		for (int i = 0; i < STADIUMS.length; i++)
		{
			if(getSpectators(i) != null && getSpectators(i).contains(player))
				return i;
		}
		return -1;
	}

	public void removeSpectator(int id, L2PcInstance spectator)
	{
		STADIUMS[id].removeSpectator(spectator);
	}

	public L2FastList<L2PcInstance> getSpectators(int id)
	{
		return STADIUMS[id].getSpectators();
	}

	public Map<Integer, L2OlympiadGame> getOlympiadGames()
	{
		return OlympiadManager.getInstance().getOlympiadGames();
	}

	public boolean playerInStadia(L2PcInstance player)
	{
		return OlympiadStadiaManager.getInstance().getStadium(player) != null;
	}

	public int[] getWaitingList()
	{
		int[] array = new int[2];

		if(!inCompPeriod())
			return null;

		int classCount = 0;

		if(_classBasedRegisters.size() != 0)
		{
			for(L2FastList<L2PcInstance> classed : _classBasedRegisters.values())
			{
				classCount += classed.size();
			}
		}

		array[0] = classCount;
		array[1] = _nonClassBasedRegisters.size();

		return array;
	}

	protected synchronized void saveNobleData()
	{
		Connection con = null;

		if(_nobles == null)
			return;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			for(Integer nobleId : _nobles.keySet())
			{
				try
				{
					StatsSet nobleInfo = _nobles.get(nobleId);

					int charId = nobleId;
					int classId = nobleInfo.getInteger(CLASS_ID);
					String charName = nobleInfo.getString(CHAR_NAME);
					int points = nobleInfo.getInteger(POINTS);
					int compDone = nobleInfo.getInteger(COMP_DONE);
					int compWon = nobleInfo.getInteger(COMP_WON);
					boolean toSave = nobleInfo.getBool("to_save");

					if(toSave)
					{
						statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES);
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						statement.setString(3, charName);
						statement.setInt(4, points);
						statement.setInt(5, compDone);
						statement.setInt(6, compWon);
						statement.execute();

						statement.close();

						nobleInfo.set("to_save", false);

						_nobles.remove(nobleId);
						_nobles.put(nobleId, nobleInfo);
					}
					else
					{
						statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES);
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, compWon);
						statement.setInt(4, charId);
						statement.execute();
						statement.close();
					}
				}
				catch(Exception ex)
				{
					_log.warning("Olympiad System: Couldnt save nobles info in db for noble id: "+nobleId);
				}
			}
		}
		catch(SQLException e)
		{
			_log.warning("Olympiad System: Couldnt save nobles info in db");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	protected void sortHerosToBe()
	{
		if(_period != 1)
			return;

		_heroesToBe = new L2FastList<StatsSet>();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			ResultSet rset;
			StatsSet hero;

			for(int element : HERO_IDS)
			{
				statement = con.prepareStatement(OLYMPIAD_GET_HEROS);
				statement.setInt(1, element);
				rset = statement.executeQuery();

				if(rset.next())
				{
					hero = new StatsSet();
					hero.set(CLASS_ID, element);
					hero.set(CHAR_ID, rset.getInt(CHAR_ID));
					hero.set(CHAR_NAME, rset.getString(CHAR_NAME));

					_heroesToBe.add(hero);
				}

				statement.close();
				rset.close();
			}
		}
		catch(SQLException e)
		{
			_log.warning("Olympiad System: Couldnt heros from db");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

	}

	public L2FastList<String> getClassLeaderBoard(int classId)
	{
		//if (_period != 1) return;

		L2FastList<String> names = new L2FastList<String>();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			ResultSet rset;
			statement = con.prepareStatement(GET_EACH_CLASS_LEADER);
			statement.setInt(1, classId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				names.add(rset.getString(CHAR_NAME));
			}

			statement.close();
			rset.close();

			return names;
		}
		catch(SQLException e)
		{
			_log.warning("Olympiad System: Couldnt heros from db");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		return names;

	}

	protected void giveHeroBonus()
	{
		if(_heroesToBe.size() == 0)
			return;

		for(StatsSet hero : _heroesToBe)
		{
			int charId = hero.getInteger(CHAR_ID);

			StatsSet noble = _nobles.get(charId);
			int currentPoints = noble.getInteger(POINTS);
			currentPoints += Config.ALT_OLY_HERO_POINTS;
			noble.set(POINTS, currentPoints);

			_nobles.remove(charId);
			_nobles.put(charId, noble);
		}
	}

	public int getNoblessePasses(int objId)
	{
		if(_period != 1 || _nobles.size() == 0)
			return 0;

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		int points = noble.getInteger(POINTS);
		if(points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH)
			return 0;

		noble.set(POINTS, 0);
		_nobles.remove(objId);
		_nobles.put(objId, noble);

		points *= Config.ALT_OLY_GP_PER_POINT;

		return points;
	}

	public boolean isRegisteredInComp(L2PcInstance player)
	{
		if(player == null)
			return false;

		if(_nonClassBasedRegisters != null && _nonClassBasedRegisters.contains(player))
		{
			return true;
		}
		else if(_classBasedRegisters != null && _classBasedRegisters.containsKey(player.getClassId().getId()))
		{
			L2FastList<L2PcInstance> classed = _classBasedRegisters.get(player.getClassId().getId());
			if(classed.contains(player))
			{
				return true;
			}
		}
		if(_inCompPeriod)
		{
			if (OlympiadManager.getInstance().getOlympiadGames() != null)
			{
				for(L2OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
				{
					if(game == null)
						continue;
					if(game._playerOne != null && game._playerOne.getObjectId() == player.getObjectId())
						return true;
					if(game._playerTwo != null && game._playerTwo.getObjectId() == player.getObjectId())
						return true;
				}
			}
		}

		return false;
	}

	public int getNoblePoints(int objId)
	{
		if(_nobles.size() == 0)
			return 0;

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		int points = noble.getInteger(POINTS);

		return points;
	}

	public int getCompetitionDone(int objId)
	{
		if(_nobles.size() == 0)
			return 0;

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		int points = noble.getInteger(COMP_DONE);

		return points;
	}

	public int getCompetitionWon(int objId)
	{
		if (_nobles.isEmpty())
			return 0;

		StatsSet noble = _nobles.get(objId);
		if (noble == null)
			return 0;
		int points = noble.getInteger(COMP_WON);

		return points;
	}

	protected void deleteNobles()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_DELETE_ALL);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.warning("Olympiad System: Couldnt delete nobles from db");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}

		_nobles.clear();
	}

	public void save() throws IOException
	{
		saveNobleData();

		Properties OlympiadProperties = new Properties();
		FileOutputStream fos = new FileOutputStream(new File(Config.DATAPACK_ROOT, OLYMPIAD_DATA_FILE));

		OlympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
		OlympiadProperties.setProperty("Period", String.valueOf(_period));
		OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
		OlympiadProperties.setProperty("ValdationEnd", String.valueOf(_validationEnd));
		OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));

		OlympiadProperties.store(fos, "Olympiad Properties");
		fos.close();
	}

	public void logoutPlayer(L2PcInstance player)
	{
		_classBasedRegisters.remove(Integer.valueOf(player.getClassId().getId()));
		_nonClassBasedRegisters.remove(player);
	}

	public void sendMatchList(L2PcInstance player)
	{
		NpcHtmlMessage message = new NpcHtmlMessage(0);
		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<center><br>Grand Olympiad Games Overview<br><table width=270 border=0 bgcolor=\"000000\">");
		replyMSG.append("<tr><td fixwidth=30>Number</td><td fixwidth=60>Status</td><td>Player 1 / Player 2</td></tr>");
		FastMap<Integer, String> matches = getMatchList();

		for (int i = 0; i < getStadiaSize(); i++)
		{
			if (matches != null && matches.containsKey(i))
			{
				String[] elem = matches.get(i).split(" ");
				replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h OlympiadArenaChange " + i + "\">" + i + "</a></td><td fixwidth=60>" + elem[0] + "</td><td>" + elem[1] + "&nbsp;" + elem[2] + "</td></tr>");
			}
			else
			{
				replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h OlympiadArenaChange " + i + "\">" + i + "</a></td><td fixwidth=60>&$906;</td><td>&nbsp;</td></tr>");
			}
		}

		replyMSG.append("</table></center></body></html>");
		message.setHtml(replyMSG.toString());
		player.sendPacket(message);
	}

	public void bypassChangeArena(String command, L2PcInstance player)
	{
		if (player == null || !player.inObserverMode())
			return;

		String[] commands = command.split(" ");
		int id = Integer.parseInt(commands[1]);
		int arena = getSpectatorArena(player);
		if (arena >= 0)
			removeSpectator(arena, player);
		else
			return;
		player.setOlympiadGameId(-1);
		changeSpectator(id, player);
	}

	public Map<Integer, StatsSet> getNobles()
	{
		return _nobles;
	}

	public Map<Integer, L2FastList<L2PcInstance>> getClassBased()
	{
		return _classBasedRegisters;
	}

	public void clearClassBased()
	{
		_classBasedRegisters.clear();
	}

	public L2FastList<L2PcInstance> getNonClassBased()
	{
		return _nonClassBasedRegisters;
	}

	public void clearNonClassBased()
	{
		_nonClassBasedRegisters.clear();
	}

	public static void setBattleStarted(boolean val)
	{
		_battleStarted = val;
	}
}
