package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.model.base.Experience;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

public class Level extends Admin
{
	public Level()
	{
		_commands = new String[]{"admin_add_level", "admin_set_level"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		L2Object targetChar = activeChar.getTarget();
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String val = "";

		if(st.countTokens() >= 1)
		{
			val = st.nextToken();
		}

		st = null;

		if(actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if(targetChar instanceof L2PlayableInstance)
				{
					((L2PlayableInstance) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("Wrong Number Format");
			}
		}
		else if(actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			try
			{
				if(targetChar == null || !(targetChar instanceof L2PlayableInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT)); // incorrect
					return false;
				}

				final L2PlayableInstance targetPlayer = (L2PlayableInstance) targetChar;

				final byte lvl = Byte.parseByte(val);
				int max_level = Experience.MAX_LEVEL;

				if(targetChar instanceof L2PcInstance && ((L2PcInstance) targetPlayer).isSubClassActive())
				{
					max_level = Experience.MAX_SUBCLASS_LEVEL;
				}

				if(lvl >= 1 && lvl <= max_level)
				{
					final long pXp = targetPlayer.getStat().getExp();
					final long tXp = Experience.getExp(lvl);

					if(pXp > tXp)
					{
						targetPlayer.getStat().removeExpAndSp(pXp - tXp, 0);
					}
					else if(pXp < tXp)
					{
						targetPlayer.getStat().addExpAndSp(tXp - pXp, 0);
					}
				}
				else
				{
					activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
					return false;
				}
			}
			catch(final NumberFormatException e)
			{
				activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
				return false;
			}
		}

		actualCommand = null;
		targetChar = null;

		return true;
	}
}
