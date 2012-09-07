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
package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.6.4 $ $Date: 2005/04/06 18:25:18 $
 */

public class MysteryPotion extends ItemAbst
{
	private static final int BIGHEAD_EFFECT = 0x2000;

	public MysteryPotion()
	{
		_items = new int[]{5234};

		_playerUseOnly = true;
		_notInObservationMode = true;
	}

	@Override
	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(!super.useItem(playable, item))
		{
			return false;
		}

		L2PcInstance player = playable.getPlayer();

		if(player.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return false;
		}

		player.broadcastPacket(new MagicSkillUser(playable, playable, 2103, 1, 0, 0));

		player.startAbnormalEffect(BIGHEAD_EFFECT);

		player.sendPacket(new SystemMessage(SystemMessageId.USE_S1).addSkillName(2103));

		ThreadPoolManager.getInstance().scheduleEffect(new MysteryPotionStop(player), 1200000);
		return true;
	}

	protected class MysteryPotionStop implements Runnable
	{
		private L2PcInstance _playable;

		public MysteryPotionStop(L2PcInstance playable)
		{
			_playable = playable;
		}

		@Override
		public void run()
		{
			try
			{
				if(!(_playable.isPlayer))
					return;

				 _playable.stopAbnormalEffect(BIGHEAD_EFFECT);
			}
			catch(Throwable t)
			{}
		}
	}
}
