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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.templates.L2Item;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items
 * with respective id, if num is not specified, assumes 1.
 * 
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class CreateItem extends Admin
{
	public CreateItem()
	{
		_commands = new String[]{"admin_itemcreate", "admin_create_item"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.equals("admin_itemcreate"))
		{
			HelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		else if(command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);

				if(st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					createItem(activeChar, idval, numval);
				}
				else if(st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, idval, 1);
				}

				val = null;
				st = null;
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //itemcreate <itemId> [amount]");
			}
			catch(NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}

			HelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		return true;
	}

	private void createItem(L2PcInstance activeChar, int id, int num)
	{
		if(num > 20)
		{
			L2Item template = ItemTable.getInstance().getTemplate(id);

			if(!template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}

			template = null;
		}

		L2PcInstance player = null;

		if(activeChar.getTarget() != null)
		{
			if(activeChar.getTarget().isPlayer)
			{
				player = (L2PcInstance) activeChar.getTarget();
			}
			else
			{
				activeChar.sendMessage("You can add an item only to a character.");
				return;
			}
		}

		if(player == null)
		{
			activeChar.setTarget(activeChar);
			player = activeChar;
		}

		player.getInventory().addItem("Admin", id, num, player, null);
		ItemList il = new ItemList(player, true);
		player.sendPacket(il);
		if(activeChar == player)
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		else
		{
			activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in " + player.getName() + "'s inventory.");
			player.sendMessage("Admin have spawned " + num + " item(s) number " + id + " in your inventory.");
		}

		player = null;
		il = null;
	}
}
