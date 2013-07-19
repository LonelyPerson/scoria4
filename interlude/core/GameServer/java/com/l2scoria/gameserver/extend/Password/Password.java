package com.l2scoria.gameserver.extend.Password;

import com.l2scoria.gameserver.extend.ExtendConfig;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created Bigli
 * Extend 1.0
 */
public class Password implements IVoicedCommandHandler, ICustomByPassHandler {

    private static final Logger _log = Logger.getLogger(Password.class.getName());
    private static final String _passwordMessage = "!!!PASSWORD MESSAGE!!!"; //Сообщения для Log.

    private static final String[] _BYPASPASS = {"password"};

    //Html
    private static final String main_html = "data/html/extend/password/main.htm";
    private static final String edit_html = "data/html/extend/password/edit.htm";
    private static final String add_html = "data/html/extend/password/add.htm";
    private static final String enter_html = "data/html/extend/password/enter.htm";

    private static Password _instance = null;
    public static Password getInstance()
    {
        if(_instance == null)
        {
            _instance = new Password();
        }
        return _instance;
    }

    public String getPassword(L2PcInstance player)
    {
        try
        {
            String password = null;
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("select extend_password from characters where obj_Id = " + player.getObjectId());
            ResultSet rs = stm.executeQuery();
            while (rs.next())
            {
                password = rs.getString(1);
            }
            rs.close();
            stm.close();
            con.close();
            return password;
        }
        catch (Exception ex){_log.info(_passwordMessage + " " + ex); return null;}
    }

    public boolean setPassword(L2PcInstance player, String password)
    {
        try
        {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("Update into extend_clan (ClanId, Msg, char_name, Char_obj) values (?,?,?,?)");

            stm.execute();
            stm.close();
            con.close();
            return true;
        }
        catch (Exception ex){_log.info(_passwordMessage + " " + ex); return false;}
    }

    public boolean checkPassword(L2PcInstance player, String password)
    {
        try
        {
            String _password = null;
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("select extend_password from characters where obj_Id = " + player.getObjectId());
            ResultSet rs = stm.executeQuery();
            while (rs.next())
            {
                password = rs.getString(1);
            }
            rs.close();
            stm.close();
            con.close();
            if(password.equals(_password))
                return true;
            else
                return false;
        }
        catch (Exception ex){_log.info(_passwordMessage + " " + ex); return false;}
    }

    public boolean isPassword(L2PcInstance player)
    {
        try
        {
            String password = null;
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("select extend_password from characters where obj_Id = " + player.getObjectId());
            ResultSet rs = stm.executeQuery();
            while (rs.next())
            {
                password = rs.getString(1);
            }
            rs.close();
            stm.close();
            if (password == null)
                return false;
            else
                return true;
        }
        catch (Exception ex){_log.info(_passwordMessage + " " + ex); return false;}
    }
    private void getMainHtml(L2PcInstance activeChar)
    {

    }
    @Override
    public String[] getByPassCommands() {
        return _BYPASPASS;
    }

    @Override
    public String[] getVoicedCommandList() {
        return new String[]{ExtendConfig.ExtendPasswordVoiceComand};
    }

    @Override
    public void handleCommand(String command, L2PcInstance player, String parameters) {

    }

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        if(activeChar == null)
            return false;


        return false;
    }

}
