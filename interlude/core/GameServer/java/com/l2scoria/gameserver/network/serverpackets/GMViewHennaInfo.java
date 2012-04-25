/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.datatables.sql.HennaTreeTable;
import com.l2scoria.gameserver.model.actor.instance.L2HennaInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

public class GMViewHennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private L2HennaInstance[] _allhennas;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];
	private int _count;
	
	@Override
	public String getType()
	{
		return "[S] 0xEA GMHennaInfo";
	}
	
	public GMViewHennaInfo(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_allhennas = HennaTreeTable.getInstance().getAvailableHenna(_activeChar.getClassId());
		
		int j = 0;
		for (int i = 0; i < 3; i++)
		{
			L2HennaInstance h = _activeChar.getHennas(i + 1);
			if (h != null)
			{
				_hennas[j++] = h;
			}
		}
		_count = j;
	}
	
	
	/**
	 * @see com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xEA);
		
		writeC(_activeChar.getHennaStatINT());
		writeC(_activeChar.getHennaStatSTR());
		writeC(_activeChar.getHennaStatCON());
		writeC(_activeChar.getHennaStatMEN());
		writeC(_activeChar.getHennaStatDEX());
		writeC(_activeChar.getHennaStatWIT());
		writeD(3); // slots?
		writeD(_count); //size
		for (int i = 0; i < _count; i++)
		{
			byte show = 0;
			writeD(_hennas[i].getSymbolId());
			for(L2HennaInstance h : _allhennas)
			{
				if(h.getSymbolId() == _hennas[i].getSymbolId())
				{
					show = 1;
					break;
				}
			}
			writeD(show);
		}
	}
	
}
