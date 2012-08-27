package com.l2scoria.gameserver.util;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.powerpak.StringTable;
import com.l2scoria.gameserver.util.sql.SQLQuery;
import com.l2scoria.gameserver.util.sql.SQLQueue;
import com.l2scoria.util.database.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class L2Utils
{
	public static interface IItemFilter
	{
		public boolean isCanShow(L2ItemInstance item);
	}

	public static void addItem(String charName, int itemId, int count)
	{
		L2PcInstance result = L2World.getInstance().getPlayer(charName);
		if (result != null)
		{
			result.addItem("PcUtils", itemId, count, null, true);
		}
		else
		{
			SQLQueue.getInstance().add(new AddToOffline(charName, itemId, count));
		}
	}

	private static class AddToOffline implements SQLQuery
	{

		private String _charName;
		private int[] _item;

		public AddToOffline(String charName, int itemid, int count)
		{
			_charName = charName;
			_item = new int[]{itemid, count};
		}

		@Override
		public void execute(Connection con)
		{
			try
			{
				PreparedStatement stm = con.prepareStatement("insert into character_items select charId,?,?,0 from characters where char_name=?");
				stm.setInt(1, _item[0]);
				stm.setInt(2, _item[1]);
				stm.setString(3, _charName);
				stm.execute();
				stm.close();
			} catch (SQLException e)
			{

			}
		}

	}

	public static L2PcInstance loadPlayer(String charName)
	{
		L2PcInstance result = L2World.getInstance().getPlayer(charName);
		if (result == null)
		{
			try
			{
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("select obj_id from characters where char_name like ?");
				stm.setString(1, charName);
				ResultSet r = stm.executeQuery();
				if (r.next())
				{
					result = L2PcInstance.load(r.getInt(1));
				}
				r.close();
				stm.close();
				try
				{
					con.close();
				} catch (Exception e)
				{
				}
			} catch (SQLException e)
			{
				result = null;
			}
		}

		return result;
	}

	public static boolean charExists(String charName)
	{
		boolean result = L2World.getInstance().getPlayer(charName) != null;
		if (!result)
		{
			try
			{
				Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("select charId from characters where char_name like ?");
				stm.setString(1, charName);
				ResultSet r = stm.executeQuery();
				if (r.next())
				{
					result = true;
				}
				r.close();
				stm.close();
				con.close();
			} catch (SQLException e)
			{
				result = false;
			}
		}
		return result;
	}

	public static String formatUserItems(L2PcInstance player, int startItem, IItemFilter filter, String actionString)
	{
		String result = "<table width=300>";
		int startwith = 0;
		for (L2ItemInstance it : player.getInventory().getItems())
		{
			if (startwith++ < startItem)
			{
				continue;
			}
			if (filter != null && !filter.isCanShow(it))
			{
				continue;
			}
			result += "<tr><td>";
			if (actionString != null)
			{
				String s = actionString.replace("%itemid%", String.valueOf(it.getItemId()));
				s = s.replace("%objectId%", String.valueOf(it.getObjectId()));
				result += ("<a action=\"" + s + "\">");
			}

			if (it.getEnchantLevel() > 0)
			{
				result += "+" + it.getEnchantLevel() + " ";
			}
			result += it.getItemName();
			if (actionString != null)
			{
				result += "</a>";
			}
			result += "</td><td>";
			if (it.getCount() > 1)
			{
				result += (it.getCount() + " шт.");
			}
			result += "</td></tr>";
		}
		result += "<table>";
		return result;
	}

	public static String loadMessage(String msg)
	{
		if (msg.startsWith("@"))
		{
			msg = msg.substring(1);
			int iPos = msg.indexOf(";");
			if (iPos != -1)
			{
				StringTable st = new StringTable(msg.substring(0, iPos));
				return st.Message(msg.substring(iPos + 1));
			}
		}
		return msg;
	}

}
