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
package com.l2scoria.gameserver.powerpak.xmlrpc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import java.sql.Connection;
import com.l2scoria.util.database.L2DatabaseFactory;

public class Server
{
	/**
	 * Оставлен для совместимости с PowerPak
	 */
	public int addItem(String key, String charName, String itemId, String count, String message)
	{
		return addItemToCharacter(charName, itemId, count, message);
	}

	/**
	 * Добавить предмет игроку<br>
	 * 
	 * @param charName as String - имя персонажа<br>
	 * @param itemId as String - ID предмета<br>
	 * @param count as String - количество<br>
	 * @param message as String - сообщение<br>
	 * @return as Integer - 0 - выполено, -1 SQL ошибка -2 нет чара -3 прочие ошибки
	 */
	public int addItemToCharacter(String charName, String itemId, String count, String message)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(charName);
		if(player == null)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement stm = con.prepareStatement("select obj_id from characters where char_name like ?");
				stm.setString(1, charName);
				ResultSet r = stm.executeQuery();

				if(r.next())
				{
					player = L2PcInstance.load(r.getInt(1));
				}

				r.close();
				stm.close();
				try { con.close(); } catch(Exception e) { }
			}
			catch(Exception e)
			{
				return -1;
			}
		}

		if(player == null)
			return -2;

		try
		{
			player.addItem("Web", Integer.parseInt(itemId), Integer.parseInt(count), null, player.isOnline() != 0);
			if(player.isOnline() == 0)
			{
				player.store();
				player.deleteMe();
			}
			else if(message != null && message.length() > 0)
			{
				player.sendMessage(message);
			}
			return 0;
		}
		catch(Exception e)
		{
			return -3;
		}

	}

	/**
	 * Получить список всех игроков on-line<br>
	 * <br>
	 * 
	 * @return as String - Список всех игроков онлайн<br>
	 */
	public String getOnLine()
	{
		String result = "<online>";
		for(L2PcInstance p : L2World.getInstance().getAllPlayers())
		{
			result += "<player id=\"" + p.getObjectId() + "\" name=\"" + p.getName() + "\" level=\"" + p.getLevel() + "\" class=\"" + p.getActiveClass() + "\" clan=\"" + p.getClan() == null ? "" : p.getClan().getName() + "\" />";
		}
		return result;
	}
}
