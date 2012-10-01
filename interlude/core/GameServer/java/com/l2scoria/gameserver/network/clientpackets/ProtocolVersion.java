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
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.serverpackets.KeyPacket;
import com.l2scoria.gameserver.network.serverpackets.SendStatus;
import org.apache.log4j.Logger;
import ru.catssoftware.protection.LameStub;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());

	private int _version;

	private byte[] _data;
	private byte[] _check;

	@Override
	protected void readImpl()
	{
		if(LameStub.ISLAME && _buf.remaining()>0x100)
		{
			_version = readD();
			_data = new byte[0x100]; 
			_check = new byte[4]; 
			readB(_data); 
			readB(_check); 
		}
		else
		{
			_version = readH();
		}
	}

	@Override
	protected void runImpl()
	{
            if(LameStub.ISLAME) 
		{
			L2GameClient client = this.getClient();
			if (_version == -2L || _version == 65534)
			{
				client.closeNow();
				return;
			}
			else if (_version == -3L || _version == 65533)
			{
				client.close(new SendStatus());
				//client.closeNow();
				return;
			}
			else if (_version == 746)
			{
				try
				{
					if(com.lameguard.LameGuard.getInstance().checkData(_data, _check))
					{
						com.lameguard.session.ClientSession cs = com.lameguard.LameGuard.getInstance().checkClient(com.lameguard.utils.Utils.findIP(client.toString()), _data);
						if(cs != null)
						{
							client.setHWID(cs.getHWID());

							// prepare answer
							byte[] key = client.enableCrypt();
							_data = com.lameguard.LameGuard.getInstance().assembleAnswer(cs, key);
							sendPacket(new KeyPacket(_data));
							return;
						}
					}
				}
				catch (Exception e)
				{

				}
			} else {
                        _log.warn("TEMPLARE DEBUG: unknown protocol version from readD() bytes => " + _version);
			client.close(KeyPacket.UNKNOWN_PROTOCOL_VERSION);
                        }
		}
		else if(_version == 65534 || _version == -2) //ping
		{
			if(Config.DEBUG)
			{
				_log.info("Ping received");
			}
			getClient().closeNow();
		}
		else if(_version == 65533 || _version == -3) //RWHO
		{
			if(Config.RWHO_LOG)
			{
				_log.info(getClient().toString() + " RWHO received");
			}
			getClient().close(new SendStatus());
		}
		else if(_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.info("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			_log.warn("Wrong Protocol Version " + _version);
			getClient().closeNow();
		}
		else
		{
			if(Config.DEBUG)
			{
				_log.info("Client Protocol Revision is ok: " + _version);
			}

			KeyPacket pk = new KeyPacket(getClient().enableCrypt());
			getClient().sendPacket(pk);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}
