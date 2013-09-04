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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.handler.items.IItemHandler;
import com.l2scoria.gameserver.handler.ItemHandler;
import com.l2scoria.gameserver.managers.CastleManager;
import com.l2scoria.gameserver.model.Inventory;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.ItemList;
import com.l2scoria.gameserver.network.serverpackets.ShowCalculator;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2ArmorType;
import com.l2scoria.gameserver.templates.L2Item;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.FloodProtector;
import com.l2scoria.gameserver.util.Util;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.3 $ $Date: 2009/04/29 13:57:30 $
 */
public final class UseItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
	private static final String _C__14_USEITEM = "[C] 14 UseItem";

	private int _objectId;

	private static class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;

		public WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}

		public void run()
		{
			//If character is still engaged in strike we should not change weapon
			if (activeChar.isAttackingNow())
				return;

			activeChar.useEquippableItem(item, false);
		}
	}

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null)
			return;

		int itemId = item.getItemId();
			
		if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_USE_ITEM))
		{
			activeChar.incFastUse(itemId);
			if(activeChar.getFastUse() >= Config.PROTECTED_ITEM_COUNT)
			{
				return;
			}
		}
		else
		{
			activeChar.clearFastUse();
		}

		if(activeChar.isStunned() || activeChar.isConfused() || activeChar.isAway() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			return;
		}

		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if(activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}

		if(item.isWear())
		{
			return;
		}

		if(activeChar._event!=null && activeChar._event.isRunning() && !activeChar._event.canUseItem(activeChar, item))
			return;

		if(item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}

		/*
		 * Alt game - Karma punishment // SOE
		 * 736  	Scroll of Escape
		 * 1538  	Blessed Scroll of Escape
		 * 1829  	Scroll of Escape: Clan Hall  	
		 * 1830  	Scroll of Escape: Castle
		 * 3958  	L2Day - Blessed Scroll of Escape
		 * 5858  	Blessed Scroll of Escape: Clan Hall
		 * 5859  	Blessed Scroll of Escape: Castle
		 * 6663  	Scroll of Escape: Orc Village
		 * 6664  	Scroll of Escape: Silenos Village
		 * 7117  	Scroll of Escape to Talking Island
		 * 7118  	Scroll of Escape to Elven Village
		 * 7119  	Scroll of Escape to Dark Elf Village
		 * 7120  	Scroll of Escape to Orc Village  	
		 * 7121  	Scroll of Escape to Dwarven Village
		 * 7122  	Scroll of Escape to Gludin Village
		 * 7123  	Scroll of Escape to the Town of Gludio
		 * 7124  	Scroll of Escape to the Town of Dion
		 * 7125  	Scroll of Escape to Floran
		 * 7126  	Scroll of Escape to Giran Castle Town
		 * 7127  	Scroll of Escape to Hardin's Private Academy
		 * 7128  	Scroll of Escape to Heine
		 * 7129  	Scroll of Escape to the Town of Oren
		 * 7130  	Scroll of Escape to Ivory Tower
		 * 7131  	Scroll of Escape to Hunters Village  
		 * 7132  	Scroll of Escape to Aden Castle Town
		 * 7133  	Scroll of Escape to the Town of Goddard
		 * 7134  	Scroll of Escape to the Rune Township
		 * 7135  	Scroll of Escape to the Town of Schuttgart.
		 * 7554  	Scroll of Escape to Talking Island
		 * 7555  	Scroll of Escape to Elven Village
		 * 7556  	Scroll of Escape to Dark Elf Village
		 * 7557  	Scroll of Escape to Orc Village
		 * 7558  	Scroll of Escape to Dwarven Village  	
		 * 7559  	Scroll of Escape to Giran Castle Town
		 * 7618  	Scroll of Escape - Ketra Orc Village
		 * 7619  	Scroll of Escape - Varka Silenos Village  	
		 * 10129    Scroll of Escape : Fortress
		 * 10130    Blessed Scroll of Escape : Fortress 
		 */
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0 && (itemId == 736 || itemId == 1538 || itemId == 1829 || itemId == 1830 || itemId == 3958 || itemId == 5858 || itemId == 5859 || itemId == 6663 || itemId == 6664 || itemId >= 7117 && itemId <= 7135 || itemId >= 7554 && itemId <= 7559 || itemId == 7618 || itemId == 7619 || itemId == 10129 || itemId == 10130))
			return;

		// Items that cannot be used
		if(itemId == 57)
			return;

		if(activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			// You cannot do anything else while fishing
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}

		L2Clan cl = activeChar.getClan();
		//A shield that can only be used by the members of a clan that owns a castle.
		if((cl == null || cl.getHasCastle() == 0) && itemId == 7015 && Config.CASTLE_SHIELD)
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}

		//A shield that can only be used by the members of a clan that owns a clan hall.
		if((cl == null || cl.getHasHideout() == 0) && itemId == 6902 && Config.CLANHALL_SHIELD)
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}

		//Apella armor used by clan members may be worn by a Baron or a higher level Aristocrat.
		if(itemId >= 7860 && itemId <= 7879 && Config.APELLA_ARMORS && (cl == null || activeChar.getPledgeClass() < 5))
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}

		//Clan Oath armor used by all clan members
		if(itemId >= 7850 && itemId <= 7859 && Config.OATH_ARMORS && cl == null)
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}

		//The Lord's Crown used by castle lords only
		if(itemId == 6841 && Config.CASTLE_CROWN && (cl == null || cl.getHasCastle() == 0 || !activeChar.isClanLeader()))
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}

		//Castle circlets used by the members of a clan that owns a castle, academy members are excluded.
		if(Config.CASTLE_CIRCLETS && (itemId >= 6834 && itemId <= 6840 || itemId == 8182 || itemId == 8183))
		{
			if(cl == null)
			{
				activeChar.sendMessage("You can't equip that");
				return;
			}
			else
			{
				int circletId = CastleManager.getInstance().getCircletByCastleId(cl.getHasCastle());
				if(activeChar.getPledgeType() == -1 || circletId != itemId)
				{
					activeChar.sendMessage("You can't equip that");
					return;
				}
			}
		}

		L2Weapon curwep = activeChar.getActiveWeaponItem();
		if(curwep != null)
		{
			if(curwep.getItemType() == L2WeaponType.DUAL && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if(curwep.getItemType() == L2WeaponType.BOW && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if(curwep.getItemType() == L2WeaponType.BIGBLUNT && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if(curwep.getItemType() == L2WeaponType.BIGSWORD && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if(curwep.getItemType() == L2WeaponType.POLE && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if(curwep.getItemType() == L2WeaponType.DUALFIST && item.getItemType() == L2WeaponType.NONE)
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
		}

		// Char cannot use item when dead
		if(activeChar.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(itemId);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		if(activeChar.isMounted())
		{
            boolean canEquipIt = true;
            if(Config.MOUNT_PROHIBIT) {
                if(!item.getItem().isConsumable())
                    canEquipIt = false;
            } else {
                canEquipIt = false;
            }
            if(!canEquipIt) {
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                sm.addItemName(itemId);
                getClient().getActiveChar().sendPacket(sm);
                sm = null;
                return;
            }
		}

		// Char cannot use pet items
		if(item.getItem().isForWolf() || item.getItem().isForHatchling() || item.getItem().isForStrider() || item.getItem().isForBabyPet())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
			sm.addItemName(itemId);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}

		if(Config.DEBUG)
		{
			_log.info(activeChar.getObjectId() + ": use item " + _objectId);
		}

		activeChar._inWorld = true;

		if(item.isEquipable())
		{
			if (activeChar.isCastingNow())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				activeChar.sendPacket(sm);
				return;
			}

			int bodyPart = item.getItem().getBodyPart();

			// Don't allow weapon/shield equipment if wearing formal wear
			if(activeChar.isWearingFormalWear() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND) && item.getItemId() != 9140)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
				activeChar.sendPacket(sm);
				return;
			}

			if(Config.PROTECTED_ENCHANT)
			{
				switch(bodyPart)
				{
					case L2Item.SLOT_LR_HAND:
					case L2Item.SLOT_R_HAND:
					{
						if(item.getEnchantLevel() > Config.ENCHANT_WEAPON_MAX && !activeChar.isGM())
						{
							activeChar.sendMessage("You try to use overenchanted item. You will be panished.");
							Util.handleIllegalPlayerAction(activeChar, "The player " + activeChar.getName() + " has been panished for use overenchanted item.", Config.DEFAULT_PUNISH);
							return;
						}
						break;
					}
					case L2Item.SLOT_L_HAND:
					case L2Item.SLOT_CHEST:
					case L2Item.SLOT_BACK:
					case L2Item.SLOT_GLOVES:
					case L2Item.SLOT_FEET:
					case L2Item.SLOT_HEAD:
					case L2Item.SLOT_FULL_ARMOR:
					case L2Item.SLOT_LEGS:
					{
						if(item.getEnchantLevel() > Config.ENCHANT_ARMOR_MAX && !activeChar.isGM())
						{
							activeChar.sendMessage("You try to use overenchanted item. You will be panished.");
							Util.handleIllegalPlayerAction(activeChar, "The player " + activeChar.getName() + " has been panished for use overenchanted item.", Config.DEFAULT_PUNISH);
							return;
						}
						break;
					}
					case L2Item.SLOT_R_EAR:
					case L2Item.SLOT_L_EAR:
					case L2Item.SLOT_NECK:
					case L2Item.SLOT_R_FINGER:
					case L2Item.SLOT_L_FINGER:
					{
						if(item.getEnchantLevel() > Config.ENCHANT_JEWELRY_MAX && !activeChar.isGM())
						{
							activeChar.sendMessage("You try to use overenchanted item. You will be panished.");
							Util.handleIllegalPlayerAction(activeChar, "The player " + activeChar.getName() + " has been panished for use overenchanted item.", Config.DEFAULT_PUNISH);
							return;
						}
						break;
					}
				}
			}

			// Don't allow weapon/shield equipment if a cursed weapon is equiped
			if(activeChar.isCursedWeaponEquiped() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND))
				return;
                        
                        if(Config.ANTI_HEAVY_SYSTEM) {
			   if (item.getItemType() == L2ArmorType.HEAVY && ((activeChar.getClassId().getId() == 8) ||  (activeChar.getClassId().getId() == 23) ||  (activeChar.getClassId().getId() == 35) ||  (activeChar.getClassId().getId() == 93) ||  (activeChar.getClassId().getId() == 101) ||  (activeChar.getClassId().getId() == 108) || (activeChar.getClassId().getId() == 9) || (activeChar.getClassId().getId() == 24) || (activeChar.getClassId().getId() == 37) || (activeChar.getClassId().getId() == 92) || (activeChar.getClassId().getId() == 102) || (activeChar.getClassId().getId() == 109)))
                                {
                                    activeChar.sendMessage("Anti-Heavy System: You can`t equip this item");
                                    return;
                                }
                        }

			// Don't allow weapon/shield hero equipment during Olimpia
			if(activeChar.isInOlympiadMode() && ((bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND) && (item.getItemId() >= 6611 && item.getItemId() <= 6621 || item.getItemId() == 6842) || Config.LIST_OLY_RESTRICTED_ITEMS.contains(item.getItemId())))
				return;

			// Don't allow Hero items equipment if not a hero
			if(!activeChar.isHero() && (item.getItemId() >= 6611 && item.getItemId() <= 6621 || item.getItemId() == 6842) && !activeChar.isGM())
				return;

			// Don't allow to put formal wear
			if(activeChar.isCursedWeaponEquiped() && itemId == 6408)
				return;

			if (activeChar.isAttackingNow())
			{
                                if(activeChar.isPlayer)
                                {
                                    activeChar.abortAttack();
                                    //return;
                                }
                                else
                                {
                                    ThreadPoolManager.getInstance().scheduleGeneral( new WeaponEquipTask(item,activeChar), (activeChar.getAttackEndTime()-GameTimeController.getGameTicks())*GameTimeController.MILLIS_IN_TICK);
                                    return;
                                }
			}

			activeChar.useEquippableItem(item, true);
		}
		else
		{
			if(activeChar.isCastingNow() && !item.isConsumable())
				return;

			L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			int itemid = item.getItemId();
			//_log.debug("item not equipable id:"+ item.getItemId());
			if(itemid == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else if(weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD && (itemid >= 6519 && itemid <= 6527 || itemid >= 7610 && itemid <= 7613 || itemid >= 7807 && itemid <= 7809 || itemid >= 8484 && itemid <= 8486 || itemid >= 8505 && itemid <= 8513))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(activeChar, false);
				sendPacket(il);
				return;
			}
			else
			{
				IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
				if(handler == null)
				{
					if(Config.DEBUG)
						_log.warn("No item handler registered for item ID " + itemId + ".");
				}
				else
				{
					handler.useItem(activeChar, item);
				}
			}
		}
		//      }
	}

	@Override
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
