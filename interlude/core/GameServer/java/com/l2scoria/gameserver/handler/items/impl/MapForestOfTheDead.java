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

import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;

public class MapForestOfTheDead extends ItemAbst
{
	public MapForestOfTheDead()
	{
		_items = new int[]{7063};

		_playerUseOnly = true;
		_notInObservationMode = true;
	}

	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		playable.sendPacket(html);
		return true;
	}

	private static NpcHtmlMessage html = new NpcHtmlMessage(5);
	static
	{
		StringBuilder map = new StringBuilder("<html><title>Map: Forest of the Dead</title>");
		map.append("<body>");
		map.append("<br>");
		map.append("Map:");
		map.append("<br>");
		map.append("<table>");
		map.append("<tr><td>");
		map.append("<img src=\"icon.Quest_deadperson_forest_t00\" width=255 height=255>");
		map.append("</td></tr>");
		map.append("</table>");
		map.append("</body></html>");
		html.setHtml(map.toString());
	}
}
