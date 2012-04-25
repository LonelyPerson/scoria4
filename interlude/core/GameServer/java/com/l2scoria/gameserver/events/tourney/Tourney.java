package com.l2scoria.gameserver.events.tourney;

/**
 *
 * @author scoria
 */

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javolution.util.FastList;
import com.l2scoria.gameserver.model.entity.Announcements;
import com.l2scoria.gameserver.datatables.HeroSkillTable;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2CubicInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

import com.l2scoria.gameserver.util.ForceUtil;

public class Tourney 
{
	public static List<L2PcInstance> members = new FastList<L2PcInstance>();
	public static String state = "none";
	private static int Day = 0;
	private static int count = 0;
    private static Timer timer = new Timer();
	
	public void init()
	{
		Calendar tmp = Calendar.getInstance();
    	tmp.set(Calendar.HOUR_OF_DAY, 19);
    	tmp.set(Calendar.MINUTE, 0);
    	tmp.set(Calendar.SECOND, 0);
    	Date needTime = tmp.getTime();
		Day = Calendar.DAY_OF_WEEK;
		if (Day == 1)
			return;
    	timer.schedule(new startEvent(), needTime, ForceUtil.addTimeHours(24));
	}
	
    class startEvent extends TimerTask 
    {
        public void run() 
        {
        	state = "announce";
        	timer.schedule(new announceEvent(), 1000, ForceUtil.addTimeMins(10));
        }
    }
    
    
    private void begin()
    {
    	state = "clean";
    	cleanMembers();
    	int count2 = members.size(), pairs = count2/2; 
		if (count2%2 != 0)
			pairs = (count2 - 1) / 2;
        Announcements.getInstance().announceToAll("Register on tourney is closed.");
        Announcements.getInstance().announceToAll("Registered players - "+count2+".");
        Announcements.getInstance().announceToAll("Opponent pairs - "+pairs+".");
        state = "fight";
    }

    
    class announceEvent extends TimerTask 
    {
    	public void run()
    	{
        	if (count == 5)
        	{
        		timer.cancel();
        		begin();
        		return;
        	}
        	else
        		count++;
        	Vars.getAnnounce(Day);
    	}
    }
    public static boolean checkInventory(L2PcInstance m)
    {
    	boolean check = false;
    	for (L2ItemInstance i : m.getInventory().getItems())
    		if (Vars.brokeCond(m, i))
    		{
    			check = true;
    			break;
    		}
    	return check;
    }
    public static void exitMember(L2PcInstance m, boolean after)
    {
    	members.remove(m);
    	m.setEventState(null);
    	if (after)
    		m.teleToLocation(82592, 148592, -3469); 
    }
    public static void cleanMembers()
    {
    	for (L2PcInstance m : members)
    	{
    		if (m.isInOlympiadMode())
    		{
    			exitMember(m, false);
    			m.sendMessage("You can not partipicate if you register on olympiad.");
    			return;
    		}
    		if (Vars.checkClass(Day, m.getActiveClass()) == false)
    		{
    			exitMember(m, false);
    			m.sendMessage("You class not acepted in tourney now.");
    			return;
    		}    			
    		if (checkInventory(m))
    		{
    			exitMember(m, false);
    			m.sendMessage("In you inventory some items don`t accepted by rulles tourney");
    			return;
    		}
    		for (L2Effect e : m.getAllEffects())
    			if (e != null)
    				e.exit();	
    		if (m.isHero())
				for(L2Skill skill: HeroSkillTable.getHeroSkills())
					m.removeSkill(skill,false);
			for(L2CubicInstance cubic : m.getCubics().values())
			{
				cubic.stopAction();
				m.delCubic(cubic.getId());
			}
			m.getCubics().clear();
			m.abortCast();
			if (m.getClan() != null)
				for(L2Skill skill: m.getClan().getAllSkills())
					m.removeSkill(skill,false);
			m.teleToLocation(-115892,-214131,-3332);
			m.setEventState("sit");
			if (!m.isSitting())
				m.sitDown();
			if (m.getPet() != null)
				m.getPet().unSummon(m);
    	}
    }
}
