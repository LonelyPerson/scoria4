package ru.catssoftware.protection;

import com.l2scoria.gameserver.network.BlowFishKeygen;
import com.l2scoria.gameserver.network.GameCrypt;

public class LameStub
{
	public static boolean ISLAME; 
	public static void main(String []args)
	{
		BlowFishKeygen._ISLAME = true;
		GameCrypt._ISLAME = true;
		ISLAME = true;
	}
}