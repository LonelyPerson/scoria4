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

import java.util.Vector;

/**
 * @author Dezmond_snz Format: cdddsdd
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private static final String _S__ED_CONFIRMDLG = "[S] ed ConfirmDlg";
	private int _messageId = 0;
	private int _requesterId = 0;
	private int _time = 25000;

	private Vector<String> _values = new Vector<String>();

	public ConfirmDlg(int messageId)
	{
		_messageId = messageId;
	}

	public ConfirmDlg addString(String text)
	{
		_values.add(text);
		return this;
	}
	
	public ConfirmDlg addZoneName(int x, int y, int z)
	{
		return this;
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		/*
		 * Пакет с подтверждением при ресуректе:
		 * "Nemu is making an attempt at resurrection. Do you want to continue with this resurrection?"
		 * ED //C
		 * E6 05 00 00 - Номер системного сообщения int 1510 "$s1 is making an attempt at resurrection. Do you want to continue with this resurrection?"
		 * 02 00 00 00 00 00 00 - Размер "прикреплений" ($S1, $S2, $S3, ...)
		 * 00 - unknown
		 * 00 4E 00 65 00 6D 00 75 00 00 - $S1 (custom string), в данном случае Nemu
		 * 00 06 00 00 - Время ответа на диалог, требуется указать в конструкторе
		 * 00 - id, требуется указать в конструкторе присвоение
		 */
		writeC(0xed); //ED
		writeD(_messageId); //id system message
		writeD(_values.size()); // size custom
		writeD(0x00); // unknown
		for(int i = 0; i < _values.size(); i++)
		{
			writeS(_values.get(i));
		}
		writeD(_time); // time
		writeD(_requesterId); // id?
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__ED_CONFIRMDLG;
	}
}
