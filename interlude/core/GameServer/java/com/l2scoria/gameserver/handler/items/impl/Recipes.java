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
package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.datatables.csv.RecipeTable;
import com.l2scoria.gameserver.model.L2RecipeList;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.5.2.5 $ $Date: 2005/04/06 16:13:51 $
 */

public class Recipes extends ItemAbst
{
	public Recipes()
	{
		RecipeTable rc = RecipeTable.getInstance();
		_items = new int[rc.getRecipesCount()];
		for(int i = 0; i < rc.getRecipesCount(); i++)
		{
			_items[i] = rc.getRecipeList(i).getRecipeId();
		}

		_playerUseOnly = true;
		_notInObservationMode = true;
		_notOnOlympiad = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		L2RecipeList rp = RecipeTable.getInstance().getRecipeByItemId(item.getItemId());
		if(activeChar.hasRecipeList(rp.getId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED));
			return false;
		}

		if(rp.isDwarvenRecipe())
		{
			if(activeChar.hasDwarvenCraft())
			{
				if(rp.getLevel() > activeChar.getDwarvenCraft())
				{
					//can't add recipe, becouse create item level too low
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
					return false;
				}

				if(activeChar.getDwarvenRecipeBook().length >= activeChar.GetDwarfRecipeLimit())
				{
					//Up to $s1 recipes can be registered.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.GetDwarfRecipeLimit()));
					return false;
				}

				activeChar.registerDwarvenRecipeList(rp);
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendMessage("Added recipe \"" + rp.getRecipeName() + "\" to Dwarven recipe book");
				return true;
			}

			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
		}
		else
		{
			if(activeChar.hasCommonCraft())
			{
				if(rp.getLevel() > activeChar.getCommonCraft())
				{
					//can't add recipe, becouse create item level too low
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
				   	return false;
				}

				if(activeChar.getCommonRecipeBook().length >= activeChar.GetCommonRecipeLimit())
				{
					//Up to $s1 recipes can be registered.
					activeChar.sendPacket(new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.GetCommonRecipeLimit()));
				    return false;
				}

				activeChar.registerCommonRecipeList(rp);
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendMessage("Added recipe \"" + rp.getRecipeName() + "\" to common recipe book");
				return true;
			}

			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
		}

		return false;
	}
}
