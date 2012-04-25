package ru.catssoftware.protection;

import com.l2scoria.gameserver.network.L2GameClient;

public class CatsGuard {
	private static CatsGuard _instance;
	public static CatsGuard getInstance() {
		if(_instance==null)
			_instance = new CatsGuard();
		return _instance;
	}
	public void initSession(L2GameClient cl) {
	}
	
	public void doneSession(L2GameClient cl) {
	}
	
}
