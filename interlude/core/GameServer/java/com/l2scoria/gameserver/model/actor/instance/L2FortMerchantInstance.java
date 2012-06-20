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

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.sql.ClanTable;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.util.random.Rnd;

import java.util.StringTokenizer;

/**
 * @author programmos, scoria dev
 */
public class L2FortMerchantInstance extends L2NpcWalkerInstance
{
	public L2FortMerchantInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if(!canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
				// to display a social action of the L2NpcInstance on their client
				SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
				broadcastPacket(sa);
				sa = null;

				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String par = "";
		if(st.countTokens() >= 1)
		{
			par = st.nextToken();
		}

		//_log.info("actualCommand : " + actualCommand);
		//_log.info("par : " + par);

		if(actualCommand.equalsIgnoreCase("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch(IndexOutOfBoundsException ioobe)
			{}
			catch(NumberFormatException nfe)
			{}
			showMessageWindow(player, val);
		}
		else if(actualCommand.equalsIgnoreCase("showSiegeInfo"))
		{
			showSiegeInfoWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
		st = null;
		actualCommand = null;
	}

	private void showMessageWindow(L2PcInstance player)
	{
		showMessageWindow(player, 0);
	}

	private void showMessageWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		String filename;

		if(val == 0)
		{
			filename = "data/html/fortress/merchant.htm";
		}
		else
		{
			filename = "data/html/fortress/merchant-" + val + ".htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));

		if(getFort().getOwnerId() > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(getFort().getOwnerId());
			html.replace("%clanname%", clan != null ? clan.getName() : "ClanNotFound");
		}
		else
		{
			html.replace("%clanname%", "NPC");
		}

		html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
		player.sendPacket(html);
		filename = null;
		html = null;
	}

	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 * 
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if(validateCondition(player))
		{
			getFort().getSiege().listRegisterClan(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/fortress/merchant-busy.htm");
			html.replace("%fortname%", getFort().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			html = null;
		}
	}

	private boolean validateCondition(L2PcInstance player)
	{
		if(getFort().getSiege().getIsInProgress())
			return false; // Busy because of siege
		return true;
	}

}
