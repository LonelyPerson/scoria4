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
package com.l2scoria.gameserver.model.zone;

import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.network.serverpackets.L2GameServerPacket;
import javolution.util.FastMap;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Abstract base class for any zone type Handles basic operations
 *
 * @author durgus
 */
public class L2ZoneDefault extends L2Zone
{
	private final int _id;
	protected L2ZoneForm _zone;
	protected FastMap<Integer, L2Character> _characterList;

	public final static int		REASON_OK					= 0;
	public final static int		REASON_MULTIPLE_INSTANCE	= 1;
	public final static int		REASON_INSTANCE_FULL		= 2;
	public final static int		REASON_SMALL_GROUP			= 3;

	// Инстансы
	protected String _instanceName;
	protected String _instanceGroup;
	protected int _minPlayers;
	protected int _maxPlayers;

	private static class InstanceResult
	{
		public int instanceId = 0;
		public int reason = REASON_OK;
	}


	/**
	 * Parameters to affect specific characters
	 */
	private boolean _checkAffected;

	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;

	protected L2ZoneDefault(int id)
	{
		_id = id;
		_characterList = new FastMap<Integer, L2Character>().setShared(true);

		_checkAffected = false;

		_minLvl = 0;
		_maxLvl = 0xFF;

		_classType = 0;

		_race = null;
		_class = null;
	}

	public int getId()
	{
		return _id;
	}

	/**
	 * Setup new parameters for this zone
	 *
	 * @param type
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;

		// Minimum leve
		if (name.equals("affectedLvlMin"))
		{
			_minLvl = Integer.parseInt(value);
		}
		// Maximum level
		else if (name.equals("affectedLvlMax"))
		{
			_maxLvl = Integer.parseInt(value);
		}
		// Affected Races
		else if (name.equals("affectedRace"))
		{
			// Create a new array holding the affected race
			if (_race == null)
			{
				_race = new int[1];
				_race[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_race.length + 1];

				int i = 0;

				for (; i < _race.length; i++)
				{
					temp[i] = _race[i];
				}

				temp[i] = Integer.parseInt(value);

				_race = temp;
			}
		}
		// Affected classes
		else if (name.equals("affectedClassId"))
		{
			// Create a new array holding the affected classIds
			if (_class == null)
			{
				_class = new int[1];
				_class[0] = Integer.parseInt(value);
			}
			else
			{
				int[] temp = new int[_class.length + 1];

				int i = 0;

				for (; i < _class.length; i++)
				{
					temp[i] = _class[i];
				}

				temp[i] = Integer.parseInt(value);

				_class = temp;
			}
		}
		// Affected class type
		else if (name.equals("affectedClassType"))
		{
			if (value.equals("Fighter"))
			{
				_classType = 1;
			}
			else
			{
				_classType = 2;
			}
		}
	}

	public void setInstanceData(Node n)
	{
		Node instanceName = n.getAttributes().getNamedItem("instanceName");
		Node instanceGroup = n.getAttributes().getNamedItem("instanceGroup");
		Node minPlayers = n.getAttributes().getNamedItem("minPlayers");
		Node maxPlayers = n.getAttributes().getNamedItem("maxPlayers");

		_instanceName = (instanceName != null) ? instanceName.getNodeValue() : null;
		_instanceGroup = (instanceGroup != null) ? instanceGroup.getNodeValue().toLowerCase() : null;
		_minPlayers = (minPlayers != null) ? Integer.parseInt(minPlayers.getNodeValue()) : -1;
		_maxPlayers = (maxPlayers != null) ? Integer.parseInt(maxPlayers.getNodeValue()) : -1;
	}

	public void setSpawnLocs(Node node1)
	{
	}

	/**
	 * Checks if the given character is affected by this zone
	 *
	 * @param character
	 * @return
	 */
	private boolean isAffected(L2Character character)
	{
		// Check lvl
		if (character.getLevel() < _minLvl || character.getLevel() > _maxLvl)
		{
			return false;
		}

		if (character.isPlayer)
		{
			// Check class type
			if (_classType != 0)
			{
				if (((L2PcInstance) character).isMageClass())
				{
					if (_classType == 1)
					{
						return false;
					}
				}
				else if (_classType == 2)
				{
					return false;
				}
			}

			// Check race
			if (_race != null)
			{
				boolean ok = false;

				for (int element : _race)
				{
					if (((L2PcInstance) character).getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}

				if (!ok)
				{
					return false;
				}
			}

			// Check class
			if (_class != null)
			{
				boolean ok = false;

				for (int clas : _class)
				{
					if (((L2PcInstance) character).getClassId().ordinal() == clas)
					{
						ok = true;
						break;
					}
				}

				if (!ok)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Set the zone for this L2ZoneType Instance
	 *
	 * @param zone
	 */
	public void setZone(L2ZoneForm zone)
	{
		_zone = zone;
	}

	/**
	 * Returns this zones zone form
	 *
	 * @param zone
	 * @return
	 */
	public L2ZoneForm getZone()
	{
		return _zone;
	}

	/**
	 * Checks if the given coordinates are within the zone
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}

	/**
	 * Checks if the given obejct is inside the zone.
	 *
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return object != null && _zone.isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public double getDistanceToZone(int x, int y)
	{
		return _zone.getDistanceToZone(x, y);
	}

	public double getDistanceToZone(L2Object object)
	{
		return _zone.getDistanceToZone(object.getX(), object.getY());
	}

	public void revalidateInZone(L2Character character, boolean enter)
	{
		// If the character cant be affected by this zone return
		if (_checkAffected)
		{
			if (!isAffected(character))
			{
				return;
			}
		}

		// If the object is inside the zone...
		if (_zone.isInsideZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.containsKey(character.getObjectId()) && enter)
			{
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (_characterList.containsKey(character.getObjectId()) && !enter)
			{
				_characterList.remove(character.getObjectId());
				onExit(character);
			}
		}
	}

	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 *
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			onExit(character);
		}
	}

	/**
	 * Will scan the zones char list for the character
	 *
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}

	public FastMap<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}

	public FastMap<Integer, L2PcInstance> getPlayersInside()
	{
		if (_characterList.isEmpty())
		{
			return null;
		}

		FastMap<Integer, L2PcInstance> playerList = new FastMap<Integer, L2PcInstance>().setShared(true);
		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}

			if (character.isPlayer)
			{
				playerList.put(character.getObjectId(), (L2PcInstance) character);
			}
		}
		return playerList;
	}

	/**
	 * Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (_characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : _characterList.values())
		{
			if (character != null && character.isPlayer)
			{
				character.sendPacket(packet);
			}
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_instanceName != null && _instanceGroup != null && character.isPlayer)
		{
			L2PcInstance pl = (L2PcInstance) character;
			InstanceResult ir = new InstanceResult();

			if (_instanceGroup.equals("party"))
			{
				if (pl.isInParty())
				{
					List<L2PcInstance> list = pl.getParty().getPartyMembers();
					getInstanceFromGroup(ir, list, false);
					checkPlayersInside(ir, list);
				}
			}
			else if (_instanceGroup.equals("clan"))
			{
				if (pl.getClan() != null)
				{
					List<L2PcInstance> list = pl.getClan().getOnlineMembers();
					getInstanceFromGroup(ir, list, true);
					checkPlayersInside(ir, list);
				}
			}
			else if (_instanceGroup.equals("alliance"))
			{
				if (pl.getAllyId() > 0)
				{
					List<L2PcInstance> list = pl.getClan().getOnlineAllyMembers();
					getInstanceFromGroup(ir, list, true);
					checkPlayersInside(ir, list);
				}
			}

			if (ir.reason == REASON_MULTIPLE_INSTANCE)
			{
				pl.sendMessage("You cannot enter this instance while other " + _instanceGroup + " members are in another instance.");
			}
			else if (ir.reason == REASON_INSTANCE_FULL)
			{
				pl.sendMessage("This instance is full. There is a maximum of " + _maxPlayers + " players inside.");
			}
			else if (ir.reason == REASON_SMALL_GROUP)
			{
				pl.sendMessage("Your " + _instanceGroup + " is too small. There is a minimum of " + _minPlayers + " players inside.");
			}
			else
			{
				try
				{
					if (ir.instanceId == 0)
					{
						ir.instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceName);
					}
					portIntoInstance(pl, ir.instanceId);
				} catch (Exception e)
				{
					pl.sendMessage("Выбарнный инстанс не может быть создан.");
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer && _instanceName != null && character.getInstanceId() > 0)
			portIntoInstance((L2PcInstance) character, 0);
	}

	@Override
	protected void onReviveInside(L2Character character)
	{}

	@Override
	protected void onDieInside(L2Character character)
	{}

	private void getInstanceFromGroup(InstanceResult ir, List<L2PcInstance> group, boolean allowMultiple)
	{
		for (L2PcInstance mem : group)
		{
			if (mem == null || mem.getInstanceId() == 0)
				continue;

			Instance i = InstanceManager.getInstance().getInstance(mem.getInstanceId());
			if (i.getName().equals(_instanceName))
			{
				ir.instanceId = i.getId(); // Player in this instance template found
				return;
			}
			else if (!allowMultiple)
			{
				ir.reason = REASON_MULTIPLE_INSTANCE;
				return;
			}
		}
	}

	private void checkPlayersInside(InstanceResult ir, List<L2PcInstance> group)
	{
		if (ir.reason != REASON_OK)
			return;

		int valid = 0, all = 0;

		for (L2PcInstance mem : group)
		{
			if (mem != null && mem.getInstanceId() == ir.instanceId)
				valid++;
			all++;

			if (valid == _maxPlayers)
			{
				ir.reason = REASON_INSTANCE_FULL;
				return;
			}
		}
		if (all < _minPlayers)
			ir.reason = REASON_SMALL_GROUP;
	}

	private void portIntoInstance(L2PcInstance pl, int instanceId)
	{
		pl.setInstanceId(instanceId);
		pl.getKnownList().updateKnownObjects();
		L2Summon pet = pl.getPet();

		if (pet != null)
		{
			pet.setInstanceId(instanceId);
			pet.getKnownList().updateKnownObjects();
		}
	}
}
