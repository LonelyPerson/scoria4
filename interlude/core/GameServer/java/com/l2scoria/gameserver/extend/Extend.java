package com.l2scoria.gameserver.extend;

import com.l2scoria.gameserver.extend.Clan.ClanMessage;
import com.l2scoria.gameserver.extend.Password.Password;
import com.l2scoria.gameserver.handler.VoicedCommandHandler;
import com.l2scoria.gameserver.handler.custom.CustomBypassHandler;
import com.l2scoria.util.Util;

/**
 * Created Bigli
 * Extend 1.0
 */
public class Extend {

    private static Extend _instance = null;

    public static Extend getInstance()
    {
        if(_instance == null)
        {
            _instance = new Extend();
        }
        return _instance;
    }

    private Extend()
    {
        ExtendConfig.load();
        Util.printSection("Extend");
        if(ExtendConfig.EnableExtend)
        {
            if(ExtendConfig.EnableClanMessage)
            {
                System.out.println("Extend Clan:");
                ClanMessage.getInstance();
                ClanMessage handlerClan = new ClanMessage();
                if(ExtendConfig.ClanMessageVoiceComand != null && ExtendConfig.ClanMessageVoiceComand.length() > 0)
                {
                    VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handlerClan);
                }
                CustomBypassHandler.getInstance().registerCustomBypassHandler(handlerClan);

            }
            if(ExtendConfig.EnableExtendPassword)
            {
                System.out.println("Enabled Extend Clan");
                System.out.print("Extend Password:");
                Password.getInstance();
                Password handlerPassword = new Password();
                if(ExtendConfig.ExtendPasswordVoiceComand != null && ExtendConfig.ExtendPasswordVoiceComand.length() >0)
                {
                    VoicedCommandHandler.getInstance().registerVoicedCommandHandler(handlerPassword);
                }
                CustomBypassHandler.getInstance().registerCustomBypassHandler(handlerPassword);
            }
        }
    }
}
