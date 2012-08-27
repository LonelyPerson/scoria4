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
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.InventoryUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Item;

import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.3.2.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUnEquipItem extends L2GameClientPacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
	private static Logger _log = Logger.getLogger(RequestUnEquipItem.class.getName());

	// cd
	private int _slot;

	/**
	 * packet type id 0x11 format: cd
	 * 
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}

	@Override
	protected void runImpl()
	{
		if(Config.DEBUG)
		{
			_log.fine("request unequip slot " + _slot);
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar._inWorld = true;

		L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		if(item == null)
			return;

		if(item.isWear())
			// Wear-items are not to be unequipped
			return;

		// Prevent of unequiping a cursed weapon
		if(_slot == L2Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquiped())
			// Message ?
			return;

		// Prevent player from unequipping items in special conditions
		if(activeChar.isStunned() || activeChar.isConfused() || activeChar.isAway() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			activeChar.sendMessage("Your status does not allow you to do that.");
			return;
		}

		if(activeChar.isCastingNow())
			return;

		if(activeChar.isAttackingNow() && (_slot == L2Item.SLOT_LR_HAND || _slot == L2Item.SLOT_R_HAND))
			return;

		if (activeChar.isFightingInEvent() && activeChar.getEventName().equals("CTF") && activeChar._CTFHaveFlagOfTeam > 0)
		{
			return;
		}

		// Remove augmentation bonus
		if(item.isAugmented())
		{
			item.getAugmentation().removeBoni(activeChar);
		}

		L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);

		// show the update in the inventory
		InventoryUpdate iu = new InventoryUpdate();

		for(L2ItemInstance element : unequiped)
		{
			activeChar.checkSSMatch(null, element);

			iu.addModifiedItem(element);
		}

		activeChar.sendPacket(iu);

		activeChar.broadcastUserInfo();

		// this can be 0 if the user pressed the right mouse button twice very fast
		if(unequiped.length > 0)
		{

			SystemMessage sm = null;
			if(unequiped[0].getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0].getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0].getItemId());
			}
			activeChar.sendPacket(sm);
			sm = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}
