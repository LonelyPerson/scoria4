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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.network.L2GameClient;
import mmo.SendablePacket;
import org.apache.log4j.Logger;


/**
 * @author ProGramMoS
 */

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GameServerPacket.class);

	/**
	 * @see com.l2jserver.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected void write()
	{
		try
		{
			getClient().getActiveChar();
			writeImpl();
			writeImpl(getClient());
		}
		catch(Throwable t)
		{
			_log.fatal("Client: " + getClient().toString() + " - Failed writing: " + getType() + " - L2J Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
			t.printStackTrace();
		}
	}

	public void runImpl()
	{

	}

	protected void writeImpl() {}

	protected void writeImpl(L2GameClient client) {}

	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}
