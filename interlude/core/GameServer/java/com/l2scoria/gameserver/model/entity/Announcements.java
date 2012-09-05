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
package com.l2scoria.gameserver.model.entity;

import com.l2scoria.Config;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.clientpackets.Say2;
import com.l2scoria.gameserver.network.serverpackets.CreatureSay;
import com.l2scoria.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.script.DateRange;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Р“Р»Р°РІРЅС‹Р№ РєР»Р°СЃСЃ Р°РЅРѕРЅСЃРѕРІ:<br>
 * <li>Р—Р°РіСЂСѓР·РєР° Р°РЅРѕРЅСЃРѕРІ РёР· С„Р°Р№Р»Р°<br> <li>РџРѕРєР°Р· Р°РЅРѕРЅСЃРѕРІ РёР· С„Р°Р№Р»Р°<br> <li>
 * Р”РѕР±Р°РІР»РµРЅРёРµ СЌРІРµРЅС‚ Р°РЅРѕРЅСЃРѕРІ<br> <li>Р›РёСЃС‚РёРЅРі Р°РЅРѕРЅСЃРѕРІ (СѓРїСЂР°РІР»РµРЅРёРµ С‡РµСЂРµР·
 * admin_*)<br> <li>Р”РѕР±Р°РІР»РµРЅРёРµ Р°РЅРѕРЅСЃРѕРІ (СЂРµР°Р»-С‚Р°Р№Рј, Р±РµР· Р·Р°РЅРµСЃРµРЅРёРµ РІ Р»РёСЃС‚РёРЅРі)
 * <br> <li>РЈРґР°Р»РµРЅРёРµ Р°РЅРѕРЅСЃРѕРІ<br> <li>Р§С‚РµРЅРёРµ СЃ РґРёСЃРєР° С„Р°Р№Р»Р°<br> <li>РЎРѕС…СЂР°РЅРµРЅРёРµ
 * С„Р°Р№Р»Р° РЅР° РґРёСЃРє<br> <li>РћС‚СЃС‹Р»РєР° Р°РЅРѕРЅСЃР° (РІ РІРёРґРµ system message)<br> <li>РћС‚СЃС‹Р»РєР°
 * Р°РЅРѕРЅСЃР° (СЃС‚Р°РЅРґР°СЂС‚)<br> <li>РҐР°РЅРґР»РµСЂ Р°РЅРѕРЅСЃРѕРІ<br>
 * <br>
 * 
 * @author ProGramMoS
 * @version 1.6
 */

public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());

	private static Announcements _instance;
	private List<String> _announcements = new FastList<String>();
	private List<List<Object>> _eventAnnouncements = new FastList<List<Object>>();

	public Announcements()
	{
		loadAnnouncements();
	}

	public static Announcements getInstance()
	{
		if(_instance == null)
		{
			_instance = new Announcements();
		}

		return _instance;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");

		if(file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.info("data/announcements.txt doesn't exist");
		}

		file = null;
	}

	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (String _announcement : _announcements)
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcement);
			activeChar.sendPacket(cs);
			cs = null;
		}

		for (List<Object> entry : _eventAnnouncements)
		{
			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();

			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);

				for (String element : msg)
				{
					sm.addString(element);
				}

				activeChar.sendPacket(sm);
				sm = null;
			}

			entry = null;
			validDateRange = null;
			msg = null;
			currentDate = null;
		}
	}

	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		List<Object> entry = new FastList<Object>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);

		entry = null;
	}

	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		TextBuilder replyMSG = new TextBuilder("<br>");

		for(int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>").append(_announcements.get(i)).append("</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement ").append(i).append("\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}

		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);

		content = null;
		adminReply = null;
		replyMSG = null;
	}

	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;

		try
		{
			int i = 0;

			String line = null;
			lnr = new LineNumberReader(new FileReader(file));

			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_announcements.add(announcement);

					i++;
				}
				st = null;
			}
			_log.info("Announcements: Loaded " + i + " Announcements.");

			line = null;
		}
		catch(IOException e1)
		{
			_log.fatal("Error reading announcements", e1);
		}
		finally
		{
			try
			{
				lnr.close();
				lnr = null;
			}
			catch(Exception e2)
			{
				// nothing
			}
		}
	}

	private void saveToDisk()
	{
		File file = new File("data/announcements.txt");
		FileWriter save = null;

		try
		{
			save = new FileWriter(file);
			for (String _announcement : _announcements)
			{
				save.write(_announcement);
				save.write("\r\n");
			}
			save.flush();
			save.close();
		}
		catch(IOException e)
		{
			_log.warn("saving the announcements file has failed: " + e);
		}

		file = null;
		save = null;
	}

	public void announceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);

		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}

		cs = null;
	}
	
	public void specialAnnounceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, 18, "", text);

		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}

		cs = null;
	}

	public void announceToAll(SystemMessage sm)
	{
		for(L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}

	public void criticalAnnounceToAll(String text)
	{
		CreatureSay cs = new CreatureSay(0, CreatureSay.SystemChatChannelId.Chat_Critical_Announce, "", text);
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			player.sendPacket(cs);
	}

	public void announceToInstance(L2GameServerPacket gsp, int instanceId)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player.getInstanceId() == instanceId)
				player.sendPacket(gsp);
		}
	}

	// Method fo handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			Announcements.getInstance().announceToAll(text);
			text = null;
		}

		// No body cares!
		catch(StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
}
