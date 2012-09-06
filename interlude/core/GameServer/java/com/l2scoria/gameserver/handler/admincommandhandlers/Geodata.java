/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Akumu, ProGramMoS
 */
public class Geodata extends Admin
{
	public Geodata()
	{
		_commands = new String[]{"admin_geo_z",
				"admin_geo_type",
				"admin_geo_nswe",
				"admin_geo_los",
				"admin_geo_position",
				"admin_geo_bug",
				"admin_geo_load",
				"admin_geo_unload",
				"admin_geo_dump"};
	}

	private enum CommandEnum
	{
		admin_geo_z,
		admin_geo_type,
		admin_geo_nswe,
		admin_geo_los,
		admin_geo_position,
		admin_geo_bug,
		admin_geo_load,
		admin_geo_unload,
		admin_geo_dump
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		String[] wordList = command.split(" ");
		CommandEnum comm;

		try
		{
			comm = CommandEnum.valueOf(wordList[0]);
		}
		catch(Exception e)
		{
			return false;
		}

		CommandEnum commandEnum = comm;
		switch(commandEnum)
		{
			case admin_geo_z:
			case admin_geo_type:
			case admin_geo_nswe:
			case admin_geo_los:
			case admin_geo_position:
			case admin_geo_bug:
			case admin_geo_unload:
				if(!Config.GEODATA)
				{
					activeChar.sendMessage("Geodata is curently off!");
					return true;
				}
				break;
		}

		switch(commandEnum)
		{
			case admin_geo_z:
				activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getInstanceId()) + " Loc_Z = " + activeChar.getZ());
				break;

			case admin_geo_type:
				short type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getInstanceId());
				activeChar.sendMessage("GeoEngine: Geo_Type = " + type);

				int height = GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getInstanceId());
				activeChar.sendMessage("GeoEngine: height = " + height);
				break;

			case admin_geo_nswe:
				String result = "";

				short nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getInstanceId());

				if((nswe & 8) == 0)
				{
					result += " N";
				}

				if((nswe & 4) == 0)
				{
					result += " S";
				}

				if((nswe & 2) == 0)
				{
					result += " W";
				}

				if((nswe & 1) == 0)
				{
					result += " E";
				}

				activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
				break;

			case admin_geo_los:
				if(activeChar.getTarget() != null)
				{
					if(GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
					{
						activeChar.sendMessage("GeoEngine: can see target.");
					}
					else
					{
						activeChar.sendMessage("GeoEngine: can`t see target");
					}
				}
				else
				{
					activeChar.sendMessage("None Target!");
				}
				break;

			case admin_geo_position:
				activeChar.sendMessage("GeoEngine:");
				activeChar.sendMessage(".... Current position: x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ());
				//activeChar.sendMessage(".... geo position: " + GeoEngine.geoPosition(activeChar.getX(), activeChar.getY()));
				break;

			case admin_geo_load:
				/*String[] v = command.substring(15).split(" ");

				if(v.length != 2)
				{
					activeChar.sendMessage("Usage: //admin_geo_load <X> <Y>");
				}
				else
				{
					try
					{
						byte rx = Byte.parseByte(v[0]);
						byte ry = Byte.parseByte(v[1]);

						boolean result2 = GeoEngine.loadGeodataFile(rx, ry);

						if(result2)
						{
							activeChar.sendMessage("GeoEngine: geo [" + rx + "," + ry + "] loaded.");
						}
						else
						{
							activeChar.sendMessage("GeoEngine: geo [" + rx + "," + ry + "] not loaded.");
						}
					}
					catch(Exception e)
					{
						activeChar.sendMessage("You have to write numbers of regions <regionX> <regionY>");
					}
				}*/
				break;

			case admin_geo_unload:
				/*String[] v2 = command.substring(17).split(" ");

				if(v2.length != 2)
				{
					activeChar.sendMessage("Usage: //admin_geo_unload <X> <Y>");
				}
				else
				{
					try
					{
						byte rx = Byte.parseByte(v2[0]);
						byte ry = Byte.parseByte(v2[1]);

						GeoEngine.unloadGeodata(rx, ry);
						activeChar.sendMessage("GeoEngine: geo [" + rx + "," + ry + "] unloaded.");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("You have to write numbers of regions <regionX> <regionY>");
					}
				}*/
				break;

			case admin_geo_bug:
				/*try
				{
					String comment = command.substring(14);
					GeoData.getInstance().addGeoDataBug(activeChar, comment);
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Usage: //admin_geo_bug <comments>");
				} */
				break;

			case admin_geo_dump:

				if(true)
				{
					break; // ибо нехуй
				}

				GeoEngine.DumpGeodata("./geodata_dump");
				break;
		}

		return true;
	}
}
