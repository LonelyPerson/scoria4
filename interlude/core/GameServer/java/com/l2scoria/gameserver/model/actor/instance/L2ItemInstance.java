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
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.sql.ItemTable;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.ItemsOnGroundManager;
import com.l2scoria.gameserver.managers.MercTicketManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.actor.knownlist.NullKnownList;
import com.l2scoria.gameserver.model.extender.BaseExtender.EventType;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.*;
import com.l2scoria.gameserver.skills.funcs.Func;
import com.l2scoria.gameserver.templates.L2Armor;
import com.l2scoria.gameserver.templates.L2EtcItem;
import com.l2scoria.gameserver.templates.L2Item;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.util.database.L2DatabaseFactory;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class manages items.
 * 
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */

public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
	private static final Logger _logItems = Logger.getLogger("item");
	private static final Logger _logAudit = Logger.getLogger("gmaudit");

	/** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT
	}

	/** ID of the owner */
	private int _ownerId;

	/** Quantity of the item */
	private int _count;
	/** Initial Quantity of the item */
	private int _initCount;
	/** Time after restore Item count (in Hours) */
	private int _time;
	/** Remaining time (in miliseconds) */
	private long _lifeTime;

	/** Quantity of the item can decrease */
	private boolean _decrease = false;

	/** ID of the item */
	private final int _itemId;

	/** Object L2Item associated to the item */
	private final L2Item _item;

	/** Location of the item : Inventory, PaperDoll, WareHouse */
	private ItemLocation _loc;

	/** Slot where item is stored */
	private int _locData;

	/** Level of enchantment of the item */
	private int _enchantLevel;

	/** Price of the item for selling */
	private int _priceSell;

	/** Price of the item for buying */
	private int _priceBuy;

	/** Wear Item */
	private boolean _wear;

	/** Augmented Item */
	private L2Augmentation _augmentation = null;

	/** Shadow item */
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;

	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;

	private long _dropTime;

	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SOULSHOT = 2; // It's a realy exists? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;

	/** Item charged with SoulShot (type of SoulShot) */
	private int _chargedSoulshot = CHARGED_NONE;
	/** Item charged with SpiritShot (type of SpiritShot) */
	private int _chargedSpiritshot = CHARGED_NONE;

	private boolean _chargedFishtshot = false;

	private boolean _protected;

	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2; //1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.

	private ScheduledFuture<?> itemLootShedule = null;
	public ScheduledFuture<?> _lifeTimeTask;

	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 * 
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		super.setKnownList(new NullKnownList(this));

		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);

		if(_itemId == 0 || _item == null)
			throw new IllegalArgumentException();

		_count = 1;
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_lifeTime = _item.getLifeTime() == -1 ? -1 : System.currentTimeMillis() + ((long)_item.getLifeTime()*60*1000);
		scheduleLifeTimeTask();
	}

	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * 
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		super.setKnownList(new NullKnownList(this));

		_itemId = item.getItemId();
		_item = item;

		if(_itemId == 0 || _item == null)
			throw new IllegalArgumentException();

		_count = 1;
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_lifeTime = _item.getLifeTime() == -1 ? -1 : System.currentTimeMillis() + ((long)_item.getLifeTime()*60*1000);
		scheduleLifeTimeTask();
	}

	/**
	 * Sets the ownerID of the item
	 * 
	 * @param process : String Identifier of process triggering this action
	 * @param owner_id : int designating the ID of the owner
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in
	 *            transformation
	 */
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference)
	{
		int oldOwner = _ownerId;
		setOwnerId(owner_id);
		fireEvent(EventType.SETOWNER.name, new Object[]
		{
				process, oldOwner
		});
		if(Config.LOG_ITEMS)
		{
                    if(!Config.LOG_ITEMS_EXC_PROC.contains(process) && !Config.LOG_ITEMS_EXC_ITEM.contains(this.getItemType().toString()))
                    {
                        List<Object> param = new ArrayList<Object>();
                        param.add("SO Process: "+process);
                        param.add(this+"("+this.getObjectId()+")");
						param.add("Count total:"+_count);
                        param.add(creator);
                        param.add(reference);
                        _logItems.info(param);
                    }
		}
	}

	/**
	 * Sets the ownerID of the item
	 * 
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if(owner_id == _ownerId)
			return;

		_ownerId = owner_id;
		_storedInDb = false;
	}

	/**
	 * Returns the ownerID of the item
	 * 
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}

	/**
	 * Sets the location of the item
	 * 
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	/**
	 * Sets the location of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * 
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if(loc == _loc && loc_data == _locData)
			return;
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	/**
	 * Returns the quantity of item
	 * 
	 * @return int
	 */
	public int getCount()
	{
		return _count;
	}

	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * 
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in
	 *            transformation
	 */
	public void changeCount(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if(count == 0)
			return;

		if(count > 0 && _count > Integer.MAX_VALUE - count)
		{
			_count = Integer.MAX_VALUE;
		}
		else
		{
			_count += count;
		}

		if(_count < 0)
		{
			_count = 0;
		}

		_storedInDb = false;

		if(Config.LOG_ITEMS)
		{
                    if(!Config.LOG_ITEMS_EXC_PROC.contains(process) && !Config.LOG_ITEMS_EXC_ITEM.contains(this.getItemType().toString()))
                    {
                        List<Object> param = new ArrayList<Object>();
                        param.add("Process: "+process);
                        param.add(this+"("+this.getObjectId()+")");
						param.add("Count total:"+_count+" passed:"+count);
                        param.add(creator);
                        param.add(reference);
                        _logItems.info(param);
                    }
		}
	}

	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if(count == 0)
			return;
		if(count > 0 && _count > Integer.MAX_VALUE - count)
		{
			_count = Integer.MAX_VALUE;
		}
		else
		{
			_count += count;
		}
		if(_count < 0)
		{
			_count = 0;
		}
		_storedInDb = false;
	}

	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * 
	 * @param count : int
	 */
	public void setCount(int count)
	{
		if(_count == count)
			return;

		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}

	/**
	 * Returns if item is equipable
	 * 
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem);
	}

	/**
	 * Returns if item is equipped
	 * 
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	/**
	 * Returns the slot where the item is stored
	 * 
	 * @return int
	 */
	public int getEquipSlot()
	{
		if(Config.ASSERT)
		{
			assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT;
		}
		return _locData;
	}

	/**
	 * Returns the characteristics of the item
	 * 
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _item;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}

	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	public boolean isWear()
	{
		return _wear;
	}

	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}

	/**
	 * Returns the type of item
	 * 
	 * @return Enum
	 */
	public Enum getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * Returns the ID of the item
	 * 
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Returns the quantity of crystals for crystallization
	 * 
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}

	/**
	 * Returns the reference price of the item
	 * 
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	/**
	 * Returns the name of the item
	 * 
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}

	/**
	 * Returns the price of the item for selling
	 * 
	 * @return int
	 */
	public int getPriceToSell()
	{
		return isConsumable() ? (int) (_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell;
	}

	/**
	 * Sets the price of the item for selling <U><I>Remark :</I></U> If loc and loc_data different from database, say
	 * datas not up-to-date
	 * 
	 * @param price : int designating the price
	 */
	public void setPriceToSell(int price)
	{
		_priceSell = price;
		_storedInDb = false;
	}

	/**
	 * Returns the price of the item for buying
	 * 
	 * @return int
	 */
	public int getPriceToBuy()
	{
		return isConsumable() ? (int) (_priceBuy * Config.RATE_CONSUMABLE_COST) : _priceBuy;
	}

	/**
	 * Sets the price of the item for buying <U><I>Remark :</I></U> If loc and loc_data different from database, say
	 * datas not up-to-date
	 * 
	 * @param price : int
	 */
	public void setPriceToBuy(int price)
	{
		_priceBuy = price;
		_storedInDb = false;
	}

	/**
	 * Returns the last change of the item
	 * 
	 * @return int
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * Sets the last change of the item
	 * 
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	/**
	 * Returns if item is stackable
	 * 
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}

	/**
	 * Returns if item is dropable
	 * 
	 * @return boolean
	 */
	public boolean isDropable()
	{
		return _item.isDropable()  && (Config.ALT_CAN_DROP_AUGMENT || !isAugmented());
	}

	/**
	 * Returns if item is destroyable
	 * 
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}

	/**
	 * Returns if item is tradeable
	 * 
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		return _item.isTradeable() && (Config.ALT_CAN_TRADE_AUGMENT || !isAugmented());
	}

	/**
	 * Returns if item is consumable
	 * 
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}

	/**
	 * Returns if item is available for manipulation
	 * 
	 * @return boolean
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena)
	{
		return !isEquipped() && getItem().getType2() != 3 && (getItem().getType2() != 4 || getItem().getType1() != 1) // TODO: what does this mean?
				&& (player.getPet() == null || getObjectId() != player.getPet().getControlItemId()) // Not Control item of currently summoned pet
				&& player.getActiveEnchantItem() != this && (allowAdena || getItemId() != 57) && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && isTradeable() && !player.getWrongHwid();
	}

	public boolean isAvailable(L2PcInstance player)
	{
		return !isEquipped() && getItem().getType2() != 3 && (getItem().getType2() != 4 || getItem().getType1() != 1)
				&& (player.getPet() == null || getObjectId() != player.getPet().getControlItemId())
				&& player.getActiveEnchantItem() != this && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && !player.getWrongHwid();
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.model.L2Object#onAction(com.l2scoria.gameserver.model.L2PcInstance)
	 * also check constraints: only soloing castle owners may pick up mercenary tickets of their castle
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		// mercenary tickets can only be picked up by the castle owner.
		if(_itemId >= 3960 && _itemId <= 4026 && player.isInParty()
		|| _itemId >= 5205 && _itemId <= 5219 && player.isInParty()
		|| _itemId >= 6038 && _itemId <= 6114 && player.isInParty()
		|| _itemId >= 6779 && _itemId <= 6801 && player.isInParty()
		|| _itemId >= 7918 && _itemId <= 7940 && player.isInParty()
		|| _itemId >= 7973 && _itemId <= 7997 && player.isInParty()
		|| _itemId >= 3960 && _itemId <= 3972 && !player.isCastleLord(1)
		|| _itemId >= 6038 && _itemId <= 6047 && !player.isCastleLord(1)
		|| _itemId >= 3973 && _itemId <= 3985 && !player.isCastleLord(2)
		|| _itemId >= 6051 && _itemId <= 6060 && !player.isCastleLord(2)
		|| _itemId >= 3986 && _itemId <= 3998 && !player.isCastleLord(3)
		|| _itemId >= 6064 && _itemId <= 6073 && !player.isCastleLord(3)
		|| _itemId >= 3999 && _itemId <= 4011 && !player.isCastleLord(4)
		|| _itemId >= 6077 && _itemId <= 6086 && !player.isCastleLord(4)
		|| _itemId >= 4012 && _itemId <= 4026 && !player.isCastleLord(5)
		|| _itemId >= 6090 && _itemId <= 6099 && !player.isCastleLord(5)
		|| _itemId >= 5205 && _itemId <= 5219 && _itemId != 5216 && _itemId != 5217 && !player.isCastleLord(6)
		|| _itemId >= 6105 && _itemId <= 6114 && !player.isCastleLord(6)
		|| _itemId >= 6779 && _itemId <= 6801 && !player.isCastleLord(7)
		|| _itemId >= 7973 && _itemId <= 7997 && !player.isCastleLord(8)
		|| _itemId >= 7918 && _itemId <= 7940 && !player.isCastleLord(9))
		{
			if(player.isInParty())
			{
				player.sendMessage("You cannot pickup mercenaries while in a party.");
			}
			else
			{
				player.sendMessage("Only the castle lord can pickup mercenaries.");
			}

			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
		}
	}

	/**
	 * Returns the level of enchantment of the item
	 * 
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	/**
	 * Sets the level of enchantment of the item
	 * 
	 * @param int
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if(_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	/**
	 * Returns the physical defense of the item
	 * 
	 * @return int
	 */
	public int getPDef()
	{
		if(_item instanceof L2Armor)
			return ((L2Armor) _item).getPDef();
		return 0;
	}

	/**
	 * Returns whether this item is augmented or not
	 * 
	 * @return true if augmented
	 */
	public boolean isAugmented()
	{
		return _augmentation == null ? false : true;
	}

	/**
	 * Returns the augmentation object for this item
	 * 
	 * @return augmentation
	 */
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}

	/**
	 * Sets a new augmentation
	 * 
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if(_augmentation != null)
			return false;
		_augmentation = augmentation;
		return true;
	}

	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation()
	{
		if(_augmentation == null)
			return;
		_augmentation.deleteAugmentationData();
		_augmentation = null;
	}

	public void endOfLife()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null)
		{
			if (isEquipped())
			{
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item: unequiped)
				{
					player.checkSSMatch(null, item);
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			if (getLocation() != ItemLocation.WAREHOUSE && getLocation() != ItemLocation.VOID)
			{
				// destroy
				player.getInventory().destroyItem("L2ItemInstance", this, player, null);
				
				// send update
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);
				
				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
			}
			else
			{
				player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
			}

			player.sendMessage("Time-limited item deleted");
		}

		if (getLocation() == ItemLocation.VOID)
		{
			L2World.getInstance().removeVisibleObject(this, getWorldRegion());
		}
		L2World.getInstance().removeObject(this);
	}

	public void scheduleLifeTimeTask()
	{
		if (!isTimeLimitedItem()) return;
		if (getRemainingTime() <= 0)
			endOfLife();
		else
		{
			if (_lifeTimeTask != null)
				_lifeTimeTask.cancel(false);
			_lifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime());
		}
	}

	public static class ScheduleLifeTimeTask implements Runnable
	{
		private final L2ItemInstance _limitedItem;
		
		public ScheduleLifeTimeTask(L2ItemInstance item)
		{
			_limitedItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_limitedItem != null)
					_limitedItem.endOfLife();
			}
			catch (Exception e)
			{
				_log.fatal("", e);
			}
		}
	}

	/**
	 * Used to decrease mana (mana means life time for shadow items)
	 */
	public class ScheduleConsumeManaTask implements Runnable
	{
		private L2ItemInstance _shadowItem;

		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}

		public void run()
		{
			try
			{
				// decrease mana
				if(_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch(Throwable t)
			{}
		}
	}

	/**
	 * Returns true if this item is a shadow item Shadow items have a limited life-time
	 * 
	 * @return
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}

	/**
	 * Sets the mana for this shadow item <b>NOTE</b>: does not send an inventory update packet
	 * 
	 * @param mana
	 */
	public void setMana(int mana)
	{
		_mana = mana;
	}

	/**
	 * Returns the remaining mana of this shadow item
	 * 
	 * @return lifeTime
	 */
	public int getMana()
	{
		return _mana;
	}

	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is
	 * running optionally one could force a new task
	 * 
	 * @param forces a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		if(!isShadowItem())
			return;

		if(_mana > 0)
		{
			_mana--;
		}

		if(_storedInDb)
		{
			_storedInDb = false;
		}
		if(resetConsumingMana)
		{
			_consumingMana = false;
		}

		L2PcInstance player = (L2PcInstance) L2World.getInstance().findObject(getOwnerId());
		if(player != null)
		{
			SystemMessage sm;
			switch(_mana)
			{
				case 10:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
				case 5:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
				case 1:
					sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addString(getItemName());
					player.sendPacket(sm);
					break;
			}

			if(_mana == 0) // The life time has expired
			{
				sm = new SystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addString(getItemName());
				player.sendPacket(sm);

				// unequip
				if(isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getEquipSlot());
					InventoryUpdate iu = new InventoryUpdate();

					for(L2ItemInstance element : unequiped)
					{
						player.checkSSMatch(null, element);
						iu.addModifiedItem(element);
					}

					player.sendPacket(iu);

					unequiped = null;
					iu = null;
				}

				if(getLocation() != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);

					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					iu = null;

					StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					su = null;
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}

				// delete from world
				L2World.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if(!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if(getLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
					iu = null;
				}
			}

			sm = null;
		}

		player = null;
	}

	private void scheduleConsumeManaTask()
	{
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}

	/**
	 * Returns false cause item can't be attacked
	 * 
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	/**
	 * Returns the type of charge with SoulShot of the item.
	 * 
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 * 
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 * 
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 * 
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched for a particular player.
	 * 
	 * @param player : L2Character designating the player
	 * @return Func[]
	 */
	public Func[] getStatFuncs(L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}

	/**
	 * Updates database.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * <B>IF</B> the item exists in database :
	 * <UL>
	 * <LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
	 * <LI><B>ELSE</B> : update item in database</LI>
	 * </UL>
	 * <B> Otherwise</B> :
	 * <UL>
	 * <LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item
	 * in database</LI>
	 * </UL>
	 */
	public void updateDatabase()
	{
		if(isWear())
			return;

		if(_existsInDb)
		{
			if(_ownerId == 0 || _loc == ItemLocation.VOID || _count == 0 && _loc != ItemLocation.LEASE)
			{
				removeFromDb();
			}
			else
			{
				updateInDb();
			}
		}
		else
		{
			if(_count == 0 && _loc != ItemLocation.LEASE)
				return;

			if(_loc == ItemLocation.VOID || _ownerId == 0)
				return;

			insertIntoDb();
		}
	}

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 * 
	 * @param objectId : int designating the objectID of the item
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int objectId)
	{
		L2ItemInstance inst = null;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left, life_time FROM items WHERE object_id = ?");
			statement.setInt(1, objectId);
			ResultSet rs = statement.executeQuery();

			if(rs.next())
			{
				int owner_id = rs.getInt("owner_id");
				int item_id = rs.getInt("item_id");
				int count = rs.getInt("count");

				ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));

				int loc_data = rs.getInt("loc_data");
				int enchant_level = rs.getInt("enchant_level");
				int custom_type1 = rs.getInt("custom_type1");
				int custom_type2 = rs.getInt("custom_type2");
				int price_sell = rs.getInt("price_sell");
				int price_buy = rs.getInt("price_buy");
				int manaLeft = rs.getInt("mana_left");
				long lifeTime = rs.getLong("life_time");

				L2Item item = ItemTable.getInstance().getTemplate(item_id);

				if(item == null)
				{
					_log.fatal("Item item_id=" + item_id + " not known, object_id=" + objectId);
					rs.close();
					statement.close();
					return null;
				}

				inst = new L2ItemInstance(objectId, item);
				inst._existsInDb = true;
				inst._storedInDb = true;
				inst._ownerId = owner_id;
				inst._count = count;
				inst._enchantLevel = enchant_level;
				inst._type1 = custom_type1;
				inst._type2 = custom_type2;
				inst._loc = loc;
				inst._locData = loc_data;
				inst._priceSell = price_sell;
				inst._priceBuy = price_buy;

				// Setup life time for shadow weapons
				inst._mana = manaLeft;

				inst._lifeTime = lifeTime;

				// if mana left is 0 delete this item
				if(inst._mana == 0)
				{
					inst.removeFromDb();

					rs.close();
					statement.close();

					return null;
				}

				loc = null;
				item = null;
			}
			else
			{
				_log.fatal("Item object_id=" + objectId + " not found");

				rs.close();
				statement.close();

				return null;
			}

			rs.close();
			statement.close();

			//load augmentation
			statement = con.prepareStatement("SELECT attributes,skill,level FROM augmentations WHERE item_id=?");
			statement.setInt(1, objectId);
			rs = statement.executeQuery();

			if(rs.next())
			{
				inst._augmentation = new L2Augmentation(inst, rs.getInt("attributes"), rs.getInt("skill"), rs.getInt("level"), false);
			}

			rs.close();
			statement.close();
			inst.fireEvent(EventType.LOAD.name, new Object[]
			{
				con
			});
			
			rs.close();
			statement.close();
			rs = null;
			statement = null;
		}
		catch(Exception e)
		{
			_log.fatal("Could not restore item " + objectId + " from DB:", e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
		return inst;
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion</li> <li>Add the
	 * L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li> <li>Add the L2ItemInstance dropped in the
	 * world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop item</li> <li>Call Pet</li><BR>
	 */
	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		if(Config.ASSERT)
		{
			assert getPosition().getWorldRegion() == null;
		}

		if(Config.GEODATA && dropper != null)
		{
			Location dropDest = GeoEngine.moveCheck(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, false, dropper.getInstanceId());
			x = dropDest.getX();
			y = dropDest.getY();
			z = dropDest.getZ();

			dropDest = null;
		}

		synchronized (this)
		{
			// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
			setIsVisible(true);
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

			// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}

		setDropTime(System.currentTimeMillis());

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2ItemInstance dropped in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);

		if(Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}
		
		if(dropper.isPlayer && ((L2PcInstance)dropper).isGM())
		{
                    _logAudit.info("Drop item: " + getItemName() + "(" + getCount() + ") GM:" + dropper.getName());
		}
	}

	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		if(Config.ASSERT)
		{
			assert _existsInDb;
		}

		if(_wear)
			return;

		if(_storedInDb)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=?,life_time=? WHERE object_id = ?");
			statement.setInt(1, _ownerId);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _priceSell);
			statement.setInt(7, _priceBuy);
			statement.setInt(8, getCustomType1());
			statement.setInt(9, getCustomType2());
			statement.setInt(10, getMana());
			statement.setLong(11, getLifeTime());
			statement.setInt(12, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
			statement = null;
			fireEvent(EventType.STORE.name, new Object[]
			{
				con
			});
		}
		catch(Exception e)
		{
			_log.fatal("Could not update item " + getObjectId() + " in DB: Reason: " + "Duplicate itemId");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		if(_wear)
			return;

		if(Config.ASSERT)
		{
			assert !_existsInDb && getObjectId() != 0;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left,life_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, _priceSell);
			statement.setInt(8, _priceBuy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, _type1);
			statement.setInt(11, _type2);
			statement.setInt(12, getMana());
			statement.setLong(13, getLifeTime());

			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
			statement = null;
		}
		catch(Exception e)
		{
			_log.fatal("Could not insert item " + getObjectId() + " into DB: Reason: " + "Duplicate itemId");
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		if(_wear)
			return;

		if(Config.ASSERT)
		{
			assert _existsInDb;
		}

		// delete augmentation data
		if(isAugmented())
		{
			_augmentation.deleteAugmentationData();
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			statement.close();
			statement = null;
			fireEvent(EventType.DELETE.name, new Object[]
			{
				con
			});
		}
		catch(Exception e)
		{
			_log.fatal("Could not delete item " + getObjectId() + " in DB:", e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { }
			con = null;
		}
	}

	/**
	 * Returns the item in String format
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		return "" + _item;
	}

	public void resetOwnerTimer()
	{
		if(itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}

	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}

	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}

	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}

	public boolean isProtected()
	{
		return _protected;
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}

	public boolean getCountDecrease()
	{
		return _decrease;
	}

	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}

	public int getInitCount()
	{
		return _initCount;
	}

	public void restoreInitCount()
	{
		if(_decrease)
		{
			_count = _initCount;
		}
	}

	public void setTime(int time)
	{
		if(time > 0)
		{
			_time = time;
		}
		else
		{
			_time = 0;
		}
	}

	public int getTime()
	{
		return _time;
	}

	public boolean isTimeLimitedItem()
	{
		return (_lifeTime > 0);
	}

	/**
	 * Returns (current system time + time) of this time limited item
	 * @return Time
	 */
	public long getLifeTime()
	{
		return _lifeTime;
	}

	public long getRemainingTime()
	{
		return _lifeTime - System.currentTimeMillis();
	}

	/**
	 * Returns the slot where the item is stored
	 * 
	 * @return int
	 */
	public int getLocationSlot()
	{
		if(Config.ASSERT)
		{
			assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT || _loc == ItemLocation.INVENTORY;
		}

		return _locData;
	}

	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member</li> <li>Remove the
	 * L2Object from the world</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World
	 * </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2ItemInstance</li> <li>_worldRegion != null <I>(L2Object is visible at the beginning)</I></li>
	 * <BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Do Pickup Item : PCInstance and Pet</li><BR>
	 * <BR>
	 * 
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(L2Character player)
	{
		if(Config.ASSERT)
		{
			assert this instanceof L2ItemInstance;
		}

		if(Config.ASSERT)
		{
			assert getPosition().getWorldRegion() != null;
		}

		L2WorldRegion oldregion = getPosition().getWorldRegion();

		// Create a server->client GetItem packet to pick up the L2ItemInstance
		GetItem gi = new GetItem((L2ItemInstance) this, player.getObjectId());
		player.broadcastPacket(gi);
		gi = null;

		synchronized (this)
		{
			setIsVisible(false);
		}

		// if this item is a mercenary ticket, remove the spawns!
		if(this instanceof L2ItemInstance)
		{
			int itemId = ((L2ItemInstance) this).getItemId();
			if(MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
			{
				MercTicketManager.getInstance().removeTicket((L2ItemInstance) this);
				ItemsOnGroundManager.getInstance().removeObject(this);
			}
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2ItemInstance from the world
		L2World.getInstance().removeVisibleObject(this, oldregion);

		oldregion = null;
	}
}
