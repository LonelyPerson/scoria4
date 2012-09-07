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

import com.l2scoria.Config;
import com.l2scoria.gameserver.ItemsAutoDestroy;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.templates.L2EtcItemType;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.random.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Drunkard Zabb0x Lets drink2code!
 */
public class L2XmassTreeInstance extends L2NpcInstance
{
	private ScheduledFuture<?> _aiTask;
	private long _despawnTime;
	private static final int SPECIAL_TREE_ID = 13007;
	private int _presentTick;

	class XmassAI implements Runnable
	{
		private L2XmassTreeInstance _caster;

		protected XmassAI(L2XmassTreeInstance caster)
		{
			_caster = caster;
		}

		public void run()
		{
			if (System.currentTimeMillis() > _despawnTime)
			{
				deleteMe();
				return;
			}
			if(_caster.getNpcId() == SPECIAL_TREE_ID)
			{
				if (!_caster.isInsideZone(ZONE_PEACE))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(2139, 1);
					if(skill!= null)
					{
						setTarget(_caster);
						doCast(skill);
					}
					skill = null;
				}
				else if ( Config.CHRISTMAS_TREE_PRESENTS && ++_presentTick >= Config.CHRISTMAS_TREE_PRESENTS_TIME)
				{
					_presentTick = 0;
					try
					{
						synchronized (Config.CHRISTMAS_PRESENTS_LIST)
						{
							for(int key : Config.CHRISTMAS_PRESENTS_LIST.keys())
							{
								if (Rnd.get(Config.CHRISTMAS_PRESENTS_LIST.size()) == 0)
								{
									DropItem(key, Config.CHRISTMAS_PRESENTS_LIST.get(key));
									return;
								}
							}
						}
					}
					catch (Exception e) {}
				}
			}
		}
	}

	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this), 3000, 3000);
		_despawnTime = System.currentTimeMillis() + Config.CHRISTMAS_TREE_LIVE_TIME;
		_presentTick = 0;
	}

	@Override
	public void deleteMe()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(true);
		}

		super.deleteMe();
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 400;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.l2scoria.gameserver.model.L2Object#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	public L2ItemInstance DropItem(int itemId, int itemCount)
	{
		int randDropLim = 50;

		L2ItemInstance ditem = null;

		for(int i = 0; i < itemCount; i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newZ = getZ() + 20;

			// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
			ditem = ItemTable.getInstance().createItem("XmassTree", itemId, itemCount, null, this);
			ditem.dropMe(this, newX, newY, newZ);

			// Add drop to auto destroy item task
			if(!Config.LIST_PROTECTED_ITEMS.contains(itemId))
			{
				if(Config.AUTODESTROY_ITEM_AFTER > 0 && ditem.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && ditem.getItemType() == L2EtcItemType.HERB)
				{
					ItemsAutoDestroy.getInstance().addItem(ditem);
				}
			}

			ditem.setProtected(false);

			// If stackable, end loop as entire count is included in 1 instance of item
			if(ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
			{
				break;
			}
		}
		return ditem;
	}
}
