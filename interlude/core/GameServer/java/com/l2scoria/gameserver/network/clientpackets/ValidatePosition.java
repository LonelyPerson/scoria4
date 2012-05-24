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
import com.l2scoria.gameserver.datatables.csv.MapRegionTable;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.geoeditorcon.GeoEditorListener;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2scoria.gameserver.thread.TaskPriority;

import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

	/** urgent messages, execute immediatly */
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		/*_data =*/readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isTeleporting()) return;
		if (Config.COORD_SYNCHRONIZE > 0)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading);
			int realX = activeChar.getX();
			int realY = activeChar.getY();
			int realZ = activeChar.getZ();
			double dx = _x - realX;
			double dy = _y - realY;
			double diffSq = Math.sqrt(dx*dx + dy*dy);
			double dz = realZ - _z;

			// add cka3ka
			if(!activeChar.isFlying() && !activeChar.isInWater())
			{

				if (dz >= 333 && Config.CONTROL_HEIGHT_DAMAGE)
				{
					activeChar.isFalling(true, (int)dz);
				}

				// если игрок провалился под текстуры вниз или его выкинуло слишком высоко его автоматом возвращает в город
				if (_z < -15000 || _z > 15000)
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					activeChar.setTarget(activeChar);
					correctPosition(activeChar);
				}
			}

			// end add

			if (diffSq > 0 && diffSq < 1000) // if too large, messes observation
			{
				if ((Config.COORD_SYNCHRONIZE & 1) == 1 && (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading))) // Heading changed on client = possible obstacle
				{
					if (Config.DEVELOPER) System.out.println(activeChar.getName() + ": Synchronizing position Client --> Server" + (activeChar.isMoving()?" (collision)":" (stay sync)"));
					if (diffSq < 50) // 50*50 - attack won't work fluently if even small differences are corrected
					{
						activeChar.setXYZ(realX, realY, _z);
					}
					else
					{
						activeChar.setXYZ(_x, _y, _z);
					}
					activeChar.setHeading(_heading);
				}
				else if ((Config.COORD_SYNCHRONIZE & 2) == 2 && diffSq > activeChar.getStat().getMoveSpeed()) // more than can be considered to be result of latency
				{
					if (Config.DEVELOPER) System.out.println(activeChar.getName() + ": Synchronizing position Server --> Client");
					if (activeChar.isInBoat())
					{
						sendPacket(new ValidateLocationInVehicle(activeChar));
					}
					else
					{
						if (activeChar.isRunning())
						{
							activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
						}
						else
						{
							activeChar.broadcastPacket(new ValidateLocation(activeChar));
						}
					}
				}
			}
			activeChar.setLastClientPosition(_x, _y, _z);
			activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		}
		else if (Config.COORD_SYNCHRONIZE == -1)
		{
			activeChar.setClientX(_x);
			activeChar.setClientY(_y);
			activeChar.setClientZ(_z);
			activeChar.setClientHeading(_heading); // No real need to validate heading.
			int realX = activeChar.getX();
			int realY = activeChar.getY();
			int realZ = activeChar.getZ();
			double dx = _x - realX;
			double dy = _y - realY;
			double diffSq = (dx*dx + dy*dy);
			if (diffSq < 250000)
			{
				activeChar.setXYZ(realX,realY,_z);
			}
			//TODO: do we need to validate?
			/*double dx = (_x - realX);
			double dy = (_y - realY);
			double dist = Math.sqrt(dx*dx + dy*dy);
			if ((dist < 500)&&(dist > 2)) //check it wasnt teleportation, and char isn't there yet
			activeChar.sendPacket(new CharMoveToLocation(activeChar));*/

			if (Config.DEBUG)
			{
				_log.fine("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
				_log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + activeChar.getHeading());
			}

			if (Config.DEVELOPER)
			{
				if (diffSq > 1000)
				{
					if (Config.DEBUG) _log.fine("client/server dist diff "+ (int)Math.sqrt(diffSq));
					if (activeChar.isInBoat())
					{
						sendPacket(new ValidateLocationInVehicle(activeChar));
					}
					else
					{
						activeChar.sendPacket(new ValidateLocation(activeChar));
					}
				}
			}
		}

		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			if (GeoEditorListener.getInstance().getThread() != null  && GeoEditorListener.getInstance().getThread().isWorking()  && GeoEditorListener.getInstance().getThread().isSend(activeChar))
			{
				GeoEditorListener.getInstance().getThread().sendGmPosition(_x,_y,(short)_z);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__48_VALIDATEPOSITION;
	}

	@Deprecated
	public boolean equal(ValidatePosition pos)
	{
		return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
	}

	private void correctPosition(L2PcInstance activeChar)
	{
		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if (realX != 0 && realY != 0 && realZ != 0)
		{
			if (GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ()) == 15)
			{
				activeChar.setXYZ(realX, realY, GeoEngine.getHeight(realX, realY, realZ));
				activeChar.broadcastPacket(new ValidateLocation(activeChar));
			}
			else
			{
				activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		else if (activeChar.getClientX() != 0 && activeChar.getClientY() != 0 && activeChar.getClientZ() != 0)
		{
			if (GeoEngine.getNSWE(activeChar.getClientX(), activeChar.getClientY(), activeChar.getClientZ()) == 15)
			{
				activeChar.setXYZ(activeChar.getClientX(), activeChar.getClientY(), GeoEngine.getHeight(activeChar.getClientX(), activeChar.getClientY(), activeChar.getClientZ()));
				activeChar.broadcastPacket(new ValidateLocation(activeChar));
			}
			else
			{
				activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		else
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}
}
