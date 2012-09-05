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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PetInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.util.Util;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestGetItemFromPet extends L2GameClientPacket
{
	private static final String REQUESTGETITEMFROMPET__C__8C = "[C] 8C RequestGetItemFromPet";
	private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());

	private int _objectId;
	private int _amount;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
		/*_unknown = */readD();// = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null || player.getPet() == null || !(player.getPet() instanceof L2PetInstance))
			return;

		L2PetInstance pet = (L2PetInstance) player.getPet();

		if(player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit , Ban This Player!", Config.DEFAULT_PUNISH);
			return;
		}

		if(_amount < 0)
		{
			player.setAccessLevel(-1);
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if(_amount == 0)
			return;

		if(player.getDistanceSq(pet) > 40000) // 200*200
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			_log.warn("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
		player.sendPacket(new ItemList(player, true));
	}

	@Override
	public String getType()
	{
		return REQUESTGETITEMFROMPET__C__8C;
	}
}
