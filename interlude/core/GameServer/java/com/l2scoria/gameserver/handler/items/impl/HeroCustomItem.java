// Hero Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   HeroCustomItem.java
// Modded by programmos, sword dev

package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.util.database.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HeroCustomItem extends ItemAbst
{

	public HeroCustomItem()
	{
		if(Config.HERO_CUSTOM_ITEMS)
		{
			_items = new int[]{Config.HERO_CUSTOM_ITEM_ID};

			_playerUseOnly = true;
			_notOnOlympiad = true;
			_notInObservationMode = true;
			_notWhenSkillsDisabled = true;
			_notSitting = true;
		}
	}

	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		if(activeChar.isHero())
		{
			activeChar.sendMessage("You are already a Hero!");
		}
		else
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
			activeChar.setIsHero(true);
			updateDatabase(activeChar, Config.HERO_CUSTOM_DAY * 24L * 60L * 60L * 1000L);
			activeChar.sendMessage("You are now a Hero. You are granted with hero status, skills and aura.");
			activeChar.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			activeChar.getInventory().addItem("Wings", 6842, 1, activeChar, null);
		}
		return true;
	}
	
	private void updateDatabase(L2PcInstance player, long heroTime)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters_custom_data WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			ResultSet result = statement.executeQuery();

			if(result.next())
			{
				PreparedStatement stmt = con.prepareStatement("UPDATE characters_custom_data SET noble=1, hero=1, hero_end_date=? WHERE obj_Id=?");
				stmt.setLong(1, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
				stmt.setInt(2, player.getObjectId());
				stmt.execute();
				stmt.close();
			}
			else
			{
				PreparedStatement stmt = con.prepareStatement("INSERT INTO characters_custom_data (obj_Id, char_name, noble, hero, hero_end_date) VALUES (?,?,?,?,?)");
				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, 1);
				stmt.setInt(4, 1);
				stmt.setLong(5, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
				stmt.execute();
				stmt.close();
			}
			result.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warn("Error: could not update database: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
		}
	}
}
