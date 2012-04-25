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
package ru.sword.gsregistering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import ru.sword.Config;
import ru.sword.GameServerTable;

/**
 * 
 * @author l2j-server
 * @author ProGramMoS
 * @version 0.2 BETA
 */

public class GameServerRegister
{
	private static String _choice;
	private static String IP;
	private static GameServerTable gsTable;
	private static boolean _choiseOk;

	public static void main(String[] args) throws IOException
	{
		Config.load();
		gsTable = new GameServerTable();
		System.out.println("\nWelcome to SwordDev GameServer Regitering");
		System.out.println("Enter The id of the server you want to register or type help to get a list of ids:");
		LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
		while(!_choiseOk)
		{
			System.out.println("Your choice:");
			_choice = _in.readLine();
			if(_choice.equalsIgnoreCase("help"))
			{
				for(Map.Entry<Integer, String> entry : gsTable.serverNames.entrySet())
				{
					System.out.println("Server: id:"+entry.getKey()+" - "+entry.getValue());
				}
				System.out.println("You can also see servername.xml");
			}
			
			System.out.println("Please enter server IP:");
			LineNumberReader chin = new LineNumberReader(new InputStreamReader(System.in));
			IP = chin.readLine();
			
			if(!_choice.equalsIgnoreCase("help"))
			{
				try
				{
					int id = new Integer(_choice).intValue();
					
					if(id >= gsTable.serverNames.size())
					{
						System.out.println("ID is too high (max is "+(gsTable.serverNames.size()-1)+")");
						continue;
					}
					
					if(id < 0)
					{
						System.out.println("ID must be positive number");
						continue;
					}
					else
					{
						if(gsTable.isIDfree(id))
						{
							byte[] hex = generateHex(16);
							gsTable.createServer(hex , id, IP);
							saveHexid(new BigInteger(hex).toString(16), id, "hexid(server "+id+").txt");
							System.out.println("Server Registered hexid saved to 'hexid(server "+id+").txt'");
							System.out.println("Put this file in the /config folder of your gameserver and rename it to 'hexid.txt'");
							return;
						}
						else
						{
							System.out.println("This ID is not free");
						}
					}
				}
				catch (NumberFormatException nfe)
				{
					System.out.println("Please, type a number or 'help'");
				}
			}
		}
	}
	
	public static void saveHexid(String hex, int id, String fileName)
	{
		try
        {
            Properties hexSetting = new Properties();
            File file = new File(fileName);
            //Create a new empty file only if it doesn't exist
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("ServerID",String.valueOf(id));
			hexSetting.setProperty("HexID",hex);
			hexSetting.store(out,"the hexID to auth into login");
			out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public static byte[] generateHex(int size)
	{
		byte [] array = new byte[size]; 
		Random rnd = new Random();
		rnd.nextBytes(array);
		return array;
	}
}