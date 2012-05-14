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
package com.l2scoria.gameserver.datatables.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2scoria.gameserver.model.L2PetData;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import java.sql.Connection;
import com.l2scoria.util.database.L2DatabaseFactory;

public class L2PetDataTable
{
	private static Logger _log = Logger.getLogger(L2PetInstance.class.getName());
	private static L2PetDataTable _instance;

	//private static final int[] PET_LIST = { 12077, 12312, 12313, 12311, 12527, 12528, 12526 };
	private static Map<Integer, Map<Integer, L2PetData>> _petTable;

	public static L2PetDataTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new L2PetDataTable();
		}

		return _instance;
	}

	private L2PetDataTable()
	{
		_petTable = new FastMap<Integer, Map<Integer, L2PetData>>();
	}

	public void loadPetsData()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT typeID, level, expMax, hpMax, mpMax, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, feedMax, feedbattle, feednormal, loadMax, hpregen, mpregen, owner_exp_taken FROM pets_stats");
			ResultSet rset = statement.executeQuery();

			int petId, petLevel;

			while(rset.next())
			{
				petId = rset.getInt("typeID");
				petLevel = rset.getInt("level");

				//build the petdata for this level
				L2PetData petData = new L2PetData();

				petData.setPetID(petId);
				petData.setPetLevel(petLevel);
				petData.setPetMaxExp(rset.getInt("expMax"));
				petData.setPetMaxHP(rset.getInt("hpMax"));
				petData.setPetMaxMP(rset.getInt("mpMax"));
				petData.setPetPAtk(rset.getInt("patk"));
				petData.setPetPDef(rset.getInt("pdef"));
				petData.setPetMAtk(rset.getInt("matk"));
				petData.setPetMDef(rset.getInt("mdef"));
				petData.setPetAccuracy(rset.getInt("acc"));
				petData.setPetEvasion(rset.getInt("evasion"));
				petData.setPetCritical(rset.getInt("crit"));
				petData.setPetSpeed(rset.getInt("speed"));
				petData.setPetAtkSpeed(rset.getInt("atk_speed"));
				petData.setPetCastSpeed(rset.getInt("cast_speed"));
				petData.setPetMaxFeed(rset.getInt("feedMax"));
				petData.setPetFeedNormal(rset.getInt("feednormal"));
				petData.setPetFeedBattle(rset.getInt("feedbattle"));
				petData.setPetMaxLoad(rset.getInt("loadMax"));
				petData.setPetRegenHP(rset.getInt("hpregen"));
				petData.setPetRegenMP(rset.getInt("mpregen"));
				petData.setPetRegenMP(rset.getInt("mpregen"));
				petData.setOwnerExpTaken(rset.getFloat("owner_exp_taken"));

				// if its the first data for this petid, we initialize its level FastMap
				if(!_petTable.containsKey(petId))
				{
					_petTable.put(petId, new FastMap<Integer, L2PetData>());
				}

				_petTable.get(petId).put(petLevel, petData);

				petData = null;
			}

			rset.close();
			statement.close();
			rset = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warning("Could not load pets stats: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	public void addPetData(L2PetData petData)
	{
		Map<Integer, L2PetData> h = _petTable.get(petData.getPetID());

		if(h == null)
		{
			Map<Integer, L2PetData> statTable = new FastMap<Integer, L2PetData>();
			statTable.put(petData.getPetLevel(), petData);
			_petTable.put(petData.getPetID(), statTable);
			statTable = null;
			return;
		}

		h.put(petData.getPetLevel(), petData);

		h = null;
	}

	public void addPetData(L2PetData[] petLevelsList)
	{
		for(L2PetData element : petLevelsList)
		{
			addPetData(element);
		}
	}

	public L2PetData getPetData(int petID, int petLevel)
	{
		//System.out.println("Getting id "+petID+" level "+ petLevel);
		return _petTable.get(petID).get(petLevel);
	}

	/**
	 * Pets stuffs
	 */
	public static boolean isWolf(int npcId)
	{
		return npcId == 12077;
	}

	public static boolean isSinEater(int npcId)
	{
		return npcId == 12564;
	}

	public static boolean isHatchling(int npcId)
	{
		return npcId > 12310 && npcId < 12314;
	}

	public static boolean isStrider(int npcId)
	{
		return npcId > 12525 && npcId < 12529;
	}

	public static boolean isWyvern(int npcId)
	{
		return npcId == 12621;
	}

	public static boolean isBaby(int npcId)
	{
		return npcId > 12779 && npcId < 12783;
	}

	public static boolean isPetFood(int itemId)
	{
		return itemId == 2515 || itemId == 4038 || itemId == 5168 || itemId == 6316 || itemId == 7582;
	}

	public static boolean isWolfFood(int itemId)
	{
		return itemId == 2515;
	}

	public static boolean isSinEaterFood(int itemId)
	{
		return itemId == 2515;
	}

	public static boolean isHatchlingFood(int itemId)
	{
		return itemId == 4038;
	}

	public static boolean isStriderFood(int itemId)
	{
		return itemId == 5168;
	}

	public static boolean isWyvernFood(int itemId)
	{
		return itemId == 6316;
	}

	public static boolean isBabyFood(int itemId)
	{
		return itemId == 7582;
	}

	public static int getFoodItemId(int npcId)
	{
		if(isWolf(npcId))
			return 2515;
		else if(isSinEater(npcId))
			return 2515;
		else if(isHatchling(npcId))
			return 4038;
		else if(isStrider(npcId))
			return 5168;
		else if(isBaby(npcId))
			return 7582;
		else
			return 0;
	}

	public static int getPetIdByItemId(int itemId)
	{
		switch(itemId)
		{
			// wolf pet a
			case 2375:
				return 12077;
				// Sin Eater
			case 4425:
				return 12564;
				// hatchling of wind
			case 3500:
				return 12311;
				// hatchling of star
			case 3501:
				return 12312;
				// hatchling of twilight
			case 3502:
				return 12313;
				//  wind strider
			case 4422:
				return 12526;
				//	Star strider
			case 4423:
				return 12527;
				// Twilight strider
			case 4424:
				return 12528;
				// Wyvern
			case 8663:
				return 12621;
				// Baby Buffalo
			case 6648:
				return 12780;
				// Baby Cougar
			case 6649:
				return 12782;
				// Baby Kookaburra
			case 6650:
				return 12781;
				// unknown item id.. should never happen
			default:
				return 0;
		}
	}

	public static int getHatchlingWindId()
	{
		return 12311;
	}

	public static int getHatchlingStarId()
	{
		return 12312;
	}

	public static int getHatchlingTwilightId()
	{
		return 12313;
	}

	public static int getStriderWindId()
	{
		return 12526;
	}

	public static int getStriderStarId()
	{
		return 12527;
	}

	public static int getStriderTwilightId()
	{
		return 12528;
	}

	public static int getWyvernItemId()
	{
		return 8663;
	}

	public static int getStriderWindItemId()
	{
		return 4422;
	}

	public static int getStriderStarItemId()
	{
		return 4423;
	}

	public static int getStriderTwilightItemId()
	{
		return 4424;
	}

	public static int getSinEaterItemId()
	{
		return 4425;
	}

	public static boolean isPetItem(int itemId)
	{
		switch(itemId)
		{
			case 2375: // wolf
			case 4425: //Sin Eater
			case 3500: // hatchlings
			case 3501:
			case 3502:
			case 4422: // striders
			case 4423:
			case 4424:
			case 8663: // Wyvern
			case 6648:
			case 6649:
			case 6650:
				return true;
			default:
				return false;
		}
	}

	public static int[] getPetItemsAsNpc(int npcId)
	{
		switch(npcId)
		{
			case 12077:// wolf pet a
				return new int[]
				{
					2375
				};
			case 12564://Sin Eater
				return new int[]
				{
					4425
				};

			case 12311:// hatchling of wind
			case 12312:// hatchling of star
			case 12313:// hatchling of twilight
				return new int[]
				{
						3500, 3501, 3502
				};

			case 12526:// wind strider
			case 12527:// Star strider
			case 12528:// Twilight strider
				return new int[]
				{
						4422, 4423, 4424
				};

			case 12621:// Wyvern
				return new int[]
				{
					8663
				};

			case 12780:// Baby Buffalo
			case 12782:// Baby Cougar
			case 12781:// Baby Kookaburra
				return new int[]
				{
						6648, 6649, 6650
				};

				// unknown item id.. should never happen
			default:
				return new int[]
				{
					0
				};
		}
	}

	public static boolean isMountable(int npcId)
	{
		return npcId == 12526 // wind strider
				|| npcId == 12527 // star strider
				|| npcId == 12528 // twilight strider
				|| npcId == 12621; // wyvern
	}
}