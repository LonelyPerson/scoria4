package ru.scoria.loginserver.clientpackets;



import ru.scoria.Config;
import ru.scoria.loginserver.L2LoginClient;
import ru.scoria.loginserver.L2LoginClient.LoginClientState;
import ru.scoria.loginserver.manager.LoginManager;
import ru.scoria.loginserver.network.serverpackets.LoginFailReason;
import ru.scoria.loginserver.network.serverpackets.LoginOk;
import ru.scoria.loginserver.network.serverpackets.ServerList;

public class SendCardCode extends L2LoginClientPacket {

	private final byte[] _raw = new byte[151];
	private byte _cardNo;
	@Override
	protected boolean readImpl() {
		if(getAvaliableBytes()==151) {
			readB(_raw);
			_cardNo = _raw[0];
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		/*			System.out.println(HexUtil.printData(_raw));
		byte [] decrypted = null;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			e.printStackTrace();
			return;
		}
//		System.out.println(HexUtil.printData(decrypted)); */
		
		L2LoginClient client = getClient();
		if(Config.CARD_ENABLED && client.getState()==LoginClientState.AUTHED_CARD && client._CardNo==_cardNo) {
			client.setState(LoginClientState.AUTHED_LOGIN);
			client._accInfo.setLastIp(getClient().getIp());
			LoginManager.getInstance().addOrUpdateAccount(client._accInfo);
			if (Config.SHOW_LICENCE)
				client.sendPacket(new LoginOk(client.getSessionKey()));
			else
				client.sendPacket(new ServerList(client,false)); 
		}
		else client.close(LoginFailReason.REASON_SYSTEM_ERROR);
	}

}
