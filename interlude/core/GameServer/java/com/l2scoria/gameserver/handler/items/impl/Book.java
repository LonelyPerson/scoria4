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

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.RadarControl;
import com.l2scoria.gameserver.network.serverpackets.ShowMiniMap;

public class Book extends ItemAbst
{
	public Book()
	{
		_items = new int[]{5588, 6317, 7561, 7064, 7082, 7083, 7084, 7085, 7086, 7087, 7088, 7089, 7090, 7091, 7092, 7093, 7094, 7095, 7096, 7097, 7098, 7099, 7100, 7101, 7102, 7103, 7104, 7105, 7106, 7107, 7108, 7109, 7110, 7111, 7112};
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!super.useItem(playable, item))
		{
			return false;
		}

		if (!(playable.isPlayer))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getPlayer();
		final int itemId = item.getItemId();

		String filename = "data/html/help/" + itemId + ".htm";
		String content = HtmCache.getInstance().getHtm(filename);

		// Quest item: Lidia's diary
		if (itemId == 7064)
		{
			activeChar.sendPacket(ShowMiniMap.STATIC_PACKET);
			activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
		}

		if (content == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><body>My Text is missing:<br>" + filename + "</body></html>");
			activeChar.sendPacket(html);
		}
		else
		{
			NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
			itemReply.setHtml(content);
			activeChar.sendPacket(itemReply);
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}
}
