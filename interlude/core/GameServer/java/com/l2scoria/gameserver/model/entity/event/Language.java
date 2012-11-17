package com.l2scoria.gameserver.model.entity.event;

import com.l2scoria.L2Properties;

/**
 *
 * @author zenn
 */
public class Language {
    
        public static String LANG_ANNOUNCE_1;
        public static String LANG_ANNOUNCE_2;
        public static String LANG_ANNOUNCE_3;
        public static String LANG_ANNOUNCE_4;
        public static String LANG_ANNOUNCE_5;
        public static String LANG_ANNOUNCE_6;
        public static String LANG_STATUS;
        public static String LANG_KILLED_MSG;
        public static String LANG_TEAM_KILL_MSG;
        public static String LANG_PENALTY_KILL_MSG;
        public static String LANG_PROHIBIT_ITEM;
        public static String LANG_PROHIBIT_SKILL;
        public static String LANG_EVEN_UNAVAILABLE;
        public static String LANG_ALWAYS_REGISTER;
        public static String LANG_DUPLICATE_HWID;
        public static String LANG_DUPLICATE_IP;
        public static String LANG_MAX_PLAYERS;
        public static String LANG_CURSED_WEAPON;
        public static String LANG_NON_ENOUGH_LEVEL;
        public static String LANG_PLAYER_REGISTER;
        public static String LANG_REGISTER_ERROR;
        public static String LANG_REGISTER_CANCEL;
        public static String LANG_REGISTER_CANCEL_ERROR;
        public static String LANG_SECOND_CHECK;
        public static String LANG_EVENT_ABORT;
        public static String LANG_EVENT_START;
        public static String LANG_WINNER;
        public static String LANG_NO_WINNER;
        public static String LANG_TAKE_FLAG;
        public static String LANG_POINT_ADD;
        public static String LANG_FLAG_GRABE;
        public static String LANG_KILL_BONUS;
        public static String LANG_FIGHT_1_MIN;
        public static String LANG_REG_EVENT_MSG;
        public static String LANG_REG_CANCEL_EVENT_MSG;
        public static String LANG_MSG_SUCC_REG;
        public static String LANG_MSG_CANC_REG;
        public static String LANG_MSG_NON_REG;
        
        public static void load()
        {
            try
            {
                L2Properties Language = new L2Properties("./config/events/events_language.properties");
                LANG_ANNOUNCE_1 = Language.getProperty("LangAnnounce1", "Registration is opened");
                LANG_ANNOUNCE_2 = Language.getProperty("LangAnnounce2", "Level dependence:");
                LANG_ANNOUNCE_3 = Language.getProperty("LangAnnounce3", "Rewards:");
                LANG_ANNOUNCE_4 = Language.getProperty("LangAnnounce4", "Starting after {$time} min");
                LANG_ANNOUNCE_5 = Language.getProperty("LangAnnounce5", "Registration end on");
                LANG_ANNOUNCE_6 = Language.getProperty("LangAnnounce6", "Until the end of registration less than a minute");
                LANG_STATUS = Language.getProperty("LangStatus", "of");
                LANG_KILLED_MSG = Language.getProperty("LangKilledMsg", "You are killed. Wait resurect.");
                LANG_TEAM_KILL_MSG = Language.getProperty("LangTeamKillMsg", "You are killed you`r team member.");
                LANG_PENALTY_KILL_MSG = Language.getProperty("LangKilledDeathPenalty", "You are killed player with penalty");
                LANG_PROHIBIT_ITEM = Language.getProperty("LangProhibitItem", "You can`t use this item on event");
                LANG_PROHIBIT_SKILL = Language.getProperty("LangPhohibitSkill", "This skill dissabled on event");
                LANG_EVEN_UNAVAILABLE = Language.getProperty("LangEventOff", "Sorry but event unavailable");
                LANG_ALWAYS_REGISTER = Language.getProperty("LangAlwaysRegister", "You`re alrady registered on event");
                LANG_DUPLICATE_HWID = Language.getProperty("LangDuplicateHWID", "Sorry but some players always registered from you PC");
                LANG_DUPLICATE_IP = Language.getProperty("LangDuplicateIp", "Sorry but some players always register from you IP");
                LANG_MAX_PLAYERS = Language.getProperty("LangMaxPlayer", "Sorry but all slot are used");
                LANG_CURSED_WEAPON = Language.getProperty("LangCursedWeapon", "Sorry but you can`t register with CursedWeapon");
                LANG_NON_ENOUGH_LEVEL = Language.getProperty("LangNotEnoughLevel", "Sorry but you`r level not in event range");
                LANG_PLAYER_REGISTER = Language.getProperty("LangPlayerRegistered", "You successfull registered on event");
                LANG_REGISTER_ERROR = Language.getProperty("LangSomeErrorReason", "Sorry but you can`t fight in event");
                LANG_REGISTER_CANCEL = Language.getProperty("LangCancelRegister", "You are canceled you`r event registration");
                LANG_REGISTER_CANCEL_ERROR = Language.getProperty("LangFailCancel", "You not registered on event to cancel it");
                LANG_SECOND_CHECK = Language.getProperty("LangSecondCheck", "Sorry you can`t fight on event");
                LANG_EVENT_ABORT = Language.getProperty("LangOnAbort", "Low players count");
                LANG_EVENT_START = Language.getProperty("LangStartedGame", "Game started!");
                LANG_WINNER = Language.getProperty("LangEventWinner", "The winner is");
                LANG_NO_WINNER = Language.getProperty("LangEventWinnerNo", "Winners are not determined");
                LANG_TAKE_FLAG = Language.getProperty("LangFlagEventMsg", "Take the flag to you base");
                LANG_POINT_ADD = Language.getProperty("LangPointAdd", "brings his team 1 point");
                LANG_FLAG_GRABE = Language.getProperty("LangFlagRestrict", "seized the flag of your team");
                LANG_KILL_BONUS = Language.getProperty("LangOnKillBonus", "You rewarded for kill");
                LANG_FIGHT_1_MIN = Language.getProperty("LangFightStart1Min", "Fight start after 1 min");
                LANG_REG_EVENT_MSG = Language.getProperty("LangVoiceRegister", "You are registered on event");
                LANG_REG_CANCEL_EVENT_MSG = Language.getProperty("LangVoiceRegLeave", "You cancel registration on event");
                LANG_MSG_SUCC_REG = Language.getProperty("LangVoiceRegister", "You are registered on event");
                LANG_MSG_CANC_REG = Language.getProperty("LangVoiceRegLeave", "You cancel registration on event");
                LANG_MSG_NON_REG = Language.getProperty("LangVoiceNonReg", "You are not registered to cancel partipication");
            }
            catch(Exception e)
            {
                //
            }
        }
    
}
