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

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.Dice;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.Broadcast;
import com.l2scoria.gameserver.util.FloodProtector;
import com.l2scoria.util.random.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */

public class RollingDice extends ItemAbst
{
	public RollingDice()
	{
		_items = new int[]{4625, 4626, 4627, 4628};

		_playerUseOnly = true;
		_notOnOlympiad = true;
		_notInObservationMode = true;
		_notWhenSkillsDisabled = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();

		int number = rollDice(activeChar);
		if (number == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER));
			return false;
		}

		Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_ROLLED_S2);
		sm.addString(activeChar.getName());
		sm.addNumber(number);
		activeChar.sendPacket(sm);

		if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
		{
			Broadcast.toKnownPlayers(activeChar, sm);
		}
		else if (activeChar.isInParty())
		{
			activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
		}

		return true;
	}

	private int rollDice(L2PcInstance player)
	{
		// Check if the dice is ready
		if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_ROLLDICE))
		{
			return 0;
		}

		return Rnd.get(1, 6);
	}
}
