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

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.network.clientpackets.RequestActionUse;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.util.random.Rnd;

/**
 * @author Scoria, Qwerty
 */

public class L2FortWyvernManagerInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public L2FortWyvernManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(command.startsWith("RideWyvern"))
		{
			if(!player.isClanLeader())
			{
				player.sendMessage("Only clan leaders are allowed.");
				return;
			}

			int petItemId = 0;
			L2ItemInstance petItem = null;

			if(player.getPet() == null)
			{
				if(player.isMounted())
				{
					petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
					if(petItem != null)
					{
						petItemId = petItem.getItemId();
					}
				}
			}
			else
			{
				petItemId = player.getPet().getControlItemId();
			}

			if(petItemId == 0 || !player.isMounted())
			{
				player.sendMessage("Ride your strider first...");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				return;
			}
			else if(player.isMounted() && petItem != null && petItem.getEnchantLevel() < 55)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				return;
			}

			// Wyvern requires Config.MANAGER_CRYSTAL_COUNT crystal for ride...
			if(player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
			{
				if (player._event!=null && !player._event.canDoAction(player, RequestActionUse.ACTION_MOUNT))
				{
					return;
				}

				if(!player.disarmWeapons())
					return;

				if(player.isMounted())
				{
					player.dismount();
				}

				if(player.getPet() != null)
				{
					player.getPet().unSummon(player);
				}

				player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());

				Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
				player.sendPacket(mount);
				player.broadcastPacket(mount);
				player.setMountType(mount.getMountType());

				player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
				player.sendMessage("The Wyvern has been summoned successfully!");

			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				player.sendMessage("You need 10 Crystals: B Grade.");
			}

			petItem = null;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
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
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/fortress/wyvernmanager-no.htm";

		int condition = validateCondition(player);

		if(condition > COND_ALL_FALSE)
		{
			if(condition == COND_OWNER)
			{
				filename = "data/html/fortress/wyvernmanager.htm";
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%count%", String.valueOf(10));
		player.sendPacket(html);
		filename = null;
		html = null;
	}

	protected int validateCondition(L2PcInstance player)
	{
		if(getFort() != null && getFort().getFortId() > 0)
		{
			if(player.getClan() != null)
			{
				if(getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				else if(getFort().getOwnerId() == player.getClanId() && player.isClanLeader())
					return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
