package com.l2scoria.gameserver.thread.daemons;

import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ExPCCafePointInfo;

/**
 *
 * @author zenn
 */
public class ServerOnline implements Runnable
{
	Logger _log = Logger.getLogger(ServerOnline.class.getName());
	private static ServerOnline _instance;
	private int _online = 0;

	public static ServerOnline getInstance()
	{
		if(_instance == null)
		{
			_instance = new ServerOnline();
		}

		return _instance;
	}

	private ServerOnline()
	{
		_log.info("Server Online Deamon is runned...");
	}

	private void setOnline(L2PcInstance activeChar, int pconline, int online, boolean add)
	{
		activeChar.sendPacket(new ExPCCafePointInfo(pconline, online, add, 24, false));
	}

	@Override
	public void run()
	{
		if(Config.PCB_ENABLE)
		{
			_log.info("Online window doesn`t support where PC bang is ENABLED!!!");
			return;
		}

		int real = L2World.getInstance().getAllPlayers().size();
		int fake = (real*Config.PCB_LIKE_WINDOW_INCREASE_RATE)/100;
		int nonline = fake + real;

		for(L2PcInstance activeChar: L2World.getInstance().getAllPlayers())
		{
			if(_online == 0)
			{
				setOnline(activeChar, nonline, 0, false);
			}
			else
			{
				if(_online != nonline)
				{
					int sub = nonline - _online;
					if (sub < 0)
					{
						setOnline(activeChar, nonline, sub, false);
					}
					else
					{
						setOnline(activeChar, nonline, sub, true);
					}
				}
			}
		}
		_online = nonline;
	}
}