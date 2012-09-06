// Hero Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   HeroCustomItem.java
// Modded by programmos, sword dev

package com.l2scoria.gameserver.handler.itemhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.handler.IItemHandler;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HeroCustomItem implements IItemHandler
{

	public HeroCustomItem()
	{
	//null
	}

	private final static Logger _log = Logger.getLogger(HeroCustomItem.class.getName());

	String INSERT_DATA= "INSERT INTO characters_custom_data (obj_Id, char_name, noble, hero, hero_end_date) VALUES (?,?,?,?,?)";
	String UPDATE_DATA = "UPDATE characters_custom_data SET noble=1, hero=1, hero_end_date=? WHERE obj_Id=?";

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(Config.HERO_CUSTOM_ITEMS)
		{
			if(!(playable.isPlayer))
				return;

			L2PcInstance activeChar = (L2PcInstance) playable;

			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
			}

			if(activeChar.isHero())
			{
				activeChar.sendMessage("You Are Already A Hero!.");
			}
			else
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.setIsHero(true);
				updateDatabase(activeChar, Config.HERO_CUSTOM_DAY * 24L * 60L * 60L * 1000L);
				activeChar.sendMessage("You Are Now a Hero,You Are Granted With Hero Status , Skills ,Aura. This Effect Will Stop When You Restart.");
				activeChar.broadcastUserInfo();
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.getInventory().addItem("Wings", 6842, 1, activeChar, null);
			}
			activeChar = null;
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	private void updateDatabase(L2PcInstance player, long heroTime)
	{
		Connection con = null;
		try
		{
			if(player == null)
				return;

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters_custom_data WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			ResultSet result = statement.executeQuery();

			if(result.next())
			{
				PreparedStatement stmt = con.prepareStatement(UPDATE_DATA);
				stmt.setLong(1, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
				stmt.setInt(2, player.getObjectId());
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			else
			{
				PreparedStatement stmt = con.prepareStatement(INSERT_DATA);
				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, 1);
				stmt.setInt(4, 1);
				stmt.setLong(5, heroTime == 0 ? 0 : System.currentTimeMillis() + heroTime);
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			result.close();
			statement.close();

			result = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.warn("Error: could not update database: " + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	private static final int ITEM_IDS[] =
	{
		Config.HERO_CUSTOM_ITEM_ID
	};

}
