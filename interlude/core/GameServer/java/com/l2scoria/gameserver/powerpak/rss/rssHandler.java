package com.l2scoria.gameserver.powerpak.rss;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.handler.IBBSHandler;


public class rssHandler implements IBBSHandler
{
	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if(player==null)
			return;

		if(command.startsWith("bbsrss"))
		{
			if (parameters.startsWith("record"))
			{
				String val = parameters.substring(6).trim();
				rss.getInstance().showRecord(val, player);
			}
			else
			{
				rss.getInstance().showList(player);
			}
		}
	}

	@Override
	public String[] getBBSCommands()
	{
		return new String [] {"bbsrss"};
	}
}
