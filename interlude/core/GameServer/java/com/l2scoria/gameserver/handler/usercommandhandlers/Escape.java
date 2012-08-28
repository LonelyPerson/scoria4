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
package com.l2scoria.gameserver.handler.usercommandhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.GameTimeController;
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.handler.IUserCommandHandler;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.MagicSkillUser;
import com.l2scoria.gameserver.network.serverpackets.SetupGauge;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.Broadcast;

//import com.l2scoria.gameserver.managers.GrandBossManager;

/**
 *
 *
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2scoria.gameserver.model.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{

		if(activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode())
			return false;

		int unstuckTimer = activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000;

		// Check to see if the player is in a festival.
		if(activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape command in a festival.");
			return false;
		}

		/*if(GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
		{
			activeChar.sendMessage("You may not use an escape command in Grand boss zone.");
			return false;
		}*/

		// Check to see if player is in jail
		if(activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}

		if(activeChar.inObserverMode())
		{
			return false;
		}
                
                if(activeChar.isSitting()) 
                {
                        activeChar.sendMessage("You can not escape when you sitting.");
			return false;
                }
                
                for(L2Effect currenteffect : activeChar.getAllEffects())
		{
                    L2Skill effectSkill = currenteffect.getSkill();
                    if(effectSkill.getSkillType() == L2Skill.SkillType.FEAR) {
                        activeChar.sendMessage("You can not escape on Fear effect");
                        return false;
                    }
                }

		if(activeChar.getAccessLevel().isGm())
		{
			activeChar.sendMessage("You use Fast Escape: 1 second.");
		}
		else if(Config.UNSTUCK_INTERVAL > 120)
		{
			activeChar.sendMessage("You use Escape: " + unstuckTimer/60000 + " minutes.");
		}
		else
		{
			activeChar.sendMessage("You use Escape: " + unstuckTimer/1000 + " seconds.");
		}

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();

		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		msk = null;
		sg = null;
		//End SoE Animation section
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);

		ef = null;

		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;

		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}

		public void run()
		{
			if(_activeChar.isDead())
				return;

			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();

			try
			{
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			catch(Throwable e)
			{
				if(Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
