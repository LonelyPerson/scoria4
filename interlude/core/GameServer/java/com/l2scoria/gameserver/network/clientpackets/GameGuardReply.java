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
package com.l2scoria.gameserver.network.clientpackets;

import ru.catssoftware.protection.CatsGuard;

/**
 * @author zabbix Lets drink to code! Unknown Packet:ca 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00
 *         E....7..........
 */

public class GameGuardReply extends L2GameClientPacket
{
	private static final String	_C__CA_GAMEGUARDREPLY	= "[C] CA GameGuardReply";


	private int[]				_reply					= new int[4];

	@Override
	protected void readImpl()
	{
		if(CatsGuard.getInstance().isEnabled() && getClient().getHWId()==null) {
			_reply[0] = readD();
			_reply[1] = readD();
			_reply[2] = readD();
			_reply[3] = readD();
		} else {
			byte [] b = new byte[getByteBuffer().remaining()];
			readB(b);
		}
			
	}

	@Override
	protected void runImpl()
	{
		if(CatsGuard.getInstance().isEnabled()) 
			CatsGuard.getInstance().initSession(getClient(), _reply);
	}

	@Override
	public String getType()
	{
		return _C__CA_GAMEGUARDREPLY;
	}
}
