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

public class EffectEnemyFlight extends L2Effect
{
	private int _x, _y, _z;
	
	public EffectEnemyFlight(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public void onStart()
	{
		// Get current position of the L2Character
		final int curX = getEffector().getX();
		final int curY = getEffector().getY();
		final int curZ = getEffector().getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		double dx = getEffected().getX() - curX;
		double dy = getEffected().getY() - curY;
		double dz = getEffected().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > 2000)
		{
			return;
		}
		int offset = Math.max((int) distance - getSkill().getFlyRadius(), 30);
		
		double cos;
		double sin;
		
		// approximation for moving closer when z coordinates are different
		// TODO: handle Z axis movement better
		offset -= Math.abs(dz);
		if (offset < 5)
			offset = 5;
		
		// If no distance
		if (distance < 1 || distance - offset <= 0)
			return;
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		_x = curX + (int) ((distance - offset) * cos);
		_y = curY + (int) ((distance - offset) * sin);
		_z = getEffected().getZ();
		
		if (Config.GEODATA)
		{
			Location destiny = GeoEngine.moveCheck(getEffector().getX(), getEffector().getY(), getEffector().getZ(), _x, _y, getEffector().getInstanceId());
			_x = destiny.getX();
			_y = destiny.getY();
		}
		getEffector().broadcastPacket(new FlyToLocation(getEffector(), _x, _y, _z, FlyType.CHARGE));
		// maybe is need force set X,Y,Z
		getEffector().setXYZ(_x, _y, _z);
		getEffector().broadcastPacket(new ValidateLocation(getEffector()));
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
