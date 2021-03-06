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

import com.l2scoria.gameserver.model.L2NpcWalkerNode;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * 
 * @author Rayan RPG for L2Emu Project
 * @author ProGramMoS
 * @since 927
 */
public class NpcWalkerRoutesTable
{
	private final static Logger _log = Logger.getLogger(NpcWalkerRoutesTable.class.getName());

	private static NpcWalkerRoutesTable _instance;

	private FastList<L2NpcWalkerNode> _routes;

	public static NpcWalkerRoutesTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new NpcWalkerRoutesTable();
			_log.info("Initializing Walkers Routes Table.");
		}

		return _instance;
	}

	private NpcWalkerRoutesTable()
	{
	//not here
	}

	public void load()
	{
		_routes = new FastList<L2NpcWalkerNode>();
		//java.sql.Connection con = null;

		LineNumberReader lnr = null;

		try
		{
			File fileData = new File("./data/csv/walker_routes.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(fileData)));
			L2NpcWalkerNode route;
			String line = null;

			//format:
			//  route_id;npc_id;move_point;chatText;move_x;move_y;move_z;delay;running
			while((line = lnr.readLine()) != null)
			{
				//ignore comments
				if(line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				route = new L2NpcWalkerNode();
				StringTokenizer st = new StringTokenizer(line, ";");

				int route_id = Integer.parseInt(st.nextToken());
				int npc_id = Integer.parseInt(st.nextToken());
				String move_point = st.nextToken();
				String chatText = st.nextToken();
				int move_x = Integer.parseInt(st.nextToken());
				int move_y = Integer.parseInt(st.nextToken());
				int move_z = Integer.parseInt(st.nextToken());
				int delay = Integer.parseInt(st.nextToken());
				boolean running = Boolean.parseBoolean(st.nextToken());

				route.setRouteId(route_id);
				route.setNpcId(npc_id);
				route.setMovePoint(move_point);
				route.setChatText(chatText);
				route.setMoveX(move_x);
				route.setMoveY(move_y);
				route.setMoveZ(move_z);
				route.setDelay(delay);
				route.setRunning(running);

				_routes.add(route);
				route = null;
			}

			_log.info("WalkerRoutesTable: Loaded " + _routes.size() + " Npc Walker Routes.");

		}
		catch(FileNotFoundException e)
		{
			_log.warn("walker_routes.csv is missing in data folder");
		}
		catch(IOException e0)
		{
			_log.warn("Error while creating table: " + e0.getMessage() + "\n" + e0);
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
	}

	public FastList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		FastList<L2NpcWalkerNode> _return = new FastList<L2NpcWalkerNode>();

		for(FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;)
		{
			if(n.getValue().getNpcId() == id)
			{
				_return.add(n.getValue());
			}
		}

		return _return;
	}
}
