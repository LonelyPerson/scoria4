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
package com.l2scoria.gameserver.network.serverpackets;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class KeyPacket extends L2GameServerPacket
{
	private static final String _S__01_KEYPACKET = "[S] 01 KeyPacket";
	public static final KeyPacket UNKNOWN_PROTOCOL_VERSION = new KeyPacket(null);

	private byte[] _key;
	public KeyPacket(byte[] key)
	{
		_key = key;
	}

	private byte[] _data;
	private boolean _isLame = false;
	public KeyPacket(byte[] data, boolean val)
	{
		_data = data;
		_isLame = val;
	}

	@Override
	public void writeImpl()
	{
		if(_isLame)
		{
			writeC(0x00);
			writeC(_data == null ? 0x00 : 0x01);

			if (_data != null)
			{
				writeB(_data);
				writeD(0x01);
				writeD(0x01);
			}

			return;
		} 
                else 
                {
                    writeC(0x00);
                    writeC(0x01);
                    writeB(_key);
                    writeD(0x01);
                    writeD(0x01);
                }
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__01_KEYPACKET;
	}

}
