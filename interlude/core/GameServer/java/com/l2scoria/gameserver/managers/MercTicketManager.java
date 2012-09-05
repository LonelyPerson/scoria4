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

import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.model.AutoChatHandler;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author yellowperil & Fulminus This class is similar to the SiegeGuardManager, except it handles the loading of the
 *         mercenary tickets that are dropped on castle floors by the castle lords. These tickets (aka badges) need to
 *         be readded after each server reboot except when the server crashed in the middle of an ongoig siege. In
 *         addition, this class keeps track of the added tickets, in order to properly limit the number of mercenaries
 *         in each castle and the number of mercenaries from each mercenary type. Finally, we provide auxilary functions
 *         to identify the castle in which each item (and its corresponding NPC) belong to, in order to help avoid
 *         mixing them up.
 */
public class MercTicketManager
{
	protected static Logger _log = Logger.getLogger(CastleManager.class.getName());

	// =========================================================
	private static MercTicketManager _instance;

	public static final MercTicketManager getInstance()
	{
		//CastleManager.getInstance();
		if(_instance == null)
		{
			System.out.println("Initializing MercTicketManager");
			_instance = new MercTicketManager();
			_instance.load();
		}
		return _instance;
	}

	// =========================================================

	// =========================================================
	// Data Field
	private List<L2ItemInstance> _droppedTickets; // to keep track of items on the ground

	//TODO move all these values into siege.properties
	// max tickets per merc type = 10 + (castleid * 2)?
	// max ticker per castle = 40 + (castleid * 20)?
	private static final int[] MAX_MERC_PER_TYPE =
	{
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10, // Gludio
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
			1, 1, 1,
			15, 15, 15, 15, 15, 15, 15, 15, 15, 15, // Dion
			15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
			1, 1, 1,
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10, // Giran
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
			1, 1, 1,
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10, // Oren
			10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
			1, 1, 1,
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20, // Aden
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			1, 1, 1, 1, 1,
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20, // Innadril
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			1, 1, 1,
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20, // Goddard
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			1, 1, 1,
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20, // Rune
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			1, 1, 1, 1, 1,
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20, // Schuttgart
			20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			1, 1, 1
	};
	private static final int[] MERCS_MAX_PER_CASTLE =
	{
			100, // Gludio
			150, // Dion
			200, // Giran
			300, // Oren
			400, // Aden
			400, // Innadril
			400, // Goddard
			400, // Rune
			400  // Schuttgart
	};

	private static final int[] ITEM_IDS =
	{
			3960, 3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, // Normal Mercenaries - Gludio
			6038, 6039, 6040, 6041, 6042, 6043, 6044, 6045, 6046, 6047, // Greater/Elite Mercenaries - Gludio
			3970, 3971, 3972,											// Teleporters - Gludio				!23
			3973, 3974, 3975, 3976, 3977, 3978, 3979, 3980, 3981, 3982, // Normal Mercenaries - Dion
			6051, 6052, 6053, 6054, 6055, 6056, 6057, 6058, 6059, 6060, // Greater/Elite Mercenaries - Dion
			3983, 3984, 3985,											// Teleporters - Dion				!46
			3986, 3987, 3988, 3989, 3990, 3991, 3992, 3993, 3994, 3995, // Normal Mercenaries - Giran
			6064, 6065, 6066, 6067, 6068, 6069, 6070, 6071, 6072, 6073, // Greater/Elite Mercenaries - Giran
			3996, 3997, 3998,											// Teleporters - Giran				!69
			3999, 4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, // Normal Mercenaries - Oren
			6077, 6078, 6079, 6080, 6081, 6082, 6083, 6084, 6085, 6086, // Greater/Elite Mercenaries - Oren
			4009, 4010, 4011,											// Teleporters - Oren				!92
			4012, 4013, 4014, 4015, 4016, 4017, 4018, 4019, 4020, 4021, // Normal Mercenaries - Aden
			6090, 6091, 6092, 6093, 6094, 6095, 6096, 6097, 6098, 6099, // Greater/Elite Mercenaries - Aden
			4022, 4023, 4024, 4025, 4026,								// Teleporters - Aden				!117
			5205, 5206, 5207, 5208, 5209, 5210, 5211, 5212, 5213, 5214, // Normal Mercenaries - Innadril
			6105, 6106, 6107, 6108, 6109, 6110, 6111, 6112, 6113, 6114, // Greater/Elite Mercenaries - Innadril
			5215, 5218, 5219,											// Teleporters - Innadril			!140
			6779, 6780, 6781, 6782, 6783, 6784, 6785, 6786, 6787, 6788, // Normal Mercenaries - Goddard
			6792, 6793, 6794, 6795, 6796, 6797, 6798, 6799, 6800, 6801, // Greater/Elite Mercenaries - Goddard
			6789, 6790, 6791,											// Teleporters - Goddard			!163
			7973, 7974, 7975, 7976, 7977, 7978, 7979, 7980, 7981, 7982, // Normal Mercenaries - Rune
			7988, 7989, 7990, 7991, 7992, 7993, 7994, 7995, 7996, 7997, // Greater/Elite Mercenaries - Rune
			7983, 7984, 7985, 7986, 7987,								// Teleporters - Rune				!188
			7918, 7919, 7920, 7921, 7922, 7923, 7924, 7925, 7926, 7927, // Normal Mercenaries - Schuttgart
			7931, 7932, 7933, 7934, 7935, 7936, 7937, 7938, 7939, 7940, // Greater/Elite Mercenaries - Schuttgart
			7928, 7929, 7930											// Teleporters - Schuttgart			!211
	};

	private static final int[] NPC_IDS =
	{
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Gludio
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35092, 35093, 35094,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Dion
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35134, 35135, 35136,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Giran
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35176, 35177, 35178,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Oren
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35218, 35219, 35220,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Aden
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35261, 35262, 35263, 35264, 35265,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Innadril
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35308, 35309, 35310,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Goddard
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35352, 35353, 35354,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Rune
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35497, 35498, 35499, 35500, 35501,
			35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Schuttgart
			35030, 35031, 35032, 35033, 35034, 35035, 35036, 35037, 35038, 35039,
			35544, 35545, 35546
	};

	// =========================================================
	// Constructor
	public MercTicketManager()
	{}

	// =========================================================
	// Method - Public
	// returns the castleId for the passed ticket item id
	public int getTicketCastleId(int itemId)
	{
		if(itemId >= ITEM_IDS[0] && itemId <= ITEM_IDS[22] || itemId >= ITEM_IDS[10] && itemId <= ITEM_IDS[19])
			return 1; // Gludio
		if(itemId >= ITEM_IDS[23] && itemId <= ITEM_IDS[45] || itemId >= ITEM_IDS[33] && itemId <= ITEM_IDS[42])
			return 2; // Dion
		if(itemId >= ITEM_IDS[46] && itemId <= ITEM_IDS[68] || itemId >= ITEM_IDS[56] && itemId <= ITEM_IDS[65])
			return 3; // Giran
		if(itemId >= ITEM_IDS[69] && itemId <= ITEM_IDS[91] || itemId >= ITEM_IDS[79] && itemId <= ITEM_IDS[88])
			return 4; // Oren
		if(itemId >= ITEM_IDS[92] && itemId <= ITEM_IDS[116] || itemId >= ITEM_IDS[102] && itemId <= ITEM_IDS[111])
			return 5; // Aden
		if(itemId >= ITEM_IDS[117] && itemId <= ITEM_IDS[139] || itemId >= ITEM_IDS[127] && itemId <= ITEM_IDS[136])
			return 6; // Innadril
		if(itemId >= ITEM_IDS[140] && itemId <= ITEM_IDS[162] || itemId >= ITEM_IDS[150] && itemId <= ITEM_IDS[159])
			return 7; // Goddard
		if(itemId >= ITEM_IDS[163] && itemId <= ITEM_IDS[187] || itemId >= ITEM_IDS[173] && itemId <= ITEM_IDS[182])
			return 8; // Rune
		if(itemId >= ITEM_IDS[188] && itemId <= ITEM_IDS[210] || itemId >= ITEM_IDS[198] && itemId <= ITEM_IDS[207])
			return 9; // Schuttgart
		return -1;
	}

	public void reload()
	{
		getDroppedTickets().clear();
		load();
	}

	// =========================================================
	// Method - Private
	private final void load()
	{
		Connection con = null;
		// load merc tickets into the world
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where isHired = 1");
			rs = statement.executeQuery();

			int npcId;
			int itemId;
			int x, y, z;
			// start index to begin the search for the itemId corresponding to this NPC
			// this will help with:
			//    a) skip unnecessary iterations in the search loop
			//    b) avoid finding the wrong itemId whenever tickets of different spawn the same npc!
			int startindex = 0;

			while(rs.next())
			{
				npcId = rs.getInt("npcId");
				x = rs.getInt("x");
				y = rs.getInt("y");
				z = rs.getInt("z");
				Castle castle = CastleManager.getInstance().getCastle(x, y, z);
				if(castle != null)
				{
					startindex = 10 * (castle.getCastleId() - 1);
				}

				// find the FIRST ticket itemId with spawns the saved NPC in the saved location
				for(int i = startindex; i < NPC_IDS.length; i++)
					if(NPC_IDS[i] == npcId) // Find the index of the item used
					{
						// only handle tickets if a siege is not ongoing in this npc's castle

						if(castle != null && !castle.getSiege().getIsInProgress())
						{
							itemId = ITEM_IDS[i];
							// create the ticket in the gameworld
							L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
							dropticket.setLocation(L2ItemInstance.ItemLocation.VOID);
							dropticket.dropMe(null, x, y, z);
							dropticket.setDropTime(0); //avoids it from beeing removed by the auto item destroyer
							L2World.storeObject(dropticket);
							getDroppedTickets().add(dropticket);
							dropticket = null;
						}
						break;
					}
				castle = null;
			}
			statement.close();
			statement = null;
			rs.close();
			rs = null;

			System.out.println("Loaded: " + getDroppedTickets().size() + " Mercenary Tickets");
		}
		catch(Exception e)
		{
			System.out.println("Exception: loadMercenaryData(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	// =========================================================
	// Property - Public
	/**
	 * Checks if the passed item has reached the limit of number of dropped tickets that this SPECIFIC item may have in
	 * its castle
	 */
	public boolean isAtTypeLimit(int itemId)
	{
		int limit = -1;
		// find the max value for this item
		for(int i = 0; i < ITEM_IDS.length; i++)
			if(ITEM_IDS[i] == itemId) // Find the index of the item used
			{
				limit = MAX_MERC_PER_TYPE[i];
				break;
			}

		if(limit <= 0)
			return true;

		int count = 0;
		L2ItemInstance ticket;
		for(int i = 0; i < getDroppedTickets().size(); i++)
		{
			ticket = getDroppedTickets().get(i);
			if(ticket != null && ticket.getItemId() == itemId)
			{
				count++;
			}
		}
		ticket = null;
		if(count >= limit)
			return true;

		return false;
	}

	/**
	 * Checks if the passed item belongs to a castle which has reached its limit of number of dropped tickets.
	 */
	public boolean isAtCasleLimit(int itemId)
	{
		int castleId = getTicketCastleId(itemId);
		if(castleId <= 0)
			return true;

		int limit = MERCS_MAX_PER_CASTLE[castleId - 1];
		if(limit <= 0)
			return true;

		int count = 0;
		L2ItemInstance ticket;
		for(int i = 0; i < getDroppedTickets().size(); i++)
		{
			ticket = getDroppedTickets().get(i);
			if(ticket != null && getTicketCastleId(ticket.getItemId()) == castleId)
			{
				count++;
			}
		}
		ticket = null;
		if(count >= limit)
			return true;

		return false;
	}

	public boolean isTooCloseToAnotherTicket(int x, int y, int z)
	{
		for (L2ItemInstance item : getDroppedTickets())
		{
			double dx = x - item.getX();
			double dy = y - item.getY();
			double dz = z - item.getZ();

			if ((dx * dx + dy * dy + dz * dz) < 25 * 25)
				return true;
		}
		return false;
	}

	/**
	 * addTicket actions 1) find the npc that needs to be saved in the mercenary spawns, given this item 2) Use the
	 * passed character's location info to add the spawn 3) create a copy of the item to drop in the world returns the
	 * id of the mercenary npc that was added to the spawn returns -1 if this fails.
	 */
	public int addTicket(int itemId, L2PcInstance activeChar, String[] messages)
	{
		int x = activeChar.getX();
		int y = activeChar.getY();
		int z = activeChar.getZ();
		int heading = activeChar.getHeading();

		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		if(castle == null) //this should never happen at this point
			return -1;

		//check if this item can be added here
		for(int i = 0; i < ITEM_IDS.length; i++)
		{
			if(ITEM_IDS[i] == itemId) // Find the index of the item used
			{
				spawnMercenary(NPC_IDS[i], x, y, z, 3000, messages, 0);

				// Hire merc for this caslte.  NpcId is at the same index as the item used.
				castle.getSiege().getSiegeGuardManager().hireMerc(x, y, z, heading, NPC_IDS[i]);

				// create the ticket in the gameworld
				L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
				dropticket.setLocation(L2ItemInstance.ItemLocation.VOID);
				dropticket.dropMe(null, x, y, z);
				dropticket.setDropTime(0); //avoids it from beeing removed by the auto item destroyer
				L2World.storeObject(dropticket); //add to the world
				// and keep track of this ticket in the list
				_droppedTickets.add(dropticket);

				dropticket = null;

				return NPC_IDS[i];
			}
		}
		castle = null;
		return -1;
	}

	private void spawnMercenary(int npcId, int x, int y, int z, int despawnDelay, String[] messages, int chatDelay)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		if(template != null)
		{
			final L2SiegeGuardInstance npc = new L2SiegeGuardInstance(IdFactory.getInstance().getNextId(), template);
			npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
			npc.setDecayed(false);
			npc.spawnMe(x, y, (z + 20));

			if(messages != null && messages.length > 0)
			{
				AutoChatHandler.getInstance().registerChat(npc, messages, chatDelay);
			}

			if(despawnDelay > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
					public void run()
					{
						npc.deleteMe();
					}
				}, despawnDelay);
			}
		}
		template = null;
	}

	/**
	 * Delete all tickets from a castle; remove the items from the world and remove references to them from this class
	 */
	public void deleteTickets(int castleId)
	{
		int i = 0;
		while(i < getDroppedTickets().size())
		{
			L2ItemInstance item = getDroppedTickets().get(i);
			if(item != null && getTicketCastleId(item.getItemId()) == castleId)
			{
				item.decayMe();
				L2World.getInstance().removeObject(item);

				// remove from the list
				getDroppedTickets().remove(i);
			}
			else
			{
				i++;
			}

			item = null;
		}
	}

	/**
	 * remove a single ticket and its associated spawn from the world (used when the castle lord picks up a ticket, for
	 * example)
	 */
	public void removeTicket(L2ItemInstance item)
	{
		int itemId = item.getItemId();
		int npcId = -1;

		// find the FIRST ticket itemId with spawns the saved NPC in the saved location
		for(int i = 0; i < ITEM_IDS.length; i++)
			if(ITEM_IDS[i] == itemId) // Find the index of the item used
			{
				npcId = NPC_IDS[i];
				break;
			}
		// find the castle where this item is
		Castle castle = CastleManager.getInstance().getCastleById(getTicketCastleId(itemId));

		if(npcId > 0 && castle != null)
		{
			new SiegeGuardManager(castle).removeMerc(npcId, item.getX(), item.getY(), item.getZ());
		}

		castle = null;

		getDroppedTickets().remove(item);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	public final List<L2ItemInstance> getDroppedTickets()
	{
		if(_droppedTickets == null)
		{
			_droppedTickets = new FastList<L2ItemInstance>();
		}
		return _droppedTickets;
	}
}
