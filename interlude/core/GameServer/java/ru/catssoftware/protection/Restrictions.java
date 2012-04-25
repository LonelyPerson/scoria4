package ru.catssoftware.protection;

import com.l2scoria.gameserver.network.L2GameClient;

public class Restrictions
{
	public static boolean onUserLogged(L2GameClient client)
	{
		return true;
	}
	public static void onDisconnect(L2GameClient cl)
	{
	}
	public static void check(L2GameClient cl)
	{	
	}
}