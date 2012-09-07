// Noble Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   NobleCustomItem.java
// Modded by programmos, sword dev

package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;

public class NobleCustomItem extends ItemAbst
{

	public NobleCustomItem()
	{
		if(Config.NOBLE_CUSTOM_ITEMS)
		{
			_items = new int[]{Config.NOOBLE_CUSTOM_ITEM_ID};

			_playerUseOnly = true;
			_notInCombat = true;
			_notInObservationMode = true;
		}
	}

	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance player = playable.getPlayer();

		if(player.isNoble())
		{
			player.sendMessage("You already are a noblesse!");
		}
		else
		{
			player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
			player.setNoble(true);
			player.sendMessage("You are now a Noble. You are granted with Noblesse status and skills.");
			player.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			player.getInventory().addItem("Tiara", 7694, 1, player, null);
		}

		return true;
	}

}
