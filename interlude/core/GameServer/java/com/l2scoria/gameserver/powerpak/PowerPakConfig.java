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
package com.l2scoria.gameserver.powerpak;

import com.l2scoria.L2Properties;
import com.l2scoria.gameserver.templates.L2Item;
import javolution.util.FastList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Nick
 */
public class PowerPakConfig
{
	private static String PP_CONFIG_FILE = "config/powerpak/powerpak.properties";

	public static boolean ENGRAVER_ENABLED;
	public static int ENGRAVE_PRICE = 0;
	public static int ENGRAVE_PRICE_ITEM = 57;
	public static int ENGRAVER_X = 82270;
	public static int ENGRAVER_Y = 149660;
	public static int ENGRAVER_Z = -3495;
	public static int MAX_ENGRAVED_ITEMS_PER_CHAR;
	public static boolean SPAWN_ENGRAVER = true;
	public static boolean ENGRAVE_ALLOW_DESTROY;
	public static ArrayList<Integer> ENGRAVE_EXCLUDED_ITEMS = new ArrayList<Integer>();
	public static ArrayList<Integer> ENGRAVE_ALLOW_GRADE = new ArrayList<Integer>();

	public static boolean BUFFER_ENABLED;
	public static List<String> BUFFER_EXCLUDE_ON = new FastList<String>();
	public static String BUFFER_COMMAND;
	public static int BUFFER_PRICE;
	public static boolean BUFFER_USEBBS;
	public static int BUFFER_MAX_PROFILES;
	public static String BUFFER_HTML_NAME;
	public static String BUFFER_HTML_USE;
	public static String BUFFER_HTML_REMOVE;
	public static String BUFF_TEMPLATE;
	
	public static String BUFF_NOT_ALLOWED_AREA;
	public static String BUFF_NOT_ALLOWED_SITDOWN;
	public static String BUFF_NOT_ALLOWED_CASTING;
	public static String BUFF_NOT_ALLOWED_DEAD;
	public static String BUFF_NOT_ALLOWED_DUNG;
	public static String BUFF_NOT_ALLOWED_OLY;
	public static String BUFF_NOT_ALLOWED_EVENT;
	public static String BUFF_NOT_ALLOWED_CURSED;
	public static String BUFF_NOT_ALLOWED_ATTACK;

	public static boolean XMLRPC_ENABLED;
	public static int XMLRPC_PORT;
	public static String XMLRPC_HOST;

	public static List<String> GLOBALGK_EXCLUDE_ON;
	public static boolean GLOBALGK_ENABDLED;
	public static boolean GLOBALGK_USEBBS;
	public static int GLOBALGK_PRICE;
	public static int GLOBALGK_TIMEOUT;
	public static String GLOBALGK_COMMAND;

	public static boolean GMSHOP_ENABLED;
	public static boolean GMSHOP_USEBBS;
	public static String GMSHOP_COMMAND;
	public static List<String> GMSHOP_EXCLUDE_ON;

	public static boolean RSS_ENABLED;
	public static int RSS_INTERVAL;
	public static String RSS_URL;

	public static boolean WEBSERVER_ENABLED;
	public static int WEBSERVER_PORT;
	public static String WEBSERVER_HOST;
	
	public static void load()
	{
		try
		{
			L2Properties p = new L2Properties(PP_CONFIG_FILE);
			ENGRAVER_ENABLED = Boolean.parseBoolean(p.getProperty("EngraveEnabled", "true"));
			ENGRAVE_PRICE = Integer.parseInt(p.getProperty("EngravePrice", "0"));
			ENGRAVE_PRICE_ITEM = Integer.parseInt(p.getProperty("EngravePriceItem", "57"));
			SPAWN_ENGRAVER = Boolean.parseBoolean(p.getProperty("EngraveSpawnNpc", "true"));
			ENGRAVE_ALLOW_DESTROY = Boolean.parseBoolean(p.getProperty("EngraveAllowDestroy", "false"));
			MAX_ENGRAVED_ITEMS_PER_CHAR = Integer.parseInt(p.getProperty("EngraveMaxItemsPerChar", "0"));
			String str = p.getProperty("EngraveNpcLocation", "").trim();
			if(str.length() > 0)
			{
				StringTokenizer st = new StringTokenizer(str, " ");
				if(st.hasMoreTokens())
				{
					ENGRAVER_X = Integer.parseInt(st.nextToken());
				}
				if(st.hasMoreTokens())
				{
					ENGRAVER_Y = Integer.parseInt(st.nextToken());
				}
				if(st.hasMoreTokens())
				{
					ENGRAVER_Z = Integer.parseInt(st.nextToken());
				}
			}
			str = p.getProperty("EngraveExcludeItems", "").trim();
			if(str.length() > 0)
			{
				StringTokenizer st = new StringTokenizer(str, ",");
				while(st.hasMoreTokens())
				{
					ENGRAVE_EXCLUDED_ITEMS.add(Integer.parseInt(st.nextToken().trim()));
				}
			}
			str = p.getProperty("EngraveAllowGrades", "all").toLowerCase();

			boolean all = str.contains("all");

			if(all || str.contains("none"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_NONE);
			}

			if(all || str.contains("a"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_A);
			}

			if(all || str.contains("b"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_B);
			}

			if(all || str.contains("c"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_C);
			}

			if(all || str.contains("d"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_D);
			}

			if(all || str.contains("s"))
			{
				ENGRAVE_ALLOW_GRADE.add(L2Item.CRYSTAL_S);
			}

			BUFFER_ENABLED = Boolean.parseBoolean(p.getProperty("BufferEnabled", "true"));
			StringTokenizer st = new StringTokenizer(p.getProperty("BufferExcludeOn", ""), " ");
			while(st.hasMoreTokens())
			{
				BUFFER_EXCLUDE_ON.add(st.nextToken());
			}
			BUFFER_COMMAND = p.getProperty("BufferCommand", "buffme");
			BUFFER_PRICE = Integer.parseInt(p.getProperty("BufferPrice", "-1"));
			BUFFER_USEBBS = Boolean.parseBoolean(p.getProperty("BufferUseBBS", "true"));
			BUFFER_MAX_PROFILES = Integer.parseInt(p.getProperty("BufferMaxProfiles", "3"));

			BUFFER_HTML_NAME = p.getProperty("BufferProfileName", "");
			BUFFER_HTML_USE = p.getProperty("BufferProfileUse", "");
			BUFFER_HTML_REMOVE = p.getProperty("BufferProfileRemove", "");

			BUFF_TEMPLATE = p.getProperty("BuffTemplate", ".*");
			BUFF_NOT_ALLOWED_AREA = p.getProperty("BuffBlockedMessage", "Buffer not allowed in this area");
			BUFF_NOT_ALLOWED_SITDOWN = p.getProperty("BuffBlockedOnSitdown", "Buffer not allowed in sit");
			BUFF_NOT_ALLOWED_CASTING = p.getProperty("BuffBlockedOnCasting", "Buffer not allowed on casting");
			BUFF_NOT_ALLOWED_DEAD = p.getProperty("BuffBlockedOnDie", "Buffer not allowed in dead");
			BUFF_NOT_ALLOWED_DUNG = p.getProperty("BuffBlockedOnDungeons", "Buffer not allowed in dungeons");
			BUFF_NOT_ALLOWED_OLY = p.getProperty("BuffBlockedOnOlympiad", "Buffer not allowed in olympia");
			BUFF_NOT_ALLOWED_EVENT = p.getProperty("BuffBlockedOnEvent", "Buffer not allowed in event");
			BUFF_NOT_ALLOWED_CURSED = p.getProperty("BuffBlockedOnCursedWeapon", "Buffer not allowed when you accured Cursed Weapon");
			BUFF_NOT_ALLOWED_ATTACK = p.getProperty("BuffBlockedOnAttack", "Buffer not allowed when you attack");
			
			GLOBALGK_ENABDLED = Boolean.parseBoolean(p.getProperty("GKEnabled", "true"));
			GLOBALGK_COMMAND = p.getProperty("GKCommand", "teleport");
			GLOBALGK_TIMEOUT = Integer.parseInt(p.getProperty("GKTimeout", "10"));
			if(GLOBALGK_TIMEOUT < 1)
			{
				GLOBALGK_TIMEOUT = 1;
			}
			GLOBALGK_PRICE = Integer.parseInt(p.getProperty("GKPrice", "-1"));
			GLOBALGK_USEBBS = Boolean.parseBoolean(p.getProperty("GKUseBBS", "true"));
			GLOBALGK_EXCLUDE_ON = new FastList<String>();
			st = new StringTokenizer(p.getProperty("GKExcludeOn", ""), " ");
			while(st.hasMoreTokens())
			{
				GLOBALGK_EXCLUDE_ON.add(st.nextToken().toUpperCase());
			}

			GMSHOP_ENABLED = Boolean.parseBoolean(p.getProperty("GMShopEnabled","true"));
			GMSHOP_COMMAND = p.getProperty("GMShopCommand", "gmshop");
			GMSHOP_USEBBS = Boolean.parseBoolean(p.getProperty("GMShopUseBBS","true"));
			GMSHOP_EXCLUDE_ON = new FastList<String>();
			st = new StringTokenizer(p.getProperty("GMShopExcludeOn", ""), " ");
			while(st.hasMoreTokens())
			{
				GMSHOP_EXCLUDE_ON.add(st.nextToken().toUpperCase());
			}
			
			XMLRPC_ENABLED = Boolean.parseBoolean(p.getProperty("XMLRPCEnabled", "false"));
			XMLRPC_HOST = p.getProperty("XMLRPCHost", "localhost");
			XMLRPC_PORT = Integer.parseInt(p.getProperty("XMLRPCPort", "7000"));

			WEBSERVER_ENABLED = Boolean.parseBoolean(p.getProperty("WebServerEnabled","false"));
			WEBSERVER_HOST = p.getProperty("WebServerHost","localhost");
			WEBSERVER_PORT = Integer.parseInt(p.getProperty("WebServerPort","8080"));

			
			RSS_ENABLED = Boolean.parseBoolean(p.getProperty("RssEnabled","true"));
			RSS_INTERVAL = Integer.parseInt(p.getProperty("RssRefreshInterval","15"));
			RSS_URL = p.getProperty("RssURL","localhost");
		}
		catch(Exception e)
		{
			System.err.println("PowerPak: Unable to read  " + PP_CONFIG_FILE);
		}
	}
}
