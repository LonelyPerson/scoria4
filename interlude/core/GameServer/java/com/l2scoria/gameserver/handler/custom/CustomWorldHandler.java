/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.handler.custom;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.L2Rebirth;

/**
 *This will simply manage any custom 'Enter World callers' needed.<br>
 *Rather then having to add them to the core's. (yuck!)
 * 
 * @author JStar
 */
public class CustomWorldHandler
{

	private static CustomWorldHandler _instance = null;

	private CustomWorldHandler()
	{
	//Do Nothing ^_-
	}

	/** Receives the non-static instance of the RebirthManager. */
	public static CustomWorldHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new CustomWorldHandler();
		}

		return _instance;
	}

	/** Requests entry into the world - manages appropriately. */
	public void enterWorld(L2PcInstance player)
	{
		L2Rebirth.getInstance().grantRebirthSkills(player);//Rebirth Caller - if player has any skills, they will be granted them.
	}

	/** Requests removal from the world - manages appropriately. */
	public void exitWorld(L2PcInstance player)
	{
	//TODO: Remove the rebirth engine's bonus skills from player?
	}

}
