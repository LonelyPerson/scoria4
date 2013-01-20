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
package com.l2scoria.gameserver.powerpak.personal;

import com.l2scoria.Config;
import com.l2scoria.crypt.Base64;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.handler.VoicedCommandHandler;
import com.l2scoria.gameserver.handler.voicedcommandhandlers.Configurator;
import com.l2scoria.gameserver.managers.QuestManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.quest.Quest;
import com.l2scoria.gameserver.model.quest.QuestState;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.database.LoginRemoteDbFactory;
import javolution.text.TextBuilder;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class Personal implements ICustomByPassHandler
{
	private static String [] _CMD =  {"personal"};

	@Override
	public String[] getByPassCommands()
	{
		return _CMD;
	}

	public void useCommand(L2PcInstance activeChar, String params)
	{
                boolean is_main = false;
		if(activeChar==null)
			return;
		String index = "";
		if(params!=null && params.length()!=0)
                {
			if(!params.equals("0")) 
                        {
				index= "-"+params; 
                        }
                        else 
                        {
                            is_main = true;
                        }
                }
		String text = HtmCache.getInstance().getHtm("data/html/custom/menu"+index+".htm");
                if(is_main)
                {
                    text = text.replace("%xprate%", getExpRate(activeChar));
                    text = text.replace("%autoloot%", getLootMode(activeChar));
                    text = text.replace("%learnskills%", getAutoLearnMode(activeChar));
                }
		activeChar.sendPacket(new NpcHtmlMessage(5,text));
	}
        
	private String getLootMode(L2PcInstance activeChar)
	{
		String result = "<font color=FF0000>OFF</font>";
		if (activeChar.getAutoLoot())
			result = "<font color=00FF00>ON</font>";
		return result;
	}

	private String getAutoLearnMode(L2PcInstance activeChar)
	{
		String result = "<font color=FF0000>OFF</font>";
		if (activeChar.getAutoLearnSkill())
			result = "<font color=00FF00>ON</font>";
		return result;
	}
	
	private String getExpRate(L2PcInstance activeChar)
	{
		if(activeChar.isDonator())
			return "<font color=FF8000>" + activeChar.getXpRate() * Config.DONATOR_XPSP_RATE + "</font>";
		else
			return "<font color=00FF00>" + activeChar.getXpRate() + "</font>";
	}
        
	public void changePass(L2PcInstance activeChar, String params)
	{
		if(activeChar==null)
			return;
		String oldPass = null;
		String newPass = null;
		String cryptOldPass, cryptNewPass;
		String oldPassDB = null;
		StringTokenizer st = new StringTokenizer(params, " ");
		try
		{
			if(st.hasMoreTokens())
			{
				oldPass = st.nextToken();
			}
			if(st.hasMoreTokens())
			{
				newPass = st.nextToken();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(Config.PERSONAL_PASS_ITEM > 0 && Config.PERSONAL_PASS_COUNT > 0)
		{
			if(activeChar.getInventory().getItemByItemId(Config.PERSONAL_PASS_ITEM) != null &&
				activeChar.getInventory().getItemByItemId(Config.PERSONAL_PASS_ITEM).getCount() >= Config.PERSONAL_PASS_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Personal", Config.PERSONAL_PASS_ITEM, Config.PERSONAL_PASS_COUNT, activeChar, null);
				activeChar.getInventory().updateDatabase();
				activeChar.sendPacket(new ItemList(activeChar, true));
			}
			else
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("Item count is incorrect.");
				return;
			}
		}

		if(oldPass == null || newPass == null)
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("Fill all rows, please.");
			return;
		}
		if(newPass.length() < 3 || newPass.length() > 16)
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("Password length must be from 3 to 16 chars.");
			return;
		}
		if(!Util.isAlphaNumeric(newPass) || !Util.isAlphaNumeric(newPass))
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("Password can not contain any special chars.");
			return;
		}

		cryptOldPass = decryptPassword(oldPass);
		cryptNewPass = decryptPassword(newPass);
		if(cryptOldPass == "0" || cryptNewPass == "0")
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("There was some errors while encrypting your password.");
			return;
		}

		try
		{
			Connection con;
			if(Config.USE_RL_DATABSE)
			{
				con = LoginRemoteDbFactory.getInstance().getConnection();
			}
			else
			{
				con = L2DatabaseFactory.getInstance().getConnection();
			}
			PreparedStatement statement = con.prepareStatement("SELECT password FROM accounts WHERE login=?");
			statement.setString(1, activeChar.getAccountName());
			ResultSet rset = statement.executeQuery();
			
			if(rset.next())
			{
				oldPassDB = rset.getString("password");
			}
			rset.close();
			statement.close();
			if(oldPassDB != null && oldPassDB.equals(cryptOldPass))
			{
				PreparedStatement statement2 = con.prepareStatement("UPDATE accounts SET password=? WHERE login=?");
				statement2.setString(1, cryptNewPass);
				statement2.setString(2, activeChar.getAccountName());
				statement2.executeUpdate();
				statement2.close();
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-11.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				try { con.close(); } catch(Exception e) { }
				return;
			}
			try { con.close(); } catch(Exception e) { }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		String text = HtmCache.getInstance().getHtm("data/html/custom/menu-12.htm");
		activeChar.sendPacket(new NpcHtmlMessage(5,text));
		activeChar.sendMessage("Old password is incorrect.");
		return;
	}

	public String decryptPassword(String pass)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] decOldPass = pass.getBytes("UTF-8");
			decOldPass = md.digest(decOldPass);
			return Base64.encodeBytes(decOldPass);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "0";
	}
        
        public void bindHWID(L2PcInstance player) {
            String hwid = null;
            String _storedHwid = player.loadHwid();
            if(_storedHwid == null || _storedHwid.equals("*") || _storedHwid.length() < 4) {
            hwid = player.gethwid();
            // processing save HWID to database
            if(hwid != null && hwid.length() > 0) {
                try {
                    Connection con;
                    if(Config.USE_RL_DATABSE)
                    {
                        con = LoginRemoteDbFactory.getInstance().getConnection();
                    }
                    else
                    {
                        con = L2DatabaseFactory.getInstance().getConnection();
                    }
                    PreparedStatement statement = con.prepareStatement("UPDATE accounts SET hwid=? WHERE login=?");
                    statement.setString(1, hwid);
                    statement.setString(2, player.getAccountName());
                    statement.executeUpdate();
                    statement.close();
                    String text = HtmCache.getInstance().getHtm("data/html/custom/menu-51.htm").replace("%HWID%", hwid);
                    player.sendPacket(new NpcHtmlMessage(5,text));
                                    try { 
                                    con.close(); 
                                    } catch(Exception e) { }
                } catch(Exception a) { }
		return;
            } else {
                String text = "<html><body>Some error when try get hwid</body></html>";
                player.sendPacket(new NpcHtmlMessage(5,text));
            }
          } else {
                player.sendPacket(new NpcHtmlMessage(5,"<html><body>You HWID allredy binded!</body></html>"));
          }
        }
	
	public void allowIP(L2PcInstance activeChar, String params)
	{
		if(activeChar==null)
			return;

		if(Config.PERSONAL_IP_ITEM > 0 && Config.PERSONAL_IP_COUNT > 0)
		{
			if(activeChar.getInventory().getItemByItemId(Config.PERSONAL_IP_ITEM) != null &&
				activeChar.getInventory().getItemByItemId(Config.PERSONAL_IP_ITEM).getCount() >= Config.PERSONAL_IP_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Personal", Config.PERSONAL_IP_ITEM, Config.PERSONAL_IP_COUNT, activeChar, null);
				activeChar.getInventory().updateDatabase();
				activeChar.sendPacket(new ItemList(activeChar, true));
			}
			else
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-22.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("Item count is incorrect.");
				return;
			}
		}

		String ip = null;
		try
		{
			ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
		}
		catch(Exception e)
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-22.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("There was some problems while geting your IP address. Please try again later.");
			return;
		}

		if(params != null && params.length() != 0 && ip != null)
		{
			StringTokenizer st = new StringTokenizer(ip, ".");

			try
			{
				if(params.equals("1"))
				{
					//we dont need to change anything...
				}
				else if(params.equals("2"))
				{
					ip = st.nextToken()+"."+st.nextToken()+"."+st.nextToken()+"."+Config.NETMASK_FIST_RULLE;
				}
				else if(params.equals("3"))
				{
					ip = st.nextToken()+"."+st.nextToken()+"."+Config.NETMASK_SECOND_RULLE;
				}
				else if(params.equals("4"))
				{
					ip = "*";
				}
				else
				{
					return;
				}
			}
			catch(Exception e)
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-22.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("There was some problems while geting your IP address. Please try again later.");
				return;
			}
		}
		else
		{
			return;
		}

		try
		{
			if(ip != null)
			{
				Connection con;
				if(Config.USE_RL_DATABSE)
				{
					con = LoginRemoteDbFactory.getInstance().getConnection();
				}
				else
				{
					con = L2DatabaseFactory.getInstance().getConnection();
				}
				PreparedStatement statement = con.prepareStatement("UPDATE accounts SET allowed_ip=? WHERE login=?");
				statement.setString(1, ip);
				statement.setString(2, activeChar.getAccountName());
				statement.executeUpdate();
				statement.close();
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-21.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				try { con.close(); } catch(Exception e) { }
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		String text = HtmCache.getInstance().getHtm("data/html/custom/menu-22.htm");
		activeChar.sendPacket(new NpcHtmlMessage(5,text));
		activeChar.sendMessage("Unknown problem... 0_o");
		return;
	}

	public void premium(L2PcInstance activeChar, String params)
	{
		if(activeChar==null)
			return;

		if(activeChar.isDonator())
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-32.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("Premium mode is already ON.");
			return;
		}

		if(Config.PERSONAL_PREMIUM_ITEM > 0 && Config.PERSONAL_PREMIUM_COUNT > 0)
		{
			if(activeChar.getInventory().getItemByItemId(Config.PERSONAL_PREMIUM_ITEM) != null &&
				activeChar.getInventory().getItemByItemId(Config.PERSONAL_PREMIUM_ITEM).getCount() >= Config.PERSONAL_PREMIUM_COUNT)
			{
				activeChar.getInventory().destroyItemByItemId("Personal", Config.PERSONAL_PREMIUM_ITEM, Config.PERSONAL_PREMIUM_COUNT, activeChar, null);
				activeChar.getInventory().updateDatabase();
				activeChar.sendPacket(new ItemList(activeChar, true));
			}
			else
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-32.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("Item count is incorrect.");
				return;
			}
		}
		long premiumTime = Config.PERSONAL_PREMIUM_TIME * 24L * 60L * 60L * 1000L;

		activeChar.setDonator(true);
		activeChar.updateNameTitleColor();
		
		Connection con = null;
		try
		{
			if(Config.USE_RL_DATABSE)
                        {
                            con = LoginRemoteDbFactory.getInstance().getConnection();
                        }
                        else
                        {
                            con = L2DatabaseFactory.getInstance().getConnection();
                        }
			
			PreparedStatement statement = con.prepareStatement("SELECT * FROM accounts WHERE login=?");
			statement.setString(1, activeChar.getAccountName());
			ResultSet result = statement.executeQuery();
			if(result.next())
			{
                            long db_premium = result.getLong("premium");
                            long new_premium = 0;
                            if(db_premium > System.currentTimeMillis())
                            {
                                new_premium = db_premium+premiumTime;
                            }
                            else
                            {
                                new_premium = premiumTime+System.currentTimeMillis();
                            }
				PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET premium = ? WHERE login = ?");
				stmt.setLong(1, new_premium);
				stmt.setString(2, activeChar.getAccountName());
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			result.close();
			statement.close();

			result = null;
			statement = null;

			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-31.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
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
		activeChar.broadcastUserInfo();
	}

	public void showlist(L2PcInstance activeChar, String params)
	{
		if(activeChar==null)
			return;

		NpcHtmlMessage html = new NpcHtmlMessage(5);
		TextBuilder sb = new TextBuilder();
		if(activeChar.getAccountChars().size() > 0)
		{
			sb.append("<html><body>");
			sb.append("Select character:<br>");
			for(Entry<Integer, String> id :activeChar.getAccountChars().entrySet())
			{
				sb.append("<a action=\"bypass custom_personal charmove " + id.getKey() + "\">"+id.getValue()+"</a><br>");
			}
			sb.append("</body></html>");
		}
		else
		{
			sb.append("<html><body>No characters.</body></html>");
		}
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}

	public void movechar(L2PcInstance activeChar, String params)
	{
		if(activeChar==null)
			return;

		if(params != null && params.length() != 0)
		{
			if(!activeChar.getAccountChars().containsKey(Integer.parseInt(params)))
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-42.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("Wrong char Id.");
				return;
			}
		}
		else
		{
			String text = HtmCache.getInstance().getHtm("data/html/custom/menu-42.htm");
			activeChar.sendPacket(new NpcHtmlMessage(5,text));
			activeChar.sendMessage("Wrong parameter.");
			return;
		}

		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT in_jail FROM characters WHERE obj_Id=?");
			statement.setInt(1, Integer.parseInt(params));
			ResultSet result = statement.executeQuery();
			if(result.next() && result.getInt("in_jail") != 1)
			{
				statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE obj_Id=?");
				statement.setInt(1, 83983);
				statement.setInt(2, 148626);
				statement.setInt(3, -2225);
				statement.setInt(4, Integer.parseInt(params));
				statement.executeUpdate();
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-41.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
			}
			else
			{
				String text = HtmCache.getInstance().getHtm("data/html/custom/menu-42.htm");
				activeChar.sendPacket(new NpcHtmlMessage(5,text));
				activeChar.sendMessage("Error! Character in jail?");
			}
			result.close();
			statement.close();

			try { con.close(); } catch(Exception e) { }
			return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		String text = HtmCache.getInstance().getHtm("data/html/custom/menu-42.htm");
		activeChar.sendPacket(new NpcHtmlMessage(5,text));
		activeChar.sendMessage("Database error.");
	}

	public void processScript(L2PcInstance activeChar, String params)
	{
		String QuestName = null, Event = null;
		StringTokenizer st = new StringTokenizer(params, " ");
		
		try
		{
			if(st.hasMoreTokens())
			{
				QuestName = st.nextToken();
			}
			if(st.hasMoreTokens())
			{
				Event = st.nextToken();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(QuestName == null || Event == null)
		{
			return;
		}

		QuestState qs = activeChar.getQuestState(QuestName);
		if(qs == null)
		{
			Quest q = QuestManager.getInstance().getQuest(QuestName);
			if(q == null)
			{
				return;
			}

			qs = q.newQuestState(activeChar);
		}

		if(qs != null)
		{
			if (Config.PERSONAL_SCRIPTS_ID.contains(qs.getQuest().getQuestIntId()))
			{
				if(qs.getQuest().notifyEvent(Event, null, activeChar))
				{
					// nothing??
				}
			}
		}
	}

	public void useVoice(L2PcInstance activeChar, String params)
	{
		String param1 = null;
		String param2 = null;
		StringTokenizer st = new StringTokenizer(params, " ");
		try
		{
			if(st.hasMoreTokens())
			{
				param1 = st.nextToken();
			}
			if(st.hasMoreTokens())
			{
				param2 = st.nextToken();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		IVoicedCommandHandler menu = VoicedCommandHandler.getInstance().getVoicedCommandHandler("menu");
		IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(param1);
		if(vch != null)
		{
			vch.useVoicedCommand(param1, activeChar, param2);
			menu.useVoicedCommand("menu", activeChar, null);
		}
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if(player == null)
			return;
	 	if(parameters == null || parameters.length() == 0)
			return;
	 	if(parameters.startsWith("chat"))
		{
	 		useCommand(player,parameters.substring(4).trim());
		}
		else if(parameters.startsWith("setpassword"))
		{
			if(Config.ALLOW_CHANGE_PASS)
			{
				changePass(player,parameters.substring(11).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
		else if(parameters.startsWith("allowIP"))
		{
			if(Config.ALLOW_CHANGE_IP)
			{
				allowIP(player,parameters.substring(7).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
                else if(parameters.startsWith("allowHWID")) {
                        bindHWID(player);
                }
		else if(parameters.startsWith("premium"))
		{
			if(Config.ALLOW_PREMIUM)
			{
				premium(player,parameters.substring(7).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
		else if(parameters.startsWith("charlist"))
		{
			if(Config.CHAR_MOVE)
			{
				showlist(player,parameters.substring(8).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
		else if(parameters.startsWith("charmove"))
		{
			if(Config.CHAR_MOVE)
			{
				movechar(player,parameters.substring(8).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
		else if(parameters.startsWith("Script"))
		{
			if(Config.ALLOW_SCRIPT)
			{
				processScript(player,parameters.substring(6).trim());
			}
			else
			{
				player.sendMessage("This option is currently off.");
			}
		}
		else if(parameters.startsWith("voice"))
		{
			useVoice(player,parameters.substring(5).trim());
		}
	}
}