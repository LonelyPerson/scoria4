/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2scoria.gameserver.skills.effects;

import com.l2scoria.Config;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.network.serverpackets.FlyToLocation;
import com.l2scoria.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.gameserver.skills.Env;

import java.util.logging.Logger;

public class EffectThrowUp extends L2Effect
{
	static final Logger _log = Logger.getLogger(EffectThrowUp.class.getName());
	
	private int _x, _y, _z;
	
	public EffectThrowUp(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.THROW_UP;
	}
	
	@Override
	public void onStart()
	{
		// Get current position of the L2Character
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();
		
		// Calculate distance between effector and effected current position
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > 2000)
		{
			_log.info("EffectThrow was going to use invalid coordinates for characters, getEffected: "+curX+","+curY+" and getEffector: "+getEffector().getX()+","+getEffector().getY());
			return;
		}
		int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);
		
		double cos;
		double sin;
		
		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
			offset = 5;
		
		// If no distance
		if (distance < 1)
			return;
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		_x = getEffector().getX() - (int) (offset * cos);
		_y = getEffector().getY() - (int) (offset * sin);
		_z = getEffected().getZ();
		
		if (Config.GEODATA)
		{
			Location destiny = GeoEngine.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, false);
			_x = destiny.getX();
			_y = destiny.getY();
		}

		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.THROW_UP));
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}
}
