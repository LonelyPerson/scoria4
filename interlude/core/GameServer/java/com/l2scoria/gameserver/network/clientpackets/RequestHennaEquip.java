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
import com.l2scoria.gameserver.datatables.csv.HennaTable;
import com.l2scoria.gameserver.datatables.sql.HennaTreeTable;
import com.l2scoria.gameserver.model.actor.instance.L2HennaInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Henna;
import com.l2scoria.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public final class RequestHennaEquip extends L2GameClientPacket
{
	private static final String _C__BC_RequestHennaEquip = "[C] bc RequestHennaEquip";
	//private static Logger _log = Logger.getLogger(RequestHennaEquip.class.getName());
	private int _symbolId;

	// format  cd

	/**
	 * packet type id 0xbb format: cd
	 * 
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);

		if(template == null)
			return;

		L2HennaInstance temp = new L2HennaInstance(template);
		int _count = 0;

		/* Prevents henna drawing exploit:
			1) talk to L2SymbolMakerInstance
			2) RequestHennaList
			3) Don't close the window and go to a GrandMaster and change your subclass
			4) Get SymbolMaker range again and press draw
		   You could draw any kind of henna just having the required subclass...
		 */
		boolean cheater = true;

		for(L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId()))
		{
			if(h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}
		}

		try
		{
			_count = activeChar.getInventory().getItemByItemId(temp.getItemIdDye()).getCount();
		}
		catch(Exception e)
		{}

		if(!cheater && _count >= temp.getAmountDyeRequire() && activeChar.getAdena() >= temp.getPrice() && activeChar.addHenna(temp))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addNumber(temp.getItemIdDye());
			activeChar.sendPacket(sm);
			sm = null;
			activeChar.sendPacket(new SystemMessage(SystemMessageId.SYMBOL_ADDED));

			activeChar.getInventory().reduceAdena("Henna", temp.getPrice(), activeChar, activeChar.getLastFolkNPC());
			activeChar.getInventory().destroyItemByItemId("Henna", temp.getItemIdDye(), temp.getAmountDyeRequire(), activeChar, activeChar.getLastFolkNPC());

			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_DRAW_SYMBOL));

			if(!activeChar.isGM() && cheater)
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__BC_RequestHennaEquip;
	}
}
