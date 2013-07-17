package com.l2scoria.gameserver.extend;

import com.l2scoria.L2Properties;
import org.apache.log4j.Logger;

/**
 * Created Bigli
 * Extend 1.0
 */
public class ExtendConfig {

    private static final Logger _log = Logger.getLogger(ExtendConfig.class.getName());

    public static String EXTEND_CONFIG_FILE = "config/extend/extend.properties";

    public static boolean EnableExtend;


    //Clan
    public static boolean EnableClanMessage;
    public static boolean ClanMessageOnlyCL;
    public static String ClanMessageVoiceComand;
    public static String LongMessage;
    public static String EmptyMessage;
    public static String DeleteMessageSuccessful;
    public static String DeleteMessageError;

    public static void load()
    {
        try
        {
            L2Properties extendProperties = new L2Properties(EXTEND_CONFIG_FILE);
            EnableExtend = Boolean.parseBoolean(extendProperties.getProperty("EnableExtend", "false"));
            EnableClanMessage = Boolean.parseBoolean(extendProperties.getProperty("EnableClanMessage", "false"));
            ClanMessageOnlyCL = Boolean.parseBoolean(extendProperties.getProperty("ClanMessgaeOnlyCL","true"));
            ClanMessageVoiceComand = extendProperties.getProperty("ClanMessageVoice","clanmsg");
            LongMessage = extendProperties.getProperty("LongMessage","");
            EmptyMessage = extendProperties.getProperty("EmptyMessage","");
            DeleteMessageSuccessful = extendProperties.getProperty("DeleteMessageSuccessful","");
            DeleteMessageError = extendProperties.getProperty("DeleteMessageError","");
        }
        catch (Exception ex)
        {
            System.err.println("Extend: Unable to read  " + EXTEND_CONFIG_FILE);
        }
    }

}
