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

import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AttackRequest extends L2GameClientPacket
{
	private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";
	private static Logger _log = Logger.getLogger(AttackRequest.class.getName());

	// cddddc
	private int _objectId;
	private int _attackId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		/*_originX = */readD();
		/*_originY = */readD();
		/*_originZ = */readD();
		_attackId = readC(); // 0 for simple click   1 for shift-click
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		// avoid using expensive operations if not needed
		L2Object target;

		if(activeChar.getTargetId() == _objectId)
		{
			target = activeChar.getTarget();
		}
		else
		{
			target = L2World.getInstance().findObject(_objectId);
		}

		if(target == null)
			return;

		if(activeChar.getTarget() != target)
		{
			target.onAction(activeChar);
		}
		else
		{
			if(target.getObjectId() != activeChar.getObjectId() && activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null)
			{
				switch(_attackId)
				{
					case 0:
						if(target.isCharacter && ((L2Character) target).isAlikeDead())
						{
							target.onAction(activeChar);
						}
						else
						{
							target.onForcedAttack(activeChar);
						}
						break;
					case 1:
						if(target.isCharacter && ((L2Character) target).isAlikeDead())
						{
							target.onAction(activeChar);
						}
						else
						{
							target.onActionShift(activeChar, true);
						}
						break;
					default:
						// Ivalid action detected (probably client cheating), log this
						_log.warn("Character: " + activeChar.getName() + " requested invalid action: " + _attackId);
						getClient().sendPacket(ActionFailed.STATIC_PACKET);
						break;
				}
			}
			else
			{
				getClient().sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}
