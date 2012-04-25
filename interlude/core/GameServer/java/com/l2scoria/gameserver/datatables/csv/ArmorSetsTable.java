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
package com.l2scoria.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2ArmorSet;

/**
 * @author Luno
 */

public class ArmorSetsTable
{
	private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
	private static ArmorSetsTable _instance;

	public FastMap<Integer, L2ArmorSet> _armorSets;

	public static ArmorSetsTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new ArmorSetsTable();
		}

		return _instance;
	}

	private ArmorSetsTable()
	{
		_armorSets = new FastMap<Integer, L2ArmorSet>();
		loadData();
	}

	private void loadData()
	{
		LineNumberReader lnr = null;

		try
		{
			File fileData = new File("./data/csv/armorsets.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(fileData)));

			String line = null;

			//format:
			// id;chest;legs;head;gloves;feet;skill_id;shield;shield_skill_id;enchant6skill
			while((line = lnr.readLine()) != null)
			{
				//ignore comments
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}

				StringTokenizer st = new StringTokenizer(line, ";");

				st.nextToken();
				int chest = Integer.parseInt(st.nextToken());
				int legs = Integer.parseInt(st.nextToken());
				int head = Integer.parseInt(st.nextToken());
				int gloves = Integer.parseInt(st.nextToken());
				int feet = Integer.parseInt(st.nextToken());
				int skill_id = Integer.parseInt(st.nextToken());
				int shield = Integer.parseInt(st.nextToken());
				int shield_skill_id = Integer.parseInt(st.nextToken());
				int enchant6skill = Integer.parseInt(st.nextToken());

				_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
			}

			_log.config("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");

		}
		catch(FileNotFoundException e)
		{
			_log.warning("armorsets.csv is missing in data folder");
		}
		catch(IOException e0)
		{
			_log.warning("Error while creating table: " + e0.getMessage() + "\n" + e0);
		}
		finally
		{
			try
			{
				lnr.close();
				lnr = null;
			}
			catch(Exception e1)
			{
				//ignore
			}
		}
		if(Config.CUSTOM_ARMORSETS_TABLE)
		{
			
		}

	}

	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}

	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public void addObj(int v, L2ArmorSet s) {
		_armorSets.put(v, s);
	}
}
