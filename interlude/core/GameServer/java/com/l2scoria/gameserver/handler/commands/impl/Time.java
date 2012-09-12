/* * This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.handler.commands.impl;

import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

public class Time extends CommandAbst
{
	public Time()
	{
		_commands = new int[]{77};
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if(!super.useUserCommand(id, activeChar))
		{
			return false;
		}

		int t = GameTimeController.getInstance().getGameTime();
		int h = t / 60 % 24;
		int m = t % 60;

		SystemMessage sm;
		sm = new SystemMessage(GameTimeController.getInstance().isNowNight() ? SystemMessageId.TIME_S1_S2_IN_THE_NIGHT : SystemMessageId.TIME_S1_S2_IN_THE_DAY);
		sm.addString(String.valueOf(h));
		sm.addString(String.valueOf(m < 10 ? "0" + m : m));
		activeChar.sendPacket(sm);
		return true;
	}
}
