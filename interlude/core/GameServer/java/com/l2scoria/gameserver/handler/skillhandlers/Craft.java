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
package com.l2scoria.gameserver.handler.skillhandlers;

import com.l2scoria.gameserver.RecipeController;
import com.l2scoria.gameserver.handler.ISkillHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.FloodProtector;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Craft implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IItemHandler#useItem(com.l2scoria.gameserver.model.L2PcInstance, com.l2scoria.gameserver.model.L2ItemInstance)
	 */
	private static final SkillType[] SKILL_IDS = { SkillType.COMMON_CRAFT, SkillType.DWARVEN_CRAFT };

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IItemHandler#useItem(com.l2scoria.gameserver.model.L2PcInstance, com.l2scoria.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar == null || !(activeChar.isPlayer))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if(!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_CRAFT))
		{
			player.sendMessage("You Cannot Use Craft So Fast!");
			return;
		}

		if(player.getPrivateStoreType() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, (skill.getSkillType() == SkillType.DWARVEN_CRAFT) ? true : false);

		player = null;
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
