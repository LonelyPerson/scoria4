package com.l2scoria.gameserver.extend.Clan;

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.extend.ExtendConfig;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Created Bigli
 * Extend 1.0
 */
public class ClanMessage implements IVoicedCommandHandler, ICustomByPassHandler {

    private static final Logger _log = Logger.getLogger(ClanMessage.class.getName());
    private static final String _clanMessage = "!!!CLAN MESSAGE!!!"; //Сообщения для Log.

    private static final String[] _BYPASCLAN = {"clan"};

    //Html
    private static final String add_html = "data/html/extend/clan/addMessage.htm";
    private static final String main_html = "data/html/extend/clan/main.htm";
    private static final String edit_html = "data/html/extend/clan/editMessage.htm";

    //Player Message
    private static final String empty_message = ExtendConfig.EmptyMessage;
    private static final String delete_message_successful = ExtendConfig.DeleteMessageSuccessful;
    private static final String delete_message_error = ExtendConfig.DeleteMessageError;

    private static ClanMessage _instance = null;
    private static ClanMessageData ClanMessageData;


    public static ClanMessage getInstance()
    {
        if(_instance == null)
        {
            _instance = new ClanMessage();
        }
        return _instance;
    }


    public static FastList<ClanMessageData> getClanMsg(L2PcInstance player) {
        //Выбирает все мессаджы, и добавляет их в ClanMessage.
        FastList<ClanMessageData> ClanMessage = new FastList<ClanMessageData>();
        try {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("select ClanId, Msg, MsgId, Char_Name, Char_obj from extend_clan where ClanId = " + player.getClanId());
            ResultSet rs = stm.executeQuery();
            while (rs.next())
            {
                int clanId = rs.getInt(1);
                String msg = rs.getString(2);
                int msg_id = rs.getInt(3);
                String char_name = rs.getString(4);
                int char_obj = rs.getInt(5);

                ClanMessage.add(new ClanMessageData(clanId,msg,msg_id,char_name,char_obj));
            }
            rs.close();
            stm.close();
            con.close();
            return ClanMessage;
        }
        catch (Exception ex){_log.info(_clanMessage + " " + ex);return null;}
    }

    private ClanMessageData getClanMsg(int msg_id)
    {
        //Выбирает только нужный мессадж, для редактирование.
        ClanMessageData dataClan = null;
        try {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("select ClanId, Msg, Char_Name, Char_obj from extend_clan where MsgId = " +msg_id);
            ResultSet rs = stm.executeQuery();
            while (rs.next())
            {
                int clanId = rs.getInt(1);
                String msg = rs.getString(2);
                String char_name = rs.getString(3);
                int char_obj = rs.getInt(4);
                dataClan = new ClanMessageData(clanId,msg,msg_id,char_name, char_obj);

            }
            rs.close();
            stm.close();
            con.close();
        }
        catch (Exception ex){_log.info(_clanMessage + " " + ex);}
        return dataClan;
    }

    private boolean deleteMessage(int id)
    {
        try
        {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("delete from extend_clan where MsgId = " + id);
            stm.execute();
            con.close();
            stm.close();
            return true;
        }
        catch (Exception ex){return false;}
    }

    private void addMessage(String msg, L2PcInstance player){
        try {

            //Метод добавления мессаджа.
            ClanMessageData _clanData = new ClanMessageData(player.getClanId(),msg,-1,player.getName(),player.getObjectId());
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement stm = con.prepareStatement("Insert into extend_clan (ClanId, Msg, char_name, Char_obj) values (?,?,?,?)");
            stm.setInt(1, _clanData.getClanId());
            stm.setString(2, _clanData.getMsg());
            stm.setString(3, _clanData.getChar_Name());
            stm.setInt(4, _clanData.getChar_obj());
            stm.execute();
            stm.close();
            con.close();
        }
        catch (Exception ex){_log.info(_clanMessage + " " + ex);}
    }

    private void getMainHtm(L2PcInstance activeChar)
    {
        /**
         * Открывает главное окно игроку, где он может просмотреть все сообщения,
         * Выбрать нужно сообщение и отредактировать его, или добавить новое.
        */

        FastList<ClanMessageData> ClanMessage = getClanMsg(activeChar);
        NpcHtmlMessage mainhtm = new NpcHtmlMessage(5);
        mainhtm.setFile(main_html);
        TextBuilder message = new TextBuilder();
        for(ClanMessageData Data : ClanMessage)
        {
            message.append(String.format("<a action = \"bypass -h custom_clan Message %s \"> %s <a><br>",Data.getMsgId(),getShortMsg(Data.getMsg())));
        }
        mainhtm.replace("%message%", message.toString());
        activeChar.sendPacket(mainhtm);
    }

    private String getShortMsg(String text)
    {
        //Укорачивает сообщение, чтобы оно влезло в окно.
        if(text.length() > 40)
        {
            text.substring(40,text.length());
        }
        return text;
    }

    private void getAddHtml(L2PcInstance activeChar)
    {
        //Открывает игроку диалог с добавлением месседжа
        NpcHtmlMessage html = new NpcHtmlMessage(5);
        String content = HtmCache.getInstance().getHtm(add_html);
        html.setHtml(content);
        activeChar.sendPacket(html);
    }

    private void getEditHtml(L2PcInstance activeChar, int MessageId)
    {
        ClanMessageData = getClanMsg(MessageId);
        NpcHtmlMessage edithtml = new NpcHtmlMessage(5);
        edithtml.setFile(edit_html);
        edithtml.replace("%message%", ClanMessageData.getMsg());
        activeChar.sendPacket(edithtml);
    }

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        if(activeChar == null)
            return false;

        if(ExtendConfig.ClanMessageOnlyCL && activeChar.isClanLeader())
        {
            getMainHtm(activeChar);
        }
        else if(!ExtendConfig.ClanMessageOnlyCL)
        {
            getMainHtm(activeChar);
        }

        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        return new String[]{ExtendConfig.ClanMessageVoiceComand};
    }

    @Override
    public String[] getByPassCommands() {
        return _BYPASCLAN;
    }

    @Override
    public void handleCommand(String command, L2PcInstance player, String parameters) {
        if(parameters.startsWith("addMessage"))
        {
            String msg = parameters.substring(10);
            if(!msg.equals("Html") && !msg.isEmpty())
            {
                addMessage(msg, player);
                getMainHtm(player);
            }
            else
            {
                player.sendMessage(empty_message);
            }
        }
        else if(parameters.equals("MessageAddHtml"))
        {
            getAddHtml(player);
        }
        else if(parameters.startsWith("Message"))
        {
            int id = Integer.valueOf(parameters.substring(8));
            getEditHtml(player,id);
        }
        else if(parameters.startsWith("deleteMessage"))
        {
            if(ClanMessageData != null)
            {
                if(deleteMessage(ClanMessageData.getMsgId()))
                    player.sendMessage(delete_message_successful);
                else
                    player.sendMessage(delete_message_error);
                ClanMessageData = null;
                getMainHtm(player);
            }
        }
        else if(parameters.startsWith("main"))
        {
            getMainHtm(player);
        }
    }
}
