package com.l2scoria.loginserver.network.clientpackets;

import com.l2scoria.Config;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

/**
 * @author Akumu
 * @date 19.03.11 14:02
 * Note: cdb
 *      d - session
 *      b - 144 byte hash (128 rsa, 16 crc)
 */
public class RequestSCCheck extends L2LoginClientPacket
{
	int session;
	byte[] _data;

	@Override
	protected boolean readImpl()
	{
		int bytes = super._buf.remaining();

		session = readD();

		_data = new byte[super._buf.remaining()];
		readB(_data);

		if(Config.DEBUG)
		{
			System.out.println("UNetworkHandler__RequestSCCheck(); size: " + bytes + ", read: " + _data.length);
		}

		return true;
	}

	@Override
	public void run()
	{
		byte[] decrypted = null;

		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_data, 0x00, 0x80);
			rsaCipher = null;
		}
		catch (GeneralSecurityException e)
		{
			e.printStackTrace();
			return;
		}

		for(byte b : decrypted)
		{
			System.out.println(b);
		}
	}
}
