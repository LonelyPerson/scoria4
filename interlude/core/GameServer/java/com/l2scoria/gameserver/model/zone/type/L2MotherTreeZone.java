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

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.base.Race;
import com.l2scoria.gameserver.model.zone.L2ZoneDefault;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * A mother-trees zone
 *
 * @author durgus
 */
public class L2MotherTreeZone extends L2ZoneDefault
{
	public L2MotherTreeZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) character;

			boolean effect = true;
			if (player.isInParty())
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					if (member.getRace() != Race.elf)
					{
						effect = false;
						break;
					}
				}
			}

			if(effect)
			{
				player.setInsideZone(L2Character.ZONE_MOTHERTREE, true);
				player.sendPacket(new SystemMessage(SystemMessageId.ENTER_SHADOW_MOTHER_TREE));
				player = null;
			}
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer && character.isInsideZone(L2Character.ZONE_MOTHERTREE))
		{
			character.setInsideZone(L2Character.ZONE_MOTHERTREE, false);
			character.sendPacket(new SystemMessage(SystemMessageId.EXIT_SHADOW_MOTHER_TREE));
		}

		super.onExit(character);
	}

}
