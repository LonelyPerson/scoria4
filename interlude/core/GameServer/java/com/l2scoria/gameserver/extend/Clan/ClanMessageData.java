package com.l2scoria.gameserver.extend.Clan;

/**
 * Created Bigli
 * Extend 1.0
 */
public class ClanMessageData {
    private int ClanId;
    private String Msg;
    private int MsgId;
    private String Char_Name;
    private int Char_obj;

    public ClanMessageData(int clanId, String msg, int msgId, String char_name, int char_obj)
    {
        this.ClanId = clanId;
        this.Msg = msg;
        this.MsgId = msgId;
        this.Char_Name = char_name;
        this.Char_obj = char_obj;
    }

    public int getClanId(){return ClanId;}
    public String getMsg(){return Msg;}
    public int getMsgId(){return MsgId;}
    public String getChar_Name(){return Char_Name;}
    public int getChar_obj(){return Char_obj;}
}
