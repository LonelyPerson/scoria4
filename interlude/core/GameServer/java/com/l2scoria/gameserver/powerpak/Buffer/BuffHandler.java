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
package com.l2scoria.gameserver.powerpak.Buffer;

import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.handler.IBBSHandler;
import com.l2scoria.gameserver.handler.ICustomByPassHandler;
import com.l2scoria.gameserver.handler.IVoicedCommandHandler;
import com.l2scoria.gameserver.managers.TownManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.powerpak.Buffer.BuffTable.Buff;
import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import com.l2scoria.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2scoria.gameserver.util.Util;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 
 * 
 * @author Nick
 */
public class BuffHandler implements IVoicedCommandHandler, ICustomByPassHandler, IBBSHandler
{
	private static final Logger _log = Logger.getLogger(BuffHandler.class.getName());
	private Map<Integer,ArrayList<Buff>> _buffs;
	private Map<Integer, String> _visitedPages;
	private ArrayList<Buff> getOwnBuffs(int objectId)
	{
		if(_buffs.get(objectId)==null)
			synchronized(_buffs)
			{
				_buffs.put(objectId,new ArrayList<Buff>());
			}
		return _buffs.get(objectId);
	}

	public BuffHandler()
	{
		_buffs = new FastMap<Integer,ArrayList<Buff>>();
		_visitedPages = new FastMap<Integer,String>();
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return new String[] {PowerPakConfig.BUFFER_COMMAND};
	}

	private boolean checkAllowed(L2PcInstance activeChar)
	{
		String msg = null;
		if (activeChar.isSitting())
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_SITDOWN;
		else if (activeChar.isCastingNow())
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_CASTING;
		else if (activeChar.isDead())
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_DEAD;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("ALL"))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquiped())
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_CURSED;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().getAttackStanceTask(activeChar))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_ATTACK;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("DUNGEON") && activeChar.isIn7sDungeon())
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_DUNG;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Character.ZONE_PVP))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Character.ZONE_PEACE))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("NOTINTOWN") && TownManager.getInstance().getTown(activeChar.getX(), activeChar.getY(), activeChar.getZ()) == null)
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_AREA;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("OLYMPIAD") && (activeChar.isInOlympiadMode() ||
				activeChar.isInsideZone(L2Character.ZONE_OLY) || Olympiad.getInstance().isRegistered(activeChar) ||
				Olympiad.getInstance().isRegisteredInComp(activeChar))) 
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_OLY;
		else if(PowerPakConfig.BUFFER_EXCLUDE_ON.contains("EVENT") && (activeChar._event!=null && activeChar._event.isRunning()))
			msg = PowerPakConfig.BUFF_NOT_ALLOWED_EVENT;
		
		if(msg!=null)
			activeChar.sendMessage(msg);

		return msg==null;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if(activeChar == null)
			return  false;

		if(!checkAllowed(activeChar))
			return false;

		if(command.compareTo(PowerPakConfig.BUFFER_COMMAND)==0)
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(activeChar.getLastQuestNpcObject());
			String text = HtmCache.getInstance().getHtm("data/html/default/50019.htm");
			htm.setHtml(text);
			activeChar.sendPacket(htm);
		}
		return false;
	}

	private static final String [] _BYPASSCMD = {"dobuff"};
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}

	@Override
	public void handleCommand(String command, L2PcInstance player, String parameters)
	{
		if (player==null)
			return;

		if(!checkAllowed(player))
			return;

		L2NpcInstance buffer = null;
		if(player.getTarget()!=null)
			if(player.getTarget() instanceof L2NpcInstance)
			{
				buffer = (L2NpcInstance)player.getTarget();
				if(buffer.getTemplate().getNpcId()!=50018)
					buffer=null;
			}

		if(parameters.compareTo("ClearBuffs")==0)
		{
			getOwnBuffs(player.getObjectId()).clear();
			player.sendMessage("Ваш набор бафов очищен");
		}
		else if(parameters.compareTo("RemoveAll")==0)
		{
			for(L2Effect e : player.getAllEffects())
			{
				if(e.getEffectType()==L2Effect.EffectType.BUFF)
					player.removeEffect(e);
			}
		}
		else if(parameters.startsWith("Chat"))
		{
			String chatIndex = parameters.substring(4).trim();
			synchronized(_visitedPages)
			{
				_visitedPages.put(player.getObjectId(), chatIndex);
			}
			
			if(chatIndex.length()>0)
				if(chatIndex.compareTo("0")==0)
					chatIndex = "";
				else
					chatIndex = "-" + chatIndex;
			String text;
			if(chatIndex.length()>0)
				text = HtmCache.getInstance().getHtm("data/html/buffer/buffer" + chatIndex + ".htm");
			else 
				text = HtmCache.getInstance().getHtm("data/html/default/50019.htm");

			if (text != null)
			{
				text = text.replace("%SCHEMA_INFO%", getProfiles(player));
			}

			if(command.startsWith("bbsbuff"))
			{
				text = text.replace("-h custom_do", "bbs_bbs");
				BaseBBSManager.separateAndSend(text, player);
			}
			else
			{
				NpcHtmlMessage htm = new NpcHtmlMessage(player.getLastQuestNpcObject());
				htm.setHtml(text);
				player.sendPacket(htm);
			}
		}
		else if(parameters.startsWith("SaveBuffs"))
		{
			String param = parameters.substring(9).trim();

			if(!Util.isAlphaNumeric(param) || param.length() < 2 || param.length() > 16)
			{
				player.sendMessage("Имя профиля некорректно");
				return;
			}

			Pattern pattern;
			try
			{
				pattern = Pattern.compile(PowerPakConfig.BUFF_TEMPLATE);
			}
			catch(PatternSyntaxException e) // case of illegal pattern
			{
				_log.warn("ERROR : Buff profile name pattern is wrong!");
				pattern = Pattern.compile(".*");
			}

			Matcher match = pattern.matcher(param);

			if(!match.matches())
			{
				player.sendMessage("Имя профиля некорректно.");
				return;
			}
			match = null;
			pattern = null;

			ArrayList<Buff> buffs = getOwnBuffs(player.getObjectId());

			if(buffs==null || buffs.size()==0)
			{
				player.sendMessage("Ваш набор бафов отсутствует");
				return;
			}
			
			if (player.getBuffProfiles() != null && player.getBuffProfiles().size() >= PowerPakConfig.BUFFER_MAX_PROFILES && player.getBuffProfiles().get(param) == null)
			{
				player.sendMessage("You exceed the profiles limit");
				return;
			}
			
			ArrayList<Integer> IDs = new ArrayList<Integer>();

			for (Buff buff : buffs)
			{
				IDs.add(buff._id);
			}

			player.saveBuffProfile(param, IDs);
			useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
		}
		else if(parameters.startsWith("RestoreAll")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE*3) {
				player.sendMessage("У вас недостаточно адены");
				return;
			}
			player.getStatus().setCurrentCp(player.getMaxCp());
			player.getStatus().setCurrentMp(player.getMaxMp());
			player.getStatus().setCurrentHp(player.getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE*3, null, true);
		}
		else if(parameters.startsWith("RestoreCP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("У вас недостаточно адены");
				return;
			}
			player.getStatus().setCurrentCp(player.getMaxCp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestoreMP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("У вас недостаточно адены");
				return;
			}
			player.getStatus().setCurrentMp(player.getMaxMp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RestoreHP")) {
			if(player.getAdena()<PowerPakConfig.BUFFER_PRICE) {
				player.sendMessage("У вас недостаточно адены");
				return;
			}
			player.getStatus().setCurrentHp(player.getMaxHp());
			player.reduceAdena("Buff", PowerPakConfig.BUFFER_PRICE, null, true);
		}
		else if(parameters.startsWith("RemoveProfile")) {
			String param = parameters.substring(13).trim();
			player.removeProfile(param);
			useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
		}
		else if(parameters.startsWith("MakeBuffs") || parameters.startsWith("RestoreBuffs") || parameters.startsWith("ProfBuffs") || parameters.startsWith("DonatorMakeBuffs"))
		{
                        String buffName;
                        if(parameters.startsWith("DonatorMakeBuffs"))
                            buffName = parameters.substring(16).trim();
                        else
                            buffName = parameters.substring(9).trim();
			int totaladena = 0;
			ArrayList<Buff> buffs = null;
			if(parameters.startsWith("RestoreBuffs"))
				buffs = getOwnBuffs(player.getObjectId());
			else if(parameters.startsWith("ProfBuffs"))
			{
				buffs = BuffTable.getInstance().getBuffsForId(player.getBuffProfiles().get(buffName));
				getOwnBuffs(player.getObjectId()).clear();
				getOwnBuffs(player.getObjectId()).addAll(buffs);
			}
                        else {
				buffs = BuffTable.getInstance().getBuffsForName(buffName);
                        }
                        
                        if(parameters.startsWith("DonatorMakeBuffs"))
                        {
                            if(!player.isDonator())
                            {
                                player.sendMessage("Данный баф доступен только для донаторов");
                                return;
                            }
                        }
                        
                        
			if(buffs!=null && buffs.size()==1)
			{
				if(!getOwnBuffs(player.getObjectId()).contains(buffs.get(0)))
					getOwnBuffs(player.getObjectId()).add(buffs.get(0));
			}
			if(buffs==null || buffs.size()==0)
			{
				player.sendMessage("Ваш набор бафов отсутствует");
				return;
			}
			for(Buff buff: buffs)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(buff._skillId, buff._skillLevel);
				if(skill!=null)
				{
					if(player.getLevel()>= buff._minLevel && player.getLevel()<=buff._maxLevel)
					{
						if(buff._price>0)
						{
							totaladena+=buff._price;
							if(player.getAdena()<totaladena)
							{
								player.sendMessage("У вас недостаточно адены");
								break;
							} 
						}
						if(!buff._force && buffer!=null)
						{
							buffer.setBusy(true);
							buffer.setCurrentMp(buffer.getMaxMp());
							buffer.setTarget(player);
							buffer.doCast(skill);
							buffer.setBusy(false);
						}
						else
							skill.getEffects(player, player);
					}
					try
					{
						Thread.sleep(100); // Задержко что бы пакетами не зафлудить..
					}
					catch (InterruptedException e)
					{
						//null
					}
				}
			}
			if(totaladena>0)
				player.reduceAdena("Buff", totaladena, null, true);
			if(_visitedPages.get(player.getObjectId())!=null)
				handleCommand(command,player,"Chat "+_visitedPages.get(player.getObjectId()));
			else 
				useVoicedCommand(PowerPakConfig.BUFFER_COMMAND, player, "");
		}
	}
	
	public String getProfiles(L2PcInstance player)
	{
		String text = "";
		if (PowerPakConfig.BUFFER_HTML_NAME == "" ||
			PowerPakConfig.BUFFER_HTML_USE == ""||
			PowerPakConfig.BUFFER_HTML_REMOVE == "")
		{
			return "Error in profiles configuration file: values not set.";
		}

		if (player.getBuffProfiles() != null)
		{
			text += "<table width=260 border=0>";

			for (Map.Entry<String, ArrayList<Integer>> elem : player.getBuffProfiles().entrySet())
			{
				//text += "<tr><td width=80><center>" + elem.getKey() + "</center></td>";
				//text += "<td><button value=\"use profile\" action=\"bypass -h custom_dobuff ProfBuffs " + elem.getKey() + "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td>";
				//text += "<td><button value=\"delete profile\" action=\"bypass -h custom_dobuff RemoveProfile " + elem.getKey() + "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>";
				text += "<tr>";
				text += PowerPakConfig.BUFFER_HTML_NAME.replace("%ElemKey%", elem.getKey());
				text += PowerPakConfig.BUFFER_HTML_USE.replace("%ActionKey%", "action=\"bypass -h custom_dobuff ProfBuffs " + elem.getKey() + "\" ");
				text += PowerPakConfig.BUFFER_HTML_REMOVE.replace("%ActionKey%", "action=\"bypass -h custom_dobuff RemoveProfile " + elem.getKey() + "\" ");
				text += "</tr>";
			}
			text += "</table>";
		}
		
		return text;
	}

	private static String [] _BBSCommand = {"bbsbuff"};
	@Override
	public String[] getBBSCommands()
	{
		return _BBSCommand;
	}
}