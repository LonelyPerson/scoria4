package ru.scoria.loginserver.network.loginserverpackets;

import java.io.IOException;

import ru.scoria.loginserver.L2LoginClient;
import ru.scoria.loginserver.network.serverpackets.ServerBasePacket;


public class ClientConnected extends ServerBasePacket {

	public ClientConnected(L2LoginClient cl) {
		writeC(0x05);
		writeS(cl.getIp());
	}
	@Override
	public byte[] getContent() throws IOException {
		return getBytes();
	}

}
