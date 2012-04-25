package com.l2scoria.gameserver.network.loginserverpackets;

import java.io.IOException;

import com.l2scoria.Config;

public class LoginNotify extends LoginServerBasePacket
{	
	private String _IP; 
	public LoginNotify(byte[] decrypt)
	{
		super(decrypt);
		_IP = readS();
		try
		{
			if(Config.ON_SUCCESS_LOGIN_COMMAND_GS.length()!=0)
			{
				Runtime.getRuntime().exec(Config.ON_SUCCESS_LOGIN_COMMAND_GS.replace("%ip%", _IP));
			}
		}
		catch(IOException e) {}
	}
}
