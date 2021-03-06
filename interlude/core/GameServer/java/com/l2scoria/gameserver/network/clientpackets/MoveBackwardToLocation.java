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
import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.actor.position.L2CharPosition;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2scoria.gameserver.network.serverpackets.StopMove;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.thread.TaskPriority;
import com.l2scoria.gameserver.util.IllegalPlayerAction;
import com.l2scoria.gameserver.util.Util;

import java.nio.BufferUnderflowException;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.4.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class MoveBackwardToLocation extends L2GameClientPacket
{
	//private static Logger _log = Logger.getLogger(MoveBackwardToLocation.class.getName());
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	private int _moveMovement;

	//For geodata
	private int _curX;
	private int _curY;

	@SuppressWarnings("unused")
	private int _curZ;

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";

	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
		}
		catch(BufferUnderflowException e)
		{
			// ignore for now
			if(Config.L2WALKER_PROTEC)
			{
				L2PcInstance activeChar = getClient().getActiveChar();
				activeChar.sendPacket(new SystemMessage(SystemMessageId.HACKING_TOOL));
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use l2walker!", IllegalPlayerAction.PUNISH_KICK);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		activeChar._inWorld = true;

		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}

		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();

		if(activeChar.isInBoat())
		{
			activeChar.setInBoat(false);
		}

		if(activeChar.getTeleMode() > 0)
		{
			if(activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}

			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}

		if(_moveMovement == 0 && !Config.ALLOW_USE_CURSOR_FOR_WALK)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if(activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
                        activeChar.abortAttack();
			double dx = _targetX - _curX;
			double dy = _targetY - _curY;

			// Can't move if character is confused, or trying to move a huge distance
			if(activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000) // 9900*9900
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));

			if(activeChar.getParty() != null)
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
			}
			if(activeChar.getSitdownTask())
			{
				activeChar.setSitdownTask(false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
}
