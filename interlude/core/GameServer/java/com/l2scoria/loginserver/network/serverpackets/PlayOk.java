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
package com.l2scoria.loginserver.network.serverpackets;

import java.io.IOException;

import com.l2scoria.Config;
import com.l2scoria.loginserver.L2LoginServer;
import com.l2scoria.loginserver.SessionKey;
import com.l2scoria.loginserver.network.loginserverpackets.ClientConnected;

/**
 *
 */
public final class PlayOk extends L2LoginServerPacket
{
	private int _playOk1, _playOk2, _id;

	public PlayOk(SessionKey sessionKey, int id)
	{
		_playOk1 = sessionKey.playOkID1;
		_playOk2 = sessionKey.playOkID2;
		_id = id;
	}

	/**
	 * @see com.l2jserver.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected void write()
	{
		try
		{
			if(Config.DDOS_PROTECTION_ENABLED)
			{
				switch (Config.ON_SUCCESS_LOGIN_ACTION)
				{
					case COMMAND:
						if(Config.ON_SUCCESS_LOGIN_COMMAND_LS.length()!=0)
						{
							Runtime.getRuntime().exec(Config.ON_SUCCESS_LOGIN_COMMAND_LS.replace("%ip%", getClient().getIntetAddress()));
						}
						break;
					case NOTIFY:
						if(L2LoginServer.getInstance().getGameServerListener().getGameServer(_id).getGameServerInfo().isDDoSEnabled())
						{
							L2LoginServer.getInstance().getGameServerListener().getGameServer(_id).sendPacket(new ClientConnected(getClient()));
						}
						break;
				}
			}
		}
		catch(IOException e) {}

		writeC(0x07);
		writeD(_playOk1);
		writeD(_playOk2);
	}
}
