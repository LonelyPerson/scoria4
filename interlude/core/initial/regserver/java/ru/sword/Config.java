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
package ru.sword;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ProGramMoS
 * @version 0.2 BETA
 */
public final class Config 
{	
    public static final String  CONFIGURATION_FILE    = "./config/options.properties";
    public static String DATABASE_DRIVER;
    public static String DATABASE_URL;
    public static String DATABASE_LOGIN;
    public static String DATABASE_PASSWORD;
    public static int DATABASE_MAX_CONNECTIONS;
    
	public static void load()
	{
			try
			{
		        Properties serverSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(CONFIGURATION_FILE));  
				serverSettings.load(is);
				is.close();
	            
	            DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
	            DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
	            DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
	            DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }
       
	}	
}
