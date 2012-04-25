package com.l2scoria.gameserver.powerpak.Buffer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javolution.util.FastMap;

import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import java.sql.Connection;
import com.l2scoria.util.database.L2DatabaseFactory;

public class BuffTable
{
	public class Buff
	{
		public int _skillId;
		public int _skillLevel;
		public boolean _force;
		public int _minLevel;
		public int _maxLevel;
		public int _price;
		public int _id;

		public Buff(ResultSet r) throws SQLException
		{
			_skillId = r.getInt(2);
			_skillLevel = r.getInt(3);
			_force = r.getInt(4) == 1;
			_minLevel = r.getInt(5);
			_maxLevel = r.getInt(6);
			_price = r.getInt(7);
			_id = r.getInt(8);
			if(_price == -1)
			{
				_price = PowerPakConfig.BUFFER_PRICE;
			}

		}
	}

	private static BuffTable _instance = null;
	private Map<String, ArrayList<Buff>> _buffs;

	public static BuffTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new BuffTable();
		}
		return _instance;
	}

	private BuffTable()
	{
		_buffs = new FastMap<String, ArrayList<Buff>>();
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("select name,skill_id,skill_level,skill_force,char_min_level,char_max_level,price_adena,id from buff_templates");
			ResultSet rs = stm.executeQuery();
			while(rs.next())
			{
				if(_buffs.get(rs.getString(1)) == null)
				{
					_buffs.put(rs.getString(1), new ArrayList<Buff>());
				}
				ArrayList<Buff> a = _buffs.get(rs.getString(1));
				a.add(new Buff(rs));
			}
			rs.close();
			stm.close();
			try { con.close(); } catch(Exception e) { }
			System.out.println("...Loaded " + _buffs.size() + " buff templates");
		}
		catch(Exception e)
		{
			System.out.println("...Error while loading buffs. Please, check buff_templates table");
		}
	}

	public ArrayList<Buff> getBuffsForName(String name)
	{
		return _buffs.get(name);
	}

	public ArrayList<Buff> getBuffsForId(ArrayList<Integer> IDs)
	{
		ArrayList<Buff> rt = new ArrayList<Buff>();

		for (Map.Entry<String, ArrayList<Buff>> elem : _buffs.entrySet())
		{
			ArrayList<Buff> data = elem.getValue();
			for (Buff buff : data)
			{
				if (IDs.contains(buff._id))
					rt.add(buff);
			}
		}
		return rt;
	}
}
