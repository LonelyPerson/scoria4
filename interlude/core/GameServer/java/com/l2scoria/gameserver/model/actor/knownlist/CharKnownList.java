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
package com.l2scoria.gameserver.model.actor.knownlist;

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.util.Util;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Collection;
import java.util.Map;

public class CharKnownList extends ObjectKnownList
{
	// =========================================================
	// Data Field
	private Map<Integer, L2PcInstance> _knownPlayers;
	private Map<Integer, Integer> _knownRelations;

	// =========================================================
	// Constructor
	public CharKnownList(L2Character activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return addKnownObject(object, null);
	}

	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if(!super.addKnownObject(object, dropper))
			return false;

		// instance -1 for gms can see everything on all instances
		if (getActiveChar().getInstanceId() != -1 && getActiveChar().getInstanceId() != object.getInstanceId())
			return false;

		if(object.isPlayer)
		{
			getKnownPlayers().put(object.getObjectId(), (L2PcInstance) object);
			getKnownRelations().put(object.getObjectId(), -1);
		}

		return true;
	}

	/**
	 * Return True if the L2PcInstance is in _knownPlayer of the L2Character.<BR>
	 * <BR>
	 * 
	 * @param player The L2PcInstance to search in _knownPlayer
	 */
	public final boolean knowsThePlayer(L2PcInstance player)
	{
		return getActiveChar() == player || getKnownPlayers().containsKey(player.getObjectId());
	}

	/**
	 * Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify
	 * AI.
	 */
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();

		// Set _target of the L2Character to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);

		// Cancel AI Task
		if(getActiveChar().hasAI())
		{
			getActiveChar().setAI(null);
		}
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if(!super.removeKnownObject(object))
			return false;

		if(object.isPlayer)
		{
			getKnownPlayers().remove(object.getObjectId());
			getKnownRelations().remove(object.getObjectId());
		}
		// If object is targeted by the L2Character, cancel Attack or Cast
		if(object == getActiveChar().getTarget())
		{
			getActiveChar().setTarget(null);
		}

		return true;
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	public L2Character getActiveChar()
	{
		return (L2Character) super.getActiveObject();
	}

	public Collection<L2Character> getKnownCharacters()
	{
		FastList<L2Character> result = new FastList<L2Character>();

		for(L2Object obj : getKnownObjects().values())
		{
			if(obj != null && obj.isCharacter)
			{
				result.add((L2Character) obj);
			}
		}

		return result;
	}

	public Collection<L2Character> getKnownCharactersInRadius(long radius)
	{
		FastList<L2Character> result = new FastList<L2Character>();

		for(L2Object obj : getKnownObjects().values())
		{
			if(obj != null && obj.isCharacter)
			{
				if(Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2Character) obj);
				}
			}
		}

		return result;
	}

	public final Map<Integer, L2PcInstance> getKnownPlayers()
	{
		if(_knownPlayers == null)
		{
			_knownPlayers = new FastMap<Integer, L2PcInstance>().setShared(true);
		}

		return _knownPlayers;
	}

	public final Map<Integer, Integer> getKnownRelations()
	{
		if(_knownRelations == null)
		{
			_knownRelations = new FastMap<Integer, Integer>().setShared(true);
		}

		return _knownRelations;
	}

	public final Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
	{
		FastList<L2PcInstance> result = new FastList<L2PcInstance>();

		for(L2PcInstance player : getKnownPlayers().values())
			if(Util.checkIfInRange((int) radius, getActiveChar(), player, true))
			{
				result.add(player);
			}

		return result;
	}
}
