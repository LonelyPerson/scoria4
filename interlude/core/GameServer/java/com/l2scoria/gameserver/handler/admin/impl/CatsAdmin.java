/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.Disconnection;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.util.L2Utils;
import javolution.text.TextBuilder;
import org.apache.log4j.Logger;
import ru.catssoftware.protection.CatsGuard;

/**
 *
 * @author zenn
 */
public class CatsAdmin extends AdminAbst {
    private static final Logger _log = Logger.getLogger(CatsAdmin.class.getName());
    
    public CatsAdmin() {
        _commands = new String[] {
        "admin_hwid",
	"admin_hwidban",
	"admin_hwidunban",
	"admin_hwidlist",
	"admin_hwidbanned"
        };
    }
    
    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
	if(!super.useAdminCommand(command, activeChar))
	{
		return false;
	}
        
        if(!CatsGuard.getInstance().isEnabled())
        {
            return false;
        }
        if(command.equals("admin_hwid"))
        {
            String html = "<html><title>Scoria admin - catsguard free</title><body><center><br>";
            html +="<edit var=\"char_name\" width=110 height=15><br>";
            html +="<table width=200><tr>";
            html +="<td><button action=\"bypass -h admin_hwidban $char_name\" value=\"Ban HWID\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td>";
            html +="<td><button action=\"bypass -h admin_hwidunban $char_name\" value=\"Unban\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td>";
            html +="</tr></table>";
            html +="Command help<br>";
            html +="//hwidban char_name - ban character hwid<br1>";
            html +="//hwidunban char_name - unban character hwid<br1>";
            html +="//hwidbanned - show banned hwid list<br1>";
            html +="//hwidlist - show players online with HWID<br1>";
            html +="</center></body></html>";
            NpcHtmlMessage msg = new NpcHtmlMessage(0);
            msg.setHtml(html);
            activeChar.sendPacket(msg);
        }
        else if(command.startsWith("admin_hwidban"))
        {
            String hwid = null;
            String value = command.substring(14);
            if(value == null || value.length() < 2)
            {
                if(activeChar.getTarget().isPlayer) 
                {
                    hwid = ((L2PcInstance)activeChar.getTarget()).getHWid();
                }
            } 
            else
            {
                L2PcInstance objectBan = L2World.getInstance().getPlayer(value);
                if(objectBan != null)
                {
                    hwid = objectBan.getHWid();
                }
            }
            if(hwid != null)
            {
                try {
                    CatsGuard.getInstance().ban(hwid);
                    for(L2PcInstance pc : L2World.getInstance().getAllPlayers()) 
                    {
                        if(pc.getHWid()!=null && pc.getHWid().equals(hwid))
                        {
                            pc.logout();
                        }
                    }
                    activeChar.sendMessage("Hwid "+hwid+" was banned!");
                } catch(Exception e) {
                    activeChar.sendMessage("Character dosn`t exists!");
                }
            }
            
        }
        else if(command.startsWith("admin_hwidunban")) {
            String value = command.substring(16);
            if(value == null || value.length() < 3)
            {
                activeChar.sendMessage("Empty params or length < 3 chars");
            }
            else
            {
                String hwid_by_player = L2Utils.getDataHwid(value);
                if(hwid_by_player != null)
                {
                    CatsGuard.getInstance().unban(hwid_by_player);
                    activeChar.sendMessage("Player "+value+" is unbanned by hwid "+hwid_by_player);
                }
                else
                {
                    CatsGuard.getInstance().unban(value);
                    activeChar.sendMessage("Hwid "+value+" try to be unbanned. Look at game log.");
                }
            }
        }
       
      return true;
     }
    
}
