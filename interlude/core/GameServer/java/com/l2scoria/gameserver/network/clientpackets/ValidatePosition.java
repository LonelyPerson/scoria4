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

import java.util.logging.Logger;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2scoria.gameserver.thread.TaskPriority;

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

                if(activeChar == null || activeChar.isTeleporting() || activeChar.isDead())
                        return;

                int realX = activeChar.getX();
                int realY = activeChar.getY();
                int realZ = activeChar.getZ();
                int realHeading = activeChar.getHeading();

                double dx = _x - realX;
                double dy = _y - realY;
                double dz = _z - realZ;
                double diffSq = dx * dx + dy * dy;

                if(Config.DEBUG)
                {
                        _log.fine("client pos: " + _x + " " + _y + " " + _z + " head " + _heading);
                        _log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + realHeading);
                }

                if(activeChar.isFlying() || activeChar.isInWater())
                {
                        activeChar.setXYZ(realX, realY, _z);
                        if (diffSq > 90000) // validate packet, may also cause z bounce if close to land
                        {
                                if(activeChar.isInBoat())
                                {
                                        sendPacket(new ValidateLocationInVehicle(activeChar));
                                }
                                else
                                {
                                        activeChar.sendPacket(new ValidateLocation(activeChar));
                                }
                        }
                }
                else if(diffSq < 250000) // if too large, messes observation
                {
                        if (Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synced to server, mainly used when no geodata but can be used also with geodata
                        {
                                activeChar.setXYZ(realX, realY, _z);
                        }
                        else if (Config.COORD_SYNCHRONIZE == 1) // Trusting also client x, y coordinates (should not be used with geodata)
                        {
                                if (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading)) // Heading changed on client = possible obstacle
                                {
                                        // character is not moving, take coordinates from client
                                        if (diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
                                                activeChar.setXYZ(realX, realY, _z);
                                        else
                                                activeChar.setXYZ(_x, _y, _z);
                                }
                                else
                                        activeChar.setXYZ(realX, realY, _z);
                                activeChar.setHeading(_heading);
                        }
                        // Sync 2 (or other),
                        // intended for geodata. Sends a validation packet to client
                        // when too far from server calculated true coordinate.
                        // Due to geodata/zone errors, some Z axis checks are made.
                        else if (Config.GEODATA > 0 && (diffSq > 10000 || Math.abs(dz) > 200))
                        {
                                if (Math.abs(dz) > 200 && Math.abs(dz) < 1500 && Math.abs(_z - activeChar.getClientZ()) < 800)
                                {
                                        activeChar.setXYZ(realX, realY, _z);
                                }
                                else
                                {
                                        if(Config.DEVELOPER)
                                        {
                                                System.out.println(activeChar.getName() + ": Synchronizing position Server --> Client");
                                        }

                                        if(activeChar.isInBoat())
                                        {
                                                sendPacket(new ValidateLocationInVehicle(activeChar));
                                        }
                                        else
                                        {
                                                activeChar.sendPacket(new ValidateLocation(activeChar));
                                        }
                                }
                        }
                        //activeChar.setLastClientPosition(_x, _y, _z);
                        //activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
                }

                if(activeChar.getParty() != null)
                {
                        activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
                }
                if(Config.ALLOW_WATER)
                {
                        activeChar.checkWaterState();
                }
                if(Config.FALL_DAMAGE && !activeChar.isInWater() && !activeChar.isFlying())
                {
                        activeChar.isFalling(true, 0); // Check if the L2Character isFalling
                }

                activeChar.setClientX(_x);
                activeChar.setClientY(_y);
                activeChar.setClientZ(_z);
                activeChar.setClientHeading(_heading); // No real need to validate heading.
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
}