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
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import java.util.StringTokenizer;

/**
 * <b>This class handles Admin mass commands:</b><br>
 * <br>
 * 
 * @author Akumu, Rayan RPG
 * @Project L2Emu Project!
 * @since 4193
 */
public class MassControl extends Admin
{

	public MassControl()
	{
		_commands = new String[]{"admin_masskill", "admin_massress"};
	}


	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_mass"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();

				if(st.nextToken().equalsIgnoreCase("kill"))
				{
					int counter = 0;

					for(L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if(!player.isGM())
						{
							counter++;
							player.getStatus().setCurrentHp(0);
							player.doDie(player);
							activeChar.sendMessage("You've Killed " + counter + " players.");
						}
					}
				}
				else if(st.nextToken().equalsIgnoreCase("ress"))
				{
					int counter = 0;

					for(L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if(!player.isGM() && player.isDead())
						{
							counter++;
							player.doRevive();
							activeChar.sendMessage("You've Ressurected " + counter + " players.");
						}
					}
				}

				st = null;
			}
			catch(Exception ex)
			{
				//ignore
			}
		}

		return true;
	}
}