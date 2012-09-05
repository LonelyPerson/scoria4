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
package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.entity.Hero;
import com.l2scoria.gameserver.model.entity.olympiad.Olympiad;
import com.l2scoria.gameserver.model.multisell.L2Multisell;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ExHeroList;
import com.l2scoria.gameserver.network.serverpackets.InventoryUpdate;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.util.List;

public class L2OlympiadManagerInstance extends L2FolkInstance
{
	private static Logger _logOlymp = Logger.getLogger(L2OlympiadManagerInstance.class.getName());

	private static final int GATE_PASS = Config.ALT_OLY_COMP_RITEM;

	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
			return;

		if(command.startsWith("OlympiadDesc"))
		{
			int val = Integer.parseInt(command.substring(13, 14));
			String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
			suffix = null;
		}
		else if(command.startsWith("OlympiadNoble"))
		{
			if(!player.isNoble() || player.getClassId().getId() < 88)
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage reply;
			TextBuilder replyMSG;

			switch(val)
			{
				case 1:
					Olympiad.getInstance().unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					int[] array = Olympiad.getInstance().getWaitingList();

					if(array != null)
					{
						classed = array[0];
						nonClassed = array[1];
					}

					reply = new NpcHtmlMessage(getObjectId());
					replyMSG = new TextBuilder("<html><body>");
					replyMSG.append("The number of people on the waiting list for " + "Grand Olympiad" + "<center>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<table width=270 border=0 bgcolor=\"000000\">" + "<tr>" + "<td align=\"left\">General</td>" + "<td align=\"right\">" + classed + "</td>" + "</tr>" + "<tr>" + "<td align=\"left\">Not class-defined</td>" + "<td align=\"right\">" + nonClassed + "</td>" + "</tr>" + "</table><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>" + "<button value=\"Back\" action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\" " + "width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");

					replyMSG.append("</body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 3:
					int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
					if(points >= 0)
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new TextBuilder("<html><body>");
						replyMSG.append("There are " + points + " Grand Olympiad " + "points granted for this event.<br><br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\">Return</a>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 4:
					Olympiad.getInstance().registerNoble(player, false);
					break;
				case 5:
					Olympiad.getInstance().registerNoble(player, true);
					break;
				case 6:
					int passes = Olympiad.getInstance().getNoblessePasses(player.getObjectId());
					if(passes > 0)
					{
						L2ItemInstance item = player.getInventory().addItem("Olympiad", GATE_PASS, passes, player, this);

						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendPacket(iu);
						iu = null;

						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
						sm.addNumber(passes);
						sm.addItemName(item.getItemId());
						player.sendPacket(sm);
						item = null;
						sm = null;
					}
					else
					{
						reply = new NpcHtmlMessage(getObjectId());
						reply.setFile(Olympiad.OLYMPIAD_HTML_FILE + "noble_nopoints2.htm");
						reply.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(reply);
					}
					break;
				case 7:
					L2Multisell.getInstance().SeparateAndSend(102, player, false, getCastle().getTaxRate());
					break;
				default:
					_logOlymp.warn("Olympiad System: Couldnt send packet for request " + val);
					break;

			}
			reply = null;
			replyMSG = null;
		}
		else if(command.startsWith("Olympiad"))
		{
			if(player._event!=null) {
				player.sendMessage("Вы не можете наблюдать, если зарегистрированы на эвент");
				return;
			}

			int val = Integer.parseInt(command.substring(9, 10));

			NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
			TextBuilder replyMSG = new TextBuilder("<html><body>");

			switch(val)
			{
				case 1:
					if (!Olympiad.getInstance().inCompPeriod())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
						break;
					}

					FastMap<Integer, String> matches = Olympiad.getInstance().getMatchList();

					replyMSG.append("<center><br>Grand Olympiad Games Overview<br><table width=270 border=0 bgcolor=\"000000\">");
					replyMSG.append("<tr><td fixwidth=30>Number</td><td fixwidth=60>Status</td><td>Player 1 / Player 2</td></tr>");

					for (int i = 0; i < Olympiad.getStadiaSize(); i++)
					{
						if (matches != null && matches.containsKey(i))
						{
							String[] elem = matches.get(i).split(" ");
							replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + i + "\">" + i + "</a></td><td fixwidth=60>" + elem[0] + "</td><td>" + elem[1] + "&nbsp;" + elem[2] + "</td></tr>");
						}
						else
						{
							replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + i + "\">" + i + "</a></td><td fixwidth=60>&$906;</td><td>&nbsp;</td></tr>");
						}
					}

					replyMSG.append("</table></center></body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 2:
					// for example >> Olympiad 2_88
					int classId = Integer.parseInt(command.substring(11));
					if(classId >= 88)
					{
						replyMSG.append("<center>Grand Olympiad Ranking");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");

						int index = 1;
						List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
						if(names.size() != 0)
						{
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");

							for(String name : names)
							{
								replyMSG.append("<tr>");
								replyMSG.append("<td align=\"left\">" + index + "</td>");
								replyMSG.append("<td align=\"right\">" + name + "</td>");
								replyMSG.append("</tr>");
								index++;

								if(index > Config.ALT_OLY_MAX_NOBLE_LIST)
									break;
							}

							replyMSG.append("</table>");
						}

						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						if(index > Config.ALT_OLY_MAX_NOBLE_LIST)
							replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 5_" + classId + "_1\">next page</a>");
						replyMSG.append("</center>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 3:
					int id = Integer.parseInt(command.substring(11));
					Olympiad.getInstance().addSpectator(id, player);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				case 5:
					// for example >> Olympiad 5_88_1
					String[] paramList = command.substring(11).split("_");
					int clasId = Integer.parseInt(paramList[0]);
					int page = Integer.parseInt(paramList[1]);
					if(clasId >= 88)
					{
						replyMSG.append("<center>Grand Olympiad Ranking");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");

						int index = Config.ALT_OLY_MAX_NOBLE_LIST * page + 1;
						List<String> names = Olympiad.getInstance().getClassLeaderBoard(clasId);
						if(names.size() != 0)
						{
							int ind = 1;
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");

							for(String name : names)
							{
								if(ind++ < index)
									continue;

								replyMSG.append("<tr>");
								replyMSG.append("<td align=\"left\">" + index + "</td>");
								replyMSG.append("<td align=\"right\">" + name + "</td>");
								replyMSG.append("</tr>");
								index++;

								if(index > Config.ALT_OLY_MAX_NOBLE_LIST * (page + 1))
									break;
							}

							replyMSG.append("</table>");
						}

						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						if(page == 1)
							replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 2_" + clasId + "\">return</a>");
						else if(page > 1)
							replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 5_" + clasId + "_" + (page-1) + "\">return</a>");
						if(index > Config.ALT_OLY_MAX_NOBLE_LIST * (page + 1))
							replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 5_" + clasId + "_" + (page+1) + "\">next page</a>");
						replyMSG.append("</center>");
						replyMSG.append("</body></html>");

						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 6:
					if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
					{
						replyMSG.append("Monument of Heroes:<br><br>");
						if (Olympiad.getInstance().getPeriod() == 1)
						{
							replyMSG.append("Grand Olympiad is in validation period."); //Custom message...
						}
						else if(Hero.getInstance().getConfirmed(player.getObjectId()) == 0)
						{
							Hero.getInstance().confirmHero(player.getObjectId());
							replyMSG.append("You are now a Hero!");//Custom message...
						}
						else
						{
							replyMSG.append("You are already a Hero!");//Custom message...
						}
						replyMSG.append("</body></html>");
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				default:
					_logOlymp.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
			reply = null;
			replyMSG = null;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_FILE;

		filename += "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";

		if(filename.equals(Olympiad.OLYMPIAD_HTML_FILE + "noble_desc0.htm"))
		{
			filename = Olympiad.OLYMPIAD_HTML_FILE + "noble_main.htm";
		}

		showChatWindow(player, filename);
		filename = null;
	}
}
