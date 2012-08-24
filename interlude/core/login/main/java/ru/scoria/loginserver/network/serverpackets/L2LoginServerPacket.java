package ru.scoria.loginserver.network.serverpackets;

import ru.scoria.loginserver.L2LoginClient;
import ru.scoria.loginserver.mmocore.SendablePacket;


/**
 *
 * @author  KenM
 */
public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
	/**
	* @see ru.scoria.loginserver.mmocore.SendablePacket#getHeaderSize()
	*/
	@Override
	protected int getHeaderSize()
	{
		return 2;
	}

	/**
	* @see ru.scoria.loginserver.mmocore.SendablePacket#writeHeader(int)
	*/
	@Override
	protected void writeHeader(int dataSize)
	{
		writeH(dataSize + this.getHeaderSize());
	}
}
