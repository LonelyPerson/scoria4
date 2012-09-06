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
package com.l2scoria.util;

import com.l2scoria.gameserver.taskmanager.MemoryWatchDog;
import javolution.text.TextBuilder;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @author luisantonioa
 */

public class Util
{
	private final static Logger _log = Logger.getLogger(Util.class.getName());

	public static boolean isInternalIP(String ipAddress)
	{
		return ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("127.0.0.1");
	}

	public static String printData(byte[] data, int len)
	{
		TextBuilder result = new TextBuilder();

		int counter = 0;

		for(int i = 0; i < len; i++)
		{
			if(counter % 16 == 0)
			{
				result.append(fillHex(i, 4)).append(": ");
			}

			result.append(fillHex(data[i] & 0xff, 2)).append(" ");
			counter++;
			if(counter == 16)
			{
				result.append("   ");

				int charpoint = i - 15;
				for(int a = 0; a < 16; a++)
				{
					int t1 = data[charpoint++];
					if(t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}

				result.append("\n");
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if(rest > 0)
		{
			for(int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}

			int charpoint = data.length - rest;
			for(int a = 0; a < rest; a++)
			{
				int t1 = data[charpoint++];
				if(t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append("\n");
		}

		return result.toString();
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for(int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}

		return number;
	}

	/**
	 * @param s
	 */

	public static void printSection(String s)
	{
		int maxlength = 79;
		s = "-[ " + s + " ]";
		int slen = s.length();
		if(slen > maxlength)
		{
			System.out.println(s);
			return;
		}
		int i;
		for(i = 0; i < maxlength - slen; i++)
		{
			s = "=" + s;
		}
		System.out.println(s);
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	/**
	 * returns how many processors are installed on this system.
	 */
	private static void printCpuInfo()
	{
		_log.info("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		_log.info("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns the operational system server is running on it.
	 */
	private static void printOSInfo()
	{
		_log.info("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		_log.info("OS Arch: " + System.getProperty("os.arch"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns JAVA Runtime Enviroment properties
	 */
	private static void printJreInfo()
	{
		_log.info("Java Platform Information");
		_log.info("Java Runtime  Name: " + System.getProperty("java.runtime.name"));
		_log.info("Java Version: " + System.getProperty("java.version"));
		_log.info("Java Class Version: " + System.getProperty("java.class.version"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * returns general infos related to machine
	 */
	private static void printRuntimeInfo()
	{
		_log.info("Runtime Information");
		_log.info("Current Free Heap Size: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb");
		_log.info("Current Heap Size: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb");
		_log.info("Maximum Heap Size: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb");
		_log.info("..................................................");
		_log.info("..................................................");

	}

	/**
	 * calls time service to get system time.
	 */
	private static void printSystemTime()
	{
		// instanciates Date Objec
		Date dateInfo = new Date();

		//generates a simple date format
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");

		//generates String that will get the formater info with values
		String dayInfo = df.format(dateInfo);

		_log.info("..................................................");
		_log.info("System Time: " + dayInfo);
		_log.info("..................................................");
	}

	/**
	 * gets system JVM properties.
	 */
	private static void printJvmInfo()
	{
		_log.info("Virtual Machine Information (JVM)");
		_log.info("JVM Name: " + System.getProperty("java.vm.name"));
		_log.info("JVM installation directory: " + System.getProperty("java.home"));
		_log.info("JVM version: " + System.getProperty("java.vm.version"));
		_log.info("JVM Vendor: " + System.getProperty("java.vm.vendor"));
		_log.info("JVM Info: " + System.getProperty("java.vm.info"));
		_log.info("..................................................");
		_log.info("..................................................");
	}

	/**
	 * prints all other methods.
	 */
	public static void printGeneralSystemInfo()
	{
		printSystemTime();
		printOSInfo();
		printCpuInfo();
		printRuntimeInfo();
		printJreInfo();
		printJvmInfo();
	}

	/**
	 * converts a given time from minutes -> miliseconds
	 * 
	 * @param string
	 * @return
	 */
	public static int convertMinutesToMiliseconds(int minutesToConvert)
	{
		return minutesToConvert * 60000;
	}

	public static int getAvailableProcessors()
	{
		Runtime rt = Runtime.getRuntime();
		return rt.availableProcessors();
	}

	public static String getOSName()
	{
		return System.getProperty("os.name");
	}

	public static String getOSVersion()
	{
		return System.getProperty("os.version");
	}

	public static String getOSArch()
	{
		return System.getProperty("os.arch");
	}

	public static byte[] securityCrypt(String data)
	{
		String pass = "It is a scoria crypt gamma string";
		String gamma = "";
		byte[] seq;

		try
		{
			while ((gamma.length()/2) < data.length())
			{
				MessageDigest md = MessageDigest.getInstance("SHA");
				
				byte z[] = new byte[hexStringToByteArray(gamma).length+pass.getBytes().length];

				System.arraycopy(hexStringToByteArray(gamma), 0, z, 0, hexStringToByteArray(gamma).length);
				System.arraycopy(pass.getBytes(), 0, z, hexStringToByteArray(gamma).length, pass.getBytes().length);
				
				seq = z;
				seq = md.digest(seq);
				
				StringBuilder hexString = new StringBuilder();
				for (int i =0; i < 8; i++)
				{
					String hex = Integer.toHexString(0xFF & seq[i]);
					if (hex.length() == 1)
					{
						hexString.append('0');
					}
					hexString.append(hex);
				}
				gamma += hexString.toString();
			}
		}
		catch (Exception ex)
		{
		}

		byte[] g = hexStringToByteArray(gamma);
		byte[] result = new byte[Math.min(data.length(), g.length)];
		
		for(int i=0; i<data.length() && i<g.length;i++)
		{
			result[i] = (byte)(data.charAt(i) ^ g[i]);
		}

		return result;
	}

	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	public static byte[] getHash()
	{
		List<Byte> hash = new ArrayList<Byte>();
		byte[] hash_array = null;
		try
		{
			JarFile jf = new JarFile("./lib/l2scoria-core-3.jar");
                        
			JarEntry je = jf.getJarEntry("com/l2scoria/util/bin.class");
			if (je == null)
				je = jf.getJarEntry("com/l2scoria/crypt/lameguard.class");
			if (je == null)
				je = jf.getJarEntry("com/l2scoria/gameserver/network/serverpackets/ExChiperRequest.class");
			//if (je == null)
                                //System.exit(1);

                        // why? don`t used entryName anywhere
			String entryName = je.getName();
			InputStream in = jf.getInputStream(je);

			int t;
			while((t = in.read()) != -1)
			{
				hash.add((byte)t);
			}

			hash_array = new byte[hash.size()];
			int i = 0;
			for(byte b : hash)
			{
				hash_array[i++] = b;
			}
		}
		catch(Exception ex)
		{
			//System.exit(1);
		}
		return hash_array;
	}

	public static long gc(int i, int delay)
	{
		long freeMemBefore = MemoryWatchDog.getMemFree();
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		while(--i > 0)
		{
			try
			{
				Thread.sleep(delay);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			rt.gc();
		}
		rt.runFinalization();
		return MemoryWatchDog.getMemFree() - freeMemBefore;
	}
}
