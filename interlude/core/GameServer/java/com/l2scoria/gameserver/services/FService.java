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
package com.l2scoria.gameserver.services;

public class FService
{
	// Config Files Paths
	//=======================================================================================================================
	//Standard
	public static final String ID_CONFIG_FILE = "./config/idfactory.properties";
	public static final String TELNET_FILE = "./config/telnet.properties";
	public static final String EXTENDER_FILE = "./config/extender.properties";
	public static final String SCRIPT_FILE = "./config/script.properties";
	public static final String DAEMONS_FILE = "./config/daemons.properties";

	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String FILTER_FILE = "./config/chatfilter.txt";
	public static final String QUESTION_FILE = "./config/questionwords.txt";
        public static final String FILTER_TRADE_FILE = "./config/tradefilter.txt";

	//head
	public static final String ALT_SETTINGS_FILE = "./config/head/altsettings.properties";
	public static final String CLANHALL_CONFIG_FILE = "./config/head/clanhall.properties";
	public static final String ENCHANT_CONFIG_FILE = "./config/head/enchant.properties";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/head/fort.properties";
	public static final String GEODATA_CONFIG_FILE = "./config/head/geodata.properties";
	public static final String OLYMP_CONFIG_FILE = "./config/head/olympiad.properties";
	public static final String OPTIONS_FILE = "./config/head/options.properties";
	public static final String OTHER_CONFIG_FILE = "./config/head/other.properties";
	public static final String RATES_CONFIG_FILE = "./config/head/rates.properties";
	public static final String SEVENSIGNS_FILE = "./config/head/sevensigns.properties";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/head/siege.properties";
	public static final String ELIT_CLANHALL_CONFIG_FILE = "./config/head/elitclanhall.properties";
	public static final String BOSS_CONFIG_FILE = "./config/head/boss.properties";
	public static final String PERSONAL_CONFIG_FILE = "./config/head/personal.properties";

	// daemons
	public static final String DAEMON_CONFIG_FILE = "./config/daemons/daemons.properties";
	public static final String L2TOP_DAEMON_CONFIG_FILE = "./config/daemons/l2top.properties";
	public static final String MMOTOP_DAEMON_CONFIG_FILE = "./config/daemons/mmotop.properties";
	public static final String HOPZONE_DAEMON_CONFIG_FILE = "./config/daemons/hopzone.properties";

	//functions
	public static final String ACCESS_CONFIGURATION_FILE = "./config/functions/access.properties";
	public static final String CRAFTING = "./config/functions/crafting.properties";
	public static final String DEVELOPER = "./config/functions/developer.properties";
	public static final String l2scoria_CONFIG_FILE = "./config/functions/l2scoria.properties";
	public static final String PHYSICS_CONFIGURATION_FILE = "./config/functions/physics.properties";
	public static final String PVP_CONFIG_FILE = "./config/functions/pvp.properties";
	public static final String POWERPAK_FILE = "./config/functions/powerpak.properires";

	//protected
	public static final String PROTECT_FLOOD_CONFIG_FILE = "./config/protected/flood.properties";
	public static final String PROTECT_OTHER_CONFIG_FILE = "./config/protected/other.properties";
	public static final String PROTECT_PACKET_CONFIG_FILE = "./config/protected/packets.properties";

	//fun -- events
	public static final String AWAY_FILE = "./config/fun/away.properties";
	public static final String BANK_FILE = "./config/fun/bank.properties";
	public static final String EVENT_CHAMPION_FILE = "./config/fun/champion.properties";
	public static final String EVENT_REBIRTH_FILE = "./config/fun/rebirth.properties";
	public static final String EVENT_WEDDING_FILE = "./config/fun/wedding.properties";
	public static final String EVENT_PC_BANG_POINT_FILE = "./config/fun/pcBang.properties";
	public static final String OFFLINE_FILE = "./config/fun/offline.properties";
        public static final String SERVICE_MANAGER_NPC = "./config/fun/servicemanager.properties";

	public static final String EVENTS = "./config/events/events.properties";

	//network
	public static final String CONFIGURATION_FILE = "./config/network/gameserver.properties";

	//version
	public static final String SERVER_VERSION_FILE = "./config/version/l2scoria-server.properties";
	public static final String DATAPACK_VERSION_FILE = "./config/version/l2scoria-datapack.properties";
}
