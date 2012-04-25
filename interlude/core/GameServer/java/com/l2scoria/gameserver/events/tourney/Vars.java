package com.l2scoria.gameserver.events.tourney;

import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2Item;

/**
 *
 * @author scoria
 */
public class Vars 
{
	public static boolean checkClass(int day, int classId)
	{
		boolean cond = false;
		switch(day)
		{
			case 2:
				switch(classId)
				{
					case 103:case 95:case 94:case 110:cond=true;	
				}
			case 3:
				switch(classId)
				{
					case 108:case 109:case 102:case 101:case 92:case 93:cond=true;	
				}
			case 4:
				switch(classId)
				{
					case 90:case 91:case 99:case 106:cond=true;	
				}
			case 5:
				switch(classId)
				{
					case 115:case 116:cond=true;	
				}
			case 6:
				switch(classId)
				{
					case 117:case 118:case 107:case 113:case 114:case 100:case 88:cond=true;	
				}
			case 7:
				switch(classId)
				{
					case 97:case 98:case 105:case 112:cond=true;	
				}
		}
		return cond;
	}
	public static boolean brokeCond(L2PcInstance m, L2ItemInstance i)
          {
    	if (	i.getItem().getBodyPart() == L2Item.SLOT_UNDERWEAR ||
        		i.getItem().getBodyPart() == L2Item.SLOT_FACE ||
        		i.getItem().getBodyPart() == L2Item.SLOT_DHAIR || 
        		//i.isHeroItem() ||
        		//i.isPotion() ||
        		i.getItemId() > 9000 || 
        		i.isAugmented() ||
    			i.getEnchantLevel() > 4 ||
    			checkEpic(i.getItemId())
    			)
    	{
    		m.sendPacket(new SystemMessage(SystemMessageId.RULES_ITEM).addItemName(i.getItemId()));
    		return true;
    	}
    	return false;
         }
        public static boolean checkEpic(int id)
	{
		switch(id)
		{
			case 6656:
			case 6657:
			case 6658:
			case 6659:
			case 6660:
			case 6661:
			case 6662:
				return true;
			default:
				return false;
		}
	}
	public static void getAnnounce(int Day)
	{
		switch(Day)
		{
	    	case 2: 
	    	{
                    Announcements.getInstance().announceToAll("Mage tourney - 1x1.");
                    Announcements.getInstance().announceToAll("In inventory not be:");
	            Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	            Announcements.getInstance().announceToAll("Augmented weapon,");
	            Announcements.getInstance().announceToAll("Epic jawels,potions");
	            Announcements.getInstance().announceToAll("Allowed class:");
	            Announcements.getInstance().announceToAll("Soultaker,Mustic Muse,Storm Screamer,Archmage");
	            Announcements.getInstance().announceToAll("Location: coliseum.");
	            Announcements.getInstance().announceToAll("Event start: 20:00.");
	    	}
	    	case 3:
	    	{
	            Announcements.getInstance().announceToAll("Archers,daggers tourney - 1x1.");
                    Announcements.getInstance().announceToAll("In inventory not be:");
	            Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	            Announcements.getInstance().announceToAll("Augmented weapon,");
	            Announcements.getInstance().announceToAll("Epic jawels,potions");
	            Announcements.getInstance().announceToAll("Allowed class:");
	            Announcements.getInstance().announceToAll("Sagittarius,Moonlight Sentinel,Ghost Sentinel");
	            Announcements.getInstance().announceToAll("Adventurer,Wind Rider,Ghost Hunter");
	            Announcements.getInstance().announceToAll("Location: coliseum.");
	            Announcements.getInstance().announceToAll("Event start: 20:00.");
	    	}
	    	case 4:
	    	{
	            Announcements.getInstance().announceToAll("Tanks tourney - 1x1.");
                    Announcements.getInstance().announceToAll("In inventory not be:");
	            Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	            Announcements.getInstance().announceToAll("Augmented weapon,");
	            Announcements.getInstance().announceToAll("Epic jawels,potions");
	            Announcements.getInstance().announceToAll("Allowed class:");
	            Announcements.getInstance().announceToAll("Phoenix Knight,Shillen Templar");
	            Announcements.getInstance().announceToAll("Hell Knight, Eva Templar");
	            Announcements.getInstance().announceToAll("Location: coliseum.");
	            Announcements.getInstance().announceToAll("Event start: 20:00.");
			}
	    	case 5:
	    	{
	             Announcements.getInstance().announceToAll("Overlord,warcryer tourney - 1x1.");
                     Announcements.getInstance().announceToAll("In inventory not be:");
	             Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	             Announcements.getInstance().announceToAll("Augmented weapon,");
	             Announcements.getInstance().announceToAll("Epic jawels,potions");
	             Announcements.getInstance().announceToAll("Allowed class:");
	             Announcements.getInstance().announceToAll("Dominator,Doomcryer.");
	             Announcements.getInstance().announceToAll("Location: coliseum.");
	             Announcements.getInstance().announceToAll("Event start: 20:00.");
	    	}
	    	case 6:
	    	{
	             Announcements.getInstance().announceToAll("Fighters tourney - 1x1.");
                     Announcements.getInstance().announceToAll("In inventory not be:");
	             Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	             Announcements.getInstance().announceToAll("Augmented weapon,");
	             Announcements.getInstance().announceToAll("Epic jawels,potions");
	             Announcements.getInstance().announceToAll("Allowed class:");
	             Announcements.getInstance().announceToAll("Fortune Seeker,Maestro,Spectral Dancer");
	             Announcements.getInstance().announceToAll("Sword Muse, Titan, Grand Khauatari.");
	             Announcements.getInstance().announceToAll("Location: coliseum.");
	             Announcements.getInstance().announceToAll("Event start: 20:00.");
	    	}
	    	case 7:
	    	{
	             Announcements.getInstance().announceToAll("Support tourney - 1x1.");
                     Announcements.getInstance().announceToAll("In inventory not be:");
	             Announcements.getInstance().announceToAll("Enchanted weapon more +4,");
	             Announcements.getInstance().announceToAll("Augmented weapon,");
	             Announcements.getInstance().announceToAll("Epic jawels,potions");
	             Announcements.getInstance().announceToAll("Allowed class:");
	             Announcements.getInstance().announceToAll("Eva Saint, Shilen Saint");
	             Announcements.getInstance().announceToAll("Hierophant, Cardinal.");
	             Announcements.getInstance().announceToAll("Location: coliseum.");
	             Announcements.getInstance().announceToAll("Event start: 20:00.");
	    	}
		}
	}
}
