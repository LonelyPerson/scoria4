package com.l2scoria.gameserver.extend.Password;

import com.l2scoria.gameserver.extend.Clan.ClanMessageData;
import com.l2scoria.gameserver.extend.ExtendConfig;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.text.TextBuilder;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String delete_html = "data/html/extend/password/delete.htm";
    private static final String enter_html = "data/html/extend/password/enter.htm";

    //Message
    private static final String isPassword = ExtendConfig.MsgIsPassword;
    private static final String noPassword = ExtendConfig.MsgNoPassword;
    private static final String errorEnterPassword = ExtendConfig.MsgErrorEnterPassword;
    private static final String addPassword = ExtendConfig.MsgAddPassword;
    private static final String incorrectPassword = ExtendConfig.MsgIncorrectPassword;
    private static final String norepwd = ExtendConfig.MsgNoRepwd;
    private static final String delete = ExtendConfig.MsgDeletePwd;

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
            PreparedStatement stm = con.prepareStatement("Update characters set extend_password = ? where obj_Id = ?");
            stm.setString(1,password);
            stm.setInt(2, player.getObjectId());
            stm.execute();
            stm.close();
            con.close();
            return true;
        }
        catch (Exception ex){_log.info(_passwordMessage + " " + ex); return false;}
    }

    private boolean deletePassword(L2PcInstance player)
    {
        try
        {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("Update characters set extend_password = " + null +
                    " where obj_Id = " + player.getObjectId());
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
                _password = rs.getString(1);
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
        NpcHtmlMessage mainhtm = new NpcHtmlMessage(5);
        mainhtm.setFile(main_html);
        if(isPassword(activeChar))
            mainhtm.replace("%password%", isPassword);
        else
            mainhtm.replace("%password%", noPassword);
        activeChar.sendPacket(mainhtm);
    }

    private void getAddHtml(L2PcInstance activeChar)
    {
        if(isPassword(activeChar))
        {
            activeChar.sendMessage(isPassword);
        }
        else
        {
            NpcHtmlMessage mainhtm = new NpcHtmlMessage(5);
            mainhtm.setFile(add_html);
            activeChar.sendPacket(mainhtm);
        }
    }

    private void getEditHtml(L2PcInstance activeChar)
    {
        if(isPassword(activeChar))
        {
            NpcHtmlMessage mainhtm = new NpcHtmlMessage(5);
            mainhtm.setFile(edit_html);
            activeChar.sendPacket(mainhtm);
        }
        else
        {
            activeChar.sendMessage(noPassword);
        }
    }

    private void getDeleteHtml(L2PcInstance activeChar)
    {
        if(isPassword(activeChar))
        {
            NpcHtmlMessage mainhtm = new NpcHtmlMessage(5);
            mainhtm.setFile(delete_html);
            activeChar.sendPacket(mainhtm);
        }
        else
        {
            activeChar.sendMessage(noPassword);
        }
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
        if(parameters.equals("AddPassword"))
        {
            getAddHtml(player);
        }

        if(parameters.equals("DeletePassword"))
        {
            getDeleteHtml(player);
        }

        if(parameters.equals("EditPassword"))
        {
            getEditHtml(player);
        }

        if(parameters.startsWith("EnterPassword"))
        {
            String param = parameters.substring(14);
            Pattern pattern = Pattern.compile("pwd: ([a-zA-Z0-9]+) repwd: ([a-zA-Z0-9]+)");
            Matcher matcher = pattern.matcher(param);
            if(matcher.matches())
            {
                String pwd = matcher.group(1).trim();
                String repwd = matcher.group(2).trim();
                if(pwd.equals(repwd))
                {
                    setPassword(player,pwd);
                    player.sendMessage(addPassword);
                    getMainHtml(player);
                }
                else
                {
                    player.sendMessage(norepwd);
                    getAddHtml(player);
                }
            }
            else
            {
                player.sendMessage(incorrectPassword);
                getAddHtml(player);
            }
        }
        if(parameters.startsWith("EditEnter"))
        {
            String param = parameters.substring(10);
            Pattern pattern = Pattern.compile("oldpwd: ([a-zA-Z0-9]+) newpwd: ([a-zA-Z0-9]+) renewpwd: ([a-zA-Z0-9]+)");
            Matcher matcher = pattern.matcher(param);
            if(matcher.matches())
            {
                String oldpwd = matcher.group(1).trim();
                String newpwd = matcher.group(2).trim();
                String renewpwd = matcher.group(3).trim();
                if(checkPassword(player,oldpwd))
                {
                    if(newpwd.equals(renewpwd))
                    {
                        setPassword(player,newpwd);
                        player.sendMessage(addPassword);
                        getMainHtml(player);
                    }
                    else
                    {
                        player.sendMessage(norepwd);
                        getMainHtml(player);
                    }
                }
                else
                {
                    player.sendMessage(errorEnterPassword);
                    getMainHtml(player);
                }
            }
            else
            {
                player.sendMessage(incorrectPassword);
                getMainHtml(player);
            }
        }
        if(parameters.startsWith("DeleteEnter"))
        {
            String param = parameters.substring(12);
            Pattern pattern = Pattern.compile("pwd: ([a-zA-Z0-9]+)");
            Matcher matcher = pattern.matcher(param);

            if(matcher.matches())
            {
                String pwd = matcher.group(1).trim();
                _log.info(pwd);
                if(checkPassword(player,pwd))
                {
                    deletePassword(player);
                    player.sendMessage(delete);
                    getMainHtml(player);
                }
                else
                {
                    player.sendMessage(errorEnterPassword);
                    getMainHtml(player);
                }
            }
            else
            {
                player.sendMessage(incorrectPassword);
                getMainHtml(player);
            }
        }



    }

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        if(activeChar == null)
            return false;

        getMainHtml(activeChar);

        return false;
    }

}
