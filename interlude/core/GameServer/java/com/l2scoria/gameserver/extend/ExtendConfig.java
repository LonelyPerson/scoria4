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

    //Password
    public static boolean EnableExtendPassword;
    public static String ExtendPasswordVoiceComand;
    public static String MsgIsPassword;
    public static String MsgNoPassword;
    public static String MsgErrorEnterPassword;
    public static String MsgAddPassword;
    public static String MsgIncorrectPassword;
    public static String MsgNoRepwd;
    public static String MsgDeletePwd;

    public static void load()
    {
        try
        {
            L2Properties extendProperties = new L2Properties(EXTEND_CONFIG_FILE);

            //PASSWORD
            EnableExtendPassword = Boolean.parseBoolean(extendProperties.getProperty("EnableExtendPassword","false"));
            ExtendPasswordVoiceComand = extendProperties.getProperty("ExtendPasswordVoiceComand","password");
            MsgIsPassword = extendProperties.getProperty("MsgIsPassword");
            MsgNoPassword = extendProperties.getProperty("MsgNoPassword");
            MsgErrorEnterPassword = extendProperties.getProperty("MsgErrorEnterPassword");
            MsgAddPassword = extendProperties.getProperty("MsgAddPassword");
            MsgIncorrectPassword = extendProperties.getProperty("MsgIncorrectPassword");
            MsgNoRepwd = extendProperties.getProperty("MsgNoRepwd");
            MsgDeletePwd = extendProperties.getProperty("MsgDeletePwd");
        }
        catch (Exception ex)
        {
            System.err.println("Extend: Unable to read  " + EXTEND_CONFIG_FILE);
        }
    }

}
