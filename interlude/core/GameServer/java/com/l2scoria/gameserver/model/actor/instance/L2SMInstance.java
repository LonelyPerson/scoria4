package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.Config;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.CharNameTable;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.PcInventory;
import com.l2scoria.gameserver.model.actor.appearance.PcAppearance;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.model.multisell.L2Multisell;
import com.l2scoria.gameserver.network.Disconnection;
import com.l2scoria.gameserver.network.L2GameClient;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.PledgeSkillList;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.powerpak.personal.Personal;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.database.L2DatabaseFactory;
import com.l2scoria.util.database.LoginRemoteDbFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class L2SMInstance extends L2FolkInstance {

    public L2SMInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(L2PcInstance player, int val) {
        if ((player == null) || (player.isDead())) {
            return;
        }

        if (player.isCursedWeaponEquiped()) {
            return;
        }

        showMain(player);
    }

    public void onBypassFeedback(L2PcInstance player, String command) {
        if (player == null) {
            return;
        }
        String[] cmd = command.split(" ");

        if (cmd[0].equalsIgnoreCase("Chat")) {
            String file = "data/html/ServiceManager/" + getNpcId() + ".htm";
            int cmdChoice = Integer.parseInt(command.split(" ")[1]);

            if (cmdChoice > 0) {
                file = "data/html/ServiceManager/" + getNpcId() + "-" + cmdChoice + ".htm";
            }
            showChatWindow(player, file);
        } else if (cmd[0].equalsIgnoreCase("Multisell")) {
            if (cmd.length < 2) {
                return;
            }
            int val = Integer.parseInt(cmd[1]);
            L2Multisell.getInstance().SeparateAndSend(val, player, false, getCastle().getTaxRate());
        } else if (cmd[0].equalsIgnoreCase("setnamecolor")) {
            if (cmd[1].equals(null)) {
                return;
            }
            int ColorId = Integer.parseInt(cmd[1]);
            changeColor(player, 1, ColorId);
        } else if (cmd[0].equalsIgnoreCase("settitlecolor")) {
            if (cmd[1].equals(null)) {
                return;
            }
            int ColorId = Integer.parseInt(cmd[1]);
            changeColor(player, 2, ColorId);
        } else {
            if (cmd[0].equals("setname")) {
                if (cmd.length < 2) {
                    return;
                }

                String nick = cmd[1];
                if ((nick.length() < 3) || (nick.length() > 16) || (nick.contains("?")) || (nick.contains("/")) || (nick.contains(";")) || (nick.contains(" ")) || (nick.contains("®")) || (nick.contains("©")) || (nick.contains(","))) {
                    player.sendMessage(Config.SM_LANG_NAME_FAIL_IS_INCORRENT);
                    return;
                }
                if (player.isClanLeader()) {
                    player.sendMessage(Config.SM_LANG_NAME_FAIL_IS_CLAN_OWNER);
                    return;
                }
                if (CharNameTable.getInstance().doesCharNameExist(nick)) {
                    player.sendMessage(Config.SM_LANG_NAME_FAIL_IS_EXISTS);
                    return;
                }
                if (!player.destroyItemByItemId("Name Change", Config.SM_ITEM_ID, Config.SM_NAME_CHANGE, player.getLastFolkNPC(), true)) {
                    return;
                }
                player.setName(nick);
                player.store();
                player.broadcastUserInfo();
                player.sendMessage(Config.SM_LANG_NAME_SUCCESS_RESULT.replace("%name%", nick));
                return;
            }
            if (cmd[0].equals("noblese")) {
                if (player.isNoble()) {
                    player.sendMessage(Config.SM_LANG_NOOBLES_FAIL_ALWAYS);
                    return;
                }
                if (!player.destroyItemByItemId("Name Change", Config.SM_ITEM_ID, Config.SM_NOBLESSE, player.getLastFolkNPC(), true)) {
                    return;
                }
                player.setNoble(true);
                return;
            }

            if (cmd[0].equals("changesex")) {
                if (!player.destroyItemByItemId("Name Change", Config.SM_ITEM_ID, Config.SM_CHANGE_SEX, player.getLastFolkNPC(), true)) {
                    return;
                }
                player.getAppearance().setSex(!player.getAppearance().getSex());
                player.broadcastUserInfo();
                player.sendMessage(Config.SM_LANG_SEX_CHANGED_OK);
                ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(player), 3000);
            }

            if (cmd[0].equals("nullpk")) {
                if (player.getPkKills() == 0) {
                    player.sendMessage(Config.SM_LANG_PKCLEAR_FAIL_NO);
                    return;
                }
                if (!player.destroyItemByItemId("PK Change", Config.SM_ITEM_ID, Config.SM_NULL_PK, player.getLastFolkNPC(), true)) {
                    return;
                }
                player.setPkKills(0);
                player.setKarma(0);
                player.sendMessage(Config.SM_LANG_PKCLEAR_SUCCESS);
                return;
            }
            if (cmd[0].equals("clanlvl8")) {
                if ((player.getClan() == null) || (player.getClan().getLevel() < 5)) {
                    player.sendMessage(Config.SM_LANG_CLAN_NOT_FOUND_OR_LVL);
                    return;
                }

                if (player.getClan().getLevel() == 8) {
                    player.sendMessage(Config.SM_LANG_CLAN_ALWAYS_HAVE_LVL);
                    return;
                }

                if (!player.destroyItemByItemId("lvl8 Clan", Config.SM_ITEM_ID, Config.SM_LVL8_CLAN, player.getLastFolkNPC(), true)) {
                    return;
                }

                player.getClan().changeLevel(8);
                player.sendMessage(Config.SM_LANG_CLAN_LEVEL_TAKE_SUCCESS);
                return;
            }
            if (cmd[0].equals("clanskills")) {
                if ((player.getClan() == null) || (player.getClan().getLevel() < 5)) {
                    player.sendMessage(Config.SM_LANG_CLAN_NOT_FOUND_OR_LVL);
                    return;
                }

                if (!player.destroyItemByItemId("Clan Skills", Config.SM_ITEM_ID, Config.SM_CLAN_SKILLS, player.getLastFolkNPC(), true)) {
                    return;
                }

                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));
                player.getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));
                player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));
                player.sendMessage(Config.SM_LANG_CLAN_TAKE_ALL_SKILLS);
                return;
            }

            if (cmd[0].equals("clancrp10k")) {
                if ((player.getClan() == null) || (player.getClan().getLevel() < 5)) {
                    player.sendMessage(Config.SM_LANG_CLAN_NOT_FOUND_OR_LVL);
                    return;
                }

                if (!player.destroyItemByItemId("Clan Skills", Config.SM_ITEM_ID, Config.SM_10K_CRP, player.getLastFolkNPC(), true)) {
                    return;
                }

                player.getClan().setReputationScore(player.getClan().getReputationScore() + 10000, true);
                return;
            }
            if (cmd[0].equals("hero")) {
                long heroTime = 0L;
                if (cmd.length > 1) {
                    try {
                        heroTime = Integer.parseInt(cmd[1]) * 24L * 60L * 60L * 1000L;
                    } catch (NumberFormatException nfe) {
                    }

                } else {
                    return;
                }

                if (player.isHero()) {
                    player.sendMessage(Config.SM_LANG_HERO_ALWAYS_HAVE);
                    return;
                }

                int days = Integer.parseInt(cmd[1]);

                if (Config.SM_HERO.get(days) == null) {
                    player.sendMessage(Config.SM_LANG_UNKNOWN_EXCEPTION_HAPPEND);
                    return;
                }

                int endPrice = ((Integer) Config.SM_HERO.get(days)).intValue();

                if (!player.destroyItemByItemId("CustomHero" + days, Config.SM_ITEM_ID, endPrice, player.getLastFolkNPC(), true)) {
                    return;
                }

                player.setIsHero(true);
                updateDatabaseHero(player, true, heroTime);
                player.sendMessage(Config.SM_LANG_HERO_TAKED_DAYS.replace("%days%", String.valueOf(days)));
                player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
                player.broadcastUserInfo();
            } else if (cmd[0].equals("premium")) {
                long premiumTime = 0L;
                if (cmd.length > 0) {
                    try {
                        premiumTime = Integer.parseInt(cmd[1]) * 24L * 60L * 60L * 1000L;
                    } catch (NumberFormatException nfe) {
                    }

                } else {
                    return;
                }

                if (player.isDonator()) {
                    player.sendMessage(Config.SM_LANG_PREMIUM_ALWAYS_HAVE);
                    return;
                }

                int days = Integer.parseInt(cmd[1]);
                if (Config.SM_PREMIUM.get(Integer.valueOf(days)) == null) {
                    player.sendMessage(Config.SM_LANG_UNKNOWN_EXCEPTION_HAPPEND);
                    return;
                }

                int endPrice = ((Integer) Config.SM_PREMIUM.get(Integer.valueOf(days))).intValue();

                if (!player.destroyItemByItemId("Premium" + days, Config.SM_ITEM_ID, endPrice, player.getLastFolkNPC(), true)) {
                    return;
                }

                player.setDonator(true);
                player.updateNameTitleColor();
                player.sendMessage(Config.SM_LANG_PREMIUM_TAKED_DAYS.replace("%days%", String.valueOf(days)));
                updateDatabasePremium(player, premiumTime);
                player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
                player.broadcastUserInfo();
                return;
            }
        }
    }

    public void showMain(L2PcInstance activeChar) {
        String htm = HtmCache.getInstance().getHtm("data/html/ServiceManager/" + getNpcId() + ".htm");
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setHtml(htm);
        html.replace("%objectId%", String.valueOf(getObjectId()));

        activeChar.sendPacket(html);
        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void changeColor(L2PcInstance player, int i, int colorId) {
        boolean check = false;
        if (i == 1) {
            if (player.destroyItemByItemId("Name Color Change", Config.SM_ITEM_ID, Config.SM_NAME_COLOR, player.getLastFolkNPC(), true)) {
                check = true;
            }
        } else if (i == 2) {
            if (player.destroyItemByItemId("Title Color Change", Config.SM_ITEM_ID, Config.SM_TITLE_COLOR, player.getLastFolkNPC(), true)) {
                check = true;
            }
        }

        String newcolor = "";

        switch (colorId) {
            case 1:
                newcolor = "FFFF00";
                break;
            case 2:
                newcolor = "000000";
                break;
            case 3:
                newcolor = "FF0000";
                break;
            case 4:
                newcolor = "FF00FF";
                break;
            case 5:
                newcolor = "808080";
                break;
            case 6:
                newcolor = "008000";
                break;
            case 7:
                newcolor = "00FF00";
                break;
            case 8:
                newcolor = "800000";
                break;
            case 9:
                newcolor = "008080";
                break;
            case 10:
                newcolor = "800080";
                break;
            case 11:
                newcolor = "808000";
                break;
            case 12:
                newcolor = "FFFFFF";
                break;
            case 13:
                newcolor = "00FFFF";
                break;
            case 14:
                newcolor = "C0C0C0";
                break;
            case 15:
                newcolor = "17A0D4";
                break;
            default:
                return;
        }

        if (check) {
            if (i == 1) {
                player.getAppearance().setNameColor(Integer.decode("0x" + newcolor).intValue());
                player.broadcastUserInfo();
                player.store();
                player.sendMessage(Config.SM_LANG_NAME_COLOR_CHANGE_OK);
            } else if (i == 2) {
                player.getAppearance().setTitleColor(Integer.decode("0x" + newcolor).intValue());
                player.broadcastUserInfo();
                player.store();
                player.sendMessage(Config.SM_LANG_TITLE_COLOR_CHANGE_OK);
            }
        }
    }

    private void updateDatabasePremium(L2PcInstance player, long premiumTime) {
        Connection con = null;
        try {
            if (Config.USE_RL_DATABSE) {
                con = LoginRemoteDbFactory.getInstance().getConnection();
            } else {
                con = L2DatabaseFactory.getInstance().getConnection();
            }
            premiumTime += System.currentTimeMillis();
            PreparedStatement stmt = con.prepareStatement("UPDATE accounts SET premium = ? WHERE login = ?");
            stmt.setLong(1, premiumTime);
            stmt.setString(2, player.getAccountName());
            stmt.execute();
            stmt.close();
            stmt = null;
        } catch (Exception e) {
        }
    }

    private void updateDatabaseHero(L2PcInstance player, boolean newHero, long heroTime) {
        Connection con = null;
        try {
            if (player == null) {
                return;
            }
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM characters_custom_data WHERE obj_Id=?");
            statement.setInt(1, player.getObjectId());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                PreparedStatement stmt = con.prepareStatement(newHero ? "UPDATE characters_custom_data SET noble=1, hero=1, hero_end_date=? WHERE obj_Id=?" : "UPDATE characters_custom_data SET hero = 0, hero_end_date=0 WHERE obj_Id=?");

                if (newHero) {
                    stmt.setLong(1, heroTime == 0L ? 0L : System.currentTimeMillis() + heroTime);
                    stmt.setInt(2, player.getObjectId());
                    stmt.execute();
                } else {
                    stmt.setInt(1, player.getObjectId());
                    stmt.execute();
                }
                stmt.close();
                stmt = null;
            } else if (newHero) {
                PreparedStatement stmt = con.prepareStatement("INSERT INTO characters_custom_data (obj_Id, char_name, noble, hero, hero_end_date) VALUES (?,?,?,?,?)");
                stmt.setInt(1, player.getObjectId());
                stmt.setString(2, player.getName());
                stmt.setInt(3, 1);
                stmt.setInt(4, 1);
                stmt.setLong(5, heroTime == 0L ? 0L : System.currentTimeMillis() + heroTime);
                stmt.execute();
                stmt.close();
                stmt = null;
            }

            result.close();
            statement.close();

            result = null;
            statement = null;
        } catch (Exception e) {
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
            con = null;
        }
    }
}