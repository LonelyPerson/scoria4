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
package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.util.random.Rnd;
import javolution.util.FastList;
import org.w3c.dom.Node;

/**
 * An arena
 * 
 * @author durgus
 */
public class L2ArenaZone extends L2ZoneDefault
{
//	private String _arenaName;
	private boolean _pvp;
	private FastList<int[]> _spawnLoc;

	public L2ArenaZone(int id)
	{
		super(id);

		_spawnLoc = new FastList<int[]>();

		_pvp = false;
	}

	@Override
	public void setParameter(String name, String value)
	{
//		if(name.equals("name"))
//		{
//			_arenaName = value;
//		}
		/*else if(name.equals("spawnX"))
		{
			_spawnLoc[0] = Integer.parseInt(value);
		}
		else if(name.equals("spawnY"))
		{
			_spawnLoc[1] = Integer.parseInt(value);
		}
		else if(name.equals("spawnZ"))
		{
			_spawnLoc[2] = Integer.parseInt(value);
		}
		else*/
		if(name.equals("pvp"))
		{
			_pvp = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	public void setSpawnLocs(Node node)
	{
		int ai[] = new int[3];

		Node node1 = node.getAttributes().getNamedItem("X");

		if(node1 != null)
		{
			ai[0] = Integer.parseInt(node1.getNodeValue());
		}

		node1 = node.getAttributes().getNamedItem("Y");

		if(node1 != null)
		{
			ai[1] = Integer.parseInt(node1.getNodeValue());
		}

		node1 = node.getAttributes().getNamedItem("Z");

		if(node1 != null)
		{
			ai[2] = Integer.parseInt(node1.getNodeValue());
		}

		_spawnLoc.add(ai);

		node1 = null;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (!_pvp)
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
		}

		if(character instanceof L2PcInstance)
		{
			character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (!_pvp)
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
		}

		if(character instanceof L2PcInstance)
		{
			character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}

		super.onExit(character);
	}

	public void oustAllPlayers()
	{
		if(_characterList == null)
			return;

		if(_characterList.isEmpty())
			return;

		for(L2Character character : _characterList.values())
		{
			if(character == null)
			{
				continue;
			}

			if(character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;

				if(player.isOnline() == 1)
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}

				player = null;
			}
		}
	}

	public final int[] getSpawnLoc()
	{
		int loc[] = new int[3];

		loc = _spawnLoc.get(Rnd.get(_spawnLoc.size()));

		return loc;
	}
}
