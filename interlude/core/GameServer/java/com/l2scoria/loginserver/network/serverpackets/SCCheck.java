package com.l2scoria.loginserver.network.serverpackets;

/**
 * @author Akumu
 * @date 19.03.11 13:45
 */
public class SCCheck extends L2LoginServerPacket
{

	public SCCheck()
	{
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x0A);
		writeD(1);
		writeC(2);
	}
}
