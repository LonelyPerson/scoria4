/* This program is free software; you can redistribute it and/or modify
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

import java.util.StringTokenizer;

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.model.entity.event.TvTEvent;
import com.l2scoria.gameserver.model.entity.siege.Castle;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.MyTargetSelected;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.util.random.Rnd;

/**
 * @author  Kerberos
 */

public class L2CastleMagicianInstance extends L2FolkInstance
{
	protected static final int	COND_ALL_FALSE				= 0;
	protected static final int	COND_BUSY_BECAUSE_OF_SIEGE	= 1;
	protected static final int	COND_OWNER					= 2;

	/**
	* @param template
	*/
	public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
				// to display a social action of the L2NpcInstance on their client
				SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
				broadcastPacket(sa);
				sa = null;

				showChatWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/castlemagician/magician-no.htm";
		int condition = validateCondition(player);

		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlemagician/magician-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				filename = "data/html/castlemagician/magician.htm";
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);

		if(condition > COND_ALL_FALSE)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			if(actualCommand.equalsIgnoreCase("clan_gate"))
			{
				L2PcInstance leader = (L2PcInstance) L2World.getInstance().findObject(player.getClan().getLeaderId());
				
				if (checkSummon(leader, player))
				{
					Castle castle = getCastle();
					player.teleToLocation(castle.getGateX(), castle.getGateY(), castle.getGateZ());
					player.sendMessage("You have been teleported to your leader.");
					player.stopMove(new L2CharPosition(castle.getGateX(), castle.getGateY(), castle.getGateZ(), player.getHeading()));
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				leader = null;
			}
			else
				super.onBypassFeedback(player, command);
		}
	}
	
	public boolean checkSummon(L2PcInstance leader, L2PcInstance player)
	{
		if(!getCastle().isGateOpen())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/castlemagician/magician-nogate.htm");
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			html = null;
			return false;
		}

		if(leader == null)
		{
			player.sendMessage("Your Leader is not online.");
			return false;
		}

		if(leader.isFlying())
		{
			player.sendMessage("Your Leader is in bad condition.");
			return false;
		}

		if(leader.isInsideZone(L2Character.ZONE_PVP))
		{
			player.sendMessage("Your Leader is in PvP zone.");
			return false;
		}

		if(leader.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			player.sendMessage("Your Leader is in zone which blocking summon.");
			return false;
		}

		if(leader.atEvent || TvTEvent.isPlayerParticipant(leader.getObjectId()))
		{
			player.sendMessage("Your leader is in an event.");
			return false;
		}
		
		if(leader.isInJail())
		{
			player.sendMessage("Your leader is in Jail.");
			return false;
		}

		if(leader.isInOlympiadMode())
		{
			player.sendMessage("Your leader is in the Olympiad now.");
			return false;
		}

		if(leader.inObserverMode())
		{
			player.sendMessage("Your leader is in Observ Mode.");
			return false;
		}

		if(leader.isInDuel())
		{
			player.sendMessage("Your leader is in a duel.");
			return false;
		}

		if(leader.isFestivalParticipant())
		{
			player.sendMessage("Your leader is in a festival.");
			return false;
		}

		if(leader.isInParty() && leader.getParty().isInDimensionalRift())
		{
			player.sendMessage("Your leader is in dimensional rift.");
			return false;
		}

		if(leader.getClan() != null && CastleManager.getInstance().getCastleByOwner(leader.getClan()) != null && CastleManager.getInstance().getCastleByOwner(leader.getClan()).getSiege().getIsInProgress())
		{
			player.sendMessage("Your leader is in siege, you can't go to your leader.");
			return false;
		}

		if(player.isClanLeader())
		{
			player.sendMessage("Your are The Leader.");
			return false;
		}

		if (player.isAlikeDead())
		{
			player.sendMessage("Your are in a bad condition.");
			return false;
		}

		if (player.isInStoreMode())
		{
			player.sendMessage("Your are in a bad condition.");
			return false;
		}

		return true;
	}

	protected int validateCondition(L2PcInstance player)
	{
		//if (player.isGM())
		//	return COND_OWNER;
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				if (getCastle().getOwnerId() == player.getClanId())
					return COND_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
}