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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javolution.io.UTF8StreamReader;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;

/**
 * 
 * @author luisantonioa
 * @author ProGramMoS
 * @version 0.2 BETA
 */

public class GameServerTable
{
	private static GameServerTable _instance;
	private List<Integer> _gameServerList;
	public Map<Integer, String> serverNames;
	@SuppressWarnings("unused")
	private long _last_IP_Update;
	private KeyPair[] _keyPairs;
	private KeyPairGenerator _keyGen;
	@SuppressWarnings("unused")
	private Random _rnd;
	
	public static GameServerTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new GameServerTable();
		}
		return _instance;
	}
	
	public GameServerTable()
	{
		_gameServerList = new FastList<Integer>();
		load();
		_last_IP_Update = System.currentTimeMillis();
		try
		{
			_keyGen = KeyPairGenerator.getInstance("RSA");
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512,RSAKeyGenParameterSpec.F4);
			_keyGen.initialize(spec);
		}
		catch (GeneralSecurityException e)
		{
			e.printStackTrace();
		}
		_keyPairs = new KeyPair[10];
		for(int i = 0; i < 10; i++)
		{
			_keyPairs[i] = _keyGen.generateKeyPair();
		}
		_rnd = new Random();
	}
	
	public void load()
	{
		_gameServerList = new FastList<Integer>();
		serverNames =  new FastMap<Integer, String>();
		loadServerNames();
		java.sql.Connection con = null;
		PreparedStatement statement = null;
		int id = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				id = rset.getInt("server_id");
				_gameServerList.add(id);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{ 
				con.close();
				statement.close();
			} 
			catch (Exception e2) 
			{
				//not here
			}
		}
	}
	
	private void loadServerNames()
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream("servername.xml");
			XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
			xpp.setInput(new UTF8StreamReader().setInput(in));
			for (int e = xpp.getEventType(); e != XMLStreamReaderImpl.END_DOCUMENT; e = xpp.next())
			{
				if (e == XMLStreamReaderImpl.START_ELEMENT)
				{
					if(xpp.getLocalName().toString().equals("server"))
					{
						Integer id = new Integer(xpp.getAttributeValue(null,"id").toString());
						String name = xpp.getAttributeValue(null,"name").toString();
						serverNames.put(id,name);
					}
				}
			}
		}
		catch (FileNotFoundException e)
		{
            System.out.println("servername.xml could not be loaded : file not found");
		}
		catch (XMLStreamException xppe)
		{
			xppe.printStackTrace();
		}
		finally
		{
			try 
			{ 
				in.close(); 
			} 
			catch (Exception e) 
			{
				//not here
			}
		}
	}
	
	public boolean isIDfree(int id)
	{
		java.sql.Connection con = null;
		PreparedStatement statement = null;
		
		int DBid;
		String DBhexid;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery();
			
			while(rset.next())
			{
				DBid = rset.getInt("server_id");
				DBhexid = rset.getString("hexid");
				
				if(DBid == id && DBhexid.equals(null))
					return false;
			}
			
			return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{ 
				con.close();
				statement.close();
			} 
			catch (Exception e2) 
			{
				//not here
			}
		}
		return true;
	}
	
	public void createServer(byte[] hexid, int id, String host)
	{
		java.sql.Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			statement.setString(1, hexToString(hexid));
			statement.setInt(2, id);
			statement.setString(3, host);
			statement.executeUpdate();
			statement.close();
		}
		catch(SQLException e)
		{
			System.out.println("SQL error while saving gameserver :"+e);
		}
		finally
		{
			try 
			{ 
				con.close();
				statement.close();
			} 
			catch (Exception e2) 
			{
				//not here
			}
		}
	}
	
	private String hexToString(byte[] hex)
	{
		if(hex == null)
			return "null";
		
		return new BigInteger(hex).toString(16);
	}
	
}