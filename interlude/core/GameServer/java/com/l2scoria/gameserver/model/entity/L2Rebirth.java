/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.base.Experience;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import java.sql.Connection;
import com.l2scoria.util.database.L2DatabaseFactory;

/**
 * <strong>This 'Custom Engine' was developed for L2J Forum Member 'sauron3256' on November 1st, 2008.</strong><br>
 * <br>
 * <strong>Quick Summary:</strong><br>
 * This engine will grant the player special bonus skills at the cost of reseting him to level 1.<br>
 * The -USER- can set up to 3 Rebirths, the skills received and their respective levels, and the item and price of each
 * rebirth.<br>
 * PLAYER's information is stored in an SQL Db under the table name: REBIRTH_MANAGER.<br>
 * 
 * @author <strong>JStar</strong>
 */
public class L2Rebirth
{
	/** The current instance - static repeller */
	private static L2Rebirth _instance = null;

	/** Basically, this will act as a cache so it doesnt have to read DB information on relog. */
	private HashMap<Integer, Integer> _playersRebirthInfo = new HashMap<Integer, Integer>();

	/** Creates a new NON-STATIC instance */
	private L2Rebirth()
	{
	//Do Nothing ^_-
	}

	/** Receives the non-static instance of the RebirthManager. */
	public static L2Rebirth getInstance()
	{
		if(_instance == null)
		{
			_instance = new L2Rebirth();
		}
		return _instance;
	}

	/** This is what it called from the Bypass Handler. (I think that's all thats needed here). */
	public void handleCommand(L2PcInstance player, String command)
	{
		if(command.startsWith("custom_rebirth_requestrebirth"))
		{
			displayRebirthWindow(player);
		}
		else if(command.startsWith("custom_rebirth_confirmrequest"))
		{
			requestRebirth(player);
		}
	}

	/** Display's an HTML window with the Rebirth Options */
	public void displayRebirthWindow(L2PcInstance player)
	{
		try
		{
			int currBirth = getRebirthLevel(player); //Returns the player's current birth level

			//Don't send html if player is already at max rebirth count.
			if(currBirth >= 3)
			{
				player.sendMessage("You are currently at your maximum rebirth count!");
				return;
			}

			//Returns true if BASE CLASS is a mage.
			boolean isMage = player.getBaseTemplate().classId.isMage();
			//Returns the skill based on next Birth and if isMage.
			L2Skill skill = getRebirthSkill((currBirth + 1), isMage);

			String icon = "" + skill.getId();//Returns the skill's id.

			//Incase the skill is only 3 digits.
			if(icon.length() < 4)
			{
				icon = "0" + icon;
			}

			skill = null;
			icon = null;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/** Checks to see if the player is eligible for a Rebirth, if so it grants it and stores information */
	public void requestRebirth(L2PcInstance player)
	{
		//Check the player's level.
		if(player.getLevel() < Config.REBIRTH_MIN_LEVEL)
		{
			player.sendMessage("You do not meet the level requirement for a Rebirth!");
			return;
		}

		else if(player.isSubClassActive())
		{
			player.sendMessage("Please switch to your Main Class before attempting a Rebirth.");
			return;
		}

		int currBirth = getRebirthLevel(player);
		int itemNeeded = 0;
		int itemAmount = 0;

		if(currBirth >= 3)
		{
			player.sendMessage("You are currently at your maximum rebirth count!");
			return;
		}

		//Get the requirements
		switch(currBirth)
		{
			case 0:
				itemNeeded = Config.REBIRTH_ITEM1_NEEDED;
				itemAmount = Config.REBIRTH_ITEM1_AMOUNT;
				break;

			case 1:
				itemNeeded = Config.REBIRTH_ITEM2_NEEDED;
				itemAmount = Config.REBIRTH_ITEM2_AMOUNT;
				break;

			case 2:
				itemNeeded = Config.REBIRTH_ITEM3_NEEDED;
				itemAmount = Config.REBIRTH_ITEM3_AMOUNT;
				break;
		}

		//Their is an item required
		if(itemNeeded != 0)
		{
			//Checks to see if player has required items, and takes them if so.
			if(!playerIsEligible(player, itemNeeded, itemAmount))
				return;
		}

		//Check and see if its the player's first Rebirth calling.
		boolean firstBirth = currBirth == 0;
		//Player meets requirements and starts Rebirth Process.
		grantRebirth(player, (currBirth + 1), firstBirth);
	}

	/** Physically rewards player and resets status to nothing. */
	public void grantRebirth(L2PcInstance player, int newBirthCount, boolean firstBirth)
	{
		try
		{
			//Set player to level 1.
			player.removeExpAndSp(player.getExp() - Experience.getExp(1), 0);
			//Resets character to first class.
			player.setClassId(player.getBaseClass());

			//Remove the player's current skills.
			for(L2Skill skill : player.getAllSkills())
			{
				player.removeSkill(skill);
			}
			//Give players their eligible skills.
			player.giveAvailableSkills();
			//Updates the player's information in the Character Database.
			player.store();

			if(firstBirth)
			{
				storePlayerBirth(player);
			}
			else
			{
				updatePlayerBirth(player, newBirthCount);
			}

			//Give the player his new Skills.
			grantRebirthSkills(player);
			//Displays a congratulation message to the player.
			displayCongrats(player);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/** Special effects when the player levels. */
	public void displayCongrats(L2PcInstance player)
	{
		//Victory Social Action.
		player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
		player.sendMessage("Congratulations " + player.getName() + ". You have been REBORN!");
	}

	/** Check and verify the player DOES have the item required for a request. Also, remove the item if he has. */
	public boolean playerIsEligible(L2PcInstance player, int itemId, int itemAmount)
	{
		String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
		L2ItemInstance itemNeeded = player.getInventory().getItemByItemId(itemId);

		if(itemNeeded == null || itemNeeded.getCount() < itemAmount)
		{
			player.sendMessage("You need atleast " + itemAmount + "  [ " + itemName + " ] to request a Rebirth!");
			return false;
		}

		//Player has the required items, so we're going to take them!
		player.getInventory().destroyItemByItemId("Rebrith Engine", itemId, itemAmount, player, null);
		player.sendMessage("Removed " + itemAmount + " " + itemName + " from your inventory!");

		itemName = null;
		itemNeeded = null;

		return true;
	}

	/** Gives the available Bonus Skills to the player. */
	public void grantRebirthSkills(L2PcInstance player)
	{
		//returns the current Rebirth Level
		int rebirthLevel = getRebirthLevel(player);
		//Returns true if BASE CLASS is a mage.
		boolean isMage = player.getBaseTemplate().classId.isMage();

		//Simply return since no bonus skills are granted.
		if(rebirthLevel == 0)
			return;

		//Load the bonus skills unto the player.
		CreatureSay rebirthText = null;
		for(int i = 0; i < rebirthLevel; i++)
		{
			L2Skill bonusSkill = getRebirthSkill((i + 1), isMage);
			player.addSkill(bonusSkill, false);

			//If you'd rather make it simple, simply comment this out and replace with a simple player.sendmessage();
			rebirthText = new CreatureSay(0, 18, "Rebirth Manager ", " Granted you [ " + bonusSkill.getName() + " ] level [ " + bonusSkill.getLevel() + " ]!");
			player.sendPacket(rebirthText);
		}

		rebirthText = null;
	}

	/** Return the player's current Rebirth Level */
	public int getRebirthLevel(L2PcInstance player)
	{
		int playerId = player.getObjectId();

		if(_playersRebirthInfo.get(playerId) == null)
		{
			loadRebirthInfo(player);
		}

		return _playersRebirthInfo.get(playerId);
	}

	/** Return the L2Skill the player is going to be rewarded. */
	public L2Skill getRebirthSkill(int rebirthLevel, boolean mage)
	{
		L2Skill skill = null;

		//Player is a Mage.
		if(mage)
		{
			switch(rebirthLevel)
			{
				case 1:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_MAGE_SKILL1_ID, Config.REBIRTH_MAGE_SKILL1_LEVEL);
					break;
				case 2:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_MAGE_SKILL2_ID, Config.REBIRTH_MAGE_SKILL2_LEVEL);
					break;
				case 3:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_MAGE_SKILL3_ID, Config.REBIRTH_MAGE_SKILL3_LEVEL);
					break;
			}
		}
		//Player is a Fighter.
		else
		{
			switch(rebirthLevel)
			{
				case 1:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_FIGHTER_SKILL1_ID, Config.REBIRTH_FIGHTER_SKILL1_LEVEL);
					break;
				case 2:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_FIGHTER_SKILL2_ID, Config.REBIRTH_FIGHTER_SKILL2_LEVEL);
					break;
				case 3:
					skill = SkillTable.getInstance().getInfo(Config.REBIRTH_FIGHTER_SKILL3_ID, Config.REBIRTH_FIGHTER_SKILL3_LEVEL);
					break;
			}
		}
		return skill;
	}

	/** Database caller to retrieve player's current Rebirth Level */
	public void loadRebirthInfo(L2PcInstance player)
	{
		int playerId = player.getObjectId();
		int rebirthCount = 0;

		Connection con = null;
		try
		{
			ResultSet rset;
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `rebirth_manager` WHERE playerId = ?");
			statement.setInt(1, playerId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				rebirthCount = rset.getInt("rebirthCount");
			}

			rset.close();
			statement.close();
			statement = null;
			rset = null;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		_playersRebirthInfo.put(playerId, rebirthCount);
	}

	/** Stores the player's information in the DB. */
	public void storePlayerBirth(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `rebirth_manager` (playerId,rebirthCount) VALUES (?,1)");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement = null;

			_playersRebirthInfo.put(player.getObjectId(), 1);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	/** Updates the player's information in the DB. */
	public void updatePlayerBirth(L2PcInstance player, int newRebirthCount)
	{
		Connection con = null;
		try
		{
			int playerId = player.getObjectId();

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE `rebirth_manager` SET rebirthCount = ? WHERE playerId = ?");
			statement.setInt(1, newRebirthCount);
			statement.setInt(2, playerId);
			statement.execute();
			statement = null;

			_playersRebirthInfo.put(playerId, newRebirthCount);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}
}
