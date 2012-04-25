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
package com.l2scoria.gameserver.powerpak.rss;

import java.net.URL;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2scoria.gameserver.communitybbs.Manager.BaseBBSManager;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.powerpak.PowerPakConfig;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

public class rss
{
	private static rss _instance = null;
	private FastMap<String, String> _records = new FastMap<String, String>();
	private FastMap<String, String> _rss = new FastMap<String, String>();

	public static rss getInstance()
	{
		if(_instance == null)
		{
			_instance = new rss();
		}
		return _instance;
	}

	private rss()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new loadContent(), 100, PowerPakConfig.RSS_INTERVAL * 60000);
	}

	class loadContent implements Runnable
	{
		public void run()
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			factory.setCoalescing(true);

			_rss = new FastMap<String, String>();

			for(String address : PowerPakConfig.RSS_URL.split(";"))
			{
				try
				{
					URL url = new URL(address);
					Document doc = factory.newDocumentBuilder().parse(url.openStream());

					String channelTitle = "";
					String text = "<html><body><br><br>Can`t recive RSS data.</body></html>";
					
					_records = new FastMap<String, String>();

					for(Node Rss = doc.getFirstChild(); Rss != null; Rss = Rss.getNextSibling())
					{
						if("rss".equalsIgnoreCase(Rss.getNodeName()))
						{
							for(Node channel = Rss.getFirstChild(); channel != null; channel = channel.getNextSibling())
							{
								if("channel".equalsIgnoreCase(channel.getNodeName()))
								{
									for(Node values = channel.getFirstChild(); values != null; values = values.getNextSibling())
									{
										if("title".equalsIgnoreCase(values.getNodeName()))
										{
											channelTitle = values.getTextContent();
										}
										else if("item".equalsIgnoreCase(values.getNodeName()))
										{
											String desc = "", title = "";
											for(Node item = values.getFirstChild(); item != null; item = item.getNextSibling())
											{
												if("title".equalsIgnoreCase(item.getNodeName()))
												{
													title = item.getTextContent();
												}
												else if("description".equalsIgnoreCase(item.getNodeName()))
												{
													desc = item.getTextContent();
												}
											}
											if (desc != "" && title != "" && desc != null && title != null)
											{
												_records.put(title, desc);
											}
										}
									}
								}
							}
						}
					}

					if (!_records.isEmpty())
					{
						TextBuilder html = new TextBuilder("<html><body><br><br>");
						for(FastMap.Entry<String, String> elem = _records.head(), end = _records.tail(); (elem = elem.getNext()) != end;)
						{
							if (html.toString().length() + elem.getKey().length() + elem.getValue().length() > 12200)
								break;

							html.append(". <font color=\"30FF30\">" + elem.getKey() + "</font><br>");
							html.append(elem.getValue() + "<br><br>");
						}
						html.append("</body></html>");

						text = html.toString();
					}

					if (channelTitle != null && channelTitle != "")
					{
						_rss.put(channelTitle, text);
					}
				}
				catch(Exception e)
				{
					System.out.println("RSS error: " + e.getMessage());
				}
			}
		}
	}
	
	public void showRecord(String channel, L2PcInstance activeChar)
	{
		BaseBBSManager.separateAndSend(_rss.get(channel), activeChar);
	}
	
	public void showList(L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder("<html><body><br><br><center>");
		for(String channel : _rss.keySet())
		{
			html.append("<button value=\""+channel+"\" action=\"bypass bbs_bbsrss record "+channel+"\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"><br1>");
		}
		html.append("</center></body></html>");
		BaseBBSManager.separateAndSend(html.toString(), activeChar);
	}
}
